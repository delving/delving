
from django.conf.urls.defaults import *
from django.conf import settings

from django.contrib import admin
admin.autodiscover()

import apps.multi_lingo





urlpatterns = patterns('',
    url(r'^$',apps.multi_lingo.views.index_page),
    #url(r'^export$',apps.multi_lingo.views.export_content,name='ml-export'),
    # Uncomment the admin/doc line below and add 'django.contrib.admindocs'
    # to INSTALLED_APPS to enable admin documentation:
    # (r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    (r'^admin/', include(admin.site.urls)),
    #(r'^portal/(?P<path>\w+)_(?P<lang>\w+).html$',  apps.multi_lingo.views.static_page),
    (r'^portal/(?P<rel_url>.*)$',  apps.multi_lingo.views.portal_page),

    #url(r'^reload_templates/', apps.multi_lingo.views.reload_templates,
    #    name='reload-templates'),

    #(r'^%s(?P<path>.*)$' % settings.MEDIA_URL[1:],
    # 'django.views.static.serve',
    # {'document_root': settings.MEDIA_ROOT}),


)

#urlpatterns += apps.multi_lingo.utils.urls()

if settings.DELIVER_STATIC_MEDIA:
    media_url = settings.MEDIA_URL
    if media_url[0] == '/':
        media_url = media_url[1:]
    if media_url[-1] == '/':
        media_url = media_url[:-1]

    urlpatterns += patterns('django.views.static',
                            (r'^%s/(?P<path>.*)$' % media_url,
                             'serve', {
                                 'document_root': settings.MEDIA_ROOT,
                                 'show_indexes': True }),)

if 'rosetta' in settings.INSTALLED_APPS:
    urlpatterns += patterns('',
        (r'^rosetta/', include('rosetta.urls')),
    )

