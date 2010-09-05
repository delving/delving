import codecs
import os

from django.conf import settings
from django.template import Context, loader
from django.utils import translation


from utils import global_environ

class StandaloneSubmit(object):

    def __init__(self):
        this_dir = os.path.split(__file__)[0]
        settings.TEMPLATE_DIRS = (os.path.join(this_dir, 'templates_submit'),
                                  os.path.join(this_dir, 'templates'))


    def run(self):
        for template_fname in (
            #'pages/about_us.html',
            'pages/newcontent.html',
            ):
            self.generate_page(template_fname)



    def generate_page(self, template_fname):
        print template_fname,
        for lang, lang_name in settings.LANGUAGES:
            print lang,
            # Activate language
            translation.activate(lang)

            # render page
            template, origin = loader.find_template(template_fname)
            c = Context()
            c.update(global_environ(None))
            html = template.render(c)

            # save rendered page to file
            base_name = os.path.splitext(os.path.split(template_fname)[1])[0]
            fname = '%s_%s.html' % (base_name, lang)
            full_path = os.path.join(settings.SUBMIT_PATH, fname)
            f = codecs.open(full_path, 'w', 'utf-8')
            f.write(html)
            f.close()
        print 'done!'
