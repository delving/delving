#!/usr/bin/env python

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

 2010-02-10 Initial release


 Tool to import original properties files for all languages into this system
"""


import os
import sys
import shutil
import codecs
import subprocess
import xml.dom.minidom


proj_path = os.path.split(os.path.dirname(os.path.abspath(__file__)))[0]
sys.path.insert(0,proj_path)
#os.chdir(proj_path) # man
import settings

from rosetta import polib


TMP_EXT = '.tmp'

BOTH='both'
START='start'
END='end'




class InitializePropertifiles(object):
    PROPFILE_DELIM = '_t='
    LOCALE_DIR = os.path.join(proj_path, 'apps',  'multi_lingo', 'locale')
    TEMPLATES_BASE = os.path.join(proj_path, 'apps',  'multi_lingo',
                                  'templates', 'pages')
    HTML_PROPS_FNAME = os.path.join(TEMPLATES_BASE, 'prop_file.html')
    ERR_NODE_VALUE = '*** NODEVALUE'
    EMPTY_STR = '** Empty string **' # hopefully will set translation to ' '
                                     # in order to avoid warnings of untranslated strings

    def __init__(self, lang='', debug=1):
        # debug lvls:
        #  1 basic output
        #  5 each key,value is displayed during translation
        self.debug = debug

        if not lang:
            return # used for erasing all files

        self.lang = lang

        if not os.path.exists(self.HTML_PROPS_FNAME):
            self.create_html_prop_file()

        self.read_html_props()
        self.read_msg_props(self.lang)
        self.po_base_name = os.path.join(self.LOCALE_DIR, self.lang,
                                    'LC_MESSAGES', 'django')


    def clear_all_files(self):
        "Removes all translations, dont run on production, you will regret it!"
        if self.LOCALE_DIR and os.path.exists(self.LOCALE_DIR):
            self.shell_cmd('rm -rf %s/*' % self.LOCALE_DIR)
        else:
            os.mkdir(self.LOCALE_DIR)

        if os.path.exists(self.HTML_PROPS_FNAME):
            os.remove(self.HTML_PROPS_FNAME)


    def handle_language(self):
        "Do languaga specific translations."
        self.create_po_file() # Create po file if not there

        self.translate_properties()
        if self.lang == 'en':
            return # dont translate english...
        if self.lang in ('de','es','fi','fr','is','it','no','sv'):
            self.transl_aboutus()
            self.po.save_as_mofile(self.po_base_name + '.mo')
        return


    #
    # Translation of specific template pages
    #
    def transl_aboutus(self):
        if self.debug > 1:
            print 'Translating about_us'


        self.po = polib.pofile(self.po_base_name + '.po')

        fname = os.path.join(proj_path,'import','html_files', 'aboutus_%s.html' % self.lang)
        html_s = self.read_html_file(fname)
        x = xml.dom.minidom.parseString('<wrapper1>%s</wrapper1>' % html_s)
        self.doc = x.childNodes[0] # dump wrapper1

        i = 0
        #<h2>
        self.translate('About us', self.nodevalue(i)) ;i+=5
        #<object><a>
        self.translate('Storyline and Credits', self.nodevalue(i)) ;i+=2
        #<h2>
        self.translate('Europeana: think culture', self.nodevalue(i)) ;i+=2

        # insert dynamic number of objs
        s = self.nodevalue(i) ;i+=2

        #<p>
        if '6' in s:
            s = s.replace('6', '%(europeana_item_count_mill)s')
        elif 'milj' in s:
            s = s.replace('milj', '%(europeana_item_count_mill)s milj')
        else:
            print
            print '*** milions of objects not found'
            sys.exit()
        self.translate('Europeana.eu is about ideas and inspiration. It links*', s)

        #<ul><li>
        i2 = 1
        self.translate('Images - paintings, drawings, maps, photos and pictures of museum objects',
                       self.nodevalue(i, i2)) ;i2+=2
        #<li>
        self.translate('Texts - books, newspapers, letters, diaries and archival papers',
                       self.nodevalue(i,i2)) ;i2+=2
        #<li>
        self.translate('Sounds - music and spoken word from cylinders, tapes, discs and radio broadcasts',
                       self.nodevalue(i,i2)) ;i2+=2
        #<li>
        self.translate('Videos - films, newsreels and TV broadcasts',
                       self.nodevalue(i,i2)) ;i2+=2

        #<p>
        i+=2 ; i2=1
        self.translate("Some of these are world famous, others are hidden treasures from Europe's",
                       self.nodevalue(i)) ;i+=2
        i2=1
        self.translate('museums and galleries',
                       self.nodevalue(i,i2)) ;i2+=2
        self.translate('archives',
                       self.nodevalue(i,i2)) ;i2+=2
        self.translate('libraries', self.nodevalue(i,i2)) ;i2+=2
        self.translate('audio-visual collections',
                       self.nodevalue(i,i2)) ;i2+=2

        #<p>
        i+=4 ; i2=1
        self.translate('Here is a', self.nodevalue(i))
        if self.lang in ('it',):
            i+=2
            s = self.nodevalue(i,0)
        else:
            s = self.EMPTY_STR
        self.translate('#[about_us: paragraph Here is a - prefix for partners.html]',s)
        self.translate('list of the organisations',
                       self.nodevalue(i,i2))

        lst = []
        for node in self.doc.childNodes[i].childNodes[2:9]:
            lst.append(node.toxml())
        value = u' '.join(lst)
        self.translate('that our content comes from. They include*', value)

        #<p>
        i+=2 ; i2=1
        if self.lang not in ('fi','es'):
            s = self.nodevalue(i)
        else:
            s = self.EMPTY_STR
        self.translate('You can use', s)
        self.translate('My Europeana', self.nodevalue(i,i2)) ;i2+=1
        self.translate('to save searches or bookmark things. You can highlight stuff and add it to your own folders',
                       self.nodevalue(i,i2)) ;i+=2

        #<p>
        self.translate('This website is a prototype. Europeana Version*',
                       self.nodevalue(i)) ;i+=2

        #<p>
        self.translate('Europeana.eu is funded by the European*',
                       self.nodevalue(i)) ;i+=2

        #<p>
        self.translate('More about', self.nodevalue(i)) ;i+=2


        #<ul><li>
        i2=1
        self.translate('How Europeana came to be developed',
                       self.nodevalue(i,i2))
        if self.lang not in ('no'):
            i3=1
            s1 = self.nodevalue(i,i2,i3) ; i3+=1
            if self.lang not in ('fi',):
                s2 =  self.nodevalue(i,i2,i3)
            else:
                s2 = self.EMPTY_STR
        else:
            s1 = s2 = self.EMPTY_STR
        self.translate('the background', s1)
        self.translate('to the project', s2)
        i2+=2
        #<li>
        i3=1
        self.translate('The deliverables from the project',
                       self.nodevalue(i,i2))
        self.translate('technical plans', self.nodevalue(i,i2,i3)) ;i3+=1
        self.translate('etc', self.nodevalue(i,i2,i3)) ;i2+=2
        #<li>
        self.translate('New projects that will be channelling material into',
                       self.nodevalue(i,i2)) ;i2+=4
        # <li>
        i3=1
        self.translate('How organisations can', self.nodevalue(i,i2))
        self.translate('contribute content', self.nodevalue(i,i2,i3)) ;i3+=1
        if self.lang not in ('nl',):
            s = self.nodevalue(i,i2,i3)
        else:
            s = self.EMPTY_STR
        self.translate('to Europeana', s)
        i2+=2
        #<li>
        if self.lang in ('de','fi','es'):
            v = self.EMPTY_STR
            i3 = 0
        else:
            v = self.nodevalue(i,i2)
            i3 = 1
        self.translate('Getting in', v)

        self.translate('contact', self.nodevalue(i,i2,i3)) ;i3+=1
        self.translate('with the Europeana team', self.nodevalue(i,i2,i3)) ;i2+=2
        #<li>
        i3 = 1
        self.translate('To be added to the', self.nodevalue(i,i2))
        self.translate('press list', self.nodevalue(i,i2,i3)) ;i2+=2
        # <li>
        i3 = 0
        if self.lang in ('de', 'sv'):
            v = self.EMPTY_STR
        else:
            v = self.nodevalue(i,i2,i3)
        self.translate('#The[about_us: paragraph More about last list item - prefix for enews.php]', v)
        self.translate('e-news', self.nodevalue(i,i2,i3)) ;i3+=1 #29,15,0
        self.translate('to keep you in touch with developments',
                       self.nodevalue(i,i2,i3)) ;i+=4

        #<h2>
        self.translate('Background', self.nodevalue(i)) ; i+=2

        # BL/A
        if self.lang not in ('no','fi','it','is'):
            s = self.nodevalue(i)
            i+=2
        else:
            s = self.EMPTY_STR
        #<p>
        self.translate('The Commission has been working for a number of years on projects to boost*',
                       s)
        #<p>
        i2 = 1
        self.translate('The idea for Europeana came from a', self.nodevalue(i)) #BL/B
        self.translate('letter', self.nodevalue(i,i2)) ;i2+=1 #BL/C
        self.translate('to the Presidency of Council and to the Commission on*', #BL/D
                       self.nodevalue(i,i2)) ;i+=2
        #<p>
        i2 = 1
        self.translate('On 30 September 2005 the European Commission published*', #BL/E
                       self.nodevalue(i))
        if self.lang not in ('de','es','fi','fr','nl','no','it'):
            s = self.nodevalue(i,i2)
            i2+=1
        else:
            i+=2 ; i2 = 0
        self.translate('Information Society i2010 Initiative', s) #BL/F

        if self.lang == 'is':
            lst = [self.nodevalue(i,i2)]
            i+=2
            lst.append(self.nodevalue(i))
            s = ' '.join(lst)
        else:
            s = self.nodevalue(i,i2)
        i+=2
        self.translate('which aims to foster growth and jobs in the information society*',s) #BL/G

        #<p>
        if self.lang in ('sv',):
            i+=2 # skip commented out block
        self.translate('The Europeana prototype is the result*', #BL/H
                       self.nodevalue(i)) ;i+=2
        #<p>
        i2=2
        self.translate('Europeana is a Thematic Network funded by  the European Commission under the',
                       self.nodevalue(i)) #BL/I
        #if self.lang == 'fr':
        #    return 0 # havent been able to match up the rest so far...
        if self.lang in ('fr','it'): #BL/J
            s = self.EMPTY_STR
        else:
            s = self.nodevalue(i,i2)
            i2+=1
        self.translate('as part of the',s)

        if self.lang in ('fr','it'): #BL/K
            s = self.EMPTY_STR
        else:
            s = self.nodevalue(i,i2)
            i2+=1
        self.translate('i2010  policy',s)

        if self.lang in ('fr','it'): #BL/L
            s = self.EMPTY_STR
        else:
            s = self.nodevalue(i,i2)
        self.translate('Originally known as the European digital library network*',
                       s) ;i+=2
        #<p>
        self.translate('The project is run by a core team based in  the national library of the Netherlands, the',
                       self.nodevalue(i)) #BL/M

        if self.lang in ('fr','it'): #BL/N
            i2=1
            s = self.EMPTY_STR
        else:
            i2=2
            s = self.nodevalue(i,i2)
            i2+=1
        self.translate('It builds on the project management and technical  expertise developed by', s)

        if self.lang in ('fr','it'): #BL/O
            s = self.EMPTY_STR
        else:
            s = self.nodevalue(i,i2)
            i2+=1
        self.translate('The  European Library', s)

        if self.lang in ('fr','it'): #BL/P
            s = self.EMPTY_STR
        else:
            s = self.nodevalue(i,i2)
            i2+=1
        self.translate('which is a service of the', s)

        if self.lang in ('fr',): #BL/Q
            s = self.EMPTY_STR
        else:
            s = self.nodevalue(i,i2)
            i2+=1
        self.translate('Conference of European National  Librarians',s);i+=2

        #<p>
        i2=2
        self.translate('Overseeing the project is the', self.nodevalue(i)) #BL/R
        self.translate('which includes key European  cultural heritage associations*', #BL/S
                       self.nodevalue(i,i2)) ;i2+=1
        self.translate('statutes', self.nodevalue(i,i2)) ;i2+=1 #BL/T
        self.translate('commit members to', self.nodevalue(i,i2),recursion=2) ;i+=2 #BL/U

        #<ul>
        self.translate('Providing access to Europe&rsquo;s cultural and scientific heritage though a <strong>cross-domain portal</strong>',
                       self.nodevalue(i,1))
        self.translate('Co-operating in the delivery and <strong>sustainability</strong> of the joint portal',
                       self.nodevalue(i,3))
        self.translate('Stimulating initiatives to <strong>bring together existing digital content</strong>',
                       self.nodevalue(i,5))
        self.translate('Supporting  <strong>digitisation</strong> of Europe&rsquo;s cultural and scientific heritage',
                       self.nodevalue(i,7)) ;i+=6

        #<h2>
        self.translate('Technical plans', self.nodevalue(i)) ;i+=2 #tp/a
        #<p>
        self.translate('The development route, site architecture and technical specifications are all published as',
                       self.nodevalue(i)) #tp/b
        if self.lang == 'is': #tp/c
            s = 'deliverable outcomes'
        else:
            s = self.nodevalue(i,1)
        self.translate('deliverable outcomes', s)

        if self.lang == 'is': #tp/d
            s = self.EMPTY_STR
        else:
            s = self.nodevalue(i,2)
        self.translate('of the project. After the launch of the Europeana prototype, the project*',
                       s) ;i+=4

        #<h2>
        self.translate('Contact us', self.nodevalue(i)) ;i+=2
        #<p>
        self.translate('Feedback form', self.nodevalue(i,1))
        if self.lang == 'fr':
            i+=8
        elif self.lang == 'it':
            i+=14
        else:
            i+=16
        #<h2>
        self.translate('To be added to the press list contact',
                       self.nodevalue(i)) ;i+=8

        #<h2>
        self.translate('To provide content to Europeana',
                       self.nodevalue(i)) ;i+=2
        #<p>
        self.translate('content providers', self.nodevalue(i,1)) ;i+=2

        self.translate('See the', self.EMPTY_STR)
        self.translate('page on our project site', self.EMPTY_STR)


        #self.translate('', self.nodevalue())




    #
    # Internals related to propperty files
    #
    def translate_properties(self):
        "updates po file with properties for this language."
        if self.debug:
            print 'Translating properties file'
        po = polib.pofile(self.po_base_name + '.po')
        for entry in po:
            key = entry.msgid[1:].split('[')[0]
            if key in self.lang_props.keys():
                entry.msgstr = self.lang_props[key]
        po.save()
        po.save_as_mofile(self.po_base_name + '.mo')


    def create_po_file(self):
        "Generate a po file, assumes all html files we are translating is in place."
        ddir = os.path.split(self.LOCALE_DIR)[0]
        self.shell_cmd('cd %s; django-admin.py makemessages -l %s' % (ddir,self.lang))


    def create_html_prop_file(self):
        "Creates the html properties file from the english properties."
        """
        Create prop_file.html
        {% load i18n %}

        MyCodeOfConduct_t={% trans "MyCodeOfConduct_t" %}
        ...
        """
        f = codecs.open(self.HTML_PROPS_FNAME, 'w', 'utf-8')
        f.write('{% load i18n %}\n')
        self.read_msg_props('en') # use english as reference
        for key in self.lang_props.keys():
            f.write(u'%s={%% trans "#%s[%s]" %%}\n' % (key,
                                                     key,
                                                     self.lang_props[key]))
        f.close()


    def read_html_props(self):
        "Since the html file is the master, use this to get authorative prop keys."
        f = codecs.open(self.HTML_PROPS_FNAME, 'r', 'utf-8')
        self.prop_keys = []
        for line in f.readlines():
            if self.PROPFILE_DELIM in line:
                key=line.split('=')[0]
                self.prop_keys.append(key)
        f.close()


    def read_msg_props(self, lang):
        "Read lang specific propperty file, and store in self.lang_props."
        fname = os.path.join(proj_path, 'import', 'message_keys',
                             'messages_%s.properties' % lang)
        f = codecs.open(fname, 'r', 'utf-8')
        self.lang_props = {}
        continuation = False
        for line in f.readlines():
            line = line.strip() # friging hate linefeeds...
            if self.PROPFILE_DELIM in line:
                continuation = False
                # nyrad
                key, value = line.split('=')
                if value[-1] == '\\':
                    value = value[:-1]
                    continuation = True
                self.lang_props[key] = value
            elif continuation:
                if line[-1] == '\\':
                    line = line[:-1]
                else:
                    continuation = False
                self.lang_props[key] = self.lang_props[key] + ' ' + line
            pass
        return


    def shell_cmd(self, cmd):
        "Run a cmd in subshell, aborting on exitcode !+ 0."
        # create po file (update it if already existing)
        excode = subprocess.call( cmd, shell=True)
        if excode:
            print
            print '***   Aborting create_po_file() due to error!'
            sys.exit(1)
        return


    #
    #  Internals related to page translation
    #
    def translate(self, key, value, punctuation_variance_ok=False,
                  purge_punctation=BOTH,recursion=0):
        if purge_punctation:
            value = self.punctuation_purge(value, purge_punctation, recursion)
        if key[-1] == '*':
            key = key[:-1] # strip end
            entry = None
            lst = []
            for itm in self.po:
                if key in itm.msgid:
                    lst.append(itm)
            if len(lst) > 1:
                print
                print '*** translation - fuzzy search gave multiple entries'
                print key
                sys.exit()
            entry = lst[0]
        else:
            entry = self.po.find(key)
        if not entry:
            print
            print '*** translation attempt on unknown key', key
            sys.exit(1)
        entry.msgstr = value.strip()
        if entry.msgstr == self.EMPTY_STR:
            entry.msgstr = ' '
        if self.debug > 2:
            print
            print u'--key: [%s]' % entry.msgid
            s =  u'value: [%s]' % entry.msgstr
            print s.encode('utf-8')
        if entry.msgstr and not punctuation_variance_ok:
            if entry.msgstr[0] in ('.,!'):
                print '+++ suspicious initial punctation!'
            if entry.msgstr[-1] in (',;:'):
                print '+++ suspicious ending punctation!'
            if self.is_punctuated(entry.msgid) != self.is_punctuated(entry.msgstr):
                print '+++ ending punctation differs between key and value!'
        if self.ERR_NODE_VALUE in entry.msgstr:
            return
        self.po.save()



    def is_punctuated(self, s):
        if s[-1] in '.,!:;':
            b = True
        else:
            b = False
        return b


    def punctuation_purge(self, s, end=BOTH,recursion=0):
        s = s.strip()
        if not s:
            return s
        if end in (BOTH,START):
            if s[0] in '.,':
                s = s[1:]
        if end in (BOTH,END):
            s = s.strip()
            if s[-1] in u'.,;:\xe5':
                s = s[:-1]
        if recursion > 0:
            recursion -= 1
            s = self.punctuation_purge(s, end, recursion)
        return s


    def nodevalue(self, main_idx, *indexes):
        try:
            x = self.doc.childNodes[main_idx]
            for sub_idx in indexes:
                x = x.childNodes[sub_idx]
            if x.nodeName == '#text':
                r = x.toxml()
            else:
                r = x.childNodes[0].toxml()
        except IndexError, e:
            r = '%s  %s  ***' % (self.ERR_NODE_VALUE, e.__doc__)
            if self.debug < 1:
                raise e
        return r


    def read_html_file(self, file_name):
        f = codecs.open(file_name, 'r', 'utf-8')
        lines = f.readlines()
        f.close()
        lst = []
        for line in lines:
            s = line.strip()
            if s:
                lst.append(s)
        r = '\n'.join(lst)
        r = r.replace('/></a>','></a>')
        r = r.replace('http://version1.europeana.eu/c/document_library/get_file?uuid=8493d624-7b32-4a6a-8a41-0c765922874e&groupId=10602','http://version1.europeana.eu/c/document_library')
        return r




def loop_on_languages():
    if 1:
        print '====>  Removing all translations files!!'
        ip = InitializePropertifiles()
        ip.clear_all_files()

    for lang in (
        #'en',
        #'de','es','fi','fr','is','it','no','sv',
        #'sv',
        settings.LANGUAGES_DICT.keys()
        ):
        print '---Creating translations for:', lang
        InitializePropertifiles(lang, debug=9).handle_language()
    return




loop_on_languages()
