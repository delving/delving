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

===============================================================================

The purpose of this packet is to administer the translations of europeana.eu

Currently we keep the site in 28 languages, and this has made the translation
process rather complicated.

The workflow with this tool is that the webmaster works with html templates
and then just tags his english texts as translation keys. Then translators are
notified. When a language is completed the webmaster is notified, when all
changes are translated the new pages are copied to the production webserver.

To avoid spamming a given translation team (one or more people) can only be in
two states "translations pending" and "all done". Only when a template change moves a language out of the "all done" state,
translators for that language are notified of pending translations.

This tool also handles the properties files - that is separate translations used
internally in the europeana.eu server. From a translator perspective there is
no difference if the translation key comes from a template or from the properties file.

For the webmaster the difference is that properties are handled under /admin through the
built in admin environment of django and static pages are handled through templates
in the multi_lingo/templates/pages

For translations we use django-rosetta (http://code.google.com/p/django-rosetta/)
So translators should work in the /rosetta hierarchy, but normally they would get
a mail notification on pending translates, and given the correct url in the notification,
so shouldn't need to care about the url to use.

This tool is intended to be run from an apache server, using the WSGI wrapper.

Dependencies

1. A resent django - 1.1 or higer should work
2. django-rosetta - we use 0.5.2 for the moment (included in this code-tree)
     It is slightly patched in (rosetta/views.py:translator_allowed ) to only allow a translator to handle languages
     they have permission (rosetta | translator | ...) for.

===============================================================================

          STATUS per 100210

---   Completed   ---
* Overal structure and operation of this tool
* Initial import of properties files

---   Remaining   ---
* Mail notifications for translators
* Initial import of template pages
* Implementing procedure to submit content to production servers



===============================================================================

Translation issues:

Keys needed to be content specific:

about_us-More about-how organisations can... end of line "to Europeana"
in german that place in that context would be translated to "k&#246;nnen"

about_us-More about-last bullet, start of line "The"
in german & swedish in that context that part should be empty
suggests a more specific key, something like #the[aboutus-moreabout-prefix to http://dev.europeana.eu/enews.php]




properties:79 and about_us:7 uses the same key "About us"

German translations
in about_us file:  &#220;ber uns
in properties file: Wir Ÿber uns



