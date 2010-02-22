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
changes are translated the new pages are copied to the production web server.

To avoid spamming a given translation team (one or more people) can only be in
two states "translations pending" and "all done". Only when a template change moves a language out of the "all done" state,
translators for that language are notified of pending translations.

This tool also handles the properties files - that is separate translations used
internally in the europeana.eu server. From a translator perspective there is
no difference if the translation key comes from a template or from the properties file.

The keys for the translations are in one of two formats:

If its a phrase or longer block of text, the text itself is the key, this is the normal
case and saves us time in that the english version of the text will automatically be
the default if a given key isn't translated.

In some cases, especially with short one-word strings, a context need to be given, then
we use this syntax (completely random, feel free to use what suits you)

#The[about_us: paragraph More about, last list item - prefix for enews.php]

witch in the english case would be translated to "The"

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
