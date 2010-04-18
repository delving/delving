#
#=========================   Fix path   ====================================
#
import os.path

#Yes the extra normpath _is_ needed, otherwise proj_root becomes invalid
#when settings are called from apps...
proj_root = os.path.normpath(os.path.dirname(__file__))



DATABASE_ENGINE = ''           # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
DATABASE_NAME = ''             # Or path to database file if using sqlite3.
DATABASE_USER = ''             # Not used with sqlite3.
DATABASE_PASSWORD = ''         # Not used with sqlite3.
DATABASE_HOST = ''             # Set to empty string for localhost. Not used with sqlite3.
DATABASE_PORT = ''             # Set to empty string for default. Not used with sqlite3.



SECRET_KEY = '%yh05 long and random string uxv6)%'

# This defines the starting point, where to look for .xml files
IMPORT_SCAN_TREE = '/Users/jaclu/tmp/ingestion'


# All output that is printed to stdout is also logged to this file
SIP_LOG_FILE = '/tmp/sip-manager.log'


# Where all downloaded Europeana:objects and generated images are stored
# depending on OLD_STYLE_IMAGE_NAMES difernet strategies will be used
# for creating further subdirs
SIP_OBJ_FILES = '/Volumes/JacBook/SIP_object_files'





#==============================================================================
#
# Optional settings - What is given here is the default, if you dont plan
#                     to change it, not needed to included in your local_settings.py
#

# This controlls if django should be run in debug mode, it gives more detailed
# error mesages when testing webpages...
DEBUG = False


# If we allow processor to run in multithreaded
# If set to False, all plugins are run sequentaly in a single-threded way
THREADING_PLUGINS = True


# If this is True (default) the IMPORT_SCAN_TREE is asumed to be structured
# in a similar way as the svn from the Ingestion Team. In other words, within this
# tree, files with extention .xml found in a directory named output_xml will be
# parsed.
# If this is False, any .xml file found will be parsed
TREE_IS_INGESTION_SVN = True


# Mostly a temporary setting, if this is True, the pre version 0.7 version
# of hash-generation and directory naming is used
# if False (please dont set this one unless you have some serious reason...)
# "New" style is used, this means that below SIP_OBJ_FILES we have three subdirs
#  each files sha256 has is uses as follows first-two/next-two/full-hash
#  original/01/23/012393C4FFA32E49D481A69E0F5B557B3A650343AD48083A6181A61C0DA3C895
#  FULL_DOC/01/23/012393C4FFA32E49D481A69E0F5B557B3A650343AD48083A6181A61C0DA3C895.jpg
#  BRIEF_DOC/01/23/012393C4FFA32E49D481A69E0F5B557B3A650343AD48083A6181A61C0DA3C895.jpg
OLD_STYLE_IMAGE_NAMES = False


# how often plugins should report what is happening
TASK_PROGRESS_INTERVALL = 15


# How often we check for new tasks
PROCESS_SLEEP_TIME = 60


# Limits amount of logging output
SIP_PROCESS_DBG_LVL = 7


# If system load is over this, new tasks wont be started
# either a single float, or (prefered three values for 1, 5 and 15 min load)
MAX_LOAD_NEW_TASKS = (2.0, 1.8,  1.7)

# If system load is over this, tasks will be terminated
# either a single float, or (prefered three values for 1, 5 and 15 min load)
MAX_LOAD_RUNNING_TASKS = (2.5, 2.2, 2.0)



#==============================================================================
#
# Debug related settings - should never be used under normal operation!
#

# If given only the named plugins will be run
#  init plugins are excluded from this check - they are always run!
#PLUGIN_FILTER = ['UriCreate',]
