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

2010-02-22  Jacob.Lundqvist@gmail.com

These are the files we import into this tool to get it kickstarted with the
initial content from the current europeana portal (version 0.4)

To go from static pages to a rosetta work takes some initial work, we have to
extract texts from templates and insert them in the po files.

initial_po_files.py sets up po files for all languages, due to time-constraints
I didn't complete all languages, but the about_is page in  nordic, French
spanish and italian are auto imported

You could use this as a template for importing your own site.


html_files - original translated static page templates
message_keys - original translated properties


Procedure
1. Prepare system and generate django-db (See top README.txt)
2. Generate translations for all the properties
  python initial_po_files.py

If no errors was displayed You should now have a working setup!

You probably want to remove this import dir from a production system