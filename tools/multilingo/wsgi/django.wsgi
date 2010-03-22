import os, sys

#
# 1) Rename to django.wsgi
# 2) replace with abs path to parrent dir
#
sys.path.append('/var/local/proj/multilingo')


os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

import django.core.handlers.wsgi
application = django.core.handlers.wsgi.WSGIHandler()
