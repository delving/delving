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

import time


from django.conf import settings

SIP_PROCESS_DBG_LVL = 9

class MainProcessor(object):
    def __init__(self, single_run=False):
        self.single_run = single_run
        self.tasks_init = [] # tasks that should be run first
        self.tasks_simple = [] # list of all tasks found
        self.tasks_heavy = [] # resourcs hogs, careful with multitasking them...
        self.find_tasks()

    def run(self):
        #a = UriCreateNewRecords()
        #b = a.short_name()
        for taskClass in self.tasks_init:
            tc = taskClass(debug_lvl=SIP_PROCESS_DBG_LVL)
            tc.run()

        while True:
            # First run all simple tasks once
            for task_group in (self.tasks_simple, self.tasks_heavy):
                for taskClass in task_group:
                    tc = taskClass(debug_lvl=SIP_PROCESS_DBG_LVL)
                    tc.run()
            #time.sleep(0.1)
            print 'Test mode, aborting after one run-through'
            break # only run once
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


"""
  Request actions

  all items with status REQS_INIT should be parsed and MdRecords created
"""