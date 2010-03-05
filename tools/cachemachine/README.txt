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

  Dependencies

===============================================================================
easy_install dnspython


===============================================================================

 20100215 Pre Alpha, early design state basically nothing usable here so far...

===============================================================================

New filecache solution for Europeana, early stages of design.

-------   Primary feature requests   ------

* on reindexing, quickly find out all new items and get them

* statistics based on providers, usage,...

* traverse filetree to remove orphans

* traverse index retrieve lost files & check broken links

-------   Secondary features   -------

* on indexing, remove now obsolete items (perhaps let orphan traversal take
  care of it on next walkthrough instead of dedicated removal)


=====================================

 Some extra hints for downloading the source xml files to your laptop/otherwise
 bw/storage limited device

=====================================

Here is a rsync command that only downloads the relevant .tgz files
replace the source url depending on from where you retrieve the ingestion files
(something like 500MB instead of 161GB)

rsync --delete-excluded \
      --include=*.xml.gz \
      --include=*/  \
      --exclude=* \
      --prune-empty-dirs \
      --bwlimit=250 \   # Or whatever other bw limit you prefer...
      -avP jacwork-vpn:proj/europeana/ingestion/trunk .

After this run the utils/expand_source_gzips with the topdir for the above download
and it will unpack all .tgz files in place, so that you then can use theese files
for creating your database
