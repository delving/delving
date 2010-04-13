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

# Create your views here.


from django.shortcuts import render_to_response, get_object_or_404


import models

def statistics(request, order_by=''):
    if order_by:
        if order_by in ('name_or_ip','imgs_waiting','imgs_ok','imgs_bad','eta'):
            if request.session.get('sortkey','').find(order_by)==1:
                # that is if it was a -key we change it to key
                p_order_by = order_by
            else:
                p_order_by = '-%s' % order_by
        else:
            p_order_by = 'name_or_ip'
    else:
        p_order_by = 'name_or_ip'
    request.session['sortkey'] = p_order_by

    # Summary

    imgs_ok = models.Uri.objects.filter(status=models.URIS_COMPLETED).count()
    imgs_waiting = models.Uri.objects.filter(status=models.URIS_CREATED,err_code=models.URIE_NO_ERROR).count()
    imgs_bad = models.Uri.objects.exclude(err_code=models.URIE_NO_ERROR).count()


    sel_common = "SELECT COUNT(*) FROM plug_uris_uri WHERE"
    sql_table_join = "AND uri_source_id=plug_uris_urisource.id"

    sql_img_ok = "%s status=%i %s" % (sel_common, models.URIS_COMPLETED, sql_table_join)
    sql_img_waiting = "%s status=%i AND item_type=%i AND err_code=%i %s" % (
        sel_common, models.URIS_CREATED, models.URIT_OBJECT, models.URIE_NO_ERROR, sql_table_join)
    sql_img_bad = "%s err_code > %i %s" % (sel_common, models.URIE_NO_ERROR, sql_table_join)

    sql = "SELECT task_eta FROM process_monitor_processmonitoring where pid=plug_uris_urisource.pid"
    uri_sources = models.UriSource.objects.extra(select={'imgs_ok':sql_img_ok,
                                                     'imgs_bad': sql_img_bad,
                                                     'imgs_waiting':sql_img_waiting,
                                                     #'eta':sql,
                                                     }).order_by(p_order_by)
        #'imgs_ok':'status = %i' % models.URIS_COMPLETED})
    i = len(uri_sources)
    return render_to_response("plug_uris/statistics.html", {
        "uri_sources":uri_sources,
        "summary": {"imgs_ok": imgs_ok,
                    "imgs_waiting": imgs_waiting,
                    "imgs_bad": imgs_bad},})


def problems(request, source_id=-1):
    try:
        urisource = models.UriSource.objects.get(pk=source_id)
    except:
        urisource = None
    problems = {}
    for k in models.URI_ERR_CODES:
        if k == models.URIE_NO_ERROR:
            continue
        uri_filter = {'err_code': k}
        if urisource:
            uri_filter['uri_source'] = urisource
        count = models.Uri.objects.filter(**uri_filter).count()
        if not count:
            continue
        problems[models.URI_ERR_CODES[k]] = {
            'err_code': k,
            'count': count,
        }
    return render_to_response('plug_uris/problems.html', {
        'urisource': urisource,
        'problems': problems})


def index(request):
    uris =  models.Uri.objects.filter(status=models.URIS_CREATED,
                                      pid=0).order_by('-uri_source')

    return render_to_response('plug_uris/index.html', {'uri':uris[0]})
