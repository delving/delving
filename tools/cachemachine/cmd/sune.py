"""
God do I need to name this module to something meaningfull :)
"""

import os
import sys
import string

import time
from django.core import exceptions
import xml.parsers.expat

import settings
from apps.cache_machine.models import Request, CacheItem, CacheSource

from utils import glob_consts
from utils.imgretrieval import ImgRetrieval



def cachemachine_starter(pm, single_request=False):
    pid = os.getpid()
    pm.pid = pid
    pm.save()

    #while 1:
    q_rec_pending = Request.objects.filter(sstate=glob_consts.ST_PENDING)
    if q_rec_pending:
        r = q_rec_pending[0] # do first pending on this run
        handle_pending_request(r)
    if not single_request:
        ImgRetrieval(debug_lvl=9).run()
    #print 'waiting...'
    #time.sleep(30)

    pm.pid = 0
    pm.save()


def handle_pending_request(r):
    pid = os.getpid()
    r.sstate = glob_consts.RS_PRE_PARSING
    r.save()
    if r.cache_items.all():
        # This request has already been parsed, this shouldnt happen
        # for the moment just remove all connections between
        # CacheItem and this request, will take some extra time but at leat
        # we will get the correct connections to this request after this run
        #Artik har koppl
        print 'Warning %s' % r.fname
        print '\twas previously parsed to some extent, we will reparse it now'
        print '\tnothing seriously broken, will just take som extra time...'
        r.cache_items = []
    r.sstate = glob_consts.ST_PARSING # since we own the r we can change it at will...
    r.save()
    if is_valid_file(r):
        ret = True
        r.sstate = glob_consts.ST_COMPLETED
    else:
        r.sstate = glob_consts.ST_FAILED
        r.message = 'File was not of correct format.'
        ret = False
    r.save()
    return ret


def is_valid_file(request):
    #x = RequestParseXML(request, debug_lvl=4)
    x = WgetFileParser(request, debug_lvl=4)
    x.run()
    return True



class XmlRecord(object):
    """
    Container for the data we gather for each "record" xml-node
    """
    def __init__(self):
        self.uri = []
        self.isShownBy = []
        self.isShownAt = []
        self.oobject = []

    def __str__(self):
        uri = ''.join(self.uri)
        isShownBy = ''.join(self.isShownBy)
        isShownAt = ''.join(self.isShownAt)
        oobject = ''.join(self.oobject)
        s = 'uri: %s\n\tisShownBy: %s\n\tisShownAt: %s\n\tobject: %s' % (uri, isShownBy, isShownAt, oobject)
        return s



class BaseXMLParser(object):
    def __init__(self, request, debug_lvl=2):
        self.request = request
        self.debug_lvl = debug_lvl
        self.fname = os.path.join(glob_consts.REQUEST_UPLOAD_PATH,
                                  settings.MEDIA_ROOT,
                                  self.request.fpath.name)

        self.record_count = 0
        self.records_bad = 0
        self.record = None # current record
        self.progress_intervall = 100
        self.last_progress = 0

    def log(self, msg, lvl=2, add_lf=True):
        if self.debug_lvl < lvl:
            return
        if add_lf:
            msg += '\n'
        sys.stdout.write(msg)
        sys.stdout.flush()

    def check_record(self):
        self.record_count += 1
        if self.record_count > self.last_progress + self.progress_intervall:
            self.log('.', 1, add_lf=False)
            self.last_progress = self.record_count
        rec = self.record
        self.record = None
        if not (rec.uri and rec.oobject):
            self.log('missing data in record %i' % self.record_count, 8)
            self.log('%s' % rec, 8)
            self.log('-', 1, add_lf=False)
            self.records_bad += 1
            return
        self.ingest_item(rec)

    def ingest_item(self, rec):
        q = CacheItem.objects.filter(uri_obj=''.join(rec.oobject))
        if q:
            # cache_item already exists, just link it to this request
            self.request.cache_items.add(q[0])
        else:
            try:
                self.request.cache_items.create(uri_id=''.join(rec.uri),
                                                uri_obj=''.join(rec.oobject))
            except Exception, e:
                self.log('Failed to create cache item; %s' % e)
                self.records_bad += 1

        #if self.record_count > 3:
        #    raise exceptions.ValidationError('devel temp done')
        return


class RequestParseXML2(BaseXMLParser):
    """
    Runs out of memory on the enourmus xml files used here...
    """

    def run(self):
        self.log('Parsing xml file for records: %s' % self.fname)
        t = time.time()
        d = xml.dom.minidom.parse(self.fname)
        records = d.childNodes[0].getElementsByTagName('record')
        t = int(time.time() - t)
        self.log('xml parsing complete after %i sec, now iterating over %i records' % (t, len(records)))
        for record in records:
            self.record = XmlRecord()
            self.record.uri = record.getElementsByTagName('europeana:uri')[0].firstChild.nodeValue
            try:
                self.record.isShownBy = record.getElementsByTagName('europeana:isShownBy')[0].firstChild.nodeValue
            except:
                pass
            try:
                self.record.isShownAt = record.getElementsByTagName('europeana:isShownAt')[0].firstChild.nodeValue
            except:
                pass
            self.check_record()
        self.log('all records processed')



class RequestParseXML(BaseXMLParser):

    def __init__(self, request, debug_lvl=2):
        super(RequestParseXML, self).__init__(request, debug_lvl)
        self.parser = xml.parsers.expat.ParserCreate()
        self.parser.StartElementHandler = self.start_element
        self.parser.EndElementHandler = self.end_element
        self.parser.CharacterDataHandler = self.char_data

        self.elements = []


    def run(self):
        self.log('Parsing xml file for records: %s' % self.fname, 1)
        f = open(self.fname)
        try:
            self.parser.ParseFile(f)
        except Exception, e:
            self.log('Parsing error; %s' % e)
        f.close()
        good_items = self.record_count - self.records_bad
        msg = 'xml parsing completed - added items: %i' % good_items
        if self.records_bad:
            msg += '\tbad items: %i' % self.records_bad
        self.log(msg, 1)


    def start_element(self, name, attrs):
        #self.log('Start element: %s %s' % (name, attrs), 5)
        self.elements.append(name)
        if name == 'record':
            self.record = XmlRecord()
        return


    def end_element(self, name):
        #self.log('End element: %s' % name, 5)
        if name == 'record':
            self.check_record()
        self.elements.pop()


    def char_data(self, data):
        #if data.strip(): self.log('Character data: %s' % repr(data), 6)
        name = self.elements[-1]
        if name == 'europeana:uri':
            self.record.uri.append(data)
        elif name == 'europeana:object':
            self.record.oobject.append(data)
        elif name == 'europeana:isShownBy':
            self.record.isShownBy.append(data)
        elif name == 'europeana:isShownAt':
            self.record.isShownAt.append(data)
        return





import hashlib
import tempfile

class WgetFileParser(RequestParseXML):
    """
    Quick and dirty alternative to just create a wget file
    and bypass database
    """
    ALL_ITEMS = '!#HEPP#!'
    WGET_ECHO_INTERVAL = 50


    def run(self):
        self.create_dirs()
        base_name = os.path.splitext(os.path.basename(self.fname))[0]
        self.wget_file = os.path.join(settings.MEDIA_ROOT, 'wget-files', base_name)
        self.fp_wget_file = open(self.wget_file, 'w')
        self.fp_wget_file.write('#!/bin/sh\n')
        self.fp_wget_file.write('echo "will show progress every %i items"\n' % self.WGET_ECHO_INTERVAL)
        self.last_wget_echo = 0

        super(WgetFileParser, self).run()

        self.fp_wget_file.close()
        self.post_process()

    def ingest_item(self, rec):
        #wget -c http://www.theeuropeanlibrary.org/portal/images/treasures/hy10.jpg -O /tmp/europeana-cache/ORIGINAL/E5/A6/E5A68260A4BCEFF341E5B0138B3D2599E72DB6FD1D8F6D1AE23747F9119C4420.original
        url = ''.join(rec.oobject)
        url_hash = hashlib.sha256(url).hexdigest().upper()
        prefix = url_hash[:3]
        base_name = url_hash[3:]
        org_name = '%s/%s.original' % (prefix, base_name)
        cmd = 'wget -c -q "%s" -O %s' % (url, org_name)
        self.fp_wget_file.write('%s\n' % cmd)

        if self.record_count >= self.last_wget_echo + self.WGET_ECHO_INTERVAL:
            self.last_wget_echo = self.record_count
            self.fp_wget_file.write('echo "%i / %s"\n' % (self.record_count, self.ALL_ITEMS))

    def post_process(self):
        fd_tmp, fname_tmp = tempfile.mkstemp(dir=settings.MEDIA_ROOT)
        f_in = open(self.wget_file, 'r')
        for line in f_in.readlines():
            l2 = line.replace(self.ALL_ITEMS, str(self.record_count))
            os.write(fd_tmp, l2)
        f_in.close()
        os.close(fd_tmp)
        os.rename(fname_tmp, self.wget_file)


    def create_dirs(self):
        base_path = os.path.join(settings.MEDIA_ROOT,
                                 settings.DIR_ORIGINAL)
        if not os.path.exists(base_path):
            os.makedirs(base_path)

        wget_path = os.path.join(settings.MEDIA_ROOT,
                                 settings.DIR_WGET_FILES)
        if not os.path.exists(wget_path):
            os.makedirs(wget_path)

        hexdigits = '0123456789ABCDEF'
        for c1 in hexdigits:
            for c2 in hexdigits:
                for c3 in hexdigits:
                    ddir = os.path.join(base_path, c1 + c2 + c3)
                    if not os.path.exists(ddir):
                        os.mkdir(ddir)


