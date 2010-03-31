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

from django.core import exceptions
from django.db import models

#from django.contrib import admin
#from django.contrib import databrowse

#from utils import glob_consts

from apps.base_item.models import MdRecord

from utils.gen_utils import dict_2_django_choice



# We want to group all cacheable images by server...
class UriSource(models.Model):
    pid = models.IntegerField(default=0) # what process 'owns' this item
    name_or_ip = models.CharField(max_length=100)




# URIS_ = Uri State
URIS_CREATED = 1
URIS_VERIFIED = 2 #  the uri responds and returns an OK
URIS_DOWNLOADED = 3
URIS_FULL_GENERATED = 4
URIS_BRIEF_GENERATED = 5
URIS_COMPLETED = 6
URIS_FAILED = 7

URI_STATES = {
    URIS_CREATED : 'created',
    URIS_VERIFIED : 'uri verified',
    URIS_DOWNLOADED : 'object downloaded',
    URIS_FULL_GENERATED : 'full_doc generated',
    URIS_BRIEF_GENERATED : 'brief_doc generated',
    URIS_COMPLETED : 'completed',
    URIS_FAILED : 'failed',
    }

# URIT_ = Uri Type
URIT_OBJECT = 1
URIT_SHOWNBY = 2
URIT_SHOWNAT = 3

URI_TYPES = {
    URIT_OBJECT : 'object',
    URIT_SHOWNBY : 'isShownBy',
    URIT_SHOWNAT : 'isShownAt',
    }

class Uri(models.Model):
    """
    Identifies one server providing thumbnail resources to Europeana, to avoid
    the risk that we hammer the same server with multiple requests
    """
    mdr = models.ForeignKey(MdRecord)
    sstate = models.IntegerField(choices=dict_2_django_choice(URI_STATES),
                                 default = URIS_CREATED)
    item_type = models.IntegerField(choices=dict_2_django_choice(URI_TYPES),
                                 default = URIT_OBJECT)
    uri_source = models.ForeignKey(UriSource)
    pid = models.IntegerField(default=0) # what process 'owns' this item
    #element
    url = models.URLField(verify_exists=False)
    err_msg = models.TextField()
    time_created = models.DateTimeField(auto_now_add=True,editable=False)
    time_lastcheck = models.DateTimeField(auto_now_add=True)





