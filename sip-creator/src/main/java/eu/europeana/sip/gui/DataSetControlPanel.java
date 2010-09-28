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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.client.RestTemplate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Data set management from here, using the REST interface
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetControlPanel extends JPanel {
    private static final String BUTTON_DEFAULT = "Enable / Disable";
    private Executor executor = Executors.newSingleThreadExecutor();
    private DataSetTableModel dataSetTableModel = new DataSetTableModel();
    private JTable table = new JTable(dataSetTableModel);
    private JButton toggleButton = new JButton(BUTTON_DEFAULT);
    private RestTemplate restTemplate;
    private String dataSetControllerUrl, accessKey;

    public DataSetControlPanel(String dataSetControllerUrl) {
        super(new BorderLayout(8,8));
        this.dataSetControllerUrl = dataSetControllerUrl;
        setBorder(BorderFactory.createTitledBorder("Control of all data sets"));
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(toggleButton, BorderLayout.SOUTH);
        toggleButton.setEnabled(false);
        setPreferredSize(new Dimension(600, 500));
        wireUp();
    }

    public void refreshList(String accessKey) {
        this.accessKey = accessKey;
        executor.execute(new ListFetcher());
    }

    private void wireUp() {
        ListSelectionModel select = table.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        select.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = table.getSelectedRow();
                if (selected < 0) {
                    toggleButton.setText(BUTTON_DEFAULT);
                    toggleButton.setEnabled(false);
                }
                else {
                    setButton(dataSetTableModel.get(selected));
                }
            }
        });
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = table.getSelectedRow();
                if (selected >= 0) {
                    toggleButton.setEnabled(false);
                    DataSetInfo info = dataSetTableModel.get(selected);
                    executor.execute(new XAbler(info.spec, Math.random() > 0.5));
                }
            }
        });
    }

    private void setButton(final DataSetInfo info) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                toggleButton.setText(String.format(
                        "Data set \"%s\" is currently %s for indexing. Press here to %s it.",
                        info.name,
                        "en-dis-abled", // todo: when that info is available
                        "en-dis-abled" // todo: the opposite
                ));
                toggleButton.setEnabled(true);
            }
        });
    }

    private class DataSetTableModel extends AbstractTableModel {
        private List list;

        public DataSetInfo get(int index) {
            return (DataSetInfo) list.get(index);
        }

        public void setList(List list) {
            int rows = getRowCount();
            this.list = null;
            fireTableRowsDeleted(0, rows);
            this.list = list;
            fireTableRowsInserted(0, getRowCount());
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "spec";
                case 1:
                    return "name";
                case 2:
                    return "provider name";
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
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (list == null) {
                return "UNKNOWN";
            }
            else {
                DataSetInfo info = (DataSetInfo) list.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return info.spec;
                    case 1:
                        return info.name;
                    case 2:
                        return info.providerName;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        }
    }

    private RestTemplate rest() {
        if (restTemplate == null) {
            XStreamMarshaller xStream = new XStreamMarshaller();
            xStream.setAnnotatedClass(DataSetInfo.class);
            List<HttpMessageConverter<?>> converterList = new ArrayList<HttpMessageConverter<?>>();
            converterList.add(new MarshallingHttpMessageConverter(xStream, xStream));
            restTemplate = new RestTemplate();
            restTemplate.setMessageConverters(converterList);
        }
        return restTemplate;
    }

    private class ListFetcher implements Runnable {
        @Override
        public void run() {
            try {
                String url = String.format(
                        "%s?accessKey=%s",
                        dataSetControllerUrl,
                        accessKey
                );
                final List list = rest().getForObject(url, List.class);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dataSetTableModel.setList(list);
                    }
                });
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(DataSetControlPanel.this, e.toString()); // todo: better way?
            }
        }
    }

    private class XAbler implements Runnable {
        private String spec;
        private boolean enable;

        private XAbler(String spec, boolean enable) {
            this.spec = spec;
            this.enable = enable;
        }

        @Override
        public void run() {
            try {
                String url = String.format(
                        "%s/indexing/%s?enable=%s?accessKey=%s",
                        dataSetControllerUrl,
                        spec,
                        String.valueOf(enable),
                        accessKey
                );
                final DataSetInfo info = rest().getForObject(url, DataSetInfo.class);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setButton(info);
                    }
                });
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(DataSetControlPanel.this, e.toString()); // todo: better way?
            }
        }
    }
}