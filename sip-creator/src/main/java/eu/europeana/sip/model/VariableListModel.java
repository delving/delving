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

import eu.europeana.definitions.annotations.EuropeanaField;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.util.List;

/**
 * Given an annotation processor, provide food for the JList to show fields
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class VariableListModel extends AbstractListModel {
    private static final long serialVersionUID = 939393939;
    private List<String> list;

    public void setList(List<String> list) {
        int size = getSize();
        this.list = null;
        fireIntervalRemoved(this, 0, size);
        this.list = list;
        fireIntervalAdded(this, 0, getSize());
    }

    @Override
    public int getSize() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public Object getElementAt(int index) {
        if (list == null) {
            return "UNKNOWN";
        }
        return list.get(index);
    }

    public static class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            EuropeanaField europeanaField = (EuropeanaField) value;
            return super.getListCellRendererComponent(list, europeanaField.getFieldNameString(), index, isSelected, cellHasFocus);
        }
    }

}