
# Copy to local_settings.py

DEBUG = True

import os.path
proj_root = os.path.normpath(os.path.dirname(__file__))



DATABASE_ENGINE = 'sqlite3'   # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
DATABASE_NAME = '%s/db.sqlite3' % proj_root # Or path to database file if using sqlite3.
DATABASE_USER = ''     # Not used with sqlite3.
DATABASE_PASSWORD = '' # Not used with sqlite3.
DATABASE_HOST = ''     # Set to empty string for localhost. Not used with sqlite3.

SECRET_KEY = '* long and secret string used as session key *'


# This url will be inserted in templates to point to static media
MEDIA_URL = '/portal/'

DELIVER_STATIC_MEDIA = True
# If DELIVER_STATIC_MEDIA is True, all references to MEDIA_URL above
# are pointed to the MEDIA_ROOT below
MEDIA_ROOT = '/Users/jaclu/proj/europeana/trunk/portal-full/src/main/webapp'
