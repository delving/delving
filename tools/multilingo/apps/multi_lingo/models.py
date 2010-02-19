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
import shutil
import time
import codecs
from subprocess import Popen

from django.db import models
from django.contrib import admin

from views import PROP_TEMPLATE


PROP_HEADER="""#
# Copyright 2010 EDL FOUNDATION
#
# Licensed under the EUPL, Version 1.1 or as soon they
# will be approved by the European Commission - subsequent
# versions of the EUPL (the "Licence");
# you may not use this work except in compliance with the
# Licence.
# You may obtain a copy of the Licence at:
#
# http://ec.europa.eu/idabc/eupl
#
# Unless required by applicable law or agreed to in
# writing, software distributed under the Licence is
# distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied.
# See the Licence for the specific language governing
# permissions and limitations under the Licence.
#"""

PROP_FNAME = 'templates/%s' % PROP_TEMPLATE
TMP_EXT = '.tmp'

"""
class TagField(models.CharField):
    def get_prep_value(self, value):
        if value[-2:] != '_t':
            value += u'_t'
        return value

class PortalProperties(models.Model):
    tag = TagField(max_length=150, primary_key=True,
                       help_text='This identifier is used in the portal to lookup the various translations of the "Display text" below')
    txt = models.TextField('display text',
                           help_text='This is the text that the translators will be expected to translate. '
                           'If this is a translation hint instead of the intended english language version of the message, '
                           'then please make sure you also add an english translation')
    description = models.TextField('description',
                                   blank=True,
                                   help_text='General description of the usage of this tag. '
                                   "Internal for admins, basically the MarComm team, not sent to translators")
    last_changed = models.DateTimeField('last changed',
                                        auto_now=True, auto_now_add=True)
    created = models.DateTimeField('creation time', auto_now_add=True)

    def __unicode__(self):
        return self.tag

    def delete(self):
        super(PortalProperties, self).delete()
        self.update_prop_file()

    def save(self):
        super(PortalProperties, self).save()
        self.update_prop_file()

    def update_prop_file(self):
        # This would fail if two people would save changes to properties
        # at exactly the same time - but come on, basically its one or two
        # people allowed to run this in our case, so lets not waste time avoiding that
        # for the moment. Feel free to implement thread safety if you need it...
        was_ok = False
        fname = os.path.join(os.path.dirname(__file__), PROP_FNAME)
        try:
            f = codecs.open(fname + TMP_EXT, "w", "utf-8")
            f.write('{% load i18n %}\n')
            f.write('%s\n' % PROP_HEADER)
            f.write('# Last changed: %s\n#\n' % time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()))
            db = PortalProperties.objects.all()
            for p in db:
                f.write(u'%s={%% blocktrans %%}%s{%% endblocktrans %%}\n' % (p.tag, p.txt))
            was_ok = True
        except:
            pass
        finally:
            try:
                f.close()
            except:
                was_ok = False
        if was_ok:
            # only replace file if a new was created
            shutil.move(fname + TMP_EXT, fname)
            # update the translation files
            p = Popen('cd %s; django-admin.py makemessages -a' % os.path.dirname(__file__),
                      shell=True)
            sts = os.waitpid(p.pid, 0)[1]
        return



class PortalPropertiesAdmin(admin.ModelAdmin):
    list_display = ('tag', 'txt','description', 'last_changed', 'created')
    list_filter = ['last_changed', 'created']
    search_fields = ['tag', 'txt', 'description']
    date_hierarchy = 'last_changed'

admin.site.register(PortalProperties, PortalPropertiesAdmin)
"""
