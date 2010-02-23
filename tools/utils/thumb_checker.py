#!/usr/bin/env python
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


 ThumbChecker for items Europeana should cache


 Version 0.0.2

 History:
 100223 jaclu  Initial release
"""

import os
import sys
import time
import urllib2
import urlparse
import threading

img = 'http://europeana.eu/portal/images/think_culture_logo_top_5.jpg'
ss = 'http://alma.ablm.se/w2ko2k.tgsz'

ITM_START = 'itm_start'
ITM_COUNT = 'itm_count'
ITM_DONE = 'itm_done'
ITM_OK = 'itm_ok'
ITM_BAD = 'itm_bad'
TIMEOUTS = 'timeouts'
ITM_BAD_TYPES = 'itm_bad_types'
PROVIDER_NAME = 'prov_name'
ABORT_REASON = 'abort_reason'

URL_TIMEOUT = 10
INTERVALL_REPORT = 120
INTERVALL_PROGRES = 60
MAX_NO_OF_TIMEOUTS = 10
MAX_NO_OF_WARNINGS = 250
MAX_NO_THREADS = 500

REPORT_FILE = '/tmp/thumb_checker_report'

class VerifyProvider(object):

    def __init__(self, debug_lvl=4):
        self.debug_lvl = debug_lvl
        self.extra_msgs = {}
        self.in_progress = {}
        self.reports = {}
        self.is_running = False
        self.q_waiting = []
        self.running_threads = {} # all running checks, key is hostname

    def run(self, param):
        if self.is_running:
            print 'Aborting, this instance is already running'
            return 1
        self.is_running = True
        if os.path.isdir(param):
            self.do_dir(param)
        else:
            self.do_file(param)
        self.create_report()
        self.is_running = False

    #=============  internals  ==============

    def do_dir(self, ddir):
        files = os.listdir(ddir)
        self.q_waiting = []
        for fname in files:
            if os.path.isdir(fname):
                continue # skip subdirs
            server_id = self.get_server_id(os.path.join(ddir, fname))
            self.q_waiting.append((server_id, os.path.join(ddir,fname)))
        self.start_free_hosts()
        t0 = t1 = time.time()
        while threading.activeCount() > 1:
            if t0 + INTERVALL_REPORT < time.time():
                self.create_report(to_file=True)
                t0 = time.time()
            time.sleep(1)
            if t1 + INTERVALL_PROGRES < time.time():
                self.display_progress()
                t1 = time.time()
            self.start_free_hosts()
            if threading.activeCount() == 1:
                pass


    def start_free_hosts(self):
        for host_name, fname in self.q_waiting[:]:
            if threading.activeCount() > MAX_NO_THREADS:
                return
            # we are working on a copy, so ok to modify self.q_waiting
            if not host_name:
                qname = self.create_qname(fname)
                self.add_report(qname,self.struct_collection(qname))
                self.q_waiting.remove((host_name, fname))
            elif not self.is_host_name_used(host_name):
                self.q_waiting.remove((host_name, fname))
                self.queue_add(host_name, fname)
        return

    def is_host_name_used(self, host_name):
        return host_name in self.running_threads.keys()

    def queue_add(self, host_name, fname):
        "Add this collection to a separate thread and start it."
        if self.is_host_name_used(host_name):
            print '*** Serious error, attempt to run towards occupied host'
            print host_name, fname
            sys.exit(1)

        time.sleep(0.1)
        self.running_threads[host_name] = {
            'thread':threading.Thread(target=self.run_thread,name=fname,args=(host_name,fname,))
        }
        self.running_threads[host_name]['thread'].start()

    def run_thread(self, host_name, fname):
        #print '++++ starting thread for', host_name
        self.do_file(fname)
        time.sleep(0.1)
        #print '---- terminating thread for', host_name
        del self.running_threads[host_name]

    def get_server_id(self, fname):
        hostname = ''
        fp = open(fname)
        lines = fp.readlines()
        fp.close()
        for line in lines:
            if 'wget' in line:
                url = line.split('wget ')[1].split('-O')[0].strip()
                hostname = urlparse.urlsplit(url).hostname
                break
        return hostname




    def report_print(self, msg):
        self.report_fp.write('%s\n' % msg)
        self.report_fp.flush()

    def create_report(self, to_file=False):
        if to_file:
            self.report_fp = open(REPORT_FILE, 'w')
        else:
            self.report_fp = sys.stdout
        self.report_print('\n')
        self.report_print('=' * 80)
        self.report_print('\n\tAvailability of thumbnail items on servers\n')
        providers = self.reports.keys()
        providers.sort()
        self.report_print('')
        for provider in providers:
            collecions = self.reports[provider].keys()
            collecions.sort()
            col_summary = self.struct_collection('summary')
            for collection in collecions:
                rep = self.reports[provider][collection]
                msg = '%s \titems: %i' % (rep[PROVIDER_NAME], rep[ITM_COUNT])
                if not rep[ITM_COUNT]:
                    msg += '\t<==='
                if rep[TIMEOUTS]:
                    msg += '\ttimeouts: %i\t<===' % rep[TIMEOUTS]
                if rep[ITM_BAD]:
                    msg += ' \tBAD ITEMS: %i (%.1f%%)' % (rep[ITM_BAD],
                                                          100 * rep[ITM_BAD]/float(rep[ITM_COUNT]))
                if rep[ABORT_REASON]:
                    msg += '  %s  <===== Aborted' % rep[ABORT_REASON]
                self.report_print(msg)
                for k in (ITM_COUNT,ITM_DONE, ITM_OK, ITM_BAD):
                    col_summary[k] += rep[k]
                if collection in self.extra_msgs.keys():
                    for msg in self.extra_msgs[collection].keys():
                        s = ' ' + msg
                        if self.extra_msgs[collection][msg] > 1:
                            s += ' (repeated %i times)' % self.extra_msgs[collection][msg]
                        self.report_print(s)
            msg = '%s \titems: %i' % (provider, col_summary[ITM_COUNT])
            if col_summary[ITM_BAD]:
                msg += ' \tBAD ITEMS: %i (%.1f%%)' % (col_summary[ITM_BAD],
                                                      100 * col_summary[ITM_BAD]/float(col_summary[ITM_COUNT]))
            self.report_print(msg)
            self.report_print('\n')
            self.report_print('-' * 80)
            self.report_print('\n')
        if to_file:
            self.report_fp.close()
        return



    def do_file(self,fname):
        qname = self.create_qname(fname)
        self.log('Starting processing of %s' % qname, 2)
        urllist = self.generate_urllist(fname)
        itm_ok = itm_bad = 0
        t0 = time.time()
        self.in_progress[qname] = self.struct_collection(qname, len(urllist))
        q = self.in_progress[qname]
        for url in urllist:
            try:
                itm = urllib2.urlopen(url)#,timeout=URL_TIMEOUT)
                content_t = itm.headers['content-type'].split(';')[0]
                if content_t not in ('audio/mpeg',
                                     'image/gif',
                                     'image/jpg',
                                     'image/jpeg',
                                     'image/png',
                                     'application/pdf',):
                    if not q[ITM_BAD_TYPES].has_key(content_t):
                        q[ITM_BAD_TYPES][content_t] = 0
                    q[ITM_BAD_TYPES][content_t] += 1
                    if content_t not in ('text/html','text/plain',):
                        if not self.report_log(qname, 'unknown bad content type: %s' % content_t):
                            q[ABORT_REASON] = 'to many bad contents'
                            break
                else:
                    q[ITM_OK] += 1
                if itm.code != 200:
                    if not self.report_log(qname, 'None 200 msg - %i' %itm.code):
                        q[ABORT_REASON] = 'to many None 200'
                        break
            except urllib2.HTTPError, e:
                s = '!'
                q[ITM_BAD] += 1
                if e.code == 403: # forbidden
                    q[ABORT_REASON] = '403 response'
                    break
                if e.code != 404:
                    if not self.report_log(qname, 'None 404 error - %i' % e.code):
                        q[ABORT_REASON] = 'to many None 404'
                        break
                if q[ITM_BAD] > 150 and ((q[ITM_DONE] - q[ITM_BAD]) < 25):
                    q[ABORT_REASON] = 'to many 404 results'
                    break
            except urllib2.URLError, e:
                if str(e.reason) == 'timed out':
                    q[TIMEOUTS] += 1
                    if q[TIMEOUTS] > MAX_NO_OF_TIMEOUTS:
                        q[ABORT_REASON] = 'to many timeouts'
                        break
            q[ITM_DONE] += 1
            self.add_report(qname, q)
            if t0 + INTERVALL_PROGRES < time.time() and not self.running_threads:
                # dont do this when we are multithreaded
                self.display_progress()
                t0 = time.time()

        self.file_is_completed(qname)

    def file_is_completed(self, qname):
        self.add_report(qname,self.in_progress[qname])
        del self.in_progress[qname]

    def create_qname(self, fname):
        qname = os.path.split(fname)[1].split('_urls')[0]
        return qname

    def add_report(self, qname, q):
        provider = qname[:3]
        collection = qname[:5]
        if not self.reports.has_key(provider):
            self.reports[provider] = {}
        self.reports[provider][collection] = q

    def display_progress(self):
        print
        keys = self.in_progress.keys()
        keys.sort()
        remaining = 0
        eta_max = 0
        for key in keys:
            try:
                q = self.in_progress[key]
            except:
                # propably just completed in other thread
                print 'skipping terminated thread in report', key
                continue
            perc_done = 100 * float(q[ITM_DONE]) / q[ITM_COUNT]
            remaining += (q[ITM_COUNT] - q[ITM_DONE])
            eta = self.eta_calculate(time.time()-q[ITM_START], perc_done)
            eta_num = self.numeric_eta(eta)
            if eta_num > eta_max:
                eta_max = eta_num
                s_eta_max = eta

            msg = '%25.25s %i/%i (%.2f%%)  \teta: %s' % (key, q[ITM_DONE], q[ITM_COUNT],
                                               perc_done, eta)

            if q[ITM_BAD]:
                p = 100 * q[ITM_BAD]/float(q[ITM_DONE])
                msg += ' - Bad items: %i (%.2f%%)' % (q[ITM_BAD],p)
            self.log(msg, 2)
        if threading.activeCount() > 1:
            print '==== threadcount %i \twaiting collections %i \tcurrently pending files to check %i eta: %s' % (threading.activeCount() - 1,
                                                                                                                  len(self.q_waiting),
                                                                                                                  remaining,
                                                                                                                  s_eta_max)
        self.time_progess = time.time()

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

    def numeric_eta(self, eta):
        h,m,s = eta.split(':')
        ih = int(h)
        im = int(m)
        iis = int(s)
        return (ih,im,iis)



    def report_log(self, qname, msg):
        collection = qname[:5]
        if not collection in self.extra_msgs.keys():
            self.extra_msgs[collection] = {}
        if not msg in self.extra_msgs[collection].keys():
            self.extra_msgs[collection][msg] = 0
            self.log('** %s %s' % (qname, msg),1)
        self.extra_msgs[collection][msg] += 1
        if self.extra_msgs[collection][msg] > MAX_NO_OF_WARNINGS:
            r = False
        else:
            r = True
        return r


    def log(self,msg,lvl=2):
        if lvl > self.debug_lvl:
            return

        print msg

    def parse_report_name(self,fname):
        s = os.path.split(fname)[1]
        return s

    def generate_urllist(self,fname):
        fp = open(fname)
        lines = fp.readlines()
        fp.close()
        urllist = []
        for line_lf in lines:
            line = line_lf.strip()
            if not line or line[0]=='#':
                continue
            line.split('wget')
            url = line.split('wget ')[1].split('-O')[0].strip()
            urllist.append(url)
        return urllist


    def struct_collection(self, qname, itm_count=0):
        return {ITM_COUNT: itm_count,
                ITM_DONE:0,
                ITM_START: time.time(),
                ITM_OK: 0,
                ITM_BAD: 0,
                PROVIDER_NAME: qname,
                ABORT_REASON:'',
                ITM_BAD_TYPES:{},
                TIMEOUTS: 0,
                }


try:
    p = sys.argv[1]
except:
    print 'Run with dir to cache output as param!'
    print 'optional second param is where to store temp reports'
    print 'othervise %s will be used' % REPORT_FILE
    sys.exit(1)

if len(sys.argv) > 2:
    REPORT_FILE = sys.argv[2]

vp = VerifyProvider()
vp.run(p)
