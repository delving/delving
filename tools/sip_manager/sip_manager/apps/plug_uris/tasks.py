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

import time
import urlparse
#from xml.dom.minidom import parseString

from django.db import connection, transaction

from apps.base_item import models as base_item
from apps.process_monitor.sipproc import SipProcess

import models


class UriCreateNewRecords(SipProcess):
    SHORT_DESCRIPTION = 'Create new uri records'

    def run_it(self):
        cursor = connection.cursor()

        # SQL logic:
        #   Finding MdRecords with no matching Uri items, since all MdRecords
        #   contains uri this indicates that this item is not processed
        cursor.execute('SELECT DISTINCT m.id FROM base_item_mdrecord m LEFT JOIN plug_uris_uri u ON m.id = u.mdr_id WHERE u.mdr_id IS NULL')
        self.task_starting('Creating new uri records', cursor.rowcount)
        t0 = time.time()
        record_count = 0
        while True:
            result = cursor.fetchone()
            if not result:
                break
            record_count += 1
            mdr_id = result[0]
            mdrs = base_item.MdRecord.objects.filter(pk=mdr_id,pid=0)
            if not mdrs:
                # we know it exists, so must be busy, leave it for later processing
                continue
            mdr = mdrs[0]
            if mdr.status not in (base_item.MDRS_CREATED,
                                  base_item.MDRS_IDLE,
                                  base_item.MDRS_PROCESSING):
                continue # never touch bad / completed items
            self.handle_md_record(mdr)
            if t0 + self.TASK_PROGRESS_TIME < time.time():
                self.task_progress(record_count)
                t0 = time.time()
        return True

    def handle_md_record(self, mdr):
        """For the moment cheat a bit and just get the europeana:object item
        we need to build the new cache asap!
        """
        #dom = parseString(mdr.source_data)
        parts = mdr.source_data.split('<europeana:object>')
        if len(parts) == 1:
            return False # no obj found - shouldnt happen
        img_url = parts[1].split('<')[0]
        srvr_name = urlparse.urlsplit(img_url).netloc.lower()
        uri_sources = models.UriSource.objects.filter(name_or_ip=srvr_name)
        if uri_sources:
            uri_source = uri_sources[0]
        else:
            uri_source = models.UriSource(name_or_ip=srvr_name)
            uri_source.save()
        uri = models.Uri(mdr=mdr,item_type=models.URIT_OBJECT,
                         url=img_url,uri_source=uri_source)
        uri.save()
        return True


class UriProcessNewRecords(SipProcess):
    SHORT_DESCRIPTION = 'process new uri records'
    PLUGIN_TAXES_NET_IO = True


    def run_it(self):
        """
        for uri_source in UriSource.items.filter(pid=0):
            for uri in Uris.items.filter(uri_source=uri_source.id, pid=0):
                process_one_uri(uri)
        """
        self.task_starting('Process new Uri records', cursor.rowcount)
        t0 = time.time()
        record_count = 0
        for uri in models.Uri.objects.filter(status=models.URIS_CREATED, pid=0):
            record_count += 1
            self.handle_uri(uri)
            if t0 + self.TASK_PROGRESS_TIME < time.time():
                self.task_progress(record_count)
                t0 = time.time()
        return True

    def handle_uri(self, uri_obj):
        self.uri = self.grab_item(models.Uri, uri_obj.pk,
                             'About to check url')
        if not self.uri:
            return False # Failed to take control of it

        if not self.verify_url():
            self.uri_state(URIS_FAILED)
            return False
        self.uri_state(models.URIS_VERIFIED)

        # URIS_DOWNLOADED = 3
        # URIS_FULL_GENERATED = 4
        # URIS_BRIEF_GENERATED = 5
        # URIS_COMPLETED = 6

        self.release_item(models.Uri, self.uri.pk)
        return True


    def verify_url(self):
        "Check if item can be downloaded."
        msg = []
        try:
            itm = urllib2.urlopen(self.uri.url)#,timeout=URL_TIMEOUT)
        except urllib2.HTTPError, e:
            self.uri.err_msg = 'http error: %i' % e.code
            return False # let caller do the save
        except urllib2.URLError, e:
            if str(e.reason) == 'timed out':
            self.uri.err_msg = 'http error: %i' % e.code
                self.thing_set_state(cache_item, glob_consts.ST_TIMEOUT)
            else:
                self.thing_set_state(cache_item, glob_consts.IS_URL_ERROR)
                cache_item.set_msg('URLError %s' % str(e.reason))
            return False
        except:
            self.thing_set_state(cache_item, glob_consts.ST_ERROR)
            cache_item.set_msg('Unhandled error when checking url')
            return False

        if itm.code != 200:
            self.thing_set_state(cache_item, glob_consts.IS_HTML_ERROR)
            cache_item.set_msg('HTML status: %i' % itm.code)
            return False
        try:
            content_t = itm.headers['content-type']
        except:
            self.thing_set_state(cache_item, glob_consts.ST_ERROR)
            cache_item.set_msg('Failed to parse mime-type')
            return False

        ret = True
        if content_t not in (self.mime_ok):
            if content_t in self.mime_not_good:
                ret = False
            else:
                if not self.verify_odd_mimetype(content_t):
                    ret = False
        if not ret:
            self.thing_set_state(cache_item, glob_consts.IS_MIME_TYPE_ERROR)
            cache_item.set_msg('Bad mime-type: %s' % content_t)
        return ret


    def uri_state(self, state):
        self.uri.state = state
        self.uri.save()






class UriCleanup(SipProcess):
    SHORT_DESCRIPTION = 'Remove uri records no longer having a request'

    def run_it(self):
        return True


class UriFileTreeMonitor(SipProcess):
    SHORT_DESCRIPTION = 'Walks file tree and finds orphan files'

    def run_it(self):
        return True # does nothing for the moment


def process_one_uri(uri):
    # Step one, find available UriSource
    pass



task_list = [UriCreateNewRecords,
             #UriProcessNewRecords,
             #UriCleanup,
             #UriFileTreeMonitor,
             ]