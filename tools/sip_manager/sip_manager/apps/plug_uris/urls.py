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



"""

from django.conf.urls.defaults import *

from django.conf import settings

import views


urlpatterns = patterns('',

    url(r'^statistics/$', views.statistics, name='uri_stats_idx'),

    url(r'^dg1/$', views.dg1, name='uri_dg1'),

    url(r'^stats_reqs/$', views.stats_req_lst, name='uri_stats_req'),
    url(r'^stats_by_reqs/(?P<sreq_id>\d+)/$', views.stats_by_req, name='uri_stats_by_req'),

    url(r'^stats_uri/(?P<order_by>\S+)/$', views.stats_by_uri, name='uri_stats'),
    url(r'^stats_uri/$', views.stats_by_uri, name='uri_stats'),

    url(r'^problems/(?P<source_id>\S+)/$', views.problems, name='uri_problems'),
    url(r'^problems/$', views.problems, name='uri_problems'),

)
