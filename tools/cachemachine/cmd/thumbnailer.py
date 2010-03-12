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


 Build thumbnails

 both FULL_DOC & BRIEF_DOC versions from files found in ORIGINAL


 TODO:
   analyse errors, extrace uri hash and log in db that this is a bad item
   and move it to DIR_BAD_ORIGINAL (if it was an original)
"""

import os
import time
import subprocess

import settings


INTERVALL_PROGRES = 10
IMG_CMD = 'mogrify'

error_count = 0


def run():
    base_dir = os.path.join(settings.MEDIA_ROOT, settings.DIR_ORIGINAL)
    t0 = time.time()
    for dirpath, dirnames, filenames in os.walk(base_dir):
        if dirpath == base_dir:
            continue
        sub_dir = os.path.split(dirpath)[1]
        if sub_dir < '85D':
            continue
        if t0 + INTERVALL_PROGRES  < time.time():
            print sub_dir
            t0 = time.time()
        if filenames:
            create_imgs(dirpath, sub_dir)
        pass
    if error_count:
        print 'Detected %i errors processing the tree' % error_count
    return


def create_imgs(dir_path, sub_dir):
    #
    #  FULL_DOC
    #
    dest_full = os.path.join(settings.MEDIA_ROOT, settings.DIR_FULL_DOC, sub_dir)
    cmd = ['%s -path %s' % (IMG_CMD, dest_full)]
    cmd.append('-format jpg')
    cmd.append('-define jpeg:size=260x200')
    cmd.append('-thumbnail 200x %s/*.original' % dir_path)
    p = subprocess.Popen(' '.join(cmd), shell=True, stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE, close_fds=True)
    retcode = p.wait()
    if retcode:
        err_msg = p.stderr.read()
        print '*** Error'
        global error_count
        error_count += 1
        print err_msg
        if 0: #not parse_error(err_msg): Skip analyse for the moment...
            sys.exit(1)


    #
    # BRIEF_DOC
    #
    """
    mogrify -path BRIEF_DOC/subdir1/subdir2
        -format jpg
        -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg

    """
    dest_brief = os.path.join(settings.MEDIA_ROOT, settings.DIR_BRIEF_DOC, sub_dir)
    cmd = ['%s -path %s' % (IMG_CMD, dest_brief)]
    cmd.append('-format jpg')
    cmd.append('-thumbnail x110 %s/*.jpg' % dest_full)
    p = subprocess.Popen(' '.join(cmd), shell=True, stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE, close_fds=True)
    retcode = p.wait()
    if retcode:
        err_msg = p.stderr.read()
        print '*** Error'
        global error_count
        error_count += 1
        print err_msg
        if 0: #not parse_error(err_msg): Skip analyse for the moment...
            sys.exit(1)
    return



def parse_error(err_msg):
    parts = err_msg.split(IMG_CMD)
    if len(parts) == 1:
        print 'Error analyse failed, Couldnt understand this at all'
        return False
    for line in parts[1:]: # we skip empty first
        parts2 = line.split(settings.MEDIA_ROOT)
        if len(parts2) == 1:
            print 'Error analyse failed, didnt find the path part'
            return False
        rel_fname = parts2[1].split("'")[0]
        if os.path.isabs(rel_fname):
            rel_fname = rel_fname[1:] # remove initial dir indicator
        full_path = os.path.join(settings.MEDIA_ROOT, rel_fname)
        if not os.path.exists(full_path):
            print 'Error analyse failed, what we thougt was a file-ref wasnt', full_path
            return False
        #
        # Ok now we now we have found a full_path to the offending file
        # time to break it down in parts and reconstruct the uri-hash, so
        # we can inform the database that we couldnt handle this item
        pass
    pass

