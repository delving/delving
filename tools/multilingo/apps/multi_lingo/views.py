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

import settings as sett2

from django.conf import settings
from django.shortcuts import render_to_response
from django.core.urlresolvers import reverse
from django.http import HttpResponseRedirect
from django.template.loader import render_to_string


import django.template.loader

from rosetta.poutil import find_pos

from dataexp import get_tarball


import models

HTML_EXT = '.html'
PROP_URL_NAME = 'message_keys/messages'
PROP_TEMPLATE = 'prop_file.html'
LANG_KEY = 'django_language'

OUR_STATIC_PAGES = {}


# some caching
PORTAL_PREFIX = settings.PORTAL_PREFIX


def index_page(request):
    page_lst = OUR_STATIC_PAGES.keys()
    page_lst.sort()
    return render_to_response('overview.html',
                              {
                                  'portal_prefix': PORTAL_PREFIX,
                                  'pages': page_lst,
                              })




def portal_url(request, rel_url, lang='', *args, **kwargs):
    """Due to the way the official portal handles urls, everything starts with
    /portal/
    This is a bit counter-intuitive when handled by django, we filter out
    the pages we want to handle ourself, and redirect everything else to the
    static page handler
    """
    if rel_url == 'index.html':
        return index_page(request) # point back to topindex
    if rel_url in OUR_STATIC_PAGES.keys():
        return our_static_pages_handler(request, rel_url)
    stat_path = os.path.join(settings.MEDIA_URL, rel_url)
    return HttpResponseRedirect(stat_path)



#=================   utils   ======================

def our_static_pages_handler(request, rel_url):
    template = OUR_STATIC_PAGES[rel_url]
    lang = request.session.get(LANG_KEY,'')
    new_lang = request.POST.get('lang')
    if not (lang or new_lang):
        new_lang = 'en'
    if new_lang:
        return set_lang_redirect(request, new_lang, rel_url)
    content = prepare_generic_vars(lang) # only accept supported languages
    return render_to_response(template, content)


def set_lang_redirect(request, lang, next_page='/'):
    request.session[LANG_KEY] = lang
    return HttpResponseRedirect(next_page)


def prepare_generic_vars(lang):
    """
    Set up a few generic variables for usage in templates
    """
    if lang not in settings.LANGUAGES_DICT.keys():
        # trigger redirect to enforce nice language specific urls
        lang = 'en'
    return {'lang': lang,
            'lang_long_name': settings.LANGUAGES_DICT[lang],
            'all_languages': settings.LANGUAGES,
            'europeana_item_count_mill': 7,
            }






#=================   Finding templates   ======================

def update_template_list():
    global OUR_STATIC_PAGES
    # project/app/locale

    OUR_STATIC_PAGES = {}
    for static_page in models.TranslatePage.objects.all():
        fname = os.path.split(static_page.file_name.name)[1]

        """
        # Also add version with language set for this page

        dot_pos = fname.find('.')

        for lang, name in settings.LANGUAGES:
            key = '%s_%s.%s' % (fname[:dot_pos],
                                lang,
                                fname[dot_pos+1:])
            OUR_STATIC_PAGES[key] = {'template':fname,
                              'lang':lang,}
        """
        OUR_STATIC_PAGES[fname] = static_page.file_name.name


update_template_list() # do an initial scan on startup

#=================   Not checked   ======================

def NOT_extract_lang_from_static_path(path):
    us_pos = path.find('_')
    dot_pos = path.find('.')
    if not (us_pos > -1 and dot_pos > us_pos):
        return path, ''
    lang = path[us_pos+1:dot_pos]
    s = path[:us_pos] + path[dot_pos:]
    return s, lang




def NOT_export_content(request):
    data = get_tarball(_templates)

def NOT_best_match_template(pname):
    "if possible pick template from url or fallback."
    r = _templates[0] # default fallback
    try:
        s = pname[1:].split('_')[0]
        if s[-1] == '/':
            s = s[:-1]
    except:
        s = 'not found here'
    for template in _templates:
        if template.find(s) > -1:
            r = template
            break
    return r





def NOT_prop_page(request, lang=''):
    content = langCheck(lang) # only accept supported languages
    if not (content):
        # we only accept urls with language selections, as a fallback send
        # visitor back to the english about_us
        return HttpResponseRedirect(reverse(PROP_URL_NAME, args=('en',)))

    if request.session.get('django_language','') != content['lang']:
        # A langugage change is detected, propably due to usage of dropdown
        # reload same page to get the propper context
        request.session['django_language'] = content['lang']
        return HttpResponseRedirect(reverse(PROP_TEMPLATE, args=(content['lang'],)))

    return render_to_response(PROP_TEMPLATE)


#===============================



def NOT_reload_templates(request):
    execute_manager(sett2, argv=['foo','makemessages', 'help'])
    a=find_pos('de')
    pass

