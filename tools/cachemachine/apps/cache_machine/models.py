"""

* Traverse by file to check for match in db - finding orphan files
* Traverse by CacheItem to check filesystem - missing files
* Traverse by CacheItem to verify existance of item
"""
from django.db import models

# Create your models here.

ITEM_STATES = (
    (0, 'retrieval'),
    (1, 'identification'),
    (2, 'full-doc'),
    (3, 'brief-doc'),
    (4, 'completed'),
    (5, 'aborted'),
    )

ITEM_STATUS = (
    (0, 'pending'),
    (1, 'completed'),
    (2, 'aborted'),
    )

ITEM_TYPES = (
    (0, 'Unknown'),
    (1, 'image'),
    (2, 'PDF'),
    )


class CacheItem(models.Model):
    request = models.ForeignKey(Request)
    source = modles.ForeignKey(CacheSource)
    url = models.URLField('source url',  #help_text='This is the'
                           )
    fname = models.TextField('hashed url')
    state = models.IntegerField(choices=ITEM_STATES)
    status =models.IntegerField(choices=ITEM_STATES)
    i_type = models.IntegerField(choices=ITEM_TYPES)
    checked_date = models.DateTimeField() # last time item was verified

class Request(models.Model):
    name = models.CharField()
    req_time = models.DateTimeField()


class CacheSource(models.Model):
    name = models.CharField()
    #
    # a complete walkthrough of this source will be evenly spread over this
    # amaount of days
    #
    traversaltime = models.IntegerField()
