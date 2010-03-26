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

from django.conf import settings

#from utils.sipproc import SipProcess

#from apps.plug_uris.tasjsimport UriCreateNewRecords


class MainProcessor(object):
    def __init__(self, single_run=False):
        self.single_run = single_run
        self.tasks = [] # list of all tasks found

        # some lists of resource heavy tasks, that should propably not
        # be running in paralell
        self.taxes_cpu = []
        self.taxes_disk = []
        self.taxes_net = []
        self.find_tasks()


    def run(self):
        #a = UriCreateNewRecords()
        #b = a.short_name()
        return True

    """
    Scan all apps, find the tasks module and add all classes found there

    """
    def find_tasks(self):
        for app in settings.INSTALLED_APPS:
            if not app.find('apps') == 0:
                continue
            try:
                exec('from %s.tasks import task_list' % app )
                for task in task_list:
                    self.tasks.append(task)
                    if task.PLUGIN_TAXES_CPU:
                        self.taxes_cpu.append(task)
                    if task.PLUGIN_TAXES_DISK_IO:
                        self.taxes_disk.append(task)
                    if task.PLUGIN_TAXES_NET_IO:
                        self.taxes_net.append(task)
            except:
                pass
            pass


"""
  Request actions

  all items with status REQS_INIT should be parsed and MdRecords created
"""