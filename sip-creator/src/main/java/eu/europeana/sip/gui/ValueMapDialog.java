package eu.europeana.sip.gui;

import eu.europeana.sip.core.RecordMapping;
import eu.europeana.sip.core.ValueMap;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allow for editing a value map
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ValueMapDialog extends JDialog {
    private RecordMapping recordMapping;
    private ValueMap valueMap;
    private JComboBox editorBox;

    public ValueMapDialog(Frame owner, RecordMapping recordMapping) {
        super(owner, true);
        this.recordMapping = recordMapping;
        this.valueMap = recordMapping.getOnlyFieldMapping().getValueMap();
        setTitle("Value Map for " + valueMap.getName());
        this.valueMap = valueMap;
        editorBox = new JComboBox(new EditorModel());
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(6, 6, 6, 6),
                        BorderFactory.createTitledBorder(valueMap.getName())
                )
        );
        p.add(new JScrollPane(createTable()), BorderLayout.CENTER);
        p.add(createButton(), BorderLayout.SOUTH);
        getContentPane().add(p);
        setLocationRelativeTo(owner);
        setLocation(owner.getWidth() / 3, 20);
        setSize(owner.getWidth() / 2, owner.getHeight() - 60);
    }

    private JTable createTable() {
        return new JTable(createTableModel(), createTableColumnModel());
    }

    private JButton createButton() {
        JButton ok = new JButton("OK, Finished");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ValueMapDialog.this.setVisible(false);
                recordMapping.notifyValueMapChange();
            }
        });
        return ok;
    }

    private TableColumnModel createTableColumnModel() {
        TableColumn left = new TableColumn(0, 60);
        left.setWidth(400);
        left.setMinWidth(100);
        TableColumn right = new TableColumn(1, 60);
        right.setWidth(400);
        right.setMinWidth(100);
        right.setCellRenderer(new Renderer());
        right.setCellEditor(new DefaultCellEditor(editorBox));
        DefaultTableColumnModel tcm = new DefaultTableColumnModel();
        tcm.addColumn(left);
        tcm.addColumn(right);
        return tcm;
    }

    private TableModel createTableModel() {
        return new MapModel();
    }

    private class MapModel extends AbstractTableModel {
        private List<String[]> rows = new ArrayList<String[]>();

        private MapModel() {
            for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                rows.add(new String[]{entry.getKey(), entry.getValue()});
            }
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Input";
                case 1:
                    return "Output";
            }
            throw new RuntimeException();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex)[columnIndex];
        }

        @Override
        public void setValueAt(Object valueObject, int rowIndex, int columnIndex) {
            if (columnIndex != 1) throw new RuntimeException();
            if (valueObject == null) {
                valueObject = "";
            }
            valueMap.put(rows.get(rowIndex)[0], rows.get(rowIndex)[1] = (String) valueObject);
        }
    }

    private class Renderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            if (((String) value).isEmpty()) {
                value = "<<< copy verbatim >>>";
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

    }

    private class EditorModel extends AbstractListModel implements ComboBoxModel {
        private static final String COPY_VERBATIM = "";
        private List<String> values = new ArrayList<String>(valueMap.getRangeValues());
        private Object selectedItem = COPY_VERBATIM;

        @Override
        public void setSelectedItem(Object item) {
            selectedItem = item;
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        @Override
        public int getSize() {
            return values.size() + 1;
        }

        @Override
        public Object getElementAt(int index) {
            if (index == 0) {
                return COPY_VERBATIM;
            }
            else {
                return values.get(index - 1);
            }
        }
    }
}
