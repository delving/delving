"""

* Traverse by file to check for match in db - finding orphan files
* Traverse by CacheItem to check filesystem - missing files
* Traverse by CacheItem to verify existance of item

oldsyntax
/3first/4: HASHNAME_BRIEF_DOC.jpg
                   _FULL_DOC.jpg

"""

import socket
import urlparse


from django.core import exceptions
from django.db import models

from django.contrib import admin
from django.contrib import databrowse

from utils import glob_consts
from utils.gen_utils import dict_2_django_choice


class MessageModel(object):
    # Dont forget to add this line to the class:
    #message = models.TextField(default='', editable=False)

    def set_msg(self, msg, append=True):
        "Normaly appends msg."
        if append:
            msg = '\n'.join(self.message, msg)
        self.message = msg
        self.save()




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
    pid = models.IntegerField(default=0) # what process 'owns' this item
    sstate = models.IntegerField(choices=dict_2_django_choice(glob_consts.ITEM_STATES),
                                 default = glob_consts.ST_IDLE,
                                 editable=False)

#admin.site.register(CacheSource)


class CacheItem(models.Model, MessageModel):
    """
    A CacheItem is basically a thumbnail that one or more sources have requested
    """
    uri_id = models.URLField('Europeana uri')
    uri_obj = models.URLField('obj uri', unique=True)  #help_text='This is the')

    source = models.ForeignKey(CacheSource)

    fname = models.TextField('url hash')
    cont_hash = models.TextField('content hash')
    pid = models.IntegerField(default=0) # what process 'owns' this item
    sstate = models.IntegerField(choices=dict_2_django_choice(glob_consts.ITEM_STATES),
                                 default = glob_consts.ST_INITIALIZING,
                                 editable=False)
    i_type = models.IntegerField(choices=dict_2_django_choice(glob_consts.ITEM_TYPES), # kind of item
                                 default = glob_consts.IT_UNKNOWN,
                                 editable=False)
    created_date = models.DateTimeField(auto_now_add=True, editable=False)
    checked_date = models.DateTimeField(null=True, editable=False) # last time item was verified
    message = models.TextField(default='', editable=False)

    def save(self, *args, **kwargs):
        if self.sstate == glob_consts.ST_INITIALIZING:
            self.do_initialize()
        super(CacheItem, self).save(*args, **kwargs)

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


class Request(models.Model, MessageModel):
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
    cache_items = models.ManyToManyField(CacheItem, editable=False)
    message = models.TextField(default='', editable=False)

    def __unicode__(self):
        return self.fname

    def save(self, *args, **kwargs):
        super(Request, self).save(*args, **kwargs)
        if self.sstate == glob_consts.ST_INITIALIZING:
            self.fname = self.fpath.name.split('/')[-1]
            self.provider = self.fname[:3]
            self.collection = self.fname[:5]
            self.sstate = glob_consts.ST_PENDING
            super(Request, self).save(*args, **kwargs)
        return

admin.site.register(Request)
databrowse.site.register(Request)





class ProcessMonitoring(models.Model):
    """
    Keeping track of various external processes working towards the database
    """
    role = models.IntegerField(choices=dict_2_django_choice(glob_consts.PROC_ROLES))
    pid = models.IntegerField(default=0)
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




