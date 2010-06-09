from local_settings import *

from django.core import exceptions

# Verify we have all (important) local_settings


try:
    DATABASES
except:
    # old style single database
    try:
        DATABASE_ENGINE
    except:
        raise exceptions.ImproperlyConfigured('Missing setting DATABASE_ENGINE - see local_settings_sample.py')


    try:
        DATABASE_NAME
    except:
        raise exceptions.ImproperlyConfigured('Missing setting DATABASE_NAME - see local_settings_sample.py')


    try:
        DATABASE_USER
    except:
        raise exceptions.ImproperlyConfigured('Missing setting DATABASE_USER - see local_settings_sample.py')


    try:
        DATABASE_PASSWORD
    except:
        raise exceptions.ImproperlyConfigured('Missing setting DATABASE_PASSWORD - see local_settings_sample.py')


    try:
        DATABASE_HOST
    except:
        raise exceptions.ImproperlyConfigured('Missing setting DATABASE_HOST - see local_settings_sample.py')


    try:
        DATABASE_PORT
    except:
        raise exceptions.ImproperlyConfigured('Missing setting DATABASE_PORT - see local_settings_sample.py')


try:
    SECRET_KEY
except:
    raise exceptions.ImproperlyConfigured('Missing setting SECRET_KEY - see local_settings_sample.py')


try:
    SIP_LOG_FILE
except:
    raise exceptions.ImproperlyConfigured('Missing setting SIP_LOG_FILE - see local_settings_sample.py')

try:
    SIP_OBJ_FILES
except:
    raise exceptions.ImproperlyConfigured('Missing setting SIP_OBJ_FILES - see local_settings_sample.py')



#
# DummyIngestion settings
#  Until integration with Repox I use the module dummy_ingester this module
#  is pointed to a path, where it reads and parses all xml files found.
#
try:
    IMPORT_SCAN_TREE
except:
    raise exceptions.ImproperlyConfigured('Missing setting IMPORT_SCAN_TREE - see local_settings_sample.py')

try:
    TREE_IS_INGESTION_SVN
except:
    TREE_IS_INGESTION_SVN = True
    print 'Using default value for TREE_IS_INGESTION_SVN =', TREE_IS_INGESTION_SVN

try:
    OLD_STYLE_IMAGE_NAMES
except:
    OLD_STYLE_IMAGE_NAMES = False
    print 'Using default value for OLD_STYLE_IMAGE_NAMES =', OLD_STYLE_IMAGE_NAMES




#
#  Optional settings, if not given default is used
#
try:
    THREADING_PLUGINS
except:
    THREADING_PLUGINS = True
    print 'Using default value for THREADING_PLUGINS =', THREADING_PLUGINS


try:
    TASK_PROGRESS_INTERVALL
except:
    TASK_PROGRESS_INTERVALL = 15
    print 'Using default value for TASK_PROGRESS_INTERVALL =', TASK_PROGRESS_INTERVALL

try:
    PROCESS_SLEEP_TIME
except:
    PROCESS_SLEEP_TIME = 60
    print 'Using default value for PROCESS_SLEEP_TIME =', PROCESS_SLEEP_TIME


try:
    SIPMANAGER_DBG_LVL
except:
    SIPMANAGER_DBG_LVL = 7
    print 'Using default value for SIPMANAGER_DBG_LVL =', SIPMANAGER_DBG_LVL


try:
    MAX_LOAD_NEW_TASKS
except:
    MAX_LOAD_NEW_TASKS = (1.7, 1.8,  1.9)
    print 'Using default value for MAX_LOAD_NEW_TASKS = (%0.1f, %0.1f, %0.1f)' % MAX_LOAD_NEW_TASKS
try:
    float(MAX_LOAD_NEW_TASKS)
    MAX_LOAD_NEW_TASKS = (MAX_LOAD_NEW_TASKS,
                          MAX_LOAD_NEW_TASKS,
                          MAX_LOAD_NEW_TASKS)
except:
    try:
        a,b,c = MAX_LOAD_NEW_TASKS
        float(a)
        float(b)
        float(c)
    except:
        raise exceptions.ImproperlyConfigured('MAX_LOAD_NEW_TASKS must be a float or a tupple of three floats - see local_settings_sample.py')


try:
    MAX_LOAD_RUNNING_TASKS
except:
    MAX_LOAD_RUNNING_TASKS = (3.0, 3.2, 4.0)
    print 'Using default value for MAX_LOAD_RUNNING_TASKS = (%0.1f, %0.1f, %0.1f)' % MAX_LOAD_RUNNING_TASKS
try:
    float(MAX_LOAD_RUNNING_TASKS)
    MAX_LOAD_RUNNING_TASKS = (MAX_LOAD_RUNNING_TASKS,
                              MAX_LOAD_RUNNING_TASKS,
                              MAX_LOAD_RUNNING_TASKS)
except:
    try:
        a,b,c = MAX_LOAD_RUNNING_TASKS
        float(a)
        float(b)
        float(c)
    except:
        raise exceptions.ImproperlyConfigured('MAX_LOAD_RUNNING_TASKS must be a float or a tupple of three floats - see local_settings_sample.py')


#
#   Debug settings
#
try:
    DEBUG
except:
    DEBUG = False
    print 'Using default value for DEBUG =', DEBUG

try:
    PLUGIN_FILTER
except:
    PLUGIN_FILTER = []








# Django settings for sip_web project.

#local_settings DEBUG = True
TEMPLATE_DEBUG = True

ADMINS = (
    # ('Your Name', 'your_email@domain.com'),
)

MANAGERS = ADMINS

#local_settings DATABASE_ENGINE = ''           # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
#local_settings DATABASE_NAME = ''             # Or path to database file if using sqlite3.
#local_settings DATABASE_USER = ''             # Not used with sqlite3.
#local_settings DATABASE_PASSWORD = ''         # Not used with sqlite3.
#local_settings DATABASE_HOST = ''             # Set to empty string for localhost. Not used with sqlite3.
#local_settings DATABASE_PORT = ''             # Set to empty string for default. Not used with sqlite3.

# Local time zone for this installation. Choices can be found here:
# http://en.wikipedia.org/wiki/List_of_tz_zones_by_name
# although not all choices may be available on all operating systems.
# If running in a Windows environment this must be set to the same as your
# system time zone.
TIME_ZONE = 'Europe/Amsterdam'

DATETIME_FORMAT = 'y-m-d H:i'

# Language code for this installation. All choices can be found here:
# http://www.i18nguy.com/unicode/language-identifiers.html
LANGUAGE_CODE = 'en-uk'

SITE_ID = 1

# If you set this to False, Django will make some optimizations so as not
# to load the internationalization machinery.
USE_I18N = True

# Absolute path to the directory that holds media.
# Example: "/home/media/media.lawrence.com/"
MEDIA_ROOT = '%s/media' % proj_root
print '*** MEDIA_ROOT', MEDIA_ROOT
# URL that handles the media served from MEDIA_ROOT. Make sure to use a
# trailing slash if there is a path component (optional in other cases).
# Examples: "http://media.lawrence.com", "http://example.com/media/"
MEDIA_URL = '/static_media/'
print '*** MEDIA_URL', MEDIA_URL

# URL prefix for admin media -- CSS, JavaScript and images. Make sure to use a
# trailing slash.
# Examples: "http://foo.com/media/", "/media/".
ADMIN_MEDIA_PREFIX = '/media/'

# Make this unique, and don't share it with anybody.
#local_settings SECRET_KEY = '%yh05u$3*@-2s*-qyney-()d4r7n_+*99j8_*aza_jwbuxv6)%'

# List of callables that know how to import templates from various sources.
TEMPLATE_LOADERS = (
    'django.template.loaders.filesystem.load_template_source',
    'django.template.loaders.app_directories.load_template_source',
#     'django.template.loaders.eggs.load_template_source',
)

MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    #'django.contrib.csrf.middleware.CsrfMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
)



ROOT_URLCONF = 'sip_manager.urls'

TEMPLATE_DIRS = (
    # Put strings here, like "/home/html/django_templates" or "C:/www/django/templates".
    # Always use forward slashes, even on Windows.
    # Don't forget to use absolute paths, not relative paths.
    '%s/templates' % proj_root,
    '/Library/Python/2.6/site-packages/djblets/datagrid/templates',
)

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.admin',
    'django.contrib.databrowse',
    #'dajaxice',
    #'dajax',
    #'djblets.datagrid',
    #'djblets.util',

    # base sipmanager modules
    'apps.sipmanager',
    'apps.dummy_ingester',
    'apps.base_item',

    # plugins
    'apps.plug_uris',

    'apps.log',
    'apps.statistics',
)

DAJAXICE_FUNCTIONS = (
	'examples.ajax.randomize',
)
