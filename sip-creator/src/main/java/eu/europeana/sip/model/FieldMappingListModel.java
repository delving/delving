/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.model;

import eu.europeana.sip.groovy.FieldMapping;
import eu.europeana.sip.groovy.RecordMapping;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A ListModel of FieldMapping instances
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldMappingListModel extends AbstractListModel implements RecordMapping.Listener {
    private List<FieldMapping> list = new ArrayList<FieldMapping>();

    public FieldMappingListModel(RecordMapping recordMapping) {
        recordMapping.addListener(this);
        refreshList(recordMapping);
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public Object getElementAt(int index) {
        return list.get(index);
    }

    @Override
    public void mappingAdded(FieldMapping fieldMapping) {
        int index = list.size();
        list.add(fieldMapping);
        fireIntervalAdded(this, index, index);
    }

    @Override
    public void mappingRemoved(FieldMapping fieldMapping) {
        int index = list.indexOf(fieldMapping);
        if (index >= 0) {
            list.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    @Override
    public void mappingsRefreshed(RecordMapping recordMapping) {
        refreshList(recordMapping);
    }

    private void refreshList(RecordMapping recordMapping) {
        clear();
        for (FieldMapping fieldMapping : recordMapping) {
            list.add(fieldMapping);
        }
        fireIntervalAdded(this, 0, getSize());
    }

    private void clear() {
        int size = getSize();
        if (size > 0) {
            this.list.clear();
            fireIntervalRemoved(this, 0, size);
        }
    }

}