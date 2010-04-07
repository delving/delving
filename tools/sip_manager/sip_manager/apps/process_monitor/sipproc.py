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
"""

import os
import threading
import time

from django.conf import settings

import models


try:
    TASK_PROGRESS_INTERVALL = settings.TASK_PROGRESS_INTERVALL
except:
    TASK_PROGRESS_INTERVALL = 5




# thinks its a teeny bit faster to extract the setting once...
SIP_LOG_FILE = settings.SIP_LOG_FILE


SHOW_DATE_LIMIT = 60 * 60 * 20 # etas further than this will display date


RUNNING_EXECUTORS = []


class SipProcessException(Exception):
    pass




class SipProcess(object):
    """
    This is the baseclass for sip processes

    each subclass should define a run() that does the actual work

    all locking to the database are done by this baseclass
    """
    SHORT_DESCRIPTION = '' # a one-two word description.

    INIT_PLUGIN = False  # If True, is run (once) before normal plugins

    EXECUTOR_STYLE = False # Limited threading, at the most one instance of a class
                           # marked with this is started in a thread,
                           # but no more are started until the running executor
                           # has terminated

    IS_THREADABLE = False # Indicates this plugin is threadable
                          # and will be called repeatedly until it returns False
                          # a True result means that a thread was spawned.


    # For loadbalancing, set to True if this plugin uses a lot of system resources
    # taskmanager will try to spread load depending on what is indicated here
    PLUGIN_TAXES_CPU = False
    PLUGIN_TAXES_DISK_IO = False
    PLUGIN_TAXES_NET_IO = False

    # how often task status should be updated
    TASK_PROGRESS_TIME = TASK_PROGRESS_INTERVALL


    def __init__(self, debug_lvl=2, run_once=False):
        self.debug_lvl = debug_lvl
        self.run_once = run_once # if true plugin should exit after one runthrough
        self.pid = os.getpid()
        self.runs_in_thread = False
        self.is_prepared = False


    def run(self, *args, **kwargs):
        global RUNNING_EXECUTORS
        ret = False
        if self.short_name() == 'RequestParseNew':
            pass
        if self.EXECUTOR_STYLE:
            if self.short_name() not in RUNNING_EXECUTORS:
                if not self.do_prepare():
                    return
                self.log('++++++ Starting executor %s (%i)' % (self.short_name(), self.pm.pk), 8)
                RUNNING_EXECUTORS.append(self.short_name())
                ret = self.run_in_thread(self.run_it, *args, **kwargs)
            else:
                pass
        else:
            if not self.do_prepare():
                return
            ret = self.run_it(*args, **kwargs)

        if not self.runs_in_thread and self.is_prepared:
            self.process_cleanup()
        return ret


    def do_prepare(self):
        b = self.prepare()
        if b:
            "Do this once prepare has indicated something to be done."
            self.log('starting task    +++++   %s   +++++' % self.short_name(), 8)
            self.pm = models.ProcessMonitoring(pid=self.pid,
                                               plugin_module = self.__class__.__module__,
                                               plugin_name = self.__class__.__name__,
                                               task_label=self.SHORT_DESCRIPTION)
            self.pm.save()
            self.is_prepared = True
        return b


    def _log_task_exception_in_monitor(self, inst):
        "TODO: If this task is in the process monitor, log the failure"
        return False


    def log(self, msg, lvl=1):
        if self.debug_lvl < lvl:
            return
        print msg
        #f = open(LOG_FILE,'a')
        #f.write('%s\n' % msg)
        #f.close()
        open(SIP_LOG_FILE,'a').write('%s\n' % msg)

    def error_log(self, msg):
        print self.short_name(), msg


    def abort_process(self, msg):
        "Terminats process, trying to clean up and remove all pid locks."
        self.log('*********************')

        pms = models.ProcessMonitoring.objects.filter(pid=self.pid)
        for pm in pms:
            # TODO: a process failed, flag it, and remove its lock
            pass
        raise SipProcessException(msg)

    def process_cleanup(self):
        global RUNNING_EXECUTORS
        self.log('  Finished task  -----   %s' % self.short_name(), 8)
        pm_id = self.pm.pk
        self.pm.delete()
        if self.runs_in_thread and (self.short_name() in RUNNING_EXECUTORS):
            # the runs_in_thread check is needed, to avoid removing an actual
            # running executor if we are terminating due to this executor
            # already running.
            # a running executor will set runs_in_thread True
            self.log('------- terminating executor %s (%i)' % (self.short_name(), pm_id), 8)
            RUNNING_EXECUTORS.remove(self.short_name())
        return


    # ==========   Must be overloaded   ====================

    def run_it(self):
        msg = 'run_it() must be implemented!'
        print '******', msg
        raise SipProcessException(msg)


    # ==========   Can be overloaded   ====================

    def prepare(self):
        "This is called before run_it() if returns True, it indicates run_it will have something to do."
        return True


    # ==========   Thread handling   ====================

    def run_in_thread(self, mthd, *args, **kwargs):
        "If threading is disabled, mthd will be run normally."
        if settings.THREADING_PLUGINS:
            self.runs_in_thread = True
            args = (mthd,) + args
            t = threading.Thread(target=self.thread_wrapper,
                                 name='pmid-%i' % self.pm.pk,
                                 args=args, kwargs=kwargs)
            t.start()
            return True
        else:
            return mthd(*args, **kwargs)


    def thread_wrapper(self, *args, **kwargs):
        mthd = args[0]
        args = args[1:]
        mthd(*args, **kwargs)
        self.process_cleanup()


    # ==========   Pid locking mechanisms   ====================
    def grab_item(self, cls, pk, task_description):
        """Locks item to current pid, if successfull, returns updated item,
        otherwise returns None. Once the item is locked, nobody else but
        the locking process may modify it.

        It is reconended to use the returned object, instead of a possible earlier incarnation of it"""
        try:
            item = cls.objects.filter(pk=pk)[0]
        except:
            # item is gone, nothing cant be locked so fail
            return None

        if not item.pid:
            item.pid = self.pid
            item.save()
            self.pm.task_label=task_description
            self.pm.save()
            return item
        else:
            # item exists but is already taken
            return None


    def release_item(self, cls, pk):
        item = cls.objects.filter(pk=pk)[0]
        if not item.pid == self.pid:
            return False
        item.pid = 0
        item.save()
        return True
    # ==========   End of Pid locking mechanisms   ====================


    # ==========   Task steps   ====================

    def task_starting(self, label, steps=0):
        "new subtask starting, give at label and if possible no of steps."
        self._task_time_start = time.time()
        self._task_steps = steps
        self._task_previous = 0

        self.pm.task_label = label
        self.pm.task_progress = ''
        self.pm.task_eta = ''
        self.pm.save()
        self.task_progress(0)
        pass

    def task_progress(self, step):
        "update stepcount and eta (from last call to task_starting()."
        if self._task_steps and step: # avoid zero div
            perc_done, self.pm.task_eta = self._task_calc_eta(step)
            since_last = step - self._task_previous
            self._task_previous = step
            self.pm.task_progress = '%i/%i %i  (%0.2f%%)' % (step, self._task_steps,
                                                             since_last, perc_done)
        else:
            self.pm.task_progress = '%i' % step
            self.pm.task_eta = 'unknown'
        self.pm.save()
        self.log('%s  -  %s  eta: %s | %i' % (self.pm.task_label, self.pm.task_progress, self.pm.task_eta, self.pm.id), 7)

    def _task_calc_eta(self, step):
        percent_done = float(step) / self._task_steps * 100
        elapsed_time = time.time() - self._task_time_start
        eta_t_from_now = int(elapsed_time / ((percent_done / 100) or 0.001))
        eta = self._task_time_start + eta_t_from_now
        if (eta - time.time()) < SHOW_DATE_LIMIT:
            eta_s = time.strftime('%H:%M:%S', time.localtime(eta))
        else:
            eta_s = time.strftime('%m-%d %H:%M:%S', time.localtime(eta))
        return percent_done, eta_s

    # ==========   End of Task steps   ====================




    def __unicode__(self):
        return '%s - [%s] %s' % (self.aggregator_id, self.name_code, self.name)

    def short_name(self):
        "Short oneword version of process name."
        # find name of this (sub-) class
        return self.__class__.__name__

    def short_descr(self):
        if not self.SHORT_DESCRIPTION:
            raise NotImplemented('SHORT_DESCRIPTION must be specified in subclass')
        return self.SHORT_DESCRIPTION

