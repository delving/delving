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

package eu.europeana.sip.gui;

import eu.delving.core.rest.DataSetInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Data set management from here, using the REST interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetControlPanel extends JPanel {
    private JButton toggleButton = new JButton();
    private DataSetTableModel dataSetTableModel = new DataSetTableModel();
    private JTable table = new JTable(dataSetTableModel);
    private DataSetPanel.Enable enable;
    private DataSetInfo selectedInfo;

    public DataSetControlPanel(DataSetPanel.Enable enable) {
        super(new BorderLayout(8, 8));
        this.enable = enable;
        setBorder(BorderFactory.createTitledBorder("Control of all data sets"));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(toggleButton, BorderLayout.SOUTH);
        selectDataSetInfo(null);
        setPreferredSize(new Dimension(480, 500));
        wireUp();
    }

    public void setInfo(DataSetInfo dataSetInfo) {
        int index = dataSetTableModel.setDataSetInfo(dataSetInfo);
        if (table.getSelectedRow() == index) {
            selectDataSetInfo(dataSetInfo);
        }
    }

    public void setList(List list) {
        dataSetTableModel.setList(list);
    }

    private void wireUp() {
        ListSelectionModel select = table.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        select.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectDataSetInfo(dataSetTableModel.get(table.getSelectedRow()));
            }
        });
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enable != null) {
                    int selected = table.getSelectedRow();
                    if (selected >= 0) {
                        DataSetInfo info = dataSetTableModel.get(selected);
                        enable.setEnabled(info.spec, enableAction(info));
                    }
                    table.getSelectionModel().clearSelection();
                }
            }
        });
    }

    private void selectDataSetInfo(DataSetInfo dataSetInfo) {
        this.selectedInfo = dataSetInfo;
        if (selectedInfo == null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    toggleButton.setText("Select from the list above");
                    toggleButton.setEnabled(false);
                    table.requestFocus();
                }
            });
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    toggleButton.setText(String.format(
                            "\"%s\" is in state %s. Press here to %s it.",
                            selectedInfo.name,
                            selectedInfo.state,
                            enableAction(selectedInfo) ? "enable" : "disable"
                    ));
                    toggleButton.setEnabled(true);
                }
            });
        }
    }

    private boolean enableAction(DataSetInfo info) {
        switch (DataSetState.valueOf(info.state)) {
            case INDEXING:
            case ENABLED:
            case QUEUED:
                return false;
            case UPLOADED:
            case DISABLED:
            case ERROR:
                return true;
            default:
                throw new RuntimeException();
        }
    }

    public enum DataSetState {
        DISABLED,
        UPLOADED,
        QUEUED,
        INDEXING,
        ENABLED,
        ERROR
    }

    private class DataSetTableModel extends AbstractTableModel {
        private List<DataSetInfo> list;

        public int setDataSetInfo(DataSetInfo dataSetInfo) {
            for (int walk = 0; walk < getRowCount(); walk++) {
                if (dataSetInfo.spec.equals(get(walk).spec)) {
                    list.set(walk, dataSetInfo);
                    fireTableRowsUpdated(walk, walk);
                    return walk;
                }
            }
            return -1;
        }

        public DataSetInfo get(int index) {
            if (index < 0) {
                return null;
            }
            else {
                return list.get(index);
            }
        }

        @SuppressWarnings("unchecked")
        public void setList(List freshList) {
            int rows = getRowCount();
            if (freshList.size() == rows) {
                this.list = freshList;
                fireTableRowsUpdated(0, rows);
            }
            else {
                this.list = null;
                fireTableRowsDeleted(0, rows);
                this.list = freshList;
                fireTableRowsInserted(0, getRowCount());
            }
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Name";
                case 1:
                    return "Spec";
                case 2:
                    return "State";
                case 3:
                    return "Indexed";
                case 4:
                    return "Total";
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int getRowCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (list == null) {
                return "UNKNOWN";
            }
            else {
                DataSetInfo info = list.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return info.name;
                    case 1:
                        return info.spec;
                    case 2:
                        return info.state;
                    case 3:
                        return info.recordsIndexed;
                    case 4:
                        return info.recordCount;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        }

    }

}