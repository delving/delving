import os, sys

#
# 1) Rename to django.wsgi
# 2) replace with abs path to parrent dir
#
sys.path.append('/Users/jaclu/proj/europeana/multilingo')


os.environ['DJANGO_SETTINGS_MODULE'] = 'multilingo.settings'

import django.core.handlers.wsgi
application = django.core.handlers.wsgi.WSGIHandler()
