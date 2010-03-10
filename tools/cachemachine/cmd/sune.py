"""
God do I need to name this module to something meaningfull :)
"""

import os
import sys

import subprocess
import tempfile

import time
from django.core import exceptions
import xml.parsers.expat

import settings
from utils import glob_consts
from utils.proc_ctrl import set_process_ownership, clear_dead_procs
from apps.cache_machine.models import Request, CacheItem, CacheSource


def cachemachine_starter(pm):
    pid = os.getpid()
    pm.pid = pid
    pm.save()

    while 1:
        q_rec_pending = Request.objects.filter(sstate=glob_consts.ST_PENDING)
        if q_rec_pending:
            r = q_rec_pending[0]
            handle_pending_request(r)
        foo()
        time.sleep(0.1)
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


def is_valid_file(r):
    x = RequestParseXML(r,debug_lvl=4)
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

    def __str__(self):
        uri = ''.join(self.uri)
        isShownBy = ''.join(self.isShownBy)
        isShownAt = ''.join(self.isShownAt)
        s = 'uri: %s\nisShownBy: %s\nisShownAt: %s' % (uri, isShownBy, isShownAt)
        return s



class BaseXMLParser(object):
    def __init__(self, request, debug_lvl=2):
        self.request = request
        self.debug_lvl = debug_lvl
        self.fname = os.path.join(glob_consts.REQUEST_UPLOAD_PATH,
                                  settings.MEDIA_ROOT,
                                  self.request.fpath.name)

        self.record_count = 0
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
        if not (rec.uri and rec.isShownAt):
            self.log('missing data in record %i [%s]' % (self.record_count, rec),1)
        q = CacheItem.objects.filter(uri_obj=''.join(rec.isShownAt))
        if q:
            self.request.cache_items.add(q[0])
        else:
            self.request.cache_items.create(uri_id=''.join(rec.uri),
                                            uri_obj=''.join(rec.isShownAt))

        if self.record_count > 100:
            raise exceptions.ValidationError('devel temp done')
        return


class RequestParseXML(BaseXMLParser):

    def __init__(self, request, debug_lvl=2):
        super(RequestParseXML, self).__init__(request, debug_lvl)
        self.parser = xml.parsers.expat.ParserCreate()
        self.parser.StartElementHandler = self.start_element
        self.parser.EndElementHandler = self.end_element
        self.parser.CharacterDataHandler = self.char_data

        self.elements = []


    def run(self):
        self.log('Parsing xml file for records:%s' % self.fname, 1)
        f = open(self.fname)
        try:
            self.parser.ParseFile(f)
        except:
            pass
        f.close()
        self.log('xml parsing completed - found %i items' % self.record_count, 1)


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
        elif name == 'europeana:isShownBy':
            self.record.isShownBy.append(data)
        elif name == 'europeana:isShownAt':
            self.record.isShownAt.append(data)
        return


class RequestParseXML2(BaseXMLParser):
    """
    Runs out of memory on the enourmus xml files used here...
    """

    def run(self):
        self.log('Parsing xml file for records:%s' % self.fname)
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



from django.db.models import Avg, Max, Min, Count


class ImgRetrieval(object):
    def __init__(self):
        self.q_cache_items = None

        # If some serious issue was detected, like a http 403 response
        # we note it as a request message and abort all processing of that
        # request
        self.request_aborted = False

        self.pid = os.getpid()

    def run(self):
        cache_source = self.find_available_cache_source()
        if not cache_source:
            return

        f, tmpfnmae = tempfile.mkstemp()
        os.close(f)
        set_process_ownership(glob_consts.LCK_CACHESOURCE,
                              cache_source.pk,
                              glob_consts.CSS_RETRIEVING,
                              self.pid)
        self.process_cache_item_queue()
        set_process_ownership(glob_consts.LCK_CACHESOURCE,
                              cache_source.pk,
                              glob_consts.ST_IDLE,
                              0)


    def find_available_cache_source(self, recurse=True):
        qcs = CacheSource.objects.filter(pid=0)
        if not qcs:
            # All CacheSources are occupied, removing all proc references to
            # stale processes so next call to this might find a free one
            if clear_dead_procs(glob_consts.LCK_CACHESOURCE, glob_consts.ST_IDLE) and recurse:
                return self.find_available_cache_source(recurse=False)
            return None

        for cache_source in qcs:
            self.q_cache_items = CacheItem.objects.filter(source=cache_source,
                                                          sstate=glob_consts.ST_PENDING,
                                                          request__sstate=glob_consts.ST_COMPLETED)
            if len(self.q_cache_items):
                break # pick first cachesource with pending cacheitems
            pass
        if not self.q_cache_items:
            return None # Found no cacheitems to process

        return cache_source


    def process_cache_item_queue(self):
        for cache_item in self.q_cache_items:
            if self.request_aborted:
                return False
            set_process_ownership(glob_consts.LCK_ITEM,
                                  cache_item.pk,
                                  glob_consts.ST_PARSING,
                                  self.pid)
            #
            # Do the different manipulations of the cache_item
            # abort if one stage fails
            #
            for b in (self.verify_url(cache_item),
                      self.calculate_url_hash(cache_item),
                      self.get_file(cache_item),
                      self.create_large_img(cache_item),
                      self.create_thumb(cache_item),
                      ):
                if not b:
                    break # abort as soon as one step gives a failure
            if not b:
                cache_item.pid = 0
                # sstate and message should have been set by the individual checks
                cache_item.save()
                continue # try next cache_item

            # all steps have succeeded, mark item as completed
            cache_item.pid = 0
            cache_item.sstate = glob_consts.ST_COMPLETED
            cache_item.save()
            return True

    def thing_set_state(self, thing, sstate):
        thing.sstate = sstate
        thing.save()

    def verify_url(self, cache_item):
        "Check if item can be downloaded."
        msg = []
        if not cache_item.uri_obj:
            self.thing_set_state(cache_item, glob_consts.IS_NO_URI)
            return False
        try:
            itm = urllib2.urlopen(cache_item.uri_obj)#,timeout=URL_TIMEOUT)
        except urllib2.HTTPError, e:
            cache_item.set_msg('http error %i' % e.code)
            self.thing_set_state(cache_item, glob_consts.IS_HTTP_ERROR)
            if e.code == 403: # forbidden
                cache_item.request_set.set_msg('403 response on %s' % cache_item.uri_obj)
                self.request_aborted = True
            return False

        except urllib2.URLError, e:
            b = True
            if str(e.reason) == 'timed out':
                self.thing_set_state(cache_item, glob_consts.ST_TIMEOUT)
            else:
                reason = 'URLError %s' % str(e.reason)
                self.url_error += 1
                if self.url_error >= ABORT_LIMIT_URLERROR:
                    b = self.file_is_completed('too many urlerrors')
            self.add_bad_item(reason, url)
            return b
        except:
            return self.file_is_completed('Unhandled error: %s')

        if itm.code != 200:
            self.add_bad_item('HTML status: %i' % itm.code, url)
            return True
        try:
            content_t = itm.headers['content-type']#.split(';')[0]
        except:
            self.add_bad_item('Failed to parse mime-type',url)
            return True
        if content_t in (self.mime_ok):
            self.items_verified += 1
        else:
            if content_t in self.mime_not_good:
                self.add_bad_item(content_t, url)
            else:
                if self.odd_mime_is_valid_file(itm, content_t):
                    self.mime_ok.append(content_t)
                    self.items_verified += 1
                else:
                    self.mime_not_good.append(content_t)
                    self.add_bad_item(content_t, url)
        return True

    def calculate_url_hash(self, cache_item):
        "Calculate hashed filename based on url."
        return "hepp"


    def get_file(self, cache_item):
        # Get file
        excode = subprocess.call( 'wget %s --output-document=%s' % (cache_item.uri_obj, tmpfnmae), shell=True)
        if excode:
            self.release_cach_item(cache_item,
            print
            print '***   Aborting create_po_file() due to error!'
            #sys.exit(1)
        # Calc checksum
        # Store orig
        #return

    def create_large_img(self, cache_item):
        pass

    def create_large_img(self, cache_item):
        pass

