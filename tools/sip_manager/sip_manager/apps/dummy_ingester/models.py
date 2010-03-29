import os
import datetime

from django.db import models
from django import forms
from django.conf import settings
from django.contrib import admin

from utils.gen_utils import dict_2_django_choice



class Aggregator(models.Model):
    name_code = models.CharField(max_length=10)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200, blank=True)

    def __unicode__(self):
        return '[%s] %s' % (self.name_code, self.name)

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
    aggregator = models.ForeignKey(Aggregator)
    name_code = models.CharField(max_length=10)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200,blank=True)
    country = models.CharField(max_length=5)
    item_type = models.IntegerField(choices=dict_2_django_choice(PROV_TYPES),
                                    default = PROVT_MUSEUM)

    def __unicode__(self):
        return '%s - [%s] %s' % (self.aggregator, self.name_code, self.name)


admin.site.register(Provider)




# DAST_ = DataSet types
DAST_ESE = 1
DAST_TYPES = {
    DAST_ESE: 'ESE',
    }

class DataSet(models.Model):
    provider = models.ForeignKey(Provider)
    name_code = models.CharField(max_length=200)
    name = models.CharField(max_length=200)
    home_page = models.CharField(max_length=200, blank=True)
    language = models.CharField(max_length=4)
    item_type = models.IntegerField(choices=dict_2_django_choice(DAST_TYPES),
                                    default = DAST_ESE)

    def __unicode__(self):
        return '[%s] %s' % (self.name_code, self.name)

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
    data_set = models.ForeignKey(DataSet)
    status = models.IntegerField(choices=dict_2_django_choice(REQS_STATES),
                                 default = REQS_INIT, editable=False)

    # we dont store path, find it by os.walk and check time_stamp
    file_name = models.CharField(max_length=200,
                                 help_text='relative filename, dont store path, system will find it with os.walk() and timestamp...')
    time_created = models.DateTimeField(editable=False)
    pid = models.IntegerField(default=0) # what process 'owns' this item

    def __unicode__(self):
        return '%s - %s' % (self.data_set, self.time_created)

    def add_from_file(self, data_set, full_path):
        file_name = os.path.split(full_path)[1]
        mtime = os.path.getmtime(full_path)

        #slightly dangerous, creating an instance of own class - watch out!
        request = Request(data_set=data_set,
                          file_name=file_name,
                          time_created=datetime.datetime.fromtimestamp(mtime))
        request.save()
        return request

class MyRequestAdminForm(forms.ModelForm):
    class Meta:
        model = Request

    def clean_file_name(self):
        fname = self.find_ingest_filename(self.cleaned_data["file_name"])
        if not fname:
            raise forms.util.ValidationError('Did not match any existing file')
        return fname # self.cleaned_data["file_name"]

    def find_ingest_filename(self, s_in):
        s = s_in.lower()
        fname = ''
        for dir_entry in os.listdir(settings.DUMMY_INGEST_DIR):
            if dir_entry.lower().find(s) == 0:
                fname = dir_entry
                break
        return fname



class RequestAdmin(admin.ModelAdmin):
    form = MyRequestAdminForm



admin.site.register(Request, RequestAdmin)
