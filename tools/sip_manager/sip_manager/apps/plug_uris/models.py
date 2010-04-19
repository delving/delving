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
from django.db import models, connection



from apps.base_item.models import MdRecord
from apps.dummy_ingester.models import Request

from utils.gen_utils import dict_2_django_choice, db_is_mysql



# We want to group all cacheable images by server...
class UriSource(models.Model):
    """
    Identifies one server providing thumbnail resources to Europeana, to avoid
    the risk that we hammer the same server with multiple requests
    """
    pid = models.FloatField(default=0) # what process 'owns' this item
    name_or_ip = models.CharField(max_length=100)

    def __unicode__(self):
        return self.name_or_ip





# URIS_ = Uri State
URIS_CREATED = 1
URIS_VERIFIED = 2 #  the uri responds and returns an OK
URIS_ORG_SAVED = 3
URIS_FULL_GENERATED = 4
URIS_BRIEF_GENERATED = 5
URIS_COMPLETED = 6
URIS_FAILED = 7

URI_STATES = {
    URIS_CREATED : 'created',
    URIS_VERIFIED : 'uri verified',
    URIS_ORG_SAVED : 'original saved',
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


# URIE_ Uri Error
URIE_NO_ERROR            = 0
URIE_OTHER_ERROR         = 1 # used
URIE_TIMEOUT             = 2 # used
URIE_HTTP_ERROR          = 3 # used
URIE_HTML_ERROR          = 4 # used
URIE_URL_ERROR           = 5 # used
URIE_MIMETYPE_ERROR      = 6 # used
URIE_WRONG_FILESIZE      = 7 # used
URIE_DOWNLOAD_FAILED     = 8 # used
URIE_WAS_HTML_PAGE_ERROR = 9
URIE_FILE_STORAGE_FAILED = 10
URIE_OBJ_CONVERTION_ERROR = 11 # used
URIE_UNRECOGNIZED_FORMAT = 12
URIE_UNSUPORTED_MIMETYPE_ERROR = 13
#URIE_NO_RESPONSE        = 13

URI_ERR_CODES = {
    URIE_NO_ERROR            : '',
    URIE_OTHER_ERROR         : 'other error',
    URIE_TIMEOUT             : 'timeout',
    URIE_HTTP_ERROR          : 'http error',
    URIE_HTML_ERROR          : 'html error',
    URIE_URL_ERROR           : 'url error',
    URIE_MIMETYPE_ERROR      : 'mime type error',
    URIE_WRONG_FILESIZE      : 'wrong filesize',
    URIE_WAS_HTML_PAGE_ERROR : 'Object was a html page',
    URIE_FILE_STORAGE_FAILED : 'file storage failed',
    URIE_OBJ_CONVERTION_ERROR: 'object convertion failed',
    URIE_DOWNLOAD_FAILED     : 'download failed',
    URIE_UNRECOGNIZED_FORMAT : 'unrecognized format',
    URIE_UNSUPORTED_MIMETYPE_ERROR : 'unsuported mime-type',

    #URIE_NO_RESPONSE     : 'no response',
    }

"""
URIS_FAILED is only used for errors before URIS_VERIFIED has been set.

This is to make sure that if we did _any_ progress on the uri we keep the state
so the check could continue later after manual intervention.

Whenever there is an error err_code is set, so in order to find problematic
records, the primary check should be on the err_code field, status indicates
if the uri made any progress before the failure.
"""



LIMIT_COUNT = 200




class UriManager(models.Manager):

    def __init__(self, *args, **kwargs):
        super(UriManager, self).__init__(*args, **kwargs)

    def base_sql(self, source_id, count=False):
        if count:
            s = 'COUNT(u.id)'
        else:
            s = 'u.id'
        lst = ["SELECT %s FROM %s_uri u" % (s, __name__.split('.')[-2])]
        lst.append("WHERE u.status=%i" % URIS_CREATED)
        lst.append("AND uri_source_id=%i" % source_id)
        lst.append("AND err_code=%i" % URIE_NO_ERROR)
        lst.append("AND item_type=%i" % URIT_OBJECT) # only for initial cachegeneration!
        lst.append("AND pid=0")
        sql = ' '.join(lst)
        return sql

    def new_by_source_count(self, source_id):
        sql = self.base_sql(source_id, count=True)
        cursor = connection.cursor()
        cursor.execute(sql)
        i = cursor.fetchone()[0]
        return i

    def new_by_source_generator(self, source_id):
        "All new uris that should be checked for one UriSource."
        sql = self.base_sql(source_id)
        cursor = connection.cursor()

        if db_is_mysql:
            limit_syntax = "LIMIT %i,%i"
        else:
            limit_syntax = "OFFSET %i LIMIT %i"

        offset = 0
        while True:
            limiter = limit_syntax % (offset, LIMIT_COUNT)
            cursor.execute('%s %s' % (sql, limiter))
            if not cursor.rowcount:
                break
            for row in cursor.fetchall():
                yield row[0]
            offset += LIMIT_COUNT


class Uri(models.Model):
    """
    Please note that Uri.status does not have an broken/aborted state
    since the image generation steps are timeconsuming and "expensive"
    we want to avoid redoing them if at all possible

    Bad items are indicated by err_code != URIE_NO_ERROR

    With this hopefully the system can continue with the problematic items
    later if the issue was solved.

    Drawback it requires you to remember to check the err_code when selecting items,
    cant have it both ways...
    """
    mdr = models.ForeignKey(MdRecord)
    status = models.IntegerField(choices=dict_2_django_choice(URI_STATES),
                                 default = URIS_CREATED,db_index=True)
    item_type = models.IntegerField(choices=dict_2_django_choice(URI_TYPES),
                                 default = URIT_OBJECT, db_index=True)
    mime_type = models.CharField(max_length=50, blank=True) # mostly relevant for objects...
    uri_source = models.ForeignKey(UriSource)
    pid = models.FloatField(default=0, db_index=True) # what process 'owns' this item
    url = models.CharField(max_length=450)
    url_hash = models.CharField(max_length=64,default='')
    content_hash = models.CharField(max_length=64,default='')
    err_code = models.IntegerField(choices=dict_2_django_choice(URI_ERR_CODES),
                                   default = URIE_NO_ERROR, db_index=True)
    err_msg = models.TextField()
    time_created = models.DateTimeField(auto_now_add=True,editable=False)
    time_lastcheck = models.DateTimeField(auto_now_add=True)

    objects = UriManager()

    def __unicode__(self):
        return self.url


class ReqUri(models.Model):
    """
    Lists all uris by request
    """
    req = models.ForeignKey(Request)
    uri = models.ForeignKey(Uri)
    # Dummy field if nothing here table isnt created
    #i = models.IntegerField(default=0, blank=True)

    def __unicode__(self):
        return self.req.__unicode__()
