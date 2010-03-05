import os
import sys
import time
import xml.parsers.expat


import settings
from utils import glob_consts
from apps.cache_machine.models import Request, CacheItem


def cachemachine_starter(pm):
    pid = os.getpid()
    pm.pid = pid
    pm.save()

    while True:
        req_pending = Request.objects.filter(sstate=glob_consts.ST_PENDING)
        if req_pending:
            r = req_pending[0]
            handle_pending_request(r)
        else:
            print 'Nothing to do'
        time.sleep(5)




def handle_pending_request(r):
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
    r.sstate = glob_consts.ST_PARSING
    r.save()
    if not is_valid_file(r):
        r.sstate = glob_consts.ST_FAILED
        r.message = 'File was not of correct format.'
        r.save()
        return
    r.sstate = glob_consts.ST_COMPLETED
    r.save()

def is_valid_file(r):
    #d = xml.dom.minidom.parse(fname)
    #doc = d.childNodes[0]
    x = RequestParseXML(r,debug_lvl=4)
    x.run()
    return True

class XmlRecord(object):
    uri = ''
    isShownBy = ''
    isShownAt = ''

    def __str__(self):
        s = 'uri: %s\nisShownBy: %s\nisShownAt: %s' % (self.uri, self.isShownBy,
                                                       self.isShownAt)
        return s


class RequestParseXML(object):

    def __init__(self, request, debug_lvl=2):
        self.request = request
        self.debug_lvl = debug_lvl
        self.fname = os.path.join(glob_consts.REQUEST_UPLOAD_PATH,
                                  settings.MEDIA_ROOT,
                                  self.request.fpath.name)

        self.parser = xml.parsers.expat.ParserCreate()
        self.parser.StartElementHandler = self.start_element
        self.parser.EndElementHandler = self.end_element
        self.parser.CharacterDataHandler = self.char_data

        self.elements = [] # stack for current element
        self.record_count = 0
        self.record = None # current record

        self.progress_intervall = 100
        self.last_progress = 0


    def run(self):
        self.log('Parsing xml file for records:%s' % self.fname)
        f = open(self.fname)
        #try:
        self.parser.ParseFile(f)
        #except:
        #    pass
        f.close()
        self.log('xml parsing completed')


    def check_record(self):
        if self.record_count > self.last_progress + self.progress_intervall:
            self.log('.', 1, add_lf=False)
            self.last_progress = self.record_count
        rec = self.record
        self.record = None
        #if self.record_count > 10:
        #    raise 'ss', 'rrrr'
        if not (rec.uri and rec.isShownAt):
            self.log('missing data in record %i [%s]' % (self.record_count, rec),1)
        q = CacheItem.objects.filter(uri_obj=rec.isShownAt)
        if q:
            self.request.cache_items.add(q[0])
        else:
            self.request.cache_items.create(uri_id=rec.uri, uri_obj=rec.isShownAt)
        return





    def start_element(self, name, attrs):
        self.log('Start element: %s %s' % (name, attrs), 5)
        self.elements.append(name)
        if name == 'record':
            self.record = XmlRecord()
        return

    def end_element(self, name):
        self.log('End element: %s' % name, 5)
        if name == 'record':
            self.record_count += 1
            self.check_record()
        if name != self.elements[-1]:
            pass
        self.elements.pop()

    def char_data(self, data):
        if data.strip():
            self.log('Character data: %s' % repr(data), 6)
        name = self.elements[-1]
        if name == 'europeana:uri':
            #if data == 'http://www.europeana.eu/resolve/record/06602/584643974D52FF7656F5630ADA373D4D96D454C4':
            #    pass
            self.record.uri += data
        elif name == 'europeana:isShownBy':
            self.record.isShownBy += data
        elif name == 'europeana:isShownAt':
            self.record.isShownAt += data
        return

    def log(self, msg, lvl=2, add_lf=True):
        if self.debug_lvl < lvl:
            return
        if add_lf:
            msg += '\n'
        sys.stdout.write(msg)
        sys.stdout.flush()
