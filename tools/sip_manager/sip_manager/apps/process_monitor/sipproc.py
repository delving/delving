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

    def __init__(self, run_once=False):
        self.run_once = run_once # if true plugin should exit after one runthrough

    def run(self, *args, **kwargs):
        print 'running ', self.short_name()
        return self.run_it(*args, **kwargs)

    def log(self, msg):
        print msg

    def error_log(self, msg):
        print self.short_name(), msg

    # Pid locking mechanisms
    def grab_item(self, cls, pk):
        "Locks item to current pid, if successfull, returns updated item, otherwise returns None."
        item = cls.objects.filter(pk=pk)[0]
        if not item.pid:
            item.pid = os.getpid()
            item.save()
            return item
        else:
            return None

    # End of Pid locking


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

