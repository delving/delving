import os
import sys
import time
import xml.parsers.expat
import xml.dom.minidom

import settings
from utils import glob_consts
from apps.cache_machine.models import Request, CacheItem


def cachemachine_starter(pm):
    pid = os.getpid()
    pm.pid = pid
    pm.save()

    #while True:
    req_pending = Request.objects.filter(sstate=glob_consts.ST_PENDING)
    if req_pending:
        r = req_pending[0]
        handle_pending_request(r)
    else:
        print 'Nothing to do'
    #time.sleep(5)



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
        #try:
        self.parser.ParseFile(f)
        #except:
        #    pass
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

