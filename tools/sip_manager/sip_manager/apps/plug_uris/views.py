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

import urllib

from django.db.models import Q
from django.shortcuts import render_to_response, get_object_or_404

from apps.dummy_ingester.models import Request

#from datagrids import UriSourcesDataGrid
import models
from apps.base_item import models as base_item

Q_OBJECT = Q(item_type=models.URIT_OBJECT)
Q_OK = Q(status=models.URIS_COMPLETED, err_code=models.URIE_NO_ERROR)
Q_BAD = ~Q(err_code=models.URIE_NO_ERROR)


BAD_BY_REQ_PG_SIZE = 150

"""

myapp/datagrid.html


reviews/dashboard.html
eta_history = []

def delta_waiting():
    global eta_history
    t0 = time.time()
    waiting = uri_summary(){'imgs_waiting'}
    if eta_history:
        latest_t, latest_waiting = eta_history[0]
        oldest_t, oldest_waiting = eta_history[-1]
    else:
        return -1
    if (t0 - latest_t) > 10:
        eta_history.insert(0, (t0, waiting))
    eta_history.insert((t0, waiting), 0)
    t0, old_waiting = eta_history.pop
    percent_done = float(step) / self._task_steps * 100
    elapsed_time = time.time() - self._task_time_start
    eta_t_from_now = int(elapsed_time / ((percent_done / 100) or 0.001))
    eta = self._task_time_start + eta_t_from_now
    if (eta - time.time()) < SHOW_DATE_LIMIT:
        eta_s = time.strftime('%H:%M:%S', time.localtime(eta))
    else:
        eta_s = time.strftime('%m-%d %H:%M:%S', time.localtime(eta))


"""

def dg1(request, template_name='plug_uris/datagrid1.html'):
    return UriSourcesDataGrid(request).render_to_response(template_name)

def statistics(request):
    return render_to_response("plug_uris/statistics.html", {
        'summary': uri_summary(),})

def stats_req_lst(request):
    lst = []
    for req in  models.ReqUri.objects.values('req').distinct():
        req_id = req['req']
        q_all = Q(req=req_id, item_type=models.URIT_OBJECT)
        qs_all = models.ReqUri.objects.filter(q_all)
        img_count = qs_all.count()
        if not img_count:
            continue # only display if request if any images
        #itm_ok = models.ReqUri.objects.filter(q_all, Q_OK).count()
        itm_ok = qs_all.filter(Q_OK).count()
        #itm_bad = models.ReqUri.objects.filter(q_all, Q_BAD).count()
        itm_bad = qs_all.filter(Q_BAD).count()
        waiting = img_count - itm_ok - itm_bad
        lst.append({'request':Request.objects.get(pk=req_id),
                    'count': img_count,
                    'waiting': waiting,
                    'ok': itm_ok,
                    'bad': itm_bad,
                    'ratio': s_calc_ratio(itm_ok, img_count),
                    })

    return render_to_response("plug_uris/stats_all_requests.html", {
        'requests': lst,})


def stats_by_req(request, sreq_id=0):
    req_id = int(sreq_id)
    request = models.Request.objects.filter(pk=req_id)[0]

    #
    # Grouped by mimetype
    #
    qs_all = models.ReqUri.objects.filter(Q_OBJECT, req=req_id)

    mime_results = []
    for row in qs_all.values_list('mime_type').distinct().order_by('mime_type'):
        mime_type = row[0]
        if not mime_type:
            continue # not set for this item
        qs_mime = qs_all.filter(mime_type=mime_type)
        itm_ok = qs_mime.filter(Q_OK).count()
        itm_bad = qs_mime.filter(Q_BAD).count()
        mime_results.append({'name':mime_type,
                             'mime_url': urllib.quote_plus(mime_type),
                             'ok': itm_ok,
                             'bad': itm_bad,
                             'ratio': s_calc_ratio_bad(itm_ok, itm_bad),
                             })

    #
    # Grouped by error
    #
    err_by_reasons = []
    for err_code in models.URI_ERR_CODES.keys():
        if err_code == models.URIE_NO_ERROR:
            continue
        count = qs_all.filter(err_code=err_code).count()
        if not count:
            continue
        err_by_reasons.append({'err_code' : err_code,
                               'err_msg': models.URI_ERR_CODES[err_code],
                               'count': count})

    #
    # Grouped by webserver
    #
    webservers = []
    tot_items = tot_good = tot_bad = tot_waiting = 0
    for row in qs_all.values_list('source_id').distinct().order_by('source_id'):
        srv_id = int(row[0])
        srv_name = models.UriSource.objects.get(pk=srv_id)
        qs_webserver = qs_all.filter(source_id=srv_id)
        items = qs_webserver.count() ; tot_items += items
        good = qs_webserver.filter(Q_OK).count(); tot_good += good
        bad = qs_webserver.filter(Q_BAD).count(); tot_bad += bad
        waiting = items - good - bad; tot_waiting += waiting
        webservers.append({'name': srv_name,
                           'srv_id': srv_id,
                           'count' :items,
                           'waiting': waiting,
                           'good': good,
                           'bad': bad,
                           'ratio': s_calc_ratio_bad(good, bad),
                           })

    return render_to_response("plug_uris/stats_by_request.html",
                              {
                                  'request': request,
                                  'mime_results': mime_results,
                                  'err_by_reasons': err_by_reasons,
                                  'webservers': webservers,
                                  'webservers_summary': {
                                      'count': tot_items,
                                      'waiting': tot_waiting,
                                      'good': tot_good,
                                      'bad': tot_bad,},
                              })


def stats_by_uri(request, order_by=''):
    """
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
    """
    p_order_by = 'name_or_ip'
    request.session['sortkey'] = p_order_by

    qs_all = models.ReqUri.objects.filter(Q_OBJECT)

    uri_sources = []
    web_servers = models.UriSource.objects.values_list('name_or_ip', 'pk'
                                                       ).order_by('name_or_ip')
    for name, source_id in web_servers:
        qs_web_server = qs_all.filter(source_id=source_id)
        img_count = qs_web_server.count()
        if not img_count:
            continue # only display if request if any images
        good = qs_web_server.filter(Q_OK).count()
        bad = qs_web_server.filter(Q_BAD).count()
        waiting = img_count - good - bad
        uri_sources.append({'name': name,
                            'id':source_id,
                            'waiting': waiting,
                            'good': good,
                            'bad': bad,
                            'ratio': s_calc_ratio_bad(good, bad)})

    return render_to_response("plug_uris/stats_uri_source.html", {
        "uri_sources":uri_sources,
        "summary": uri_summary(),})


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









def uri_bad_by_req_mime(request, req_id, mime_type):
    s = urllib.unquote(mime_type)
    print 'mime param', mime_type
    print 'mime used', s
    request.session['req_filter'] = {'key': 'mime_type',
                                     'value': s,
                                     'req_id': req_id,
                                     'filter_label':'mime-type',
                                     }
    return uri_bad_by_request(request)


def uri_bad_by_req_err(request, req_id, err_code):
    request.session['req_filter'] = {'key': 'err_code',
                                     'value': err_code,
                                     'req_id': req_id,
                                     'filter_label':'error',
                                     }
    return uri_bad_by_request(request)


def uri_bad_by_server(request, req_id, webserver_id):
    request.session['req_filter'] = {'key': 'source_id',
                                     'value': webserver_id,
                                     'req_id': req_id,
                                     'filter_label': 'webserver',
                                     }
    return uri_bad_by_request(request)


def uri_bad_by_request(request, offset=0):
    offset = int(offset)
    sel = request.session['req_filter']
    req = models.Request.objects.get(pk=sel['req_id'])
    q_selection = Q((sel['key'], sel['value']), req=sel['req_id'])
    #
    #
    #
    qs = models.ReqUri.objects.filter(q_selection, Q_OBJECT, Q_BAD)
    problems = []
    for requri in qs[ offset : offset+BAD_BY_REQ_PG_SIZE ]:
        uri =models.Uri.objects.get(pk=requri.uri_id)
        problems.append({'url': uri.url,
                         'uri_id': uri.pk,
                         'status': models.URI_STATES[requri.status],
                         'errname': models.URI_ERR_CODES[requri.err_code],
                         'err_msg': uri.err_msg,
                         })

    item_count = qs.count()
    pages = item_count / BAD_BY_REQ_PG_SIZE + 1
    return render_to_response("plug_uris/bad_by_request.html",
                              {
                                  'request': request,
                                  'req': req,
                                  'filter_label': sel['filter_label'],
                                  #'mime_type': mime_type,
                                  'problems': problems,

                                  'prev': max(offset - BAD_BY_REQ_PG_SIZE, 0),
                                  'next': offset + BAD_BY_REQ_PG_SIZE,
                                  'last': max(0, item_count - BAD_BY_REQ_PG_SIZE),
                                  'pages': pages,
                                  'item_count': item_count,
                              })



def rescedule(request):
    sel = request.session['req_filter']
    req = models.Request.objects.get(pk=sel['req_id'])
    q_selection = Q((sel['key'], sel['value']), req=sel['req_id'])
    qs = models.ReqUri.objects.filter(q_selection, Q_OBJECT, Q_BAD)
    item_count = 0
    for requri in qs:
        uri =models.Uri.objects.get(pk=requri.uri_id)
        uri.status = models.URIS_CREATED
        uri.mime_type = ''
        uri.err_code = models.URIE_NO_ERROR
        uri.err_msg = ''
        uri.save()
        requri.mime_type = uri.mime_type
        requri.err_code = uri.err_code
        requri.status = uri.status
        requri.save()
        item_count += 1

    return render_to_response("plug_uris/bad_resceduled.html",
                              {
                                  'request': request,
                                  'req': req,
                                  'filter_label': sel['filter_label'],
                                  'item_count': item_count,
                              })




def try_again_uri(request, uri_id):
    pass


def uri_summary():
    imgs_all =  models.ReqUri.objects.filter(item_type=models.URIT_OBJECT).count()
    imgs_ok = models.ReqUri.objects.filter(item_type=models.URIT_OBJECT,
                                           status=models.URIS_COMPLETED,
                                           ).count()
    imgs_bad = models.ReqUri.objects.filter(item_type=models.URIT_OBJECT
                                            ).exclude(err_code=models.URIE_NO_ERROR
                                                      ).count()
    return {"imgs_ok": imgs_ok,
            "imgs_waiting": imgs_all - imgs_ok - imgs_bad,
            "imgs_bad": imgs_bad}




def index(request):
    uris =  models.Uri.objects.filter(status=models.URIS_CREATED,
                                      pid=0).order_by('-uri_source')

    return render_to_response('plug_uris/index.html', {'uri':uris[0]})





#
# Util funcs
#

def s_calc_ratio_bad(good, bad):
    s = '%0.2f' % (100 - calc_ratio(bad, good))
    return s

def s_calc_ratio(part, whole):
    s = '%0.2f' % calc_ratio(part, whole)
    return s

def calc_ratio(part, whole):
    if not whole:
        # avoid divide by zero
        return 100
    f =  (part/float(whole)) * 100
    return f


