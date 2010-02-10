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

from django.conf import settings
from django.shortcuts import render_to_response
from django.template import RequestContext
from django.core.urlresolvers import reverse
from django.http import HttpResponseRedirect



HTML_EXT = '.html'
PROP_URL_NAME = 'message_keys/messages'
PROP_TEMPLATE = 'prop_file.html'



def find_templates():
    templates = []
    # project/app/locale
    for fname in os.listdir(os.path.join(os.path.split(__file__)[0] + '/templates/pages')):
        if fname.find(HTML_EXT) < 0:
            continue
        templates.append(fname.split(HTML_EXT)[0])
    return templates

_templates = find_templates()


def langCheck(lang):
    """
    Set up a few language dependent environ variables
    """
    if lang not in settings.LANGUAGES_DICT.keys():
        # trigger redirect to enforce nice language specific urls
        return None
    return {'lang': lang,
            'lang_long_name': settings.LANGUAGES_DICT[lang],
            'all_languages': settings.LANGUAGES,
            }

def best_match_template(pname):
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

def show_page(request, lang=''):
    # if language is changed in dropdown, let POST param override url derived
    # lang selection
    lang = request.POST.get('lang',lang)
    template = best_match_template(request.path)

    content = langCheck(lang) # only accept supported languages
    if not (content):
        # we only accept urls with language selections, as a fallback send
        # visitor back to the english about_us
        return HttpResponseRedirect(reverse(template, args=('en',)))

    if request.session.get('django_language','') != content['lang']:
        # A langugage change is detected, propably due to usage of dropdown
        # reload same page to get the propper context
        request.session['django_language'] = content['lang']
        return HttpResponseRedirect(reverse(template, args=(content['lang'],)))

    # Map url to template
    return render_to_response('pages/%s.html' % template, content,
                              context_instance=RequestContext(request))


def index_page(request):
    return render_to_response('overview.html',
                              {
                                  'pages': _templates,
                              },
                              context_instance=RequestContext(request))



def prop_page(request, lang=''):

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

