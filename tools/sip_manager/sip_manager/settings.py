from local_settings import *

from django.core import exceptions

# Verify we have all (important) local_settings

try:
    IMPORT_SCAN_TREE
except:
    raise exceptions.ImproperlyConfigured('Missing setting IMPORT_SCAN_TREE')

try:
    SIP_LOG_FILE
except:
    raise exceptions.ImproperlyConfigured('Missing setting SIP_LOG_FILE (logfile)')

try:
    SIP_OBJ_FILES
except:
    raise exceptions.ImproperlyConfigured('Missing setting SIP_OBJ_FILES (directory)')

try:
    THREADING_PLUGINS
except:
    raise exceptions.ImproperlyConfigured('Missing setting THREADING_PLUGINS (boolean)')

# Django settings for sip_web project.

#local_settings DEBUG = True
TEMPLATE_DEBUG = DEBUG

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

# Language code for this installation. All choices can be found here:
# http://www.i18nguy.com/unicode/language-identifiers.html
LANGUAGE_CODE = 'en-uk'

SITE_ID = 1

# If you set this to False, Django will make some optimizations so as not
# to load the internationalization machinery.
USE_I18N = True

# Absolute path to the directory that holds media.
# Example: "/home/media/media.lawrence.com/"
MEDIA_ROOT = ''

# URL that handles the media served from MEDIA_ROOT. Make sure to use a
# trailing slash if there is a path component (optional in other cases).
# Examples: "http://media.lawrence.com", "http://example.com/media/"
MEDIA_URL = ''

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
    'django.contrib.auth.middleware.AuthenticationMiddleware',
)

ROOT_URLCONF = 'sip_manager.urls'

TEMPLATE_DIRS = (
    # Put strings here, like "/home/html/django_templates" or "C:/www/django/templates".
    # Always use forward slashes, even on Windows.
    # Don't forget to use absolute paths, not relative paths.
)

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.admin',

    'django.contrib.databrowse',

    'apps.base_item',
    'apps.dummy_ingester',
    'apps.plug_uris',
    'apps.process_monitor',

    'apps.statistics',
)

