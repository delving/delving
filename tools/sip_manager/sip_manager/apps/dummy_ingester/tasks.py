"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)



"""

import datetime
import time
import os

import django.db
from django.core import exceptions
from django.conf import settings

from apps.process_monitor.sipproc import SipProcess
from apps.base_item import models as base_item

import models

TREE_IS_INGESTION_SVN = settings.TREE_IS_INGESTION_SVN
IMPORT_SCAN_TREE = settings.IMPORT_SCAN_TREE


REC_START = '<record>'
REC_STOP = '</record>'


class RequestCreate(SipProcess):
    SHORT_DESCRIPTION = 'Checking file tree for new requests'
    EXECUTOR_STYLE = True

    already_parsed = {}

    def __init__(self, *args, **kwargs):
        self.skip_dirs = ['error', 'to-import', 'to-import-cache-only',
                          'to-import-no-cache', 'uploading', '.svn']
        super(RequestCreate, self).__init__(*args, **kwargs)
        self.default_task_show_log_lvl = self.task_show_log_lvl
        self.task_show_log_lvl = 9 # to try to avoid shoing initial task progress
                                   # will be reset to normal if we actually do something

    def run_it(self):
        """
        Dirs to be scanned:
            finished
            imported
            importing
            nonexistent
            uploaded
            validated
            validating

        Indicating a deletion:
            error

        Avoid the following:
            to-import
            to-import-cache-only
            to-import-no-cache
            uploading
        """
        skip_trees = [] # if we find a subtree that shouldnt be followed like .svn dont dive into it
        i_found = i_added = 0
        for dirpath, dirnames, filenames in os.walk(IMPORT_SCAN_TREE):
            if dirpath == IMPORT_SCAN_TREE:
                continue # we dont want this one to end up in skip_trees, that would be lonely...
            b = False
            for bad_tree in skip_trees:
                if bad_tree in dirpath:
                    b = True
                    break
            if b:
                continue

            # avoid specific relative dirs that shouldnt be traversed
            if os.path.split(dirpath)[-1] in self.skip_dirs:
                skip_trees.append(dirpath)
                continue

            # if we are scanning the ingestion svn only /.../output_xml/ should be used
            if TREE_IS_INGESTION_SVN and os.path.split(dirpath)[-1] != 'output_xml':
                continue

            for filename in filenames:
                if os.path.splitext(filename)[1] != '.xml':
                    continue
                # if we are scanning the ingestion svn avoid things like 'dddd.sample.xml'
                if TREE_IS_INGESTION_SVN and os.path.splitext(os.path.splitext(filename)[0])[1] != '':
                    # extra check needed for using ingestion svn tree
                    continue

                i_found += 1
                full_path = os.path.join(dirpath, filename)
                mtime = os.path.getmtime(full_path)
                #if not (self.already_parsed.has_key(full_path) and self.already_parsed[full_path] == mtime):
                #    # we dont bother with files we have already checked
                request, was_created = models.Request.objects.get_or_create_from_file(full_path)
                if was_created:
                    self.task_show_log_lvl = self.default_task_show_log_lvl
                    self.task_force_progress_timeout()
                    self.log('Added request %s' % filename, 3)
                    i_added += 1
                if request:
                    self.already_parsed[full_path] = mtime
                self.task_time_to_show('%i / %i' % (i_added, i_found))
        return True





class RequestParseNew(SipProcess):
    SHORT_DESCRIPTION = 'Parse new Requests'
    EXECUTOR_STYLE = True

    def prepare(self):
        try:
            request = models.Request.objects.filter(status=models.REQS_PRE,
                                                        pid=0)[0]
        except:
            return False

        # in order not to grab control to long, just handle one request on each call to this
        self.request_id = request.id
        return True


    def run_it(self):
        request = self.grab_item(models.Request, self.request_id,
                                 'About to parse for ese records')
        if not request:
            return False # Failed to take control of it

        self.current_request = request # make it available without params for other modules
        full_path = self.find_file()
        if not full_path:
            return self.request_failure('Cant find file %s for Request %i' % (
                request.file_name, request.pk))

        self.log('Parsing ese file for records: %s' % full_path, 1)
        f = open(full_path, 'r')
        record = []
        self.task_starting('Reading ESE records from file (req:%i)' % request.pk,request.record_count)
        line = f.readline()[:-1].strip() # skip lf and other pre/post whitespace
        record_count = 0
        while line:
            if line == REC_START:
                record = []
                record_count += 1
            elif line == REC_STOP:
                record.sort()
                # start and stop tags shouldnt be sorted so add them after
                record.insert(0, REC_START)
                record.append(REC_STOP)

                self.add_record(record, request)
            elif line: # skip empty lines
                record.append(line)
            line = f.readline()[:-1].strip() # skip lf and other pre/post whitespace
            self.task_time_to_show(record_count)
        f.close()
        request.status = models.REQS_INIT
        request.save()
        self.release_item(models.Request, request.pk)
        return True


    def add_record(self, record, request):
        record_str = '\n'.join(record)
        r_hash = base_item.calculate_mdr_content_hash(record_str)
        mdr, was_created = base_item.MdRecord.objects.get_or_create(
            content_hash=r_hash, source_data=record_str)

        # join the mdrecord to its request
        r_m = base_item.RequestMdRecord(request=request, md_record=mdr)
        r_m.save()


    def find_file(self):
        ret = ''
        found = False
        for dirpath, dirnames, filenames in os.walk(IMPORT_SCAN_TREE):
            if found:
                break
            for filename in filenames:
                if filename == self.current_request.file_name:
                    full_path = os.path.join(dirpath, filename)
                    mtime = os.path.getmtime(full_path)
                    time_created = datetime.datetime.fromtimestamp(mtime)
                    if self.current_request.time_created == time_created:
                        found = True
                        ret = full_path
                        break
                    pass
            pass
        return ret


    def request_failure(self, msg):
        self.current_request.status = models.REQS_ABORTED
        self.current_request.save()
        self.release_item(models.Request, self.current_request.pk)
        self.current_request = None
        self.error_log(msg)
        return False # propagate error





task_list = [RequestCreate,
             RequestParseNew
             ]
