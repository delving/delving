
from django.conf.urls.defaults import *


import views



urlpatterns = patterns('',
  #url(r'^$', views.index, name='clm_index'),
  url(r'^foo/$', views.foo, name='uri_foo'),
  )
