import sys
import os
import shutil
import hashlib

import pexpect


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



class ItemRetriever(object):

    def __init__(self,loglvl=1, flush_all=False):
        self.loglvl = loglvl

        # this will be rmrfed on flush_all, so dont point it
        # to somewhere unsuitable, you have been warned...
        self._path_base = '/tmp/europeana-cache'

        self._path_sha256 = os.path.join(self._path_base, 'org_content')
        self._path_orgs = os.path.join(self._path_base, 'org_url')
        self._path_full = os.path.join(self._path_base, 'FULL_DOC')
        self._path_brief = os.path.join(self._path_base, 'BRIEF_DOC')
        self._path_tmp = os.path.join(self._path_base, 'tmp')
        self._ext_orig = '.original'
        self.sha_cmd = 'shasum -a 256'
        if flush_all:
            self.flush_all()
        return

    def log(self, msg, loglvl=1, flushit=True):
        if loglvl <= self.loglvl:
            if isinstance(msg, (list,tuple)):
                msg = ' '.join(msg)
            sys.stderr.write('%s\n' % msg)
            if flushit:
                sys.stderr.flush()

    def flush_all(self):
        if os.path.exists(self._path_base):
            shutil.rmtree(self._path_base)
        os.makedirs(self._path_base)
        os.makedirs(self._path_sha256)
        os.makedirs(self._path_orgs)
        os.makedirs(self._path_full)
        os.makedirs(self._path_brief)
        os.makedirs(self._path_tmp)


    def generate_hash(self, url=None):
        if not url:
            url=self.url
        shex = hashlib.sha256(url).hexdigest().upper()
        return shex

    def generate_hash_path(self, url=None):
        shex = self.generate_hash(url)
        fname = os.path.join(self._path_orgs, shex[:2], shex[2:4], shex)
        fname = os.path.join(self._path_orgs, shex)
        #return shex, fname
        return shex, fname

    def sha256_file(self, fname):
        output, excode = pexpect.run('%s %s' % (self.sha_cmd, fname),
                                     withexitstatus=True)
        if excode:
            print output
            print
            print '***   Aborting sha256_file() due to error!'
            sys.exit(1)
        sha256 = output.split()[0]
        #fname = os.path.join(self._path_sha256, sha256[:2], sha256[2:4], sha256)
        fname = os.path.join(self._path_sha256, sha256)
        return fname


    def retrieve_file(self, url):
        shex = self.generate_hash(url)
        #dest_fname = os.path.join(self._path_orgs, fname)
        tmpfile = os.path.join(self._path_tmp, shex)
        # --limit-rate=amount
        output, excode = pexpect.run('wget -x %s -O %s' % (url, tmpfile), # --force-directories
                             withexitstatus=True)
        if excode:
            print output
            print
            print '***   Aborting retrieve_file() due to error!'
            sys.exit(1)
        msg = [url,'-']
        try:
            speed = output.split('\r\n')[-3].split('(')[1].split(')')[0]
        except:
            speed = 'retrieved (unable to parse speed)'
        msg.append(speed)
        self.log(msg,2)
        return tmpfile

    def check_if_downloaded(self, url):
        tmpfile = self.retrieve_file(url)
        sha256fname = self.sha256_file(tmpfile)
        if not os.path.exists(sha256fname):
            self.handle_new_orig(tmpfile, sha256fname)
        self.link_urlorg(url, sha256fname)

    def handle_new_orig(self, tmpfile, sha256fname):
        self.log('handle_new_orig()', 9)
        self.create_original(tmpfile, sha256fname)
        ttype = self.identify(sha256fname)
        self.create_full(sha256fname)
        self.create_brief(sha256fname)

    def create_original(self, tmpfile, sha256fname):
        self.log('create_original()', 9)
        ddir = os.path.dirname(sha256fname)
        if not os.path.exists(ddir):
            os.makedirs(ddir)
        shutil.move(tmpfile, sha256fname)
        self.log('created original %s' % sha256fname, 9)

    def link_urlorg(self, url, sha256fname):
        foo, fname = self.generate_hash_path(url)
        if os.path.exists(fname):
            print '***  in link_urlorg()'
            print '***  original already exists!', url
            print '***  destination', fname
            sys.exit(1)
        output, excode = pexpect.run('ln %s %s' % (sha256fname, fname),
                                     withexitstatus=True)
        if excode:
            print '***  in link_urlorg()'
            print output
            print
            print '***   Aborting link_urlorg() due to error!'
            sys.exit(1)
        self.log('linked origfile %s' % url, 9)



    def identify(self, sha256fname):
        self.log('identify()', 9)
        output, excode = pexpect.run('identify %s' % sha256fname,
                                     withexitstatus=True)
        if output.split()[1].lower() in ('jpeg',):
            r = 'image'
        else:
            print '***  in identify()'
            print output
            print
            print '***   Aborting link_urlorg() due to error!'

        return r

    def create_full(self, sha256fname):
        """
        mogrify -path FULL_DOC/subdir1/subdir2
        -format jpg
        -define jpeg:size=260x200
        -thumbnail 200x *.original ;
        """

    def create_brief(self, sha256fname):
        """
        mogrify -path BRIEF_DOC/subdir1/subdir2
        -format jpg
        -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg
        """


def read_input_file(fname):
    f = open(fname)
    lines = f.readlines()
    f.close()
    ir = ItemRetriever(loglvl=9,flush_all=True)
    for line in lines:
        ir.check_if_downloaded(line[:-1])



read_input_file('92001_Ag_EU_TELtreasures_urls.request')



