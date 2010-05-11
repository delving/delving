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
import eu.europeana.sip.groovy.FieldMapping;
import eu.europeana.sip.model.AnalysisTree;
import eu.europeana.sip.model.FieldListModel;
import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.model.VariableListModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
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

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class MappingPanel extends JPanel {
    private static final Dimension PREFERRED_SIZE = new Dimension(300, 700);
    private SipModel sipModel;
    private JButton createMappingButton = new JButton("Create Mapping");
    private JButton removeMappingButton = new JButton("Remove Selected Mapping");
    private FieldMapping fieldMapping = new FieldMapping();
    private JList variablesList, mappingList, fieldList;

    public MappingPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 1;
        add(createVariablesPanel(), gbc);
        gbc.gridx++;
        add(createFieldsPanel(), gbc);
//        gbc.gridx++;
//        add(generatorPanel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        add(createStatisticsPanel(), gbc);
        gbc.gridx++;
//        gbc.gridwidth = 2;
        add(createFieldMappingListPanel(), gbc);
        wireUp();
    }

    private JPanel createVariablesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Unmapped Variables"));
        variablesList = new JList(sipModel.getUnmappedVariablesListModel());
        variablesList.setCellRenderer(new VariableListModel.CellRenderer());
        p.add(scroll(variablesList));
        return p;
    }

    private JPanel createFieldsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Unmapped Fields"));
        fieldList = new JList(sipModel.getUnmappedFieldListModel());
        fieldList.setCellRenderer(new FieldListModel.CellRenderer());
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
        p.add(scroll(mappingList), BorderLayout.CENTER);
        p.add(createButtonPanel(), BorderLayout.EAST);
        return p;
    }

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        createMappingButton.setEnabled(false);
        p.add(createMappingButton, gbc);
        gbc.gridy++;
        removeMappingButton.setEnabled(false);
        p.add(removeMappingButton, gbc);
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
        createMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // todo: generate some code
//                CodeTemplate codeTemplate = generatorPanel.getSelectedTemplate();
//                if (codeTemplate != null) {
//                    fieldMapping.getCodeLines().clear();
//                    for (Object line : codeTemplate.getCode(fieldMapping)) {
//                        fieldMapping.addCodeLine(line.toString());
//                    }
//                }
                sipModel.addFieldMapping(fieldMapping);
                variablesList.clearSelection();
                fieldList.clearSelection();
                mappingList.setSelectedIndex(mappingList.getModel().getSize() - 1);
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
        variablesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                AnalysisTree.Node node = (AnalysisTree.Node) variablesList.getSelectedValue();
                sipModel.selectNode(node);
                fieldMapping.getFromVariables().clear();
                for (Object variable : variablesList.getSelectedValues()) {
                    fieldMapping.addFromVariable(((AnalysisTree.Node) variable).getVariableName());
                }
//                createMappingButton.setEnabled(generatorPanel.refresh(Generator.getTemplates(fieldMapping)));
            }
        });
        fieldList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                fieldMapping.getToFields().clear();
                for (Object field : fieldList.getSelectedValues()) {
                    fieldMapping.addToField(((EuropeanaField) field).getFieldNameString());
                }
//                createMappingButton.setEnabled(generatorPanel.refresh(Generator.getTemplates(fieldMapping)));
            }
        });
    }

}