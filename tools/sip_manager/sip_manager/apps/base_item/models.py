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

from django.db import models
from django.contrib import admin

from utils.gen_utils import dict_2_django_choice

from apps.dummy_ingester.models import Request





# MDRS_ = MdRecord state
MDRS_CREATED = 1
MDRS_IDLE = 2
MDRS_PROCESSING = 3
MDRS_PROBLEMATIC = 4
MDRS_BROKEN = 5
MDRS_VERIFIED = 6

MDRS_STATES = {
    MDRS_CREATED: 'created',
    MDRS_IDLE: 'idle',
    MDRS_PROCESSING: 'processing',
    MDRS_PROBLEMATIC: 'problematic',
    MDRS_BROKEN: 'broken',
    MDRS_VERIFIED: 'verified',
    }

class MdRecord(models.Model):
    Status_Md_record = models.IntegerField(choices=dict_2_django_choice(MDRS_STATES),
                                 default = MDRS_CREATED)
    Content_hash = models.CharField(max_length=100)
    Source_data = models.XMLField()

    Enrichment_done = models.BooleanField(default=False)
    pid = models.IntegerField(default=0) # what process 'owns' this item
    Uniqueness_hash = models.CharField(max_length=100)
    Date_Created = models.TimeField()
    Date_last_modified = models.TimeField()

admin.site.register(MdRecord)



class Request_MdRecord(models.Model):
    Request_id = models.ManyToManyRel(Request)
    md_rec = models.ManyToManyRel(MdRecord)

admin.site.register(Request_MdRecord)
