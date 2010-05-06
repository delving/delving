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
from django.template import RequestContext
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

_TEMPLATES = []
_STATIC_PAGES = {}


# some caching
PORTAL_PREFIX = settings.PORTAL_PREFIX


def index_page(request):
    return render_to_response('overview.html',
                              {
                                  'portal_prefix': PORTAL_PREFIX,
                                  'pages': _TEMPLATES,
                              },
                              context_instance=RequestContext(request))




def portal_page(request, rel_url, lang='', *args, **kwargs):
    # if language is changed in dropdown, let POST param override url derived
    # lang selection
    if rel_url in _STATIC_PAGES.keys():
        return static_page(request, rel_url)
    path, url_item = os.path.split(rel_url)
    if path:
        parts = path.split('/')
        if parts[0] in ('css','js','images'):
            static_url = '%s%s' % (settings.MEDIA_URL, rel_url)
            return HttpResponseRedirect(static_url)
        else:
            pass
    return HttpResponseRedirect('/')



#=================   utils   ======================

def static_page(request, rel_url):
    template = _STATIC_PAGES[rel_url]
    new_lang = request.POST.get('lang')
    if new_lang:
        lang = new_lang
    else:
        lang = request.session.get('django_language','')
    content = langCheck(lang) # only accept supported languages
    if not (content):
        # we only accept urls with language selections, as a fallback send
        # visitor back to the english about_us
        return HttpResponseRedirect(rel_url)

    if request.session.get('django_language','') != content['lang']:
        # A langugage change is detected, propably due to usage of dropdown
        # reload same page to get the propper context
        request.session['django_language'] = content['lang']
        return HttpResponseRedirect(rel_url)#'%s/%s' % (settings.PORTAL_PREFIX, rel_url))

    return render_to_response(template, content,
                              context_instance=RequestContext(request))


def extract_lang_from_static_path(path):
    us_pos = path.find('_')
    dot_pos = path.find('.')
    if not (us_pos > -1 and dot_pos > us_pos):
        return path, ''
    lang = path[us_pos+1:dot_pos]
    s = path[:us_pos] + path[dot_pos:]
    return s, lang


def langCheck(lang):
    """
    Set up a few language dependent environ variables
    """
    if lang not in settings.LANGUAGES_DICT.keys():
        # trigger redirect to enforce nice language specific urls
        lang = 'en'
    return {'lang': lang,
            'lang_long_name': settings.LANGUAGES_DICT[lang],
            'all_languages': settings.LANGUAGES,
            }



#=================   Finding templates   ======================

def update_template_list():
    global _STATIC_PAGES, _TEMPLATES
    # project/app/locale

    _STATIC_PAGES = {}
    _TEMPLATES = []
    for static_page in models.TranslatePage.objects.all():
        fname = os.path.split(static_page.file_name.name)[1]
        """
        dot_pos = fname.find('.')

        for lang, name in settings.LANGUAGES:
            key = '%s_%s.%s' % (fname[:dot_pos],
                                lang,
                                fname[dot_pos+1:])
            _STATIC_PAGES[key] = {'template':fname,
                              'lang':lang,}
        """
        _STATIC_PAGES[fname] = static_page.file_name.name
        _TEMPLATES.append(fname)
    _TEMPLATES.sort()


update_template_list()

#=================   Not checked   ======================




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

    return render_to_response(PROP_TEMPLATE,
                              context_instance=RequestContext(request))


#===============================



def NOT_reload_templates(request):
    execute_manager(sett2, argv=['foo','makemessages', 'help'])
    a=find_pos('de')
    pass

