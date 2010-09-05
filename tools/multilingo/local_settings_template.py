
# Copy to local_settings.py

DEBUG = False

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


# Where the europeana templates can be found, the user running this webapp
# must have writepriv since we create softlinks here to local extra content
MEDIA_ROOT = '/Users/jaclu/proj/europeana/trunk/portal-full/src/main/webapp'


# sorted in display order
LANGUAGES = (
    ('ca', 'Catalan (ca)'),
    ('bg', 'Bulgarian (bul)'),
    ('cs', 'Czech (cze/cse)'),
    ('da', 'Dansk (dan)'),
    ('de', 'Deutsch (deu)'),
    ('el', 'Greek (ell/gre)'),
    ('en', 'English (eng)'),
    ('es', 'Espanol (esp)'),
    ('et', 'Eesti (est)'),
    ('fi', 'Suomi (fin)'),
    ('fr', 'Francais (fre)'),
    ('ga', 'Irish (gle)'),
    ('hu', 'Magyar (hun)'),
    ('is', 'Islenska (ice)'),
    ('it', 'Italiano (ita)'),
    ('lt', 'Lithuanian (lit)'),
    ('lv', 'Latvian (lav)'),
    ('mt', 'Malti (mlt)'),
    ('nl', 'Nederlands (dut)'),
    ('no', 'Norsk (nor)'),
    ('pl', 'Polski (pol)'),
    ('pt', 'Portuguese (por)'),
    ('ro', 'Romanian (rom'),
    ('sk', 'Slovkian (slo)'),
    ('sl', 'Slovenian (slv)'),
    ('sv', 'Svenska (sve/swe)'),
)

