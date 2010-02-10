# Django settings for europeana project.

from local_settings import *

if proj_root[-1] == '/':
    # to make it consistent, remove trailing space
    proj_root = proj_root[:-1]

TEMPLATE_DEBUG = DEBUG

ADMINS = (
    #('Jacob Lundqvist', 'jacob.lundqvist@gmail.com'),
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
LANGUAGE_CODE = 'en'

SITE_ID = 1

# If you set this to False, Django will make some optimizations so as not
# to load the internationalization machinery.
USE_I18N = True

# Absolute path to the directory that holds media.
# Example: "/home/media/media.lawrence.com/"
#local_settings MEDIA_ROOT = '/full/path/to/static/media'

# URL that handles the media served from MEDIA_ROOT. Make sure to use a
# trailing slash if there is a path component (optional in other cases).
# Examples: "http://media.lawrence.com", "http://example.com/media/"
#local_settings MEDIA_URL = '/static_media/'


# URL prefix for admin media -- CSS, JavaScript and images. Make sure to use a
# trailing slash.
# Examples: "http://foo.com/media/", "/media/".
ADMIN_MEDIA_PREFIX = '/admin_media/'

# Make this unique, and don't share it with anybody.
#local_settings SECRET_KEY = '3h&^gpvh*pn)r$$!)7g+8s^4!4jp6k17@#3gihk+vr8i4zty_h'


TEMPLATE_CONTEXT_PROCESSORS = (
    "django.core.context_processors.auth",
    "django.core.context_processors.debug",
    "django.core.context_processors.i18n",
    "django.core.context_processors.media",
    "django.core.context_processors.request",
    "apps.multi_lingo.utils.global_environ",
    )

MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.locale.LocaleMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
)

LANGUAGE_COOKIE_NAME = 'django_language'

ROOT_URLCONF = 'urls'

TEMPLATE_DIRS = (
    # Put strings here, like "/home/html/django_templates" or "C:/www/django/templates".
    # Always use forward slashes, even on Windows.
    # Don't forget to use absolute paths, not relative paths.
    '%s/apps/multi_lingo/templates' % proj_root,
)

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.admin',
    'django.contrib.admindocs',
    'django.contrib.contenttypes',
    'django.contrib.markup',
    'django.contrib.sessions',
    'django.contrib.sites',
    'rosetta',
    'apps.multi_lingo',
)




#
#=====================   Europeana languages settings   =======================
#

#This one is sorted in display order
LANGUAGES = (
    ('bg', '&#x0411;&#x044a;&#x043b;&#x0433;&#x0430;&#x0440;&#x0441;&#x043a;&#x0438; (bul)'),
    ('ca', 'Catal&#224; (ca)'),
    ('cs', '&#268;e&#353;tina (cze/cse)'),
    ('da', 'Dansk (dan)'),
    ('de', 'Deutsch (deu)'),
    ('el', '&#917;&#955;&#955;&#951;&#957;&#953;&#954;&#940; (ell/gre)'),
    ('en', 'English (eng)'),
    ('es', 'Espa&#241;ol (esp)'),
    ('et', 'Eesti (est)'),
    ('fi', 'Suomi (fin)'),
    ('fr', 'Fran&#231;ais (fre)'),
    ('ga', 'Irish (gle)'),
    ('hu', 'Magyar (hun)'),
    ('is', '&#205;slenska (ice)'),
    ('it', 'Italiano (ita)'),
    ('lt', 'Lietuvi&#371; (lit)'),
    ('lv', 'Latvie&#353;u (lav)'),
    ('mt', 'Malti (mlt)'),
    ('nl', 'Nederlands (dut)'),
    ('no', 'Norsk (nor)'),
    ('pl', 'Polski (pol)'),
    ('pt', 'Portugu&#234;s (por)'),
    ('ro', 'Rom&#226;n&#259; (rom'),
    ('sk', 'Slovensk&#253; (slo)'),
    ('sl', 'Sloven&#353;&#269;ina (slv)'),
    ('sv', 'Svenska (sve/swe)'),
)

# just the lang keys for quick lookups
LANGUAGES_DICT = {}
for _k, _lbl in LANGUAGES:
    LANGUAGES_DICT[_k] = _lbl
# clean up of temp vars
del _k, _lbl




#
#======================   Rosetta specifics   =================================
#

# Number of messages to display per page.
ROSETTA_MESSAGES_PER_PAGE = 10

# Enable Google translation suggestions
ROSETTA_ENABLE_TRANSLATION_SUGGESTIONS = True

"""
When running WSGI daemon mode, using mod_wsgi 2.0c5 or later, this setting
controls whether the contents of the gettext catalog files should be
automatically reloaded by the WSGI processes each time they are modified.

Notes:

 * The WSGI daemon process must have write permissions on the WSGI script file
   (as defined by the WSGIScriptAlias directive.)
 * WSGIScriptReloading must be set to On (it is by default)
 * For performance reasons, this setting should be disabled in production environments
 * When a common rosetta installation is shared among different Django projects,
   each one running in its own distinct WSGI virtual host, you can activate
   auto-reloading in individual projects by enabling this setting in the project's
   own configuration file, i.e. in the project's settings.py

Refs:

 * http://code.google.com/p/modwsgi/wiki/ReloadingSourceCode
 * http://code.google.com/p/modwsgi/wiki/ConfigurationDirectives#WSGIReloadMechanism

"""
ROSETTA_WSGI_AUTO_RELOAD = True
