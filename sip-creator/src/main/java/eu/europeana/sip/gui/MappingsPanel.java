package eu.europeana.sip.gui;


import eu.europeana.core.querymodel.beans.AllFieldBean;
import eu.europeana.core.querymodel.beans.BriefBean;
import eu.europeana.core.querymodel.beans.FullBean;
import eu.europeana.core.querymodel.beans.IdBean;
import eu.europeana.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.definitions.annotations.EuropeanaField;
import eu.europeana.definitions.annotations.ValidationLevel;
import eu.europeana.sip.groovy.GroovyMapping;
import eu.europeana.sip.groovy.GroovyService;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Provides a set of GUI elements to create basic mappings. Detected ESE elements can
 * be shown in a JTable. Each element can be enabled or disabled using a checbox.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class MappingsPanel extends JPanel {

    private Object[][] data = new Object[][]{};
    private java.util.List<Class<?>> list = new ArrayList<Class<?>>();
    private AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
    private GroovyMapping groovyMapping;
    private JComboBox additionalFields;
    private JComboBox mappableFields;
    private static final String NO_MAPPING = "- no mapping -";

    {
        list.add(IdBean.class);
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        annotationProcessor.setClasses(list);
        Vector<String> additionals = new Vector<String>();
        additionals.add(NO_MAPPING);
        Vector<String> required = new Vector<String>();
        required.add(NO_MAPPING);
        for (EuropeanaField field : annotationProcessor.getFields(ValidationLevel.ESE_PLUS_REQUIRED)) {
            additionals.add(field.getFieldNameString());
        }
        for (EuropeanaField field : annotationProcessor.getFields(ValidationLevel.ESE_REQUIRED)) {
            required.add(field.getFieldNameString());
        }
        additionalFields = new JComboBox(additionals);
        mappableFields = new JComboBox(required);
    }

    private class MappablesTab extends JPanel {

        private MappablesTab() {
            super(new BorderLayout());
            add(new JScrollPane(constructMappableTable(mappableFields)), BorderLayout.CENTER);
        }
    }

    private class AdditionalsTab extends JPanel {

        private AdditionalsTab() {
            super(new BorderLayout());
            add(new JScrollPane(constructMappableTable(additionalFields)), BorderLayout.CENTER);
        }
    }

    /**
     * Construct a 3-column table with detected ESE fields on the left side and target ESE fields
     * in the middle. The third column is containing a checkbox where the mapping can be accepted
     * or ignored during normalization.
     *
     * @param comboBox which combobox to render
     * @return The table
     */
    private JTable constructMappableTable(JComboBox comboBox) {
        final JTable jTable = new JTable(new DefaultTableModel() {

            String[] columnNames = {"Source", "Target", "Normalize?"};

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
                Object o = getValueAt(rowIndex, columnIndex);
                return o instanceof String || o instanceof Boolean || o instanceof SourceField || o instanceof JComboBox;
            }

            @Override
            public void fireTableCellUpdated(int row, int column) {
                try {
                    String target = getValueAt(row, 1).toString();
                    String source = getValueAt(row, 0).toString();
                    if ((Boolean) getValueAt(row, 2) && !NO_MAPPING.equals(target)) {
                        groovyMapping.storeNode(new GroovyMapping.Delimiter(String.format("%s:%s", source, target)), GroovyService.generateGroovyLoop(source, target));
                    }
                    else {
                        groovyMapping.deleteNode(new GroovyMapping.Delimiter(String.format("%s:%s", source, target)));
                    }
                }
                catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving snippet " + e.getMessage());
                }
            }
        });
        jTable.setFillsViewportHeight(true);
        jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable.getColumnModel().getColumn(0).setCellRenderer(new SourceFieldRenderer());
        jTable.setDefaultEditor(SourceField.class, new SourceFieldEditor());
        jTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox)); // todo: retrieve from nodes
        return jTable;
    }

    private class SourceFieldRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return (Component) value;
        }
    }

    private class SourceFieldEditor extends AbstractCellEditor implements TableCellEditor {
        private SourceField sourceField;

        @Override
        public Object getCellEditorValue() {
            return sourceField;
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
            this.sourceField = (SourceField) value;
            sourceField.getjButton().setToolTipText("Add extractor");
            sourceField.getjButton().addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            System.out.printf("Inserting after %d for total %d%n", row, table.getModel().getRowCount());
                            // todo: add new row
//                           ((DefaultTableModel) table.getModel()).insertRow(row, new Object[] {"abc", "def", true});
                        }
                    }
            );
            return sourceField;
        }
    }


    interface Listener {
        void mappingCreated(StringBuffer mapping);
    }

    public MappingsPanel(final GroovyMapping groovyMapping, final Listener listener) {
        super(new BorderLayout());
        JTabbedPane mappingsTabs = new JTabbedPane();
        mappingsTabs.addTab("Mappables", new MappablesTab());
        mappingsTabs.addTab("Additionals", new AdditionalsTab());
        add(mappingsTabs, BorderLayout.CENTER);
        this.groovyMapping = groovyMapping;
        JButton saveButton = new JButton("save");
        saveButton.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            listener.mappingCreated(groovyMapping.createMapping());
                        }
                        catch (IOException e1) {
                            e1.printStackTrace();  // todo: handle catch
                        }
                    }
                }
        );
        add(saveButton, BorderLayout.SOUTH);
    }

    private class SourceField extends JPanel {

        private JButton jButton = new JButton("+");
        private String name;

        public JButton getjButton() {
            return jButton;
        }

        private SourceField(String name) {
            super(new BorderLayout());
            this.name = name;
            add(new JLabel(name), BorderLayout.CENTER);
            add(jButton, BorderLayout.EAST);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private boolean isMappable(String v) {
        v = v.substring(v.lastIndexOf(".") + 1); // stripping prefix
        for (Object value : annotationProcessor.getMappableFields()) {
            EuropeanaField field = (EuropeanaField) value;
            if (v.equals(field.getFieldNameString()) && field.isMappable()) {
                try {
                    groovyMapping.storeNode(new GroovyMapping.Delimiter(String.format("%s:%s", v, v)), GroovyService.generateGroovyLoop(v, v));
                }
                catch (IOException e) {
                    e.printStackTrace();  // todo: handle catch
                }
                return true;
            }
        }
        return false;
    }

    public void updateAvailableNodes(java.util.List<String> nodes) {
        int counter = 0;
        data = new Object[nodes.size()][];
        JComboBox availableNodes = new JComboBox(nodes.toArray());
        availableNodes.setOpaque(false);
        for (String s : nodes) {
            if (isMappable(s)) {
                data[counter] = new Object[]{new SourceField(s.substring(s.lastIndexOf(".") + 1)), s.substring(s.lastIndexOf(".") + 1), isMappable(s)};
            }
            else {
                data[counter] = new Object[]{new SourceField(s.substring(s.lastIndexOf(".") + 1)), NO_MAPPING, isMappable(s)};
            }
            counter++;
        }
    }
}
