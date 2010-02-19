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

 2010-02-18 Not Yet completed...


 Tool to import original properties files for all languages into this system
"""


import xml.dom.minidom
import codecs
import os
import sys

# hack to be able to import from parent dir
proj_path = os.path.split(os.path.dirname(__file__))[0]
sys.path.append(proj_path)

from rosetta import polib




ERR_NODE_VALUE = '*** NODEVALUE'
NOT_IN_THIS_LANG = '* not used in this language *'


def read_html_file(file_name):
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


class TranslateTemplate(object):


    def __init__(self, html_s, lang, debug=1):
        self.lang = lang
        # debug lvls:
        #  1 basic output
        #  5 each key,value is displayed during translation
        self.debug = debug

        if self.debug:
            print 'Translating language', self.lang

        x = xml.dom.minidom.parseString('<wrapper1>%s</wrapper1>' % html_s)
        self.doc = x.childNodes[0] # dump wrapper1

        self._translation_file_base = os.path.join(proj_path, 'apps/multi_lingo/locale/%s/LC_MESSAGES/django' % lang)
        self.po = polib.pofile(self._translation_file_base + '.po')



    def translate(self, key, value):
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
        if self.debug > 2:
            print '--key:', entry.msgid
            print 'value:', entry.msgstr
        if ERR_NODE_VALUE in entry.msgstr:
            return
        if value == NOT_IN_THIS_LANG:
            return
        self.po.save()



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
            r = '%s  %s  ***' % (ERR_NODE_VALUE, e.__doc__)
            if self.debug < 1:
                raise e
        return r



    def transl_aboutus(self):
        if self.debug > 1:
            print 'Translating about_us'

        i = 0
        self.translate('About us', self.nodevalue(i)) ;i+=5
        self.translate('Storyline and Credits', self.nodevalue(i)) ;i+=2
        self.translate('Europeana: think culture', self.nodevalue(i)) ;i+=2

        # insert dynamic number of objs
        s = self.nodevalue(i) ;i+=2
        if '6' in s:
            s = s.replace('6', '%(europeana_item_count_mill)s')
        else:
            print
            print '*** milions of objects not found'
            sys.exit()
        self.translate('Europeana.eu is about ideas and inspiration. It links*', s)

        i2 = 1
        self.translate('Images - paintings, drawings, maps, photos and pictures of museum objects',
                       self.nodevalue(i, i2)) ;i2+=2
        self.translate('Texts - books, newspapers, letters, diaries and archival papers',
                       self.nodevalue(i,i2)) ;i2+=2
        self.translate('Sounds - music and spoken word from cylinders, tapes, discs and radio broadcasts',
                       self.nodevalue(i,i2)) ;i2+=2
        self.translate('Videos - films, newsreels and TV broadcasts',
                       self.nodevalue(i,i2)) ;i2+=2

        i+=2 ; i2=1
        self.translate("Some of these are world famous, others are hidden treasures from Europe's",
                       self.nodevalue(i)) ;i+=2
        i2=1
        self.translate('museums and galleries',
                       self.nodevalue(i,i2)) ;i2+=2
        self.translate('archives', self.nodevalue(i,i2)) ;i2+=2
        self.translate('libraries', self.nodevalue(i,i2)) ;i2+=2
        self.translate('audio-visual collections',
                       self.nodevalue(i,i2)) ;i2+=2

        i+=4 ; i2=1
        self.translate('Here is a', self.nodevalue(i))
        self.translate('list of the organisations',
                       self.nodevalue(i,i2))

        lst = []
        for node in self.doc.childNodes[i].childNodes[2:9]:
            lst.append(node.toxml())
        value = u' '.join(lst)
        self.translate('that our content comes from. They include*', value)

        i+=2 ; i2=1
        self.translate('You can use', self.nodevalue(i))
        self.translate('My Europeana', self.nodevalue(i,i2)) ;i2+=1
        self.translate('to save searches or bookmark things. You can highlight stuff and add it to your own folders',
                       self.nodevalue(i,i2)) ;i+=2
        self.translate('This website is a prototype. Europeana Version*',
                       self.nodevalue(i)) ;i+=2
        self.translate('Europeana.eu is funded by the European*',
                       self.nodevalue(i)) ;i+=2

        # remove a trailing : from previous templates
        v = self.nodevalue(i).strip() ;i+=2
        if v[-1] == ':':
            v = v[:-1]
        self.translate('More about', v)

        # <li>
        i2=1
        self.translate('How Europeana came to be developed',
                       self.nodevalue(i,i2))
        i3=1
        self.translate('the background',
                       self.nodevalue(i,i2,i3)) ;i3+=1
        self.translate('to the project',
                       self.nodevalue(i,i2,i3)) ;i2+=2
        # <li>
        i3=1
        self.translate('The deliverables from the project',
                       self.nodevalue(i,i2))
        self.translate('technical plans',
                       self.nodevalue(i,i2,i3)) ;i3+=1
        self.translate('etc',
                       self.nodevalue(i,i2,i3)) ;i2+=2
        # <li>
        self.translate('New projects that will be channelling material into',
                       self.nodevalue(i,i2)) ;i2+=4
        # <li>
        i3=1
        self.translate('How organisations can',
                       self.nodevalue(i,i2))
        self.translate('contribute content',
                       self.nodevalue(i,i2,i3)) ;i3+=1
        self.translate('to Europeana',
                       self.nodevalue(i,i2,i3)) ;i2+=2

        # <li>
        if self.lang == 'de':
            v = NOT_IN_THIS_LANG
            i3 = 0
        else:
            v = self.nodevalue(i,i2)
            i3 = 1
        self.translate('Getting in', v)
        self.translate('contact',
                       self.nodevalue(i,i2,i3)) ;i3+=1
        self.translate('with the Europeana team',
                       self.nodevalue(i,i2,i3)) ;i2+=2
        # <li>
        i3 = 1
        self.translate('To be added to the',
                       self.nodevalue(i,i2))
        self.translate('press list',
                       self.nodevalue(i,i2,i3)) ;i2+=2

        # <li>
        i3 = 0
        if self.lang in ('de', 'sv'):
            v = NOT_IN_THIS_LANG
        else:
            v = self.nodevalue(i,i2,i3)
        self.translate('The', v)
        self.translate('e-news', self.nodevalue(i,i2,i3)) ;i3+=1 #29,15,0
        self.translate('to keep you in touch with developments',
                       self.nodevalue(i,i2,i3)) ;i+=4


        self.translate('Background', self.nodevalue(i)) ; i+=2
        self.translate('The Commission has been working for a number of years on projects to boost*',
                       self.nodevalue(i)) ; i+=2

        i2 = 1
        self.translate('The idea for Europeana came from a',
                       self.nodevalue(i))
        self.translate('letter', self.nodevalue(i,i2)) ;i2+=1
        self.translate('to the Presidency of Council and to the Commission on*',
                       self.nodevalue(i,i2)) ;i+=2

        self.translate('On 30 September 2005 the European Commission published*',
                       self.nodevalue(i)) ;i+=2
        self.translate('which aims to foster growth and jobs in the information society*',
                       self.nodevalue(i)) ;i+=2
        self.translate('The Europeana prototype is the result*',
                       self.nodevalue(i)) ;i+=2

        #<p>
        i2=2
        self.translate('Europeana is a Thematic Network funded by  the European Commission under the',
                       self.nodevalue(i))
        self.translate('as part of the', self.nodevalue(i,i2)) ;i2+=1
        self.translate('i2010  policy', self.nodevalue(i,i2)) ;i2+=1
        self.translate('Originally known as the European digital library network*',
                       self.nodevalue(i,i2)) ;i2+=1

        #<p>
        self.translate('The project is run by a core team based in  the national library of the Netherlands, the',
                       self.nodevalue())
        #self.translate('', self.nodevalue())
        #self.translate('', self.nodevalue())
        #self.translate('', self.nodevalue())

        self.po.save_as_mofile(self._translation_file_base + '.mo')
        pass

def parse_about_us(lang):
    s = read_html_file('html_files/aboutus_%s.html' % lang)
    t = TranslateTemplate(s, lang, debug=9)
    t.transl_aboutus()








parse_about_us('sv')

