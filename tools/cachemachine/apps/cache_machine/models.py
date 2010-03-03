"""

* Traverse by file to check for match in db - finding orphan files
* Traverse by CacheItem to check filesystem - missing files
* Traverse by CacheItem to verify existance of item
"""
from django.db import models

# Create your models here.

ITEM_STATES = (
    (0, 'pending'),
    (1, 'aborted'),
    (2, 'retrieval'),
    (3, 'no response'),
    (4, 'identification'),
    (5, 'completed'),
    (6, 'failed'), # failure in identification of item
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


class CacheItem(models.Model):
    request = models.ForeignKey(Request)
    source = modles.ForeignKey(CacheSource)
    uri_id = models.URLField('Europeana uri')
    uri_obj = models.URLField('obj uri')  #help_text='This is the')
    fname = models.TextField('url hash')
    cont_hash = models.TextField('content hash')
    state = models.IntegerField(choices=ITEM_STATES)
    i_type = models.IntegerField(choices=ITEM_TYPES) # kind of item
    checked_date = models.DateTimeField() # last time item was verified

class Request(models.Model):
    """
    A Request is typically a ingestion file
    """
    name = models.CharField()
    req_time = models.DateTimeField()


class CacheSource(models.Model):
    """
    Identifies one server providing thumnail resources to Europeana, to avoid
    the risk that we hammer the same server with multiple requests
    """
    name = models.CharField()
    #
    # a complete walkthrough of this source will be evenly spread over this
    # amaount of days
    #
    traversaltime = models.IntegerField('How often in days can we rechek things from this server.')
