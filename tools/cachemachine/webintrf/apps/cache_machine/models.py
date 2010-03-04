"""

* Traverse by file to check for match in db - finding orphan files
* Traverse by CacheItem to check filesystem - missing files
* Traverse by CacheItem to verify existance of item
"""
from django.db import models

from django.contrib import admin


REQUEST_UPLOAD_PATH = 'requests_new'


class Request(models.Model):
    """
    A Request is typically a ingestion file
    """
    REQUEST_STATES = (
        (0, 'pending'),
        (10, 'parsing'),
        (90, 'completed'),
        (92, 'aborted'),
        (95, 'invalid input file'),
        (97, 'failed'), # failure in identification of item
        )

    fname = models.FileField(upload_to=REQUEST_UPLOAD_PATH, help_text='')
    req_time = models.DateTimeField(auto_now_add=True, editable=False)
    state = models.IntegerField(choices=REQUEST_STATES, default = REQUEST_STATES[0][0],
                                editable=False)


class CacheSource(models.Model):
    """
    Identifies one server providing thumbnail resources to Europeana, to avoid
    the risk that we hammer the same server with multiple requests
    """
    name = models.CharField(max_length=200)
    #
    # a complete walkthrough of this source will be evenly spread over this
    # amaount of days
    #
    traversaltime = models.IntegerField('How often in days can we rechek things from this server.')

class CacheItem(models.Model):
    """
    A CacheItem is basically a thumbnail that one or more sources have requested
    """
    ITEM_STATES = (
        (0, 'pending'),
        (10, 'retrieval'),
        (11, 'no response'),
        (15, 'identification'),
        (90, 'completed'),
        (91, 'aborted'),
        (92, 'failed'), # failure in identification of item
        #(5, 'full-doc'),
        #(6, 'brief-doc'),
        )

    ITEM_TYPES = (
        (0, 'Unknown'),
        (1, 'image'),
        (2, 'PDF'),
        (3, 'movie'),
        (4, 'audio'),
        )
    source = models.ForeignKey(CacheSource)
    request = models.ManyToManyField(Request)

    uri_id = models.URLField('Europeana uri')
    uri_obj = models.URLField('obj uri')  #help_text='This is the')
    fname = models.TextField('url hash')
    cont_hash = models.TextField('content hash')
    state = models.IntegerField(choices=ITEM_STATES)
    i_type = models.IntegerField(choices=ITEM_TYPES) # kind of item
    checked_date = models.DateTimeField() # last time item was verified





admin.site.register(Request)
admin.site.register(CacheSource)
admin.site.register(CacheItem)
