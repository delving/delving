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
import eu.europeana.sip.groovy.FieldMapping;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Given an annotation processor, provide food for the JList to show fields
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class VariableListModel extends AbstractListModel {
    private static final long serialVersionUID = 939393939;
    private List<String> variableList = new ArrayList<String>();

    public void setVariableList(List<String> variableList) {
        clear();
        this.variableList.addAll(variableList);
        fireIntervalAdded(this, 0, getSize());
    }

    public void clear() {
        int size = getSize();
        this.variableList.clear();
        fireIntervalRemoved(this, 0, size);
    }

    public ListModel createUnmapped(FieldMappingListModel fieldMappingListModel) {
        Unmapped unmapped = new Unmapped(fieldMappingListModel.getList());
        fieldMappingListModel.addListDataListener(unmapped);
        this.addListDataListener(unmapped);
        return unmapped;
    }

    @Override
    public int getSize() {
        return variableList.size();
    }

    @Override
    public Object getElementAt(int index) {
        return variableList.get(index);
    }

    public static class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            EuropeanaField europeanaField = (EuropeanaField) value;
            return super.getListCellRendererComponent(list, europeanaField.getFieldNameString(), index, isSelected, cellHasFocus);
        }
    }

    public class Unmapped extends AbstractListModel implements ListDataListener {
        private List<FieldMapping> fieldMappingList;
        private List<String> unmappedVariables = new ArrayList<String>();

        public Unmapped(List<FieldMapping> fieldMappingList) {
            this.fieldMappingList = fieldMappingList;
        }

        @Override
        public int getSize() {
            return unmappedVariables.size();
        }

        @Override
        public Object getElementAt(int index) {
            return unmappedVariables.get(index);
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            refresh();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            refresh();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            refresh();
        }

        private void refresh() {
            int sizeBefore = getSize();
            unmappedVariables.clear();
            fireIntervalRemoved(this, 0, sizeBefore);
            nextVariable:
            for (String variable : variableList) {
                for (FieldMapping fieldMapping : fieldMappingList) {
                    for (String mappedVariable : fieldMapping.getFromVariables()) {
                        if (mappedVariable.equals(variable)) {
                            continue nextVariable;
                        }
                    }
                }
                unmappedVariables.add(variable);
            }
            fireIntervalAdded(this, 0, getSize());
        }
    }
}