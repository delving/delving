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

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given an annotation processor, provide food for the JList to show fields
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class VariableListModel extends AbstractListModel {
    private List<AnalysisTree.Node> variableList = new ArrayList<AnalysisTree.Node>();

    public void setVariableList(List<AnalysisTree.Node> variableList) {
        clear();
        this.variableList.addAll(variableList);
        Collections.sort(this.variableList);
        fireIntervalAdded(this, 0, getSize());
    }

    public void clear() {
        int size = getSize();
        this.variableList.clear();
        fireIntervalRemoved(this, 0, size);
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
            AnalysisTree.Node node = (AnalysisTree.Node) value;
            return super.getListCellRendererComponent(list, node.getVariableName(), index, isSelected, cellHasFocus);
        }
    }

}