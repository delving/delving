
# Copy to local_settings.py

DEBUG = True

import os.path
proj_root = os.path.normpath(os.path.dirname(__file__))


DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3', # Add 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': '%s/db.sqlite3' % proj_root,    # Or path to database file if using sqlite3.
        'USER': '',                      # Not used with sqlite3.
        'PASSWORD': '',                  # Not used with sqlite3.
        'HOST': '',                      # Set to empty string for localhost. Not used with sqlite3.
        'PORT': '',                      # Set to empty string for default. Not used with sqlite3.
    }
}


SECRET_KEY = '* long and secret string used as session key *'


# This url will be inserted in templates to point to static media
MEDIA_URL = '/portal/'

DELIVER_STATIC_MEDIA = True
# If DELIVER_STATIC_MEDIA is True, all references to MEDIA_URL above
# are pointed to the MEDIA_ROOT below
MEDIA_ROOT = '/Users/jaclu/proj/europeana/trunk/portal-full/src/main/webapp'
