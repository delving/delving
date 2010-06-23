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

import eu.europeana.definitions.annotations.EuropeanaField;
import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.DataSetDetails;
import eu.europeana.sip.core.FieldMapping;
import eu.europeana.sip.core.RecordRoot;
import eu.europeana.sip.model.AnalysisTree;
import eu.europeana.sip.model.FieldListModel;
import eu.europeana.sip.model.FieldMappingListModel;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.model.VariableListModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class MappingPanel extends JPanel {
    private static final String CREATE = "Create mapping";
    private static final String CREATE_FOR = "<html><center>Create mapping for<br><b>%s</b>";
    private static final String CREATE_OBVIOUS_FOR = "<html><center>Create obvious mapping for<br><b>%s</b>";
    private static final Dimension PREFERRED_SIZE = new Dimension(300, 700);
    private SipModel sipModel;
    private JTextField constantField = new JTextField("?");
    private JButton createMappingButton = new JButton(String.format(CREATE_FOR, "?"));
    private JButton removeMappingButton = new JButton("Remove the selected mapping");
    private JList variablesList, mappingList, fieldList;

    public MappingPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 1;
        add(createInputPanel(), gbc);
        gbc.gridx++;
        add(createFieldsPanel(), gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        add(createStatisticsPanel(), gbc);
        gbc.gridx++;
        add(createFieldMappingListPanel(), gbc);
        wireUp();
    }

    private JPanel createInputPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(createVariablesPanel(), BorderLayout.CENTER);
        p.add(createConstantFieldPanel(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createVariablesPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Source Fields"));
        variablesList = new JList(sipModel.getVariablesListWithCountsModel());
        p.add(scroll(variablesList), BorderLayout.CENTER);
        return p;
    }

    private JPanel createConstantFieldPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Constant Value Source"));
        p.add(constantField);
        return p;
    }

    private JPanel createFieldsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Unmapped Target Fields"));
        fieldList = new JList(sipModel.getUnmappedFieldListModel());
        fieldList.setCellRenderer(new FieldListModel.CellRenderer());
        fieldList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(scroll(fieldList));
        return p;
    }

    private JPanel createStatisticsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setPreferredSize(PREFERRED_SIZE);
        p.setBorder(BorderFactory.createTitledBorder("Statistics"));
        JPanel tablePanel = new JPanel(new BorderLayout());
        JTable statsTable = new JTable(sipModel.getStatisticsTableModel(), createStatsColumnModel());
        statsTable.getTableHeader().setReorderingAllowed(false);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePanel.add(statsTable.getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(scroll(statsTable), BorderLayout.CENTER);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel createFieldMappingListPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Field Mappings"));
        mappingList = new JList(sipModel.getFieldMappingListModel());
        mappingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mappingList.setCellRenderer(new FieldMappingListModel.CellRenderer());
        p.add(scroll(mappingList), BorderLayout.CENTER);
        p.add(createButtonPanel(), BorderLayout.EAST);
        return p;
    }

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 10, 30, 10);
        gbc.gridy = gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        createMappingButton.setEnabled(false);
        p.add(createMappingButton, gbc);
        gbc.gridy++;
        removeMappingButton.setEnabled(false);
        p.add(removeMappingButton, gbc);
        p.setPreferredSize(new Dimension(350, 300));
        return p;
    }

    private DefaultTableColumnModel createStatsColumnModel() {
        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        columnModel.addColumn(new TableColumn(0));
        columnModel.getColumn(0).setHeaderValue("Percent");
        columnModel.getColumn(0).setMaxWidth(80);
        columnModel.addColumn(new TableColumn(1));
        columnModel.getColumn(1).setHeaderValue("Count");
        columnModel.getColumn(1).setMaxWidth(80);
        columnModel.addColumn(new TableColumn(2));
        columnModel.getColumn(2).setHeaderValue("Value");
        return columnModel;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(400, 400));
        return scroll;
    }

    private void wireUp() {
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedFileSet(FileSet fileSet, DataSetDetails details) {
                variablesList.clearSelection();
                fieldList.clearSelection();
                mappingList.clearSelection();
                prepareCreateMappingButton();
            }

            @Override
            public void updatedRecordRoot(RecordRoot recordRoot) {
            }

            @Override
            public void updatedConstantFieldModel(ConstantFieldModel constantFieldModel) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
            }
        });
        createMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFieldMapping();
                variablesList.clearSelection();
                fieldList.clearSelection();
                mappingList.setSelectedIndex(mappingList.getModel().getSize() - 1);
//                prepareCreateMappingButton();
            }
        });
        removeMappingButton.setEnabled(false);
        removeMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    sipModel.removeFieldMapping(fieldMapping);
                }
            }
        });
        mappingList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    removeMappingButton.setEnabled(true);
                }
                else {
                    removeMappingButton.setEnabled(false);
                }
            }
        });
        constantField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                variablesList.clearSelection();
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        variablesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                VariableListModel.VariableHolder holder = (VariableListModel.VariableHolder) variablesList.getSelectedValue();
                if (holder != null) {
                    sipModel.selectNode(holder.getNode());
                    constantField.setText("?");
                }
            }
        });
        sipModel.getFieldMappingListModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                prepareCreateMappingButton();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                prepareCreateMappingButton();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                prepareCreateMappingButton();
            }
        });
        fieldList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                prepareCreateMappingButton();
            }
        });
    }

    private void prepareCreateMappingButton() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String fieldName = null;
                boolean obvious = false;
                EuropeanaField field = (EuropeanaField) fieldList.getSelectedValue();
                if (field != null) {
                    fieldName = field.getFieldNameString();
                }
                else {
                    field = getObviousMappingField();
                    if (field != null) {
                        fieldName = field.getFieldNameString();
                        obvious = true;
                    }
                }
                if (fieldName != null) {
                    createMappingButton.setText(String.format(obvious ? CREATE_OBVIOUS_FOR : CREATE_FOR, field.getFieldNameString()));
                    createMappingButton.setEnabled(true);
                }
                else {
                    createMappingButton.setText(CREATE);
                    createMappingButton.setEnabled(false);
                }
            }
        });
    }

    private void addFieldMapping() {
        EuropeanaField field = (EuropeanaField) fieldList.getSelectedValue();
        if (field != null) {
            FieldMapping fresh = new FieldMapping(field);
            Object[] selected = variablesList.getSelectedValues();
            if (selected.length == 0) {
                fresh.addCodeLine(String.format(
                        "%s.%s '%s'",
                        field.getPrefix(),
                        field.getLocalName(),
                        constantField.getText()
                ));
            }
            else {
                for (Object variableHolderObject : variablesList.getSelectedValues()) {
                    VariableListModel.VariableHolder variableHolder = (VariableListModel.VariableHolder) variableHolderObject;
                    generateCopyCode(fresh.getEuropeanaField(), variableHolder.getNode(), fresh);
                }
            }
            sipModel.addFieldMapping(fresh);
        }
        else {
            FieldMapping obviousMapping = createObviousMapping();
            if (obviousMapping != null) {
                sipModel.addFieldMapping(obviousMapping);
            }
        }
    }

    private FieldMapping createObviousMapping() {
        EuropeanaField field = getObviousMappingField();
        if (field != null) {
            FieldMapping obvious = new FieldMapping(field);
            if (field.europeana().constant()) {
                obvious.addCodeLine(String.format(
                        "%s.%s %s",
                        field.getPrefix(),
                        field.getLocalName(),
                        field.getFieldNameString()
                ));
            }
            else {
                for (int walkVar = 0; walkVar < sipModel.getVariablesListModel().getSize(); walkVar++) {
                    VariableListModel.VariableHolder variableHolder = (VariableListModel.VariableHolder) sipModel.getVariablesListModel().getElementAt(walkVar);
                    String variableName = variableHolder.getVariableName();
                    String fieldName = field.getFieldNameString();
                    if (variableName.endsWith(fieldName)) {
                        generateCopyCode(field, variableHolder.getNode(), obvious);
                    }
                }
            }
            if (!obvious.isEmpty()) {
                return obvious;
            }
        }
        return null;
    }

    private EuropeanaField getObviousMappingField() {
        for (int walkField = 0; walkField < sipModel.getUnmappedFieldListModel().getSize(); walkField++) {
            EuropeanaField field = (EuropeanaField) sipModel.getUnmappedFieldListModel().getElementAt(walkField);
            if (field.europeana().constant()) {
                return field;
            }
            for (int walkVar = 0; walkVar < sipModel.getVariablesListModel().getSize(); walkVar++) {
                VariableListModel.VariableHolder variableHolder = (VariableListModel.VariableHolder) sipModel.getVariablesListModel().getElementAt(walkVar);
                String variableName = variableHolder.getVariableName();
                String fieldName = field.getFieldNameString();
                if (variableName.endsWith(fieldName)) {
                    return field;
                }
            }
        }
        return null;
    }

    private void generateCopyCode(EuropeanaField field, AnalysisTree.Node node, FieldMapping fieldMapping) {
        if (field.solr().multivalued()) {
            fieldMapping.addCodeLine(String.format("%s.each {", node.getVariableName()));
            if (field.europeana().converter().isEmpty()) {
                fieldMapping.addCodeLine(String.format("%s.%s it", field.getPrefix(), field.getLocalName()));
            }
            else if (field.europeana().converterMultipleOutput()) {
                fieldMapping.addCodeLine(String.format("for (part in %s(it)) {", field.europeana().converter()));
                fieldMapping.addCodeLine(String.format("%s.%s part", field.getPrefix(), field.getLocalName()));
                fieldMapping.addCodeLine("}");
            }
            else {
                fieldMapping.addCodeLine(String.format("%s.%s %s(it)", field.getPrefix(), field.getLocalName(), field.europeana().converter()));
            }
            fieldMapping.addCodeLine("}");
        }
        else {
            if (field.europeana().converter().isEmpty()) {
                fieldMapping.addCodeLine(String.format("%s.%s %s[0]", field.getPrefix(), field.getLocalName(), node.getVariableName()));
            }
            else if (field.europeana().converterMultipleOutput()) {
                fieldMapping.addCodeLine(String.format("for (part in %s(%s[0])) {", field.europeana().converter(), node.getVariableName()));
                fieldMapping.addCodeLine(String.format("%s.%s part", field.getPrefix(), field.getLocalName()));
                fieldMapping.addCodeLine("}");
            }
            else {
                fieldMapping.addCodeLine(String.format("%s.%s %s(%s[0])", field.getPrefix(), field.getLocalName(), field.europeana().converter(), node.getVariableName()));
            }
        }
    }
}