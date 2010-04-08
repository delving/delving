
DEBUG = True



DATABASE_ENGINE = ''           # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
DATABASE_NAME = ''             # Or path to database file if using sqlite3.
DATABASE_USER = ''             # Not used with sqlite3.
DATABASE_PASSWORD = ''         # Not used with sqlite3.
DATABASE_HOST = ''             # Set to empty string for localhost. Not used with sqlite3.
DATABASE_PORT = ''             # Set to empty string for default. Not used with sqlite3.




SECRET_KEY = '%yh05 long and random string uxv6)%'




TREE_IS_INGESTION_SVN = True
#IMPORT_SCAN_TREE = '/Users/jaclu/proj/europeana/ingestion/trunk/sourcedata/xml'
IMPORT_SCAN_TREE = '/Users/jaclu/tmp/ingestion'


SIP_LOG_FILE = '/tmp/sip-manager.log'

# Where all downloaded Europeana:objects and generated images are stored
SIP_OBJ_FILES = '/Volumes/JacBook/SIP_object_files'


# how often plugins should report what is happening
#  this setting is optional 15 is default
TASK_PROGRESS_INTERVALL = 15

# How often we check for new tasks
#  this setting is optional 60 is default
#PROCESS_SLEEP_TIME = 60


#==============================================================================
#
# Debug related settings - should never be used under normal operation!
#

# If given only the named plugins will be run
#  init plugins are excluded from this check - they are always run!
#PLUGIN_FILTER = ['UriCreate',]
