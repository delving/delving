package eu.europeana.sip.gui;

import eu.delving.metadata.FieldMapping;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    private FieldMapping fieldMapping;
    private JButton selectionSetButton;
    private JComboBox editorBox, selectionEditorBox;
    private JTable table;

    public ValueMapDialog(Frame owner, FieldMapping fieldMapping) {
        super(owner, true);
        this.fieldMapping = fieldMapping;
        setTitle("Value Map for " + fieldMapping.getFieldDefinition().getTag());
        editorBox = new JComboBox(new EditorModel());
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(6, 6, 6, 6),
                        BorderFactory.createTitledBorder(fieldMapping.getFieldDefinition().getTag().toString())
                )
        );
        p.add(createSelectionSetter(), BorderLayout.NORTH);
        p.add(new JScrollPane(createTable()), BorderLayout.CENTER);
        p.add(createButton(), BorderLayout.SOUTH);
        getContentPane().add(p);
        setLocationRelativeTo(owner);
        setLocation(owner.getWidth() / 3, 20);
        setSize(owner.getWidth() / 2, owner.getHeight() - 60);
    }

    private JPanel createSelectionSetter() {
        selectionSetButton = new JButton("Set selected items to this value");
        selectionSetButton.setEnabled(false);
        selectionSetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int [] selectedRows = table.getSelectedRows();
                for (int row : selectedRows) {
                    table.getModel().setValueAt(selectionEditorBox.getSelectedItem(), row, 1);
                }

            }
        });
        selectionEditorBox = new JComboBox(new EditorModel());
        JPanel p = new JPanel();
        p.add(selectionEditorBox);
        p.add(selectionSetButton);
        return p;
    }

    private JTable createTable() {
        table = new JTable(createTableModel(), createTableColumnModel());
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int [] selectedRows = table.getSelectedRows();
                selectionSetButton.setEnabled(selectedRows.length > 0);
            }
        });
        return table;
    }

    private JButton createButton() {
        JButton ok = new JButton("OK, Finished");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ValueMapDialog.this.setVisible(false);
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
            for (Map.Entry<String, String> entry : fieldMapping.valueMap.entrySet()) {
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
            fieldMapping.valueMap.put(rows.get(rowIndex)[0], rows.get(rowIndex)[1] = (String) valueObject);
            // todo: notify the world
            fireTableCellUpdated(rowIndex, columnIndex);
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
        private List<String> values = new ArrayList<String>(fieldMapping.getFieldDefinition().options);
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
