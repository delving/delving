#
#=========================   Fix path   ====================================
#
import os.path

#Yes the extra normpath _is_ needed, otherwise proj_root becomes invalid
#when settings are called from apps...
proj_root = os.path.normpath(os.path.dirname(__file__))



DATABASE_ENGINE = 'postgresql_psycopg2'    # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
DATABASE_NAME = 'sipmanager'             # Or path to database file if using sqlite3.
DATABASE_USER = 'sipmanager'             # Not used with sqlite3.
DATABASE_PASSWORD = 'replace with a password'         # Not used with sqlite3.
DATABASE_HOST = ''             # Set to empty string for localhost. Not used with sqlite3.
DATABASE_PORT = ''             # Set to empty string for default. Not used with sqlite3.



SECRET_KEY = '%yh05 long and random string uxv6)%'



# All output that is printed to stdout is also logged to this file
SIP_LOG_FILE = '/tmp/sip-manager.log'


# Where all downloaded Europeana:objects and generated images are stored
# depending on OLD_STYLE_IMAGE_NAMES difernet strategies will be used
# for creating further subdirs
SIP_OBJ_FILES = '/Volumes/JacBook/SIP_object_files'



#==============================================================================
#
# DummyIngestion settings
#  Until integration with Repox I use the module dummy_ingester this module
#  is pointed to a path, where it reads and parses all xml files found.

# This defines the starting point, where to look for .xml files
IMPORT_SCAN_TREE = '/Users/jaclu/tmp/ingestion'

# If this is True (default) the IMPORT_SCAN_TREE is asumed to be structured
# in a similar way as the svn from the Ingestion Team. In other words, within this
# tree, files with extention .xml found in a directory named output_xml will be
# parsed.
# If this is False, any .xml file found will be parsed
# if not set the default will be printed and used
#TREE_IS_INGESTION_SVN =


# Mostly a temporary setting, if this is True, the pre version 0.7 version
# of hash-generation and directory naming is used
# if False (please dont set this one unless you have some serious reason...)
# "New" style is used, this means that below SIP_OBJ_FILES we have three subdirs
#  each files sha256 has is uses as follows first-two/next-two/full-hash
#  original/01/23/012393C4FFA32E49D481A69E0F5B557B3A650343AD48083A6181A61C0DA3C895
#  FULL_DOC/01/23/012393C4FFA32E49D481A69E0F5B557B3A650343AD48083A6181A61C0DA3C895.jpg
#  BRIEF_DOC/01/23/012393C4FFA32E49D481A69E0F5B557B3A650343AD48083A6181A61C0DA3C895.jpg
# if not set the default will be printed and used
#OLD_STYLE_IMAGE_NAMES =







#==============================================================================
#
# Optional settings - What is given here is the default, if you dont plan
#                     to change it, not needed to included in your local_settings.py
#                     If a given optional is not specified, the default will be used
#                     and printed out

# If we allow processor to run in multithreaded
# If set to False, all plugins are run sequentaly in a single-threded way
# if not set the default will be printed and used
# THREADING_PLUGINS = False/True




# how often plugins should report what is happening (seconds)
# if not set the default will be printed and used
#TASK_PROGRESS_INTERVALL =


# How often we check for new tasks (seconds)
# if not set the default will be printed and used
#PROCESS_SLEEP_TIME =


# Limits amount of logging output (1-9)
# i normaly use 7 any higher value will be extreamly talkative
# if not set the default will be printed and used
#SIPMANAGER_DBG_LVL =


# If system load is over this, new tasks wont be started
# either a single float, or (prefered three values for 1, 5 and 15 min load)
# if not set the default will be printed and used
#MAX_LOAD_NEW_TASKS =

# If system load is over this, tasks will be terminated
# either a single float, or (prefered three values for 1, 5 and 15 min load)
# if not set the default will be printed and used
#MAX_LOAD_RUNNING_TASKS =





#==============================================================================
#
# Debug related settings - should never be used under normal operation!
#

# This controlls if django should be run in debug mode, it gives more detailed
# error mesages when testing webpages...
# Important you really shouldnt enable this unless you know what you are
# doing, the dummyingester will eat gigantous amounts of memory...
DEBUG = False



# If given only the named plugins will be run
#  init plugins are excluded from this check - they are always run!
#  if not set the default will be printed and used
#PLUGIN_FILTER = ['UriCreate',]
