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
import time

from django.conf import settings

import models


# thinks its a teeny bit faster to extract the setting once...
SIP_LOG_FILE = settings.SIP_LOG_FILE



class SipProcessException(Exception):
    pass


class SipProcess(object):
    """
    This is the baseclass for sip processes

    each subclass should define a run() that does the actual work

    all locking to the database are done by this baseclass
    """
    SHORT_DESCRIPTION = '' # a one-two word description.

    # For loadbalancing, set to True if this plugin uses a lot of system resources
    # taskmanager will try to spread load depending on what is indicated here
    PLUGIN_TAXES_CPU = False
    PLUGIN_TAXES_DISK_IO = False
    PLUGIN_TAXES_NET_IO = False


    TASK_PROGRESS_TIME = 5 # how often task status should be updated


    def __init__(self, debug_lvl=2, run_once=False):
        self.debug_lvl = debug_lvl
        self.run_once = run_once # if true plugin should exit after one runthrough
        self.pid = os.getpid()
        self.pm = None # process monitor item ascociated with this task

    def run(self, *args, **kwargs):
        print 'running ', self.short_name()
        #try:
        ret = self.run_it(*args, **kwargs)
        #except Exception as inst:
        #    self._log_task_exception_in_monitor(inst)
        #    ret = False
        return ret

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
        pms = models.ProcessMonitoring.objects.filter(pid=self.pid)
        for pm in pms:
            # TODO: a process failed, flag it, and remove its lock
            pass
        raise SipProcessException(msg)

    # ==========   Pid locking mechanisms   ====================
    def grab_item(self, cls, pk, task_description):
        """Locks item to current pid, if successfull, returns updated item, otherwise returns None.
        Once the item is locked, nobody else but the locking process may modify it"""
        item = cls.objects.filter(pk=pk)[0]
        if not item.pid:
            item.pid = self.pid
            item.save()
            self.pm = models.ProcessMonitoring(pid=item.pid,
                                          task_label=task_description)
            self.pm.save()
            return item
        else:
            return None


    def release_item(self, cls, pk):
        item = cls.objects.filter(pk=pk)[0]
        if not item.pid == self.pid:
            return False
        item.pid = 0
        item.save()
        self.pm = None
        return True
    # ==========   End of Pid locking mechanisms   ====================


    # ==========   Task steps   ====================

    def task_starting(self, label, steps=0):
        "new subtask starting, give at label and if possible no of steps."
        self._task_time_start = time.time()
        self._task_steps = steps

        self.pm.task_label = label
        self.pm.task_progress = ''
        self.pm.task_eta = ''
        self.pm.save()
        self.task_progress(0)
        pass

    def task_progress(self, step):
        "update stepcount and eta (from last call to task_starting()."
        #percent_done = '%0.2f' % (float(step) / self._task_steps * 100)
        if self._task_steps and step: # avoid zero div
            perc_done, self.pm.task_eta = self._task_calc_eta(step)
            self.pm.task_progress = '%i / %i  (%0.2f%%)' % (step, self._task_steps,perc_done)
        else:
            self.pm.task_progress = '%i' % step
            self.pm.task_eta = 'unknown'
        self.pm.save()

    def _task_calc_eta(self, step):
        percent_done = float(step) / self._task_steps * 100
        eta_t_from_now = int( (time.time() - self._task_time_start) / ((percent_done / 100) or 0.001))
        eta = self._task_time_start + eta_t_from_now - time.time()
        hours = 0
        while eta > 3600:
            eta -= 3600
            hours += 1
        if hours:
            eta_s = '%02i:' % hours
        else:
            eta_s = ''
        minutes = 0
        while eta > 60:
            eta -= 60
            minutes += 1
        eta_s += '%02i:%02i' % (minutes, eta)
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

