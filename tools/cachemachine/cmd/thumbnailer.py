"""

http://www.theeuropeanlibrary.org/portal/images/treasures/hy10.jpg



wget -c http://www.theeuropeanlibrary.org/portal/images/treasures/hy10.jpg -O /tmp/europeana-cache/ORIGINAL/E5/A6/E5A68260A4BCEFF341E5B0138B3D2599E72DB6FD1D8F6D1AE23747F9119C4420.original


mogrify -path FULL_DOC/subdir1/subdir2
    -format jpg
    -define jpeg:size=260x200
    -thumbnail 200x *.original ;


mogrify -path BRIEF_DOC/subdir1/subdir2
        -format jpg
        -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg


<retrieval thread>
for each file
   hashlib.sha256('http://www.theeuropeanlibrary.org/portal/images/treasures/hy10.jpg').hexdigest()
   get file and store in /ORIGINAL
   log as retrieved in db

<image handling thread>
for each item in db
    identify
    if image
        mogrify -path FULL_DOC/subdir1/subdir2
    -format jpg
    -define jpeg:size=260x200
    -thumbnail 200x *.original ;


mogrify -path BRIEF_DOC/subdir1/subdir2
        -format jpg
        -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg




"""

import os
import subprocess

import settings


def run():
    base_dir = os.path.join(settings.MEDIA_ROOT, settings.DIR_ORIGINAL)
    for dirpath, dirnames, filenames in os.walk(base_dir):
        if dirpath == base_dir:
            continue
        if filenames:
            sub_dir = os.path.split(dirpath)[1]

            pass
        pass


def create_full_foc(dir_path, sub_dir):
    dest = os.path.join(settings.MEDIA_ROOT, settings.DIR_FULL_DOC, sub_dir)
    cmd = ['mogrify -path %s' % dest]
    cmd.append('-format jpg')
    cmd.append('-define jpeg:size=260x200')
    cmd.append('-thumbnail 200x %s/*.original' % dirpath)
    p = subprocess.Popen(' '.join(cmd), shell=True, stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE, close_fds=True)
    retcode = p.wait()
    if retcode:
        print '*** Error'
        print p.stderr.read()
        sys.exit(1)
