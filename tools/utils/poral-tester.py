"""
Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com) 

 20100212   Initial releass

 Simple tool to check that the static pages are responding in all languages.
 Not much intelligence, only aborts on http errors, if actual content is ok
 is not checked in any way.

 Mostly usefull during updates to see that all the pages are still responding
 this script will abort as soon as a page request fails.

===============================================================================
"""
import sys
import time
import subprocess



# Change if you actually care to read the recieved doc...
DESTINATION='/dev/null'


SERVER='europeana.eu'
BASE_PATH='portal'


URLS_TO_CHECK = (
    'http://%s/%s/communities.html' % (SERVER, BASE_PATH),
    'http://%s/%s/partners.html' % (SERVER, BASE_PATH),
    'http://%s/%s/year-grid.html' % (SERVER, BASE_PATH),
    'http://%s/%s/thought-lab.html' % (SERVER, BASE_PATH),
    'http://%s/%s/aboutus.html' % (SERVER, BASE_PATH),
    'http://%s/%s/using-europeana.html' % (SERVER, BASE_PATH),
    'http://%s/%s/accessibility.html' % (SERVER, BASE_PATH),
    'http://%s/%s/sitemap.html'  % (SERVER, BASE_PATH),
    'http://%s/%s/termsofservice.html'  % (SERVER, BASE_PATH),
    'http://%s/%s/privacy.html'  % (SERVER, BASE_PATH),
    'http://%s/%s/languagepolicy.html'  % (SERVER, BASE_PATH),
    'http://%s/%s/contact.html'  % (SERVER, BASE_PATH),
    )

#
# We only use first part of each line, for quicker cut & paste we use 
# same syntax as in templates
#
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

foutput=open('/dev/null','w')

def check_one(url, lang='en'):
    if lang:
        url2 = '%s?lang=%s' % (url, lang)
    else:
        url2 = url
    excode = subprocess.call(['wget', '%s' % url2, '--output-document=%s' % DESTINATION],
                             stderr=foutput # comment this line out if you want
                                            # to see wget output
                             )
    if excode:
        print
        print '***   Aborting check_one() due to error!'
        sys.exit(1)
    else:
        print 'ok', url,
        if lang:
            print lang
        else:
            print
    time.sleep(0.2)
    return True


def loop_urls():
    i = 0
    while True:
        for url in URLS_TO_CHECK:
            if True: # Do languages
                for lang, long_lang in LANGUAGES:
                    check_one(url,lang)
            else:
                check_one(url,lang='')
        i += 1
        print '====================  iteration ', i
        time.sleep(2)


loop_urls()
