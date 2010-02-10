#from local_settings import *
DEBUG = True

import os.path
proj_root = os.path.normpath(os.path.dirname(__file__))

DATABASE_ENGINE = 'mysql'           # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
DATABASE_NAME = 'rhine_cms'              # Or path to database file if using sqlite3.
DATABASE_USER = 'django1'             # Not used with sqlite3.
DATABASE_PASSWORD = 'lkjyijk'         # Not used with sqlite3.
DATABASE_HOST = '127.0.0.1'             # Set to empty string for localhost. Not used with sqlite3.

SECRET_KEY = 'fgkj6%^jghkldr@$^*(OjlfjkGUJ45tkj5489dfjkh3i5jk88kjlfg8'


# This url will be inserted in templates to point to static media
MEDIA_URL = '/portal/'

DELIVER_STATIC_MEDIA = True
# If DELIVER_STATIC_MEDIA is True, static content will be delivered from this
# point in the filesystem. If false, django will not handle static media
MEDIA_ROOT = '/Users/jaclu/proj/europeana/trunk/portal-full/src/main/webapp'
