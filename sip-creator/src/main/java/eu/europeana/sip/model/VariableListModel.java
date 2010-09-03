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

import eu.europeana.sip.core.FieldMapping;
import eu.europeana.sip.core.RecordMapping;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given an annotation processor, provide food for the JList to show fields
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class VariableListModel extends AbstractListModel {
    private List<VariableHolder> variableList = new ArrayList<VariableHolder>();
    private WithCounts withCounts;

    public void setVariableList(List<AnalysisTree.Node> variableList) {
        clear();
        for (AnalysisTree.Node node : variableList) {
            this.variableList.add(new VariableHolder(node));
        }
        Collections.sort(this.variableList);
        fireIntervalAdded(this, 0, getSize());
        if (withCounts != null) {
            withCounts.refresh();
        }
    }

    public void clear() {
        int size = getSize();
        if (size > 0) {
            this.variableList.clear();
            fireIntervalRemoved(this, 0, size);
        }
    }

    @Override
    public int getSize() {
        return variableList.size();
    }

    @Override
    public Object getElementAt(int index) {
        return variableList.get(index);
    }

    public ListModel getWithCounts(RecordMapping recordMapping) {
        if (withCounts == null) {
            withCounts = new WithCounts(recordMapping);
            recordMapping.addListener(withCounts);
        }
        return withCounts;
    }

    public class WithCounts extends AbstractListModel implements RecordMapping.Listener {
        private RecordMapping recordMapping;
        private List<VariableHolder> variableHolderList = new ArrayList<VariableHolder>();

        public WithCounts(RecordMapping recordMapping) {
            this.recordMapping = recordMapping;
            refresh();
        }

        @Override
        public int getSize() {
            return variableHolderList.size();
        }

        @Override
        public Object getElementAt(int index) {
            return variableHolderList.get(index);
        }

        @Override
        public void mappingAdded(FieldMapping fieldMapping) {
            refresh();
        }

        @Override
        public void mappingRemoved(FieldMapping fieldMapping) {
            refresh();
        }

        @Override
        public void mappingsRefreshed(RecordMapping recordMapping) {
            refresh();
        }

        private void refresh() {
            int sizeBefore = getSize();
            variableHolderList.clear();
            fireIntervalRemoved(this, 0, sizeBefore);
            for (VariableHolder uncountedHolder : variableList) {
                VariableHolder variableHolder = new VariableHolder(uncountedHolder.node);
                for (FieldMapping fieldMapping : recordMapping) {
                    for (String variable : fieldMapping.getVariables()) {
                        variableHolder.checkIfMapped(variable);
                    }
                }
                variableHolderList.add(variableHolder);
            }
            // todo: this is a rude trick to correct the stupid decision to make variables lower-case
            for (VariableHolder holder : variableHolderList) {
                String to = holder.getVariableName();
                String from = to.toLowerCase();
                for (FieldMapping fieldMapping : recordMapping) {
                    fieldMapping.fixVariableName(from, to);
                }
            }
            // todo: remove the above block sometime
            Collections.sort(variableHolderList);
            fireIntervalAdded(this, 0, getSize());
        }

    }


    public static class VariableHolder implements Comparable<VariableHolder> {
        private AnalysisTree.Node node;
        private String variableName;
        private int mappingCount;

        private VariableHolder(AnalysisTree.Node node) {
            this.node = node;
            this.variableName = node.getVariableName();
        }

        public void checkIfMapped(String variableName) {
            if (this.variableName.equals(variableName)) {
                mappingCount++;
            }
        }

        public AnalysisTree.Node getNode() {
            return node;
        }

        public String getVariableName() {
            return variableName;
        }

        public String toString() {
            StringBuilder out = new StringBuilder(variableName);
            switch (mappingCount) {
                case 0:
                    break;
                case 1:
                    out.append(" (mapped once)");
                    break;
                case 2:
                    out.append(" (mapped twice)");
                    break;
                default:
                    out.append(" (mapped ").append(mappingCount).append(" times)");
                    break;
            }
            return out.toString();
        }

        @Override
        public int compareTo(VariableHolder o) {
            if (mappingCount > o.mappingCount) {
                return 11;
            }
            else if (mappingCount < o.mappingCount) {
                return -1;
            }
            else {
                return node.compareTo(o.node);
            }
        }
    }
}