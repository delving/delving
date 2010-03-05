


REQUEST_UPLOAD_PATH = 'requests_new'



# Generic states
# ST_ = STate
ST_INITIALIZING = 0 # just created
ST_PENDING = 10 # waiting to be processed
ST_PARSING = 20
ST_COMPLETED = 90
ST_ABORTED = 94
ST_FAILED = 95

# Table specific states, allways > 99

# RS_ = RequestState
RS_PRE_PARSING = 100
RS_INV_INP_FILE = 101

# IS_ = ItemState
IS_RETRIEVAL = 200
IS_NO_RESPONSE = 201
IS_IDENTIFICATION = 202

# IT_ = ItemType
IT_UNKNOWN = 230
IT_IMAGE = 231
IT_PDF = 232
IT_MOVIE = 233
IT_AUDIO = 234


REQUEST_STATES = {
    ST_INITIALIZING: 'inializing',
    ST_PENDING: 'pending',
    RS_PRE_PARSING: 'pre parsing',
    ST_PARSING: 'parsing',
    ST_COMPLETED: 'completed',
    ST_ABORTED: 'aborted',
    ST_FAILED: 'failed', # failure in identification of item
    RS_INV_INP_FILE: 'invalid input file',
}


ITEM_STATES = {
    ST_INITIALIZING: 'inializing',
    ST_PENDING: 'pending',
    ST_COMPLETED: 'completed',
    ST_ABORTED: 'aborted',
    ST_FAILED: 'failed', # failure in identification of item
    IS_RETRIEVAL: 'retrieveal',
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