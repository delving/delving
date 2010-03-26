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

#from models import UriSource, Uri
#from apps.base_item.models import MdRecord

from utils.sipproc import SipProcess


class UriCreateNewRecords(SipProcess):
    SHORT_DESCRIPTION = 'Create new uri records'

    def run(self):
        for mdRecord in MdRecord.items.all():
            if not Uri.items.filter(md_rec_id=mdRecord.id):
                u = Uri(md_rec_id=mdRecord.id)
                u.save()
        return

class UriProcessNewRecords(SipProcess):
    SHORT_DESCRIPTION = 'process new uri records'
    PLUGIN_TAXES_NET_IO = True

    def run(self):
        for uri_source in UriSource.items.filter(pid=0):
            for uri in Uris.items.filter(uri_source=uri_source.id, pid=0):
                process_one_uri(uri)
        return

class UriCleanup(SipProcess):
    SHORT_DESCRIPTION = 'Remove uri records no longer having a request'

    def run(self):
        pass


class UriFileTreeMonitor(SipProcess):
    SHORT_DESCRIPTION = 'Walks file tree and finds orphan files'

    def run(self):
        return True # does nothing for the moment


def process_one_uri(uri):
    # Step one, find available UriSource
    pass



task_list = [UriCreateNewRecords,UriProcessNewRecords,UriCleanup, UriFileTreeMonitor]