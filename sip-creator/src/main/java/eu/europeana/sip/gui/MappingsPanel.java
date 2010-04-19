package eu.europeana.sip.gui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
 * Provides a set of GUI elements to create basic mappings. Detected ESE elements can
 * be shown in a JTable. Each element can be enabled or disabled using a checbox.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class MappingsPanel extends JPanel implements AnalyzerPanel.Listener {

    private Object[][] data = new Object[][]{};

    public MappingsPanel() {
        super(new BorderLayout());
        init();
    }

    /**
     * Construct a 3-column table with detected ESE fields on the left side and target ESE fields
     * in the middle. The third column is containing a checkbox where the mapping can be accepted
     * or ignored during normalization.
     *
     * @return The table
     */
    private JTable constructTable() {
        JTable jTable = new JTable(new AbstractTableModel() {

            String[] columnNames = {"ESE", "ESE+", "Normalize?"};

            @Override
            public int getRowCount() {
                return data.length;
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public String getColumnName(int column) {
                return columnNames[column];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return data[rowIndex][columnIndex];
            }

            @Override
            public void setValueAt(Object value, int rowIndex, int columnIndex) {
                data[rowIndex][columnIndex] = value;
                fireTableCellUpdated(rowIndex, columnIndex);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return getValueAt(0, columnIndex).getClass();
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return getValueAt(rowIndex, columnIndex) instanceof Boolean;
            }

            @Override
            public void fireTableCellUpdated(int row, int column) {
                System.out.printf("->[%d,%d] %10s%n", row, column, getValueAt(row, column)); // todo: implement
            }
        });
        jTable.setFillsViewportHeight(true);
        return jTable;
    }

    private void init() {
        JButton create = new JButton("create");
        add(create, BorderLayout.SOUTH);
        JScrollPane foundElementsContainer = new JScrollPane(constructTable());
        add(foundElementsContainer);
        add(foundElementsContainer, BorderLayout.CENTER);
    }

    @Override
    public void updateAvailableNodes(java.util.List<String> nodes) {
        int counter = 0;
        data = new Object[nodes.size()][];
        for (String s : nodes) {
            data[counter] = new Object[]{s, s, true};
            counter++;
        }
    }
}
