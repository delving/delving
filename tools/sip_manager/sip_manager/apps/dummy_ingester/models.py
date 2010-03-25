import os

from django.core import exceptions
from django.db import models
from django import forms
from django.conf import settings
from django.contrib import admin

from utils.gen_utils import dict_2_django_choice



class Aggregator(models.Model):
    name_code = models.CharField(max_length=10)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200)

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
    aggregator_id = models.ForeignKey(Aggregator)
    name_code = models.CharField(max_length=10)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200)
    country = models.CharField(max_length=5)
    ttype = models.IntegerField(choices=dict_2_django_choice(PROV_TYPES),
                                default = PROVT_MUSEUM)

    def __unicode__(self):
        return self.Name


admin.site.register(Provider)




# DAST_ = DataSet types
DAST_ESE = 1
DAST_TYPES = {
    DAST_ESE: 'ESE',
    }

class DataSet(models.Model):
    provider_id = models.ForeignKey(Provider)
    name_code = models.CharField(max_length=200)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200)
    language = models.CharField(max_length=4)
    #collection_name = models.CharField(max_length=200)
    #q_name = models.CharField(max_length=200)
    ttype = models.IntegerField(choices=dict_2_django_choice(DAST_TYPES),
                               default = DAST_ESE)

    def __unicode__(self):
        return self.Collection_name

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
    data_set_id = models.ForeignKey(DataSet)
    status = models.IntegerField(choices=dict_2_django_choice(REQS_STATES),
                                 default = REQS_INIT)
    file_name = models.CharField(max_length=200)
    time_created = models.DateTimeField(auto_now_add=True, editable=False)
    #data_format = models.IntegerField() - what was this??

    def save(self, *args, **kwargs):
        fname = self.find_ingest_filename()
        if not fname:
            raise exceptions.ValidationError('Did not match any existing file')
        super(Request, self).save(*args, **kwargs)

    def find_ingest_filename(self):
        s = self.File_name.lower()
        fname = ''
        for dir_entry in os.listdir(settings.DUMMY_INGEST_DIR):
            if dir_entry.lower().find(s) == 0:
                fname = dir_entry
                break
        return fname

class MyRequestAdminForm(forms.ModelForm):
    class Meta:
        model = Request

    def clean_file_name(self):
        return self.cleaned_data["file_name"]

class RequestAdmin(admin.ModelAdmin):
    form = MyRequestAdminForm



admin.site.register(Request, RequestAdmin)
