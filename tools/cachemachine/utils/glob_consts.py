
REQUEST_UPLOAD_PATH = 'requests_new'



# Generic states
# ST_ = STate
ST_INITIALIZING = 0 # just created
ST_PENDING = 10 # waiting to be processed
ST_RETRIEVED = 15 # item has been retrieved
ST_PARSING = 20
ST_IDLE = 30
ST_COMPLETED = 90
ST_ABORTED = 94
ST_FAILED = 95

# Table specific states, allways > 99

# CSS  = CacheSourceState
CSS_RETRIEVING = 100

# RS_ = RequestState
RS_PRE_PARSING = 200
RS_IMG_CHECKS = 205
RS_INV_INP_FILE = 210

# IS_ = ItemState
IS_NO_RESPONSE = 300
IS_IDENTIFICATION = 310

# IT_ = ItemType
IT_UNKNOWN = 1001
IT_IMAGE = 1002
IT_PDF = 1003
IT_MOVIE = 1004
IT_AUDIO = 1005


CACHESOURCE_STATES = {
    ST_IDLE: 'idle',
    CSS_RETRIEVING: 'retrieving',

}


REQUEST_STATES = {
    ST_INITIALIZING: 'inializing',
    ST_PENDING: 'pending',
    RS_PRE_PARSING: 'pre parsing',
    ST_PARSING: 'parsing',  # Creating related image items
    RS_IMG_CHECKS: 'img_checks', # One or more images needs checking
    ST_COMPLETED: 'completed',  # everything done for the moment
    ST_ABORTED: 'aborted',
    ST_FAILED: 'failed', # failure in identification of item
    RS_INV_INP_FILE: 'invalid input file',
}


ITEM_STATES = {
    ST_INITIALIZING: 'inializing',
    ST_PENDING: 'pending',
    ST_RETRIEVED: 'retrieved',
    ST_COMPLETED: 'completed',
    ST_ABORTED: 'aborted',
    ST_FAILED: 'failed', # failure in identification of item
    IS_NO_RESPONSE: 'no response',
    IS_IDENTIFICATION: 'identification',
}


ITEM_TYPES = {
    IT_UNKNOWN: 'unknown',
    IT_IMAGE: 'image',
    IT_PDF: 'PDF',
    IT_MOVIE: 'movie',
    IT_AUDIO: 'audio',
}


# PMR_ = Process Monitoring Role
PMR_REQ_HANDLER = 1000

PROC_ROLES = {
    PMR_REQ_HANDLER: 'request handler',
}

PM_STATES = {
    ST_INITIALIZING: 'initialzing',
}



#
# Module locks (utils/proc_ctrl.py)
#
LCK_ITEM = 'CacheItem'
LCK_CACHESOURCE = 'CacheSource'
