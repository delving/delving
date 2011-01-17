/*
 * Copyright 2010 DELVING BV
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

package eu.europeana.sip.gui;

import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetState;
import eu.delving.sip.FileStore;
import eu.delving.sip.FileStoreException;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Generate a description in HTML of a Data Set
 *
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class DataSetListModel extends AbstractListModel {

    private List<Entry> entries = new ArrayList<Entry>();

    public void setDataSetStore(FileStore.DataSetStore dataSetStore) {
        Entry entry = getEntry(dataSetStore.getSpec());
        entry.setDataSetStore(dataSetStore);
    }

    public Entry setDataSetInfo(DataSetInfo dataSetInfo) {
        Entry entry = getEntry(dataSetInfo.spec);
        entry.setDataSetInfo(dataSetInfo);
        return entry;
    }

    public Set<String> setDataSetInfo(List<DataSetInfo> dataSetInfoList) {
        Map<String, DataSetInfo> map = new TreeMap<String, DataSetInfo>();
        for (DataSetInfo info : dataSetInfoList) {
            map.put(info.spec, info);
        }
        Set<String> untouched = new TreeSet<String>();
        for (Entry entry : entries) {
            DataSetInfo freshInfo = map.get(entry.getSpec());
            entry.setDataSetInfo(freshInfo);
            if (freshInfo == null) {
                untouched.add(entry.getSpec());
            }
        }
        return untouched;
    }

    public Entry getEntry(int i) {
        return entries.get(i);
    }

    public void clear() {
        int before = getSize();
        entries.clear();
        fireIntervalRemoved(this, 0, before);
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public Object getElementAt(int i) {
        return entries.get(i);
    }

    public class Entry implements Comparable<Entry> {
        int index = -1;
        private String spec;
        private FileStore.DataSetStore dataSetStore;
        private DataSetInfo dataSetInfo;

        public Entry(String spec) {
            this.spec = spec;
        }

        public void setDataSetStore(FileStore.DataSetStore dataSetStore) {
            this.dataSetStore = dataSetStore;
            fireContentsChanged(DataSetListModel.this, index, index);
        }

        public String getSpec() {
            return spec;
        }

        public FileStore.DataSetStore getDataSetStore() {
            return dataSetStore;
        }

        public DataSetInfo getDataSetInfo() {
            return dataSetInfo;
        }

        public void setDataSetInfo(DataSetInfo dataSetInfo) {
            if (this.dataSetInfo == null && dataSetInfo == null) return;
            this.dataSetInfo = dataSetInfo;
            fireContentsChanged(DataSetListModel.this, index, index);
        }

        public String toHtml() throws FileStoreException {
            StringBuilder html = new StringBuilder(String.format("<html><table><tr><td width=250><b>%s</b></td><td>", spec));
            if (dataSetStore != null && dataSetInfo != null) {
                html.append("<p>Data Set is present in the repository as well as in the local file store.</p>");
            }
            else if (dataSetStore != null) {
                html.append("<p>Data Set is in the local file store but not in the repository.</p>");
            }
            else if (dataSetInfo != null) {
                html.append("<p>Data Set is present only in the repository, and not in the local file store.</p>");
            }

            if (dataSetStore != null) {
                List<String> mappingPrefixes = dataSetStore.getMappingPrefixes();
                switch (mappingPrefixes.size()) {
                    case 0:
                        break;
                    case 1:
                        html.append(String.format(
                                "<p>There is a mapping created for <strong>%s</strong>.</p>",
                                mappingPrefixes.get(0)
                        ));
                        break;
                    case 2:
                        html.append(String.format(
                                "<p>There are mappings created for <strong>%s</strong> and <strong>%s</strong>.</p>",
                                mappingPrefixes.get(0),
                                mappingPrefixes.get(1)
                        ));
                        break;
                    default:
                        html.append(String.format(
                                "<p>There are mappings created for all of <strong>%s</strong>.</p>",
                                mappingPrefixes
                        ));
                        break;
                }
            }
            if (dataSetInfo != null) {
                DataSetState state = DataSetState.valueOf(dataSetInfo.state);
                switch (state) {
                    case DISABLED:
                        html.append(String.format("<p>Data Set is disabled, but it has %d records and it can be indexed.</p>", dataSetInfo.recordCount));
                        break;
                    case INCOMPLETE:
                        html.append(String.format(
                                "<p>Data Set is incomplete.  So far %d records.</p>",
                                dataSetInfo.recordCount
                        ));
                        break;
                    case ENABLED:
                        html.append(String.format(
                                "<p><strong>Data set is enabled, with %d records indexed of the total %d.</strong></p>",
                                dataSetInfo.recordsIndexed,
                                dataSetInfo.recordCount
                        ));
                        break;
                    case ERROR:
                        html.append("<p>Data set is in <strong>error</strong>. Indexing can be retried.</p>");
                        break;
                    case INDEXING:
                        html.append(String.format(
                                "<p>Data set is busy indexing, with %d records processed so far of %d.</p>",
                                dataSetInfo.recordsIndexed,
                                dataSetInfo.recordCount
                        ));
                        break;
                    case QUEUED:
                        html.append("<p>Data set is queued for indexing, which should start shortly.</p>");
                        break;
                    case UPLOADED:
                        html.append(String.format(
                                "<p>Data Set is fully uploaded with %d records.</p>",
                                dataSetInfo.recordCount
                        ));
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
            html.append("</td></table></html>");
            return html.toString();
        }

        public String toString() {
            try {
                return toHtml();
            }
            catch (FileStoreException e) {
                return e.toString();
            }
        }

        @Override
        public int compareTo(Entry entry) {
            return spec.compareTo(entry.spec);
        }
    }

    private Entry getEntry(String spec) {
        for (Entry entry : entries) {
            if (entry.spec.equals(spec)) {
                return entry;
            }
        }
        Entry fresh = new Entry(spec);
        int added = entries.size();
        entries.add(fresh);
        fireIntervalAdded(this, added, added);
        Collections.sort(entries);
        int index = 0;
        for (Entry entry : entries) {
            if (entry.index != index) {
                entry.index = index;
                fireContentsChanged(this, index, index);
            }
            index++;
        }
        return fresh;
    }

    public static class Cell implements ListCellRenderer {
        private JPanel p = new JPanel(new BorderLayout());
        private DefaultListCellRenderer renderer = new DefaultListCellRenderer();

        public Cell() {
            p.add(renderer);
        }

        @Override
        public Component getListCellRendererComponent(JList jList, Object o, int i, boolean selected, boolean hasFocus) {
            renderer.getListCellRendererComponent(jList, o, i, selected, false);
            if (selected) {
                p.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                BorderFactory.createLoweredBevelBorder()
                        )
                );
                renderer.setForeground(Color.BLACK);
                renderer.setBackground(Color.WHITE);
            }
            else {
                p.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                BorderFactory.createRaisedBevelBorder()
                        )
                );
                renderer.setForeground(Color.BLACK);
                renderer.setBackground(p.getBackground());
            }
            return p;
        }
    }
}
