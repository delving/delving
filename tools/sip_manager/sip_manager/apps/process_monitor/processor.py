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

import sys
import time


from django.conf import settings
from django.db import connection

import sipproc


SIP_PROCESS_DBG_LVL = 7

class MainProcessor(object):
    def __init__(self, options):
        self.single_run = options['single-run']
        self.tasks_init = [] # tasks that should be run first
        self.tasks_simple = [] # list of all tasks found
        self.tasks_heavy = [] # resourcs hogs, careful with multitasking them...
        if options['flush-all']:
            self.flush_all()
            sys.exit(0)
        elif options['clear-pids']:
            self.clear_pids()
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
        # First run all init tasks once
        print
        print 'Running init plugins'
        for taskClass in self.tasks_init:
            tc = taskClass(debug_lvl=SIP_PROCESS_DBG_LVL)
            print '\t%s' % tc.short_name()
            tc.run()

        print
        print 'Commencing operations'
        while True:
            # First run all simple tasks once
            for task_group in (self.tasks_simple, self.tasks_heavy):
                for taskClass in task_group:
                    if settings.THREADING_PLUGINS and taskClass.IS_THREADABLE:
                        while taskClass(debug_lvl=SIP_PROCESS_DBG_LVL).run():
                            # Continue to start new threads as long as they
                            # find something to work on
                            #print '*** started thread for', taskClass.__name__
                            pass
                    else:
                        taskClass(debug_lvl=SIP_PROCESS_DBG_LVL).run()
            if self.single_run:
                print 'Single run, aborting after one run-through'
                break # only run once
            #print 'sleeping a while'
            time.sleep(10)
        return True

    """
    Scan all apps, find the tasks module and add all classes found there

    """
    def find_tasks(self):
        print 'Scanning for plugins'
        for app in settings.INSTALLED_APPS:
            if not app.find('apps') == 0:
                continue
            try:
                exec('from %s.tasks import task_list' % app )
                print ' %s:' % app,
                for task in task_list:
                    print task.__name__,
                    if task.INIT_PLUGIN:
                        self.tasks_init.append(task)
                        continue
                    resource_hog = False
                    if task.PLUGIN_TAXES_CPU:
                        resource_hog = True
                    if task.PLUGIN_TAXES_DISK_IO:
                        resource_hog = True
                    if task.PLUGIN_TAXES_NET_IO:
                        resource_hog = True
                    if resource_hog:
                        self.tasks_heavy.append(task)
                    else:
                        self.tasks_simple.append(task)
                print
            except ImportError as inst:
                if inst.args[0].find('No module named ') != 0:
                    raise inst
        print 'done!'


    def flush_all(self):
        cursor = connection.cursor()
        if 'mysql' in cursor.db.__module__:
            sql = 'TRUNCATE %s'
        else:
            sql = 'TRUNCATE %s CASCADE'
        for table in ('base_item_mdrecord',
                      'base_item_requestmdrecord',
                      'dummy_ingester_aggregator',
                      'dummy_ingester_dataset',
                      'dummy_ingester_provider',
                      'dummy_ingester_request',
                      'plug_uris_uri',
                      'plug_uris_urisource',
                      'process_monitor_processmonitoring',
                      ):
            cursor.execute(sql % table)
        return True

    def clear_pids(self):
        cursor = connection.cursor()
        cursor.execute('TRUNCATE process_monitor_processmonitoring')
        for table in ('base_item_mdrecord',
                      'dummy_ingester_request',
                      'plug_uris_uri',
                      'plug_uris_urisource',
                      ):
            cursor.execute('UPDATE %s SET pid=0' % table)


"""
  Request actions

  all items with status REQS_INIT should be parsed and MdRecords created
"""