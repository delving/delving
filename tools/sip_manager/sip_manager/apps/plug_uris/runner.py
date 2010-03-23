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



"""

from models import UriSource, Uri
from apps.base_item.models import MdRecord

def constructor():
    """Scan all MdRecords for items with no Uris connected
    For each record extract all uris in that record and create uris and join it to a UriSource
    """

    # this selection process is super inefficient, needs to be changed...
    for mdRecord in MdRecord.items.all():
        if not Uri.items.filter(md_rec_id=mdRecord.id):
            u = Uri(md_rec_id=mdRecord.id)
            u.save()


def processor():
    """For each available UriSource loop on all unprocessed Uris
      check this uri and if it is a object try to download and generate thumbnail
      end the Uri in one of the final states to indicate that this record is done for
      the timebeing
    """
    for uri_source in UriSource.items.filter(pid=0):

        for uri in Uris.items.filter(uri_source=uri_source.id, pid=0):
            process_one_uri(uri)

def destructor():
    """Find all Uris where the MdRecord has been deleted
      if its an object remove all the relevant files, then remove the Uri
    """
    return False # always fail for the moment

def file_tree_monitor():
    """Run quarterly or similar, this takes a while :)
    For each file, check if there is an Uri refering to this file, if not remove it.
    """
    return False # always fail for the moment


def process_one_uri(uri):
    # Step one, find available UriSource
    pass