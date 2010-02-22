from django.conf.urls.defaults import *
from django.conf import settings

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

import apps.multi_lingo


urlpatterns = patterns('',
    url(r'^$',apps.multi_lingo.views.index_page, name='sune'),
    # Uncomment the admin/doc line below and add 'django.contrib.admindocs'
    # to INSTALLED_APPS to enable admin documentation:
    # (r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    (r'^admin/', include(admin.site.urls)),
)

urlpatterns += apps.multi_lingo.utils.urls()

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

