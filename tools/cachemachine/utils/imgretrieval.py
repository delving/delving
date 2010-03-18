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


 wget file 100 imgs - 54s
 db                   74s
"""
import os
import sys
import time
import urllib2
import hashlib
import subprocess
import tempfile

import settings

from apps.cache_machine.models import CacheItem, CacheSource
from utils.proc_ctrl import set_process_ownership, clear_dead_procs

from utils import glob_consts


WGET_TIMEOUT = 10
INTERVALL_PROGRES = 5
STORE_CHECKSUM = False # if CacheItems should store content checksum in content_hash

VALID_IMG_TYPES = ['jpeg',
                   'tif',
                   ]


class ImgRetrieval(object):
    def __init__(self, debug_lvl=2):
        self.debug_lvl = debug_lvl

        self.q_cache_items = None
        self.items_good = 0
        self.items_bad = 0
        self.temp_fname = ''
        self.mime_ok = ['audio/mpeg',
                        'image/gif',
                        'image/jpg',
                        'image/jpeg',
                        'image/png',
                        'image/tiff',
                        'video/mpeg',
                        'video/x-ms-wmv',
                        'application/pdf',
                        ]
        self.mime_not_good = ['text/html',] # mime types tested to be bad

        # If some serious issue was detected, like a http 403 response
        # we note it as a request message and abort all processing of that
        # request
        self.request_aborted = False

        self.pid = os.getpid()

    def run(self):
        cache_source = self.find_available_cache_source()
        if not cache_source:
            return False

        tmp_dir = os.path.join(settings.MEDIA_ROOT, 'tmp/')
        self.create_needed_subdirs(tmp_dir)
        f, self.temp_fname = tempfile.mkstemp(dir=tmp_dir)
        os.close(f)
        set_process_ownership(glob_consts.LCK_CACHESOURCE,
                              cache_source.pk,
                              glob_consts.CSS_RETRIEVING,
                              self.pid)
        if self.process_cache_item_queue():
            sstate = glob_consts.ST_IDLE
            ret = True
        else:
            sstate = glob_consts.CSS_ERROR
            ret = False
        cache_source.pid = 0
        cache_source.sstate = sstate
        cache_source.save()
        try:
            os.remove(self.temp_fname)
        except:
            pass # no prob temp file wasnt around
        return ret


    def find_available_cache_source(self, recurse=True):
        qcs = CacheSource.objects.filter(pid=0)
        if not qcs:
            # All CacheSources are occupied, removing all proc references to
            # stale processes so next call to this might find a free one
            if clear_dead_procs(glob_consts.LCK_CACHESOURCE, glob_consts.ST_IDLE) and recurse:
                # Since we cleared at least one, do one more check
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
        "Returns False if something is seriously wrong with this cachesource."
        item_count = len(self.q_cache_items)
        self.log('Handling %i images' % item_count, 2)
        items_done = 0
        self.time_started = t0 = time.time()
        for cache_item in self.q_cache_items:
            if self.request_aborted:
                return False
            if t0 + INTERVALL_PROGRES < time.time():
                self.show_progress(item_count, items_done)
                t0 = time.time()
            cache_item = set_process_ownership(glob_consts.LCK_ITEM,
                                               cache_item.pk,
                                               glob_consts.ST_PARSING,
                                               self.pid)
            #
            # Do the different manipulations of the cache_item
            # abort if one stage fails
            #
            for mthd in (self.verify_url,
                         self.calculate_old_style_url_hash,
                         #self.calculate_url_hash,
                         self.get_file,
                         #self.create_large_img,
                         #self.create_thumb,
                         ):
                b = mthd(cache_item)
                if not b:
                    break # abort as soon as one step gives a failure
            items_done += 1

            if self.items_bad > 150 and not self.items_good:
                cache_item.request_set.set_msg('Too many bad items %i of %i checked' % (self.items_bad, self.items_good))
                self.request_aborted = True
            if b:
                self.items_good += 1
                cache_item.sstate = glob_consts.ST_COMPLETED
            else:
                self.items_bad += 1
                # sstate and message should have been set by the individual checks

            # all steps have succeeded, mark item as completed and release it
            cache_item.pid = 0
            cache_item.save()

            if items_done > 99:
                return True # timing test
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
                # Should propably also mark the Request as bad?
            return False

        except urllib2.URLError, e:
            if str(e.reason) == 'timed out':
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

    def calculate_url_hash(self, cache_item):
        url_hash = hashlib.sha256(cache_item.uri_obj).hexdigest()
        cache_item.fname='%s/%s/%s' % (url_hash[:2], url_hash[2:4],url_hash)
        return False


    def calculate_old_style_url_hash(self, cache_item):
        """Calculate hashed filename based on url.
        for each file
        hashlib.sha256('http://www.theeuropeanlibrary.org/portal/images/treasures/hy10.jpg').hexdigest()
        get file and store in /ORIGINAL
        log as retrieved in db

        oldsyntax
        /3first/4: HASHNAME_BRIEF_DOC.jpg
        _FULL_DOC.jpg

        """
        url_hash = hashlib.sha256(cache_item.uri_obj).hexdigest().upper()
        cache_item.fname = '%s/%s' % (url_hash[:3], url_hash[3:])
        return True


    def get_file(self, cache_item):
        # Get file
        #cache_item.uri_obj = 'http://prensahistorica.mcu.es/es/catalogo_imagenes/imagen_id.cmd?idImagen=10426212&formato=jpg&altoMaximo=200&anchoMaximo=125'
        cmd = 'wget "%s" --timeout=%i --output-document=%s' % (cache_item.uri_obj,
                                                               WGET_TIMEOUT,
                                                               self.temp_fname)
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE, close_fds=True)
        retcode = p.wait()
        if retcode:
            msg = 'wget errcode: %s' % retcode
            self.log(msg + '\t' + cache_item.uri_obj)
            self.log('\terror message: %s' % p.stderr.read())
            self.thing_set_state(cache_item, glob_consts.IS_DOWNLOAD_FAILED)
            cache_item.set_msg(msg)
            return False

        # Verify its an ok file
        p2 = subprocess.Popen('identify %s' % self.temp_fname, shell=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                              close_fds=True)
        retcode = p2.wait()
        output = p2.stdout.read()
        msg = None
        if not retcode:
            try:
                img_type = output.split()[1].lower()
            except:
                img_type = output
            if img_type not in VALID_IMG_TYPES:
                msg = 'Bad image type: %s' % img_type
            else:
                # was an ok image, store item type!
                cache_item.i_type = glob_consts.IT_IMAGE
        else:
            msg = 'identify failed: %s' % retcode
        if msg:
            self.log(msg + '\t' + cache_item.uri_obj)
            self.log('\terror message: %s' % p2.stderr.read())
            self.thing_set_state(cache_item, glob_consts.IS_INVALID_DATA)
            cache_item.set_msg(msg)
            bad_org_fname = os.path.join(settings.MEDIA_ROOT,
                                         settings.DIR_BAD_ORIGINAL,
                                         cache_item.fname + '.original')
            self.create_needed_subdirs(bad_org_fname)
            os.rename(self.temp_fname, bad_org_fname)
            return False


        # Calc checksum
        if STORE_CHECKSUM:
            fp = open(self.temp_fname, 'r')
            data = fp.read()
            fp.close()
            cache_item.content_hash = hashlib.sha256(data).hexdigest()
            #cache_item.save()

        # Store orig
        org_fname = os.path.join(settings.MEDIA_ROOT,
                                 settings.DIR_ORIGINAL,
                                 cache_item.fname + '.original')
        os.rename(self.temp_fname, org_fname)
        return True

    def create_large_img(self, cache_item):
        return False

    def create_thumb(self, cache_item):
        return False


    def verify_odd_mimetype(self, mimetype):
        #url = '/Users/jaclu/Documents/ablm/reserakning.pdf'
        self.log('Checking strange mimetype: %s' % mimetype, 7)
        if mimetype.split(';')[0] in self.mime_ok:
            # only check when base mime is valid, but broken webserver gives
            # multiple fields as content type
            self.log('\tseems to be acceptable', 7)
            self.mime_ok.append(mimetype)
            ret = True
        else:
            self.log('!!!  invalid mimetype: %s' % mimetype, 4)
            self.mime_not_good.append(mimetype)
            ret = False
        return ret

    def log(self,msg,lvl=0):
        if self.debug_lvl < lvl:
            return
        print msg

    def show_progress(self, tot_count, items_done):
        perc_done = 100 * float(items_done) / (tot_count + 0.001)
        eta = 'eta: %s' % self.eta_calculate(time.time() - self.time_started, perc_done)
        msg = '%i/%i (%.2f%%)  \t%s' % (items_done, tot_count, perc_done, eta)
        if self.items_bad:
            msg += '\t good: %i\t bad: %i' % (self.items_good, self.items_bad)
        self.log(msg, 2)

    def eta_calculate(self, elapsed_time, perc_done):
        if perc_done == 0:
            perc_done = 0.001
        eta = elapsed_time / float(perc_done/100)
        h = 0
        while eta > 3600:
            h += 1
            eta -= 3600
        m = 0
        while eta > 60:
            m += 1
            eta -= 60
        if h:
            s = '%i:' % h
        else:
            s = '0:'
        if m:
            s += '%02i' % m
        else:
            s += '00'
        s += ':%02i' % eta
        return s


    def create_needed_subdirs(self, fname):
        "Make sure dir for fname exists."
        ddir = os.path.dirname(fname)
        if not os.path.exists(ddir):
            os.makedirs(ddir)

