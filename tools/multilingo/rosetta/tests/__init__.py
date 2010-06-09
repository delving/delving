# -*- coding: utf-8 -*-
from django.core.urlresolvers import reverse
from django.test import TestCase
from django.utils.translation import ugettext_lazy as _
from django.contrib.auth.models import User
import datetime, os, shutil
from django.conf import settings
from rosetta.conf import settings as rosetta_settings


class RosettaTestCase(TestCase):
    urls = 'rosetta.tests.urls'
    
    
    def __init__(self, *args,**kwargs):
        super(RosettaTestCase,self).__init__(*args,**kwargs)
        self.curdir = os.path.dirname(__file__)
        self.dest_file = os.path.normpath(os.path.join(self.curdir, '../locale/xx/LC_MESSAGES/django.po'))


    def setUp(self):
        user = User.objects.create_user('test_admin', 'test@test.com', 'test_password')
        user.is_superuser = True
        user.save()
        
        self.client.login(username='test_admin',password='test_password')
        settings.LANGUAGES = (('xx','dummy language'),)
        

    def test_1_ListLoading(self):
        r = self.client.get(reverse('rosetta-pick-file') +'?rosetta')
        self.assertTrue(os.path.normpath('rosetta/locale/xx/LC_MESSAGES/django.po') in r.content)
        
        
    def test_2_PickFile(self):
        r = self.client.get(reverse('rosetta-language-selection', args=('xx',0,), kwargs=dict() ) +'?rosetta')
        r = self.client.get(reverse('rosetta-home'))
        
        self.assertTrue('dummy language' in r.content)
        
    def test_3_DownloadZIP(self):
        r = self.client.get(reverse('rosetta-language-selection', args=('xx',0,), kwargs=dict() ) +'?rosetta')
        r = self.client.get(reverse('rosetta-home'))
        r = self.client.get(reverse('rosetta-download-file' ) +'?rosetta')
        self.assertTrue ('content-type' in r._headers.keys() )
        self.assertTrue ('application/x-zip' in r._headers.get('content-type'))
    
    def test_4_DoChanges(self):
        
        # copy the template file
        shutil.copy(self.dest_file, self.dest_file + '.orig')
        shutil.copy(os.path.normpath(os.path.join(self.curdir,'./django.po.template')), self.dest_file)

        # Load the template file
        r = self.client.get(reverse('rosetta-language-selection', args=('xx',0,), kwargs=dict() ) +'?rosetta')
        r = self.client.get(reverse('rosetta-home') + '?filter=untranslated')
        r = self.client.get(reverse('rosetta-home'))
        
        # make sure both strings are untranslated
        self.assertTrue('dummy language' in r.content)
        self.assertTrue('String 1' in r.content)
        self.assertTrue('String 2' in r.content)
        
        # post a translation
        r = self.client.post(reverse('rosetta-home'), dict(m_1='Hello, world', _next='_next'))
        
        # reload all untranslated strings
        r = self.client.get(reverse('rosetta-language-selection', args=('xx',0,), kwargs=dict() ) +'?rosetta')
        r = self.client.get(reverse('rosetta-home') + '?filter=untranslated')
        r = self.client.get(reverse('rosetta-home'))
        
        # the translated string no longer is up for translation
        self.assertTrue('String 1'  in r.content)
        self.assertTrue('String 2' not in r.content)
        
        # display only translated strings
        r = self.client.get(reverse('rosetta-home') + '?filter=translated')
        r = self.client.get(reverse('rosetta-home'))
        
        # The tranlsation was persisted
        self.assertTrue('String 1' not  in r.content)
        self.assertTrue('String 2' in r.content)
        self.assertTrue('Hello, world' in r.content)
        
        # reset the original file
        shutil.move(self.dest_file+'.orig', self.dest_file)
        

    def test_5_TestIssue67(self):
        # testcase for issue 67: http://code.google.com/p/django-rosetta/issues/detail?id=67
        # copy the template file
        shutil.copy(self.dest_file, self.dest_file + '.orig')
        shutil.copy(os.path.normpath(os.path.join(self.curdir,'./django.po.issue67.template')), self.dest_file)
        
        # Make sure the plurals string is valid
        f_ = open(self.dest_file,'rb')
        content = f_.read()
        f_.close()
        self.assertTrue(u'Hello, world' not in content)
        self.assertTrue(u'|| n%100>=20) ? 1 : 2)' in content)
        del(content)
        
        # Load the template file
        r = self.client.get(reverse('rosetta-language-selection', args=('xx',0,), kwargs=dict() ) +'?rosetta')
        r = self.client.get(reverse('rosetta-home') + '?filter=untranslated')
        r = self.client.get(reverse('rosetta-home'))
        
        # make sure all strings are untranslated
        self.assertTrue('dummy language' in r.content)
        self.assertTrue('String 1' in r.content)
        self.assertTrue('String 2' in r.content)
        
        # post a translation
        r = self.client.post(reverse('rosetta-home'), dict(m_1='Hello, world', _next='_next'))
        
        # Make sure the plurals string is still valid
        f_ = open(self.dest_file,'rb')
        content = f_.read()
        f_.close()
        self.assertTrue(u'Hello, world' in content)
        self.assertTrue(u'|| n%100>=20) ? 1 : 2)' in content)
        self.assertTrue(u'or n%100>=20) ? 1 : 2)' not in content)
        del(content)

        shutil.move(self.dest_file + '.orig', self.dest_file)
        

    def test_6_ExcludedApps(self):
        
        rosetta_settings.EXCLUDED_APPLICATIONS = ('rosetta',)
        
        r = self.client.get(reverse('rosetta-pick-file') +'?rosetta')
        self.assertTrue('rosetta/locale/xx/LC_MESSAGES/django.po' not in r.content)
        
        rosetta_settings.EXCLUDED_APPLICATIONS = ()
        
        r = self.client.get(reverse('rosetta-pick-file') +'?rosetta')
        self.assertTrue('rosetta/locale/xx/LC_MESSAGES/django.po' in r.content)
        
    def test_7_selfInApplist(self):    
        r = self.client.get(reverse('rosetta-pick-file') +'?rosetta')
        self.assertTrue('rosetta/locale/xx/LC_MESSAGES/django.po' in r.content)

        r = self.client.get(reverse('rosetta-pick-file'))
        self.assertTrue('rosetta/locale/xx/LC_MESSAGES/django.po' not in r.content)
