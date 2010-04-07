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

import django.db


from apps.process_monitor.sipproc import SipProcess

import models




class MdRecordFixDB(SipProcess):
    INIT_PLUGIN = True

    def run_it(self):
        "Set source_data to be a binary field."
        cursor = django.db.connection.cursor()
        if not 'mysql' in cursor.db.__module__:
            return True
        sql = 'ALTER TABLE base_item_mdrecord CHANGE source_data source_data LONGTEXT CHARACTER SET utf8 COLLATE utf8_bin NOT NULL'
        cursor.execute(sql)
        return True


# List of active plugins from this file
task_list = [
    MdRecordFixDB,
             ]