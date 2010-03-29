# Create your views here.

from django.http import HttpResponse
from django.conf import settings


def logfile(request):
    fp = open(settings.SIP_LOG_FILE)
    lst = fp.readlines()
    fp.close()
    lst = lst[:50] # trim logfile
    s = ''.join(lst).replace('\n','<br>').replace('\t','&nbsp;&nbsp;&nbsp;')
    return HttpResponse("Content of logfile<br>%s" % s)
