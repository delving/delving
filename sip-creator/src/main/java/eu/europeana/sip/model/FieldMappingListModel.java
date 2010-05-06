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

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A ListModel of FieldMapping instances
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldMappingListModel extends AbstractListModel {
    private List<FieldMapping> list = new ArrayList<FieldMapping>();

    public void setList(List<FieldMapping> list) {
        clear();
        this.list.addAll(list);
        fireIntervalAdded(this, 0, getSize());
    }

    public void clear() {
        int size = getSize();
        this.list.clear();
        fireIntervalRemoved(this, 0, size);
    }

    public List<FieldMapping> getList() {
        return list;
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public Object getElementAt(int index) {
        return list.get(index);
    }

}