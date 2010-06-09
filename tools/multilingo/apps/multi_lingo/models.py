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

 Initial release: 2010-02-05
"""

import os
import threading

from django.conf import settings
from django.core import exceptions, urlresolvers
from django.db import models
from django.db.models.signals import post_delete
from django.core.files.storage import FileSystemStorage


from gen_utils.shell_cmd import cmd_execute

import views


THIS_DIR = os.path.split(__file__)[0]
TEMPLATES_DIR = os.path.join(THIS_DIR, 'templates')
STATIC_PAGES = 'static_pages'
STATIC_PAGES_FULLP = os.path.join(TEMPLATES_DIR, STATIC_PAGES)

MEDIA_FILE_PATH = 'sp'

T_DELETE = None # timer object for delayed translate updates on multiple deletes


class TranslatePage(models.Model):
    """
    A page that should be translated
    """
    file_name = models.FileField(upload_to=STATIC_PAGES, storage=FileSystemStorage(location=TEMPLATES_DIR))
    active = models.BooleanField('If checked this page will be included in submits to production',
                                 default=False)
    time_created = models.DateTimeField(auto_now_add=True,editable=False)


    def __unicode__(self):
        return self.file_name.name

    def save(self, *args, **kwargs):
        super(TranslatePage, self).save(*args, **kwargs)
        update_translations()


def update_translations():
    #print 'running update_translations...',
    views.update_template_list()
    output = cmd_execute('python ../../manage.py makemessages -a',
                          cwd=THIS_DIR)
    output = cmd_execute('python ../../manage.py compilemessages',
                          cwd=THIS_DIR)
    a = urlresolvers.resolve('/')
    #print 'Done!'


def translate_page_post_delete_cb(sender, instance, **kwargs):
    """We use a timer to make sure we only run update_translations() once on
    multiple deletes"""
    global T_DELETE
    if T_DELETE and T_DELETE.isAlive():
        T_DELETE.cancel()
    T_DELETE = threading.Timer(2.0, update_translations)
    T_DELETE.start()

post_delete.connect(translate_page_post_delete_cb, sender=TranslatePage)




class MediaFile(models.Model):
    file_name = models.FileField(upload_to=MEDIA_FILE_PATH)

    def __unicode__(self):
        return self.file_name.name

#
#  Startup checks
#

def check_template_link_exists():
    "To be able to download translation pages a link must exist, create if not found."
    dir_src = os.path.join(settings.MEDIA_ROOT, STATIC_PAGES)
    if os.path.exists(dir_src):
        return
    cmd = 'ln -s %s .' % STATIC_PAGES_FULLP
    output = cmd_execute(cmd, cwd=settings.MEDIA_ROOT)
    if output:
        print '***'
        msg = 'Failed to link template_dir: %s' % output
        print msg
        raise exceptions.ImproperlyConfigured(msg)

check_template_link_exists()

def checkMedia_link_exists():
    dir_support_media = os.path.join(settings.MEDIA_ROOT, MEDIA_FILE_PATH)
    if os.path.exists(dir_support_media):
        return
    print '***'
    msg = 'support media dir is missing: %s' % dir_support_media
    print msg
    raise exceptions.ImproperlyConfigured(msg)

checkMedia_link_exists()


class Language(models.Model):
    short_name = models.CharField(max_length=10)
    visible_name = models.CharField(max_length=100)

    def __unicode__(self):
        return self.short_name
#ALTER TABLE `multi_lingo_language` CHANGE `visible_name` `visible_name` VARCHAR( 100 ) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL