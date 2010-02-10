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
from subprocess import Popen

proj_path = os.path.split(os.path.dirname(__file__))[0]
sys.path.append(proj_path)
import settings


TMP_EXT = '.tmp'
PROPFILE_DELIM = '_t='


def create_po_file(locale_path, lang):
    # create po file (update it if already existing)
    locale_parrent = os.path.split(locale_path)[0]
    p = Popen('cd %s; django-admin.py makemessages -l %s' % (locale_parrent, lang),
              shell=True)
    sts = os.waitpid(p.pid, 0)[1]

def handle_language(locale_path, lang):
    if lang =='en':
        return # english is used as key so no need to create translations

    prop_file_name = os.path.join(os.path.split(locale_path)[0],
                                  'templates',
                                  'prop_file.html')

    create_po_file(locale_path, lang)

    # read the original .po file
    fname = os.path.join(locale_path, lang, 'LC_MESSAGES','django.po')
    f = codecs.open(fname, 'r', 'utf-8')
    org_lines = f.readlines()
    f.close()

    # write the modified one (with translations included)
    fout = codecs.open(fname + TMP_EXT, 'w', 'utf-8')
    orgfile_line_no = 0
    while orgfile_line_no < len(org_lines):
        org_line = org_lines[orgfile_line_no]
        fout.write(org_line)
        if org_line.find('templates/prop_file') > -1:
            # line looks like:   #: templates/prop_file.html:25
            prop_line_no =  int(org_line.split(':')[-1][:-1])

            # write msgid line(-s)
            while True:
                orgfile_line_no += 1
                org_line = org_lines[orgfile_line_no]
                if org_line.find('msgstr') != 0:
                    fout.write(org_line)
                else:
                    break

            # write translation
            s = translation(prop_file_name, prop_line_no, lang)
            fout.write('msgstr "%s"\n' % s)
        orgfile_line_no += 1
    fout.close()

    # replace org file
    shutil.move(fname + TMP_EXT, fname)



def translation(prop_file_name, prop_line_no, lang):
    "find tag at given line, and return translation for this tag."
    f = codecs.open(prop_file_name, 'r', 'utf-8')
    prop_file = f.readlines()
    f.close()
    tag = prop_file[prop_line_no - 1].split('=')[0]
    return translation_from_propfile(lang, tag)


def translation_from_propfile(lang, tag):
    "Return translation for given tag."
    prop_file_name = os.path.join(proj_path, 'import', 'message_keys',
                            'messages_%s.properties' % lang)
    f = codecs.open(prop_file_name, 'r', 'utf-8')
    prop_file = f.readlines()
    f.close()
    line_no = 0
    while line_no < len(prop_file):
        line = prop_file[line_no]
        line_no += 1
        if line.find(tag) == 0:
            # tag found read translation
            msg = [line.split(PROPFILE_DELIM)[1].strip()]
            line_no += 1
            while (line_no < len(prop_file)) and  (prop_file[line_no].find(PROPFILE_DELIM) < 1):
                # a continuation for the translation, keep reading
                msg.append(prop_file[line_no].strip())
                line_no += 1
            break
    return ''.join(msg) # will generate exception if not found, shouldnt happen




def loop_on_languages():
    locale_path = os.path.join(proj_path, 'apps', 'multi_lingo', 'locale')
    create_po_file(locale_path, 'en') # make sure the english file exists
    for lang in settings.LANGUAGES_DICT.keys():
        handle_language(locale_path, lang)
    return




loop_on_languages()
