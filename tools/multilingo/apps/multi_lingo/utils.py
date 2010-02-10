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

from django.conf.urls.defaults import url, patterns

#from views import find_templates, show_page, prop_page, PROP_URL_NAME
import views



def global_environ(request):
    """Insert some additional information into the template context
    from the settings.
    Specifically, the LOGOUT_URL, MEDIA_URL and BADGES settings.
    """
    additions = {
        #'DJANGO_ROOT': request.META['SCRIPT_NAME'],
        'europeana_item_count_mill': 6, # in miljons how large the dataset is
    }
    return additions



def urls():
    """
    (r'^about_us/$', common_index),
    (r'^about_us.html$', common_index),
    url(r'^about_us_(?P<lang>\w+).html$', common_index, name='about_us'),
    """
    lst = []

    for template in views.find_templates():
        lst.append( (r'^%s/$' % template, views.show_page) )
        lst.append( (r'^%s.html$' % template, views.show_page) )
        lst.append(url(r'^%s_(?P<lang>\w+).html$' % template,
                       views.show_page,
                       name=template))
    lst.append(url(r'^%s_(?P<lang>\w+).properties$' % views.PROP_URL_NAME,
                   views.prop_page,
                   name=views.PROP_TEMPLATE))
    r = patterns('',*lst)
    return r
