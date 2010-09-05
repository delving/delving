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

 Maintains and runs tasks
"""

import os
import sys
import time


from django.conf import settings
from django.db import connection

from utils.gen_utils import db_is_mysql

import sip_task
import models


# Since looking up in settings takes some extra cpu, important settings
# are cached within the module
PROCESS_SLEEP_TIME = settings.PROCESS_SLEEP_TIME
PLUGIN_FILTER = settings.PLUGIN_FILTER
SIPMANAGER_DBG_LVL = settings.SIPMANAGER_DBG_LVL


TASK_THROTTLE_TIME = 330

class MainProcessor(sip_task.SipTask):
    ALL_TABLES = [('base_item_mdrecord', True),
                  ('base_item_requestmdrecord', False),
                  ('dummy_ingester_aggregator', False),
                  ('dummy_ingester_dataset', False),
                  ('dummy_ingester_provider', False),
                  ('dummy_ingester_request', True),
                  ('log_errlog', False),
                  ('plug_uris_requri', False),
                  ('plug_uris_uri', True),
                  ('plug_uris_urisource', True),
                  ('%s_processmonitoring' % __name__.split('.')[-2], False)]


    def __init__(self, options):
        super(MainProcessor, self).__init__(debug_lvl=SIPMANAGER_DBG_LVL)
        self.single_run = options['single-run']
        self.tasks_init = [] # tasks that should be run first
        self.tasks = [] # list of all tasks found
        if options['flush-all']:
            self.cmd_flush_all()
            sys.exit(0)
        elif options['drop-all']:
            self.cmd_drop_all()
            sys.exit(0)
        elif options['clear-pids']:
            self.cmd_clear_pids()
            sys.exit(0)
        self.find_tasks()

    def run(self):
        "wrapper to catch Ctrl-C."
        try:
            self.run2()
        except KeyboardInterrupt:
            print 'Terminated from Keyboard!'
            sys.exit(1)
        return

    def run2(self):
        """
        Main loop inside a Ctrl-C
        """
        # First run all init tasks once
        print
        print ' =====   Running init plugins   ====='
        for taskClass in self.tasks_init:
            tc = taskClass(debug_lvl=SIPMANAGER_DBG_LVL)
            print '\t%s' % tc.short_name()
            tc.run()

        print
        print ' =====   Commencing operations   ====='
        print 'Tastk start limits  = (%0.1f, %0.1f, %0.1f)' % settings.MAX_LOAD_NEW_TASKS
        print 'Task kill limits    = (%0.1f, %0.1f, %0.1f)' % settings.MAX_LOAD_RUNNING_TASKS
        idle_count = 0
        while True:
            new_task_started = False
            busy = False
            for taskClass in self.tasks:
                busy, loads = self.system_is_occupied()
                if busy:
                    break
                task = taskClass(debug_lvl=SIPMANAGER_DBG_LVL)
                if task.run():
                    new_task_started = True
                    # it was started
                    # should we allow one or more plugs / sleep period?
                    # if no set busy = True here
                    if self.task_throttling():
                        # If we recently terminated a task, do slow starting,
                        # just one task per timeslot
                        busy = True
                        break

            if self.single_run:
                print 'Single run, aborting after one run-through'
                break # only run once


            if new_task_started:
                idle_count = 0
            else:
                idle_count += 1
            if SIPMANAGER_DBG_LVL > 7 or idle_count > 10: # dont indicate idling too often...
                idle_count = 0
                if not models.ProcessMonitoring.objects.all().count():
                    print ' nothing to do for the moment...'


            if self.task_throttling():
                t = max(PROCESS_SLEEP_TIME, 60)
                if SIPMANAGER_DBG_LVL > 5:
                    print '... throtled waiting'
            else:
                t = PROCESS_SLEEP_TIME
            time.sleep(t)

        return True


    def task_throttling(self):
        "Indicate a reasent task kill."
        if (sip_task.LAST_TASK_TERMINATION + TASK_THROTTLE_TIME) > time.time():
            b = True
        else:
            b = False
        return b


    """
    Scan all apps, find the tasks module and add all classes found there

    """
    def find_tasks(self):
        print ' =====   Scanning for plugins   ====='
        tasks = []
        for app in settings.INSTALLED_APPS:
            if not app.find('apps') == 0:
                continue
            try:
                exec('from %s.tasks import task_list' % app )
            except ImportError as inst:
                if inst.args[0].find('No module named ') != 0:
                    raise inst
                continue
            print ' %s:' % app,
            for task in task_list:
                print task.__name__,
                if task.INIT_PLUGIN:
                    self.tasks_init.append(task)
                    continue
                if PLUGIN_FILTER and not task.__name__ in PLUGIN_FILTER:
                    # we dont ever want to prevent task inits to run...
                    continue
                resource_hog = False
                if task.PLUGIN_TAXES_CPU:
                    resource_hog = True
                if task.PLUGIN_TAXES_DISK_IO:
                    resource_hog = True
                if task.PLUGIN_TAXES_NET_IO:
                    resource_hog = True
                tasks.append((task.PRIORITY, task))
            print

        tasks.sort()
        for pri, task in tasks:
            self.tasks.append(task)
        print 'done!'


    def cmd_flush_all(self):
        cursor = connection.cursor()
        if db_is_mysql:
            sql = 'TRUNCATE %s'
        else:
            sql = 'TRUNCATE %s CASCADE'
        for table, has_pid in self.ALL_TABLES:
            cursor.execute(sql % table)
        return True

    def cmd_drop_all(self):
        cursor = connection.cursor()
        if db_is_mysql:
            sql = 'DROP TABLE %s'
        else:
            sql = 'DROP TABLE %s CASCADE'
        for table, has_pid in self.ALL_TABLES:
            try:
                cursor.execute(sql % table)
            except:
                print 'Failed to remove %s' % table

        cursor.execute('commit')
        return True

    def cmd_clear_pids(self):
        cursor = connection.cursor()
        cursor.execute('TRUNCATE %s_processmonitoring' % __name__.split('.')[-2])

        # Clear request in progress
        cursor.execute('UPDATE dummy_ingester_request SET status=0 WHERE status=1')

        for table, has_pid in self.ALL_TABLES:
            if has_pid:
                cursor.execute('UPDATE %s SET pid=0 WHERE pid > 0' % table)
        cursor.execute('commit')

"""
  Request actions

  all items with status REQS_INIT should be parsed and MdRecords created
"""
