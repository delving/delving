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


 Two alternative image generation methods are available
   generate_images_pil() - 20% faster but more limited in image formats
   generate_images_magic() - richer support for image formats, slightly better image quality

 Selectable with the global boolean USE_IMAGE_MAGIC



 Url structure for generated thumbnails, can be one of

 1 = - old style (pre 0.6)

        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.FULL_DOC.jpg
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.BRIEF_DOC.jpg

 2 = from now on
        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as:
        original/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51
        FULL_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg
        BRIEF_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg

        only FULL_DOC & BRIEF_DOC is sent to production

Old img generation
FULL_DOC
mogrify -path /dest_dir
    -format jpg
    -define jpeg:size=260x200
    -thumbnail 200x [one orginals sub dir]/*.original


BRIEF_DOC
mogrify -path BRIEF_DOC/subdir1/subdir2
    -format jpg
    -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg
"""

import datetime
import hashlib
import subprocess
import os
import sys
import time
import urllib2
import urlparse
#from xml.dom.minidom import parseString


# sudo ln -s /opt/local/lib/libMagickWand.dylib /opt/local/lib/libWand.dylib
#from pythonmagickwand.image import Image

from django.db import connection
from django.conf import settings

from apps.base_item import models as base_item
from apps.process_monitor.sipproc import SipProcess

import models


try:
    OLD_STYLE_IMAGE_NAMES = settings.OLD_STYLE_IMAGE_NAMES
except:
    OLD_STYLE_IMAGE_NAMES = False

SIP_OBJ_FILES = settings.SIP_OBJ_FILES


FULLDOC_SIZE = (200, 10000)
BRIEFDOC_SIZE = (10000, 110)


USE_IMAGE_MAGIC = True
URL_TIMEOUT = 10





if not USE_IMAGE_MAGIC:
    try:
        from PIL import Image
    except ImportError:
        try:
            import Image
        except:
            print '*** No module named PIL - needed by %s' % __file__
            print '\tsuggested solution: sudo easy_install pil'
            sys.exit(1)



# To avoid typos, we define the dirnames here and later use theese vars
REL_DIR_ORIGINAL = 'original'
if OLD_STYLE_IMAGE_NAMES:
    REL_DIR_BRIEF = REL_DIR_FULL = 'OLD_STYLE_IMGS'
else:
    REL_DIR_FULL = 'FULL_DOC'
    REL_DIR_BRIEF = 'BRIEF_DOC'


class UriPepareStorageDirs(SipProcess):
    INIT_PLUGIN = True
    PLUGIN_TAXES_DISK_IO = True

    def run_it(self):
        if OLD_STYLE_IMAGE_NAMES:
            places = (REL_DIR_ORIGINAL, REL_DIR_BRIEF)
            tst_dir = '6EA'
        else:
            places = (REL_DIR_ORIGINAL, REL_DIR_FULL, REL_DIR_BRIEF)
            tst_dir = '12/32'

        for s in places:
            test_dir = os.path.join(SIP_OBJ_FILES, s, tst_dir)
            if not os.path.exists(test_dir):
                self.task_starting('Creating dirs for %s' % s, 256)
                if OLD_STYLE_IMAGE_NAMES:
                    self.pre_generate_uri_trees_old_style(s)
                else:
                    self.pre_generate_uri_trees(s)
        return True

    def pre_generate_uri_trees(self, prefix):
        hex_str = '0123456789ABCDEF'
        base_dir = os.path.join(SIP_OBJ_FILES, prefix)
        if not os.path.exists(base_dir):
            os.makedirs(base_dir)
        for f1 in hex_str:
            for f2 in hex_str:
                first_dir = os.path.join(base_dir, '%s%s' % (f1, f2))
                os.mkdir(first_dir)
                for s1 in hex_str:
                    for s2 in hex_str:
                        second_dir = os.path.join(first_dir, '%s%s' % (s1, s2))
                        os.mkdir(second_dir)

    def pre_generate_uri_trees_old_style(self, prefix):
        hex_str = '0123456789ABCDEF'
        base_dir = os.path.join(SIP_OBJ_FILES, prefix)
        if not os.path.exists(base_dir):
            os.makedirs(base_dir)
        for f1 in hex_str:
            for f2 in hex_str:
                for f3 in hex_str:
                    new_dir = os.path.join(base_dir, '%s%s%s' % (f1, f2, f3))
                    os.mkdir(new_dir)


class UriCreate(SipProcess):
    SHORT_DESCRIPTION = 'Create new uri records'
    EXECUTOR_STYLE = True

    def prepare(self):
        self.cursor = connection.cursor()
        # SQL logic:
        #   Finding MdRecords with no matching Uri items, since all MdRecords
        #   contains uri this indicates that this item is not processed
        # TODO: In desperate need of optimization!
        self.cursor.execute('SELECT DISTINCT m.id FROM base_item_mdrecord m LEFT JOIN plug_uris_uri u ON m.id = u.mdr_id WHERE u.mdr_id IS NULL')
        if self.cursor.rowcount:
            return True
        else:
            return False

    def run_it(self):
        self.task_starting('Creating new uri records', self.cursor.rowcount)
        record_count = 0
        while True:
            result = self.cursor.fetchone()
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
            self.task_time_to_show(record_count)
        return True

    def handle_md_record(self, mdr):
        """For the moment we cheat a bit and dont bother with treating this
        as xml, just plain old stringparsing
        """
        #dom = parseString(mdr.source_data)
        for tag, itype in (('<europeana:object>',models.URIT_OBJECT),
                           ('isShownAt>', models.URIT_SHOWNAT),
                           ):
            url = self.find_tag(tag, mdr.source_data)
            if url:
                break
        if not url:
            return False

        srvr_name = urlparse.urlsplit(url).netloc.lower()
        uri_sources = models.UriSource.objects.filter(name_or_ip=srvr_name)
        if uri_sources:
            uri_source = uri_sources[0]
        else:
            uri_source = models.UriSource(name_or_ip=srvr_name)
            uri_source.save()
        uri = models.Uri(mdr=mdr, item_type=itype, url=url, uri_source=uri_source)
        uri.save()
        return True

    def find_tag(self, tag, source_data):
        parts = source_data.split(tag)
        if len(parts) == 1:
            return None
        url = parts[1].split('<')[0]
        return url




class UriValidateSave(SipProcess):
    SHORT_DESCRIPTION = 'process new uri records'
    PLUGIN_TAXES_NET_IO = True
    #IS_THREADABLE = True

    def prepare(self):
        urisources = models.UriSource.objects.filter(pid=0)
        if not urisources:
            return False

        self.urisource = None
        for urisource in urisources:
            # Loop over available sources, see if any of them has pending jobs
            if models.Uri.objects.new_by_source_count(urisource.pk):
                # found one!  lets work on it
                self.urisource = urisource
                break

        if not self.urisource:
            return False

        return True # Found something to do!


    def run_it(self):
        # We must mark this item early, before possible threading kicks in
        # otherwise we might find it again before the thread actually starts
        # to work on the current set
        if not self.grab_item(models.UriSource, self.urisource.pk,'processing all imgs for source'):
            return False

        self.run_in_thread(self.do_one_source)
        return True


    def do_one_source(self):
        current_count = models.Uri.objects.new_by_source_count(self.urisource.pk)
        self.task_starting('Process new Uri records', current_count)
        record_count = 0
        for uri_id in models.Uri.objects.new_by_source_generator(self.urisource.pk):
            record_count += 1

            self.uri = self.grab_item(models.Uri, uri_id, 'Checking urls for %s' % self.urisource.name_or_ip)
            if not self.uri:
                continue # Failed to take control of it

            self.handle_uri()

            self.release_item(models.Uri, uri_id)

            self.task_time_to_show(record_count)

            if record_count > current_count:
                # More items has turned up since we started this loop,
                # not a problem as such but in order to show a correct eta and stuff
                # we should terminate now, and things will continue on next call
                # to this plugin
                break
        self.release_item(models.UriSource, self.urisource.pk)
        return True


    def handle_uri(self):
        url_itm = self.verify()
        if not url_itm:
            return False

        if self.uri.item_type == models.URIT_OBJECT:
            # we only download objects
            if not self.save_object(url_itm):
                return False

        self.uri_state(models.URIS_COMPLETED)
        return True


    def verify(self):
        "Check if url is responding and giving a 200 result."
        self.uri.time_lastcheck = datetime.datetime.now()
        try:
            itm = urllib2.urlopen(self.uri.url,timeout=URL_TIMEOUT)
        except urllib2.HTTPError, e:
            return self.set_urierr(models.URIE_HTTP_ERROR, 'http error: %i' % e.code)
        except urllib2.URLError, e:
            if str(e.reason) == 'timed out':
                code = models.URIE_TIMEOUT
                msg = ''
            else:
                code = models.URIE_URL_ERROR
                msg =  '%s: %s' % (models.URI_ERR_CODES[code], str(e.reason))
            return self.set_urierr(code, msg)
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR, 'Unhandled error when checking url')

        if itm.code != 200:
            return self.set_urierr(models.URIE_HTML_ERROR, 'HTML status: %i' % itm.code)
        try:
            content_t = itm.headers['content-type']
        except:
            return self.set_urierr(models.URIE_MIMETYPE_ERROR, 'Failed to parse mime-type')

        self.uri.mime_type = content_t
        self.uri_state(models.URIS_VERIFIED)
        return itm



    def save_object(self, itm):
        if self.uri.mime_type == 'text/html':
            return self.set_urierr(models.URIE_WAS_HTML_PAGE_ERROR)
        try:
            data = itm.read()
        except:
            return self.set_urierr(models.URIE_DOWNLOAD_FAILED,
                                   'Failed to read object content')
        try:
            content_length = int(itm.headers['content-length'])
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR,
                                   'Bad header response, missing "content-length"')

        if len(data) != content_length:
            return self.set_urierr(models.URIE_WRONG_FILESIZE,
                                   'Wrong filesize, expected: %i recieved: %i' % (content_length, len(data)))

        self.uri.content_hash = self.generate_hash(data)

        if OLD_STYLE_IMAGE_NAMES:
            base_fname = self.file_name_from_hash_old_style(self.generate_hash(self.uri.url))
        else:
            base_fname = self.file_name_from_hash(self.generate_hash(self.uri.url))
        org_fname = os.path.join(SIP_OBJ_FILES, REL_DIR_ORIGINAL, base_fname)
        #self.make_needed_dirs(org_fname)
        try:
            fp = open(org_fname, 'w')
            fp.write(data)
            fp.close()
        except:
            return self.set_urierr(models.URIE_FILE_STORAGE_FAILED,
                                   'Failed to save original')
        self.uri_state(models.URIS_ORG_SAVED)

        if USE_IMAGE_MAGIC:
            return self.generate_images_magic(base_fname, org_fname)
        else:
            return self.generate_images_pil(base_fname, org_fname)

    def NOT_make_needed_dirs(self, fname):
        dest_dir = os.path.split(fname)[0]
        try:
            if not os.path.exists(dest_dir):
                os.makedirs(dest_dir)
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR,
                                   'Failed to create dir [%s] for storing object file' % dest_dir)
        return True


    def generate_hash(self, thing):
        hex_hash = hashlib.sha256(thing).hexdigest().upper()
        return hex_hash

    def file_name_from_hash(self, url_hash):
        fname = '%s/%s/%s' % (url_hash[:2], url_hash[2:4],url_hash)
        return fname

    def file_name_from_hash_old_style(self, url_hash):
        "Old style (<= 0.6 release) way of generating filenames from hash."
        fname = '%s/%s' % (url_hash[:3], url_hash[3:])
        return fname


    def set_urierr(self, code, msg=''):
        if code not in models.URI_ERR_CODES:
            raise SipProcessException('set_urierr called with invalid errcode')
        self.uri.err_code = code
        if msg:
            self.uri.err_msg = msg
        else:
            # give name of error as default message
            self.uri.err_msg = models.URI_ERR_CODES[code]
        self.uri.save()
        return False # propagate error


    def uri_state(self, state):
        self.uri.status = state
        self.uri.save()

    #------------------   PIL utility methods   -------------------------------
    def generate_images_pil(self, base_fname, org_fname):

        try:
            img = Image.open(org_fname)
        except IOError as inst:
            return self.set_urierr(models.URIE_UNRECOGNIZED_FORMAT, inst.args)

        fname = os.path.join(SIP_OBJ_FILES, REL_DIR_FULL, '%s.jpg' % base_fname)
        img_full = self.clone_and_save_img(img, FULLDOC_SIZE, fname)
        self.uri_state(models.URIS_FULL_GENERATED)

        fname = os.path.join(SIP_OBJ_FILES, REL_DIR_BRIEF, '%s.jpg' % base_fname)
        self.clone_and_save_img(img_full, BRIEFDOC_SIZE, fname)
        self.uri_state(models.URIS_BRIEF_GENERATED)
        return True


    def clone_and_save_img(self, org_img, dest_size, fname):
        img2 = org_img.resize(self.resize_proportional(org_img.size, dest_size))
        #self.make_needed_dirs(fname)
        img2.save(fname)
        return img2

    def resize_proportional(self, org_size, max_size):
        org_x, org_y = org_size
        max_x,max_y = max_size
        x_factor = max_x / float(org_x)
        y_factor = max_y / float(org_y)
        factor = min(x_factor, y_factor)
        format_corrected = (int(org_x * factor), int(org_y * factor))
        return format_corrected

    #---------------   End of PIL utility methods   ---------------------------






    #--------------   ImageMagic utility methods   ----------------------------
    def generate_images_magic(self, base_fname, org_fname):
        """
        Old img generation
            FULL_DOC
            mogrify -path /dest_dir
                -format jpg
                -define jpeg:size=260x200
                -thumbnail 200x [one orginals sub dir]/*.original


            BRIEF_DOC
            mogrify -path BRIEF_DOC/subdir1/subdir2
                -format jpg
                -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg

        """
        if OLD_STYLE_IMAGE_NAMES:
            ext = '.FULL_DOC.jpg'
        else:
            ext = '.jpg'
        fname_full = os.path.join(SIP_OBJ_FILES, REL_DIR_FULL,
                                  '%s%s' % (base_fname, ext))
        cmd = ['/opt/local/bin/convert -resize 200x']
        cmd.append(org_fname)
        cmd.append(fname_full)
        if self.cmd_execute(cmd):
            return self.set_urierr(models.URIE_OBJ_CONVERTION_ERROR,
                                   'Failed to generate FULL_DOC')
        self.uri_state(models.URIS_FULL_GENERATED)

        if OLD_STYLE_IMAGE_NAMES:
            ext = '.BRIEF_DOC.jpg'
        else:
            ext = '.jpg'
        fname_brief = os.path.join(SIP_OBJ_FILES, REL_DIR_BRIEF,
                                   '%s%s' % (base_fname, ext))
        cmd = ['/opt/local/bin/convert -resize x110']
        cmd.append(fname_full)
        cmd.append(fname_brief)
        if self.cmd_execute(cmd):
            return self.set_urierr(models.URIE_OBJ_CONVERTION_ERROR,
                                   'Failed to generate BRIEF_DOC')
        self.uri_state(models.URIS_BRIEF_GENERATED)
        return True

    def cmd_execute(self, cmd):

        """Returns 0 on success, or errcode on failure.

        """
        if isinstance(cmd, (list, tuple)):
            cmd = ' '.join(cmd)
        try:
            retcode = subprocess.call(cmd, shell=True)
            """
            p = subprocess.Popen(cmd, shell=True)
            print '+++ cmd started', cmd
            while True:
                print 'hepp1'
                pid, retcode = -1
                try:
                    pid, retcode = os.waitpid(p.pid, os.WNOHANG)
                except:
                    print '*******  Wait problem %i %i' % (pid, retcode)
                print 'hepp2'
                if retcode:
                    self.die('found retcode %i' % retcode)
                    break
                time.sleep(0.1)
                if t0 + 10 < time.time():
                    self.die('timeout retcode')
                    p.kill()
                    retcode = -1
                    break
            """
            """
            p = subprocess.Popen(cmd, shell=True)
            while p.returncode == None:
                time.sleep(0.1)
                if t0 + 10 < time.time():
                    p.kill()
                    break
            if p.returncode != None:
                retcode = p.returncode
            else:
                retcode = -1
            """
        except:
            #self.cmd_lock.release()
            print '******************* cmd_execute() exception - terminating'
            print cmd
            sys.exit(1)
        #self.cmd_lock.release()
        #self.die('would have continued')
        return retcode

    def die(self, msg):
        print '**********', msg
        sys.exit(1)


class UriCleanup(SipProcess):
    SHORT_DESCRIPTION = 'Remove uri records no longer having a request'

    def run_it(self):
        "Not implemented"
        return True


class UriFileTreeMonitor(SipProcess):
    SHORT_DESCRIPTION = 'Walks file tree and finds orphan files'

    def run_it(self):
        "Not implemented"
        return True # does nothing for the moment



# List of active plugins from this file
task_list = [UriPepareStorageDirs,
             UriCreate,
             UriValidateSave,
             #UriCleanup,
             #UriFileTreeMonitor,
             ]