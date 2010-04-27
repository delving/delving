
from django.shortcuts import render_to_response, get_object_or_404


import models




def show_monitor(request):
    procs = models.ProcessMonitoring.objects.order_by('time_created')
    return render_to_response("sipmanager/show_procs.html",
                              {
                                  'request': request,
                                  'procs': procs,
                              })
