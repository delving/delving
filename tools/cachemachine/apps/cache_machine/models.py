"""

* Traverse by file to check for match in db - finding orphan files
* Traverse by CacheItem to check filesystem - missing files
* Traverse by CacheItem to verify existance of item
"""

import socket
import urlparse
import threading


from django.core import exceptions
from django.db import models

from django.contrib import admin
from django.contrib import databrowse

from utils import glob_consts
from utils.gen_utils import dict_2_django_choice



"""

This system uses separate subprocesses and threads to do most of the actual
work on managing cache items. In order to avoid collitions we have a global lock
that makes sure that only one process can attempt to change ownership of a task
"""
db_procs_lock = threading.Lock() # Global lock for processes taking/releasing tasks

def set_process_ownership(pid, # pid of requestor
                          req_state,
                          module):
    """
    To avoid the race condition where proc a is starting and wants to take care
    of retrieving a image, then before it has locked that image to itself another
    process/thread also want to get to the same, they must tell us what they belive
    is the previous state.

    ------   Time 0 both want access, changing state 10 => 20   -----

    A  [pidof A]   20        Item state: 10
    B  [pidof B]   20        Item PID:   None


    -------   Time 1 A has aquired a lock and now B gets a go   -------

    A already done   Item state: 20
    B [pidof B 20    Item PID:   [pidof A]

    Since B realisez That pidof A is currently "owning" the item it double checks
    if there is a [pidof A], if it is it just fails. If the process has disapeared
    without releasing the lock (flagged by an item pid) a system error is logged
    also
    """
    return False


class CacheSource(models.Model):
    """
    Identifies one server providing thumbnail resources to Europeana, to avoid
    the risk that we hammer the same server with multiple requests
    """
    fqdn = models.CharField(max_length=200)
    #
    # a complete walkthrough of this source will be evenly spread over this
    # amaount of days
    #
    traversaltime = models.IntegerField(default=180,
                                        #'How often in days can we rechek things from this server.'
                                        )
    created_date = models.DateTimeField(auto_now_add=True, editable=False)
    checked_date = models.DateTimeField(auto_now=True, editable=False) # last time item was verified

#admin.site.register(CacheSource)


class CacheItem(models.Model):
    """
    A CacheItem is basically a thumbnail that one or more sources have requested
    """
    uri_id = models.URLField('Europeana uri')
    uri_obj = models.URLField('obj uri', unique=True)  #help_text='This is the')

    source = models.ForeignKey(CacheSource)

    fname = models.TextField('url hash')
    cont_hash = models.TextField('content hash')
    sstate = models.IntegerField(choices=dict_2_django_choice(glob_consts.ITEM_STATES),
                                 default = glob_consts.ST_INITIALIZING,
                                 editable=False)
    i_type = models.IntegerField(choices=dict_2_django_choice(glob_consts.ITEM_TYPES), # kind of item
                                 default = glob_consts.IT_UNKNOWN,
                                 editable=False)
    created_date = models.DateTimeField(auto_now_add=True, editable=False)
    checked_date = models.DateTimeField(null=True, editable=False) # last time item was verified

    def save(self, force_insert=False, force_update=False):
        if self.sstate == glob_consts.ST_INITIALIZING:
            self.do_initialize()
        super(CacheItem, self).save(force_insert,force_update)

    def do_initialize(self):
        if not self.uri_id:
            raise exceptions.ValidationError('Europeana uri missing')
        if not self.uri_obj:
            raise exceptions.ValidationError('obj uri missing')

        if not urlparse.urlparse(self.uri_obj)[1]:
            pass
        s = urlparse.urlparse(self.uri_obj)[1]
        fqdn = socket.getfqdn(socket.gethostbyname(s))
        sources = CacheSource.objects.filter(fqdn=fqdn)
        if not sources:
            cs = CacheSource(fqdn=fqdn)
            cs.save()
        else:
            cs = sources[0]
        self.source = cs

        self.sstate = glob_consts.ST_PENDING

#admin.site.register(CacheItem)
databrowse.site.register(CacheItem)


class Request(models.Model):
    """
    A Request is typically a ingestion file

    """
    provider = models.CharField(max_length=3,editable=False)
    collection = models.CharField(max_length=5,editable=False)

    fname = models.CharField(max_length=200, editable=False)
    fpath = models.FileField(upload_to=glob_consts.REQUEST_UPLOAD_PATH, help_text='')
    req_time = models.DateTimeField(auto_now_add=True, editable=False)
    sstate = models.IntegerField(choices=dict_2_django_choice(glob_consts.REQUEST_STATES),
                                default = glob_consts.ST_INITIALIZING,
                                editable=False)
    message = models.TextField(default='', editable=False)
    cache_items = models.ManyToManyField(CacheItem, editable=False)

    def __unicode__(self):
        return self.fname

    def save(self):
        super(Request, self).save()
        if self.sstate == glob_consts.ST_INITIALIZING:
            self.fname = self.fpath.name.split('/')[-1]
            self.provider = self.fname[:3]
            self.collection = self.fname[:5]
            self.sstate = glob_consts.ST_PENDING
            super(Request, self).save()
        return

admin.site.register(Request)
databrowse.site.register(Request)



class ProcessMonitoring(models.Model):
    """
    Keeping track of various external processes working towards the database
    """
    role = models.IntegerField(choices=dict_2_django_choice(glob_consts.PROC_ROLES))
    pid = models.IntegerField()
    sstate = models.IntegerField(choices=dict_2_django_choice(glob_consts.PM_STATES))
    start_time = models.DateTimeField(auto_now_add=True, editable=False)


if 0: # some testcode
    r = Request.objects.filter(collection='03907')
    if r:
        r[0].sstate=10
        r[0].save()

if 0: # some test code
    r = Request()
    r.save()
    ci = CacheItem(uri_id='wwwwww',
                  uri_obj='http://www.sunet.se/gadnd.jpg')
    ci.save()
    r.cache_items.create(uri_id='sune',
                         uri_obj='http://www.sunet.se/gadnd.jpg')




