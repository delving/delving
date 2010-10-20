package org.oclc.oai.harvester.app;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A table model where each row represents a harvest
 *
 * @author Gerald de Jong <geralddejong@gmai.com>
 */
public class HarvestTableModel extends AbstractTableModel implements HarvestTask.Parent {

    private List<HarvestTask> tasks = new ArrayList<HarvestTask>();

    public HarvestTableModel(List<HarvestConfig.Harvest> harvests) {
        for (HarvestConfig.Harvest harvest : harvests) {
            tasks.add(new HarvestTask(this, harvest));
        }
        sortOn(1);
    }

    public List<HarvestTask> getTasks() {
        return tasks;
    }

    public int getRowCount() {
        return tasks.size();
    }

    public int getColumnCount() {
        return HarvestTask.getColumnCount();
    }

    public Object getValueAt(int row, int column) {
        return tasks.get(row).getColumn(column);
    }

    public void sortOn(int column) {
        Collections.sort(tasks, HarvestTask.getColumnComparator(column));
        fireTableDataChanged();
    }

    public HarvestTask getRow(int row) {
        if (row < 0) {
            return null;
        }
        return tasks.get(row);
    }

    public void changed(HarvestTask task) {
        int rowNumber = tasks.indexOf(task);
        fireTableRowsUpdated(rowNumber, rowNumber);
    }

    public TableCellRenderer getCellRenderer() {
        return new Renderer();
    }

    private class Renderer extends JLabel implements TableCellRenderer {

        protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        private Renderer() {
            setOpaque(true);
            setBorder(noFocusBorder);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            if (selected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            }
            else {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
            }
            switch (tasks.get(row).getStatus()) {
                case IDLE:
                    break;
                case PENDING:
                    super.setBackground(new Color(0x00FFCC));
                    break;
                case PROCESSING:
                    super.setBackground(new Color(0x33CC00));
                    break;
                case ABORTED:
                    super.setBackground(new Color(0xCC00CC));
                    break;
                case ERROR:
                    super.setBackground(new Color(0xFF66FF));
                    break;
            }
            setFont(table.getFont());
            if (focus) {
                Border border = null;
                if (selected) {
                    border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = UIManager.getBorder("Table.focusCellHighlightBorder");
                }
                setBorder(border);
            }
            else {
                setBorder(noFocusBorder);
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

}
