
Theese are the files we import into this tool to get it kickstarted with the
initial conntent from the current europeana portal (version 0.4)

html_files - original translated static page templates
message_keys - original translated properties
utils/templates - new templates to use instead of the ones in html_files

100209-initial-properties.sql
    SQL dump to populate database with initial properties, and translation keys



Procedure
1. Prepare system and generate django-db (See top README.txt)
2. Populate with sqldata, depending on db used something like:
  cat 100209-inital-properties.sql > mysql MultiLingo
3. Generate translations for all the properties
  python initial_po_files.py

If no errors was displayed You should now have a working setup!
