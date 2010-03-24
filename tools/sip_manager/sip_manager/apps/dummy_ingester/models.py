from django.db import models
from django.contrib import admin

from utils.gen_utils import dict_2_django_choice



class Aggregator(models.Model):
    Name = models.CharField(max_length=200)
    Home_page = models.CharField(max_length=200)

    def __unicode__(self):
        return self.Name

admin.site.register(Aggregator)




# PROVT_ = Provider types
PROVT_MUSEUM = 1
PROVT_ARCHIVE = 2
PROVT_LIBRARY = 3
PROVT_AUDIO_VIS_ARCH = 4
PROVT_AGGREGATOR = 5


PROV_TYPES = {
    PROVT_MUSEUM : 'Museum',
    PROVT_ARCHIVE : 'Archive',
    PROVT_LIBRARY : 'Library',
    PROVT_AUDIO_VIS_ARCH : 'Audio Visual Archive',
    PROVT_AGGREGATOR : 'Aggregator',
    }


class Provider(models.Model):
    Name = models.CharField(max_length=200)
    Aggregator_id = models.ForeignKey(Aggregator)
    Country = models.CharField(max_length=5)
    Type = models.IntegerField(choices=dict_2_django_choice(PROV_TYPES),
                               default = PROVT_MUSEUM)
    Home_page = models.CharField(max_length=200)

    def __unicode__(self):
        return self.Name


admin.site.register(Provider)




# DAST_ = DataSet types
DAST_ESE = 1
DAST_TYPES = {
    DAST_ESE: 'ESE',
    }

class DataSet(models.Model):
    Provider_id = models.ForeignKey(Provider)
    Name = models.CharField(max_length=200)
    Language = models.CharField(max_length=4)
    QName = models.CharField(max_length=200)
    Collection_name = models.CharField(max_length=200)
    Type = models.IntegerField(choices=dict_2_django_choice(DAST_TYPES),
                               default = DAST_ESE)

    def __unicode__(self):
        return self.Name

admin.site.register(DataSet)




# REQS_ = Request status
REQS_INIT = 1
REQS_IMPORTED = 2
REQS_ABORTED = 3
REQS_SIP_PROCESSING = 4
REQS_PENDING_VALIDATION_SIGNOFF = 5
REQS_PENDING_AIP_SIGNOFF = 6
REQS_CREATING_AIP = 7
REQS_AIP_COMPLETED = 8

REQS_STATES = {
    REQS_INIT: 'under construction',
    REQS_IMPORTED: 'import completed',
    REQS_ABORTED: 'aborted',
    REQS_SIP_PROCESSING: 'sip processing',
    REQS_PENDING_VALIDATION_SIGNOFF: 'pending validation sign off',
    REQS_PENDING_AIP_SIGNOFF: 'pending AIP sign off',
    REQS_CREATING_AIP: 'creating AIP',
    REQS_AIP_COMPLETED: 'AIP completed',

    }

class Request(models.Model):
    Data_set_id = models.ForeignKey(DataSet)
    Status = models.IntegerField(choices=dict_2_django_choice(REQS_STATES),
                                 default = REQS_INIT)
    File_name = models.CharField(max_length=200)
    Date = models.TimeField()
    # Data_format = models.IntegerField() - what was this??

admin.site.register(Request)
