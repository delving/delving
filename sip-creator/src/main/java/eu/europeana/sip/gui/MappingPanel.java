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
import eu.europeana.sip.groovy.Conversion;
import eu.europeana.sip.groovy.FieldMapping;
import eu.europeana.sip.model.FieldListModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class MappingPanel extends JPanel {
    private SipModel sipModel;
    private JButton createMappingButton = new JButton("Create Mapping");
    private JComboBox conversionChoice = new JComboBox(Conversion.values());
    private JTextArea groovyCodeArea = new JTextArea();
    private JList variablesList, mappingList, fieldList;

    public MappingPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        gbc.weighty = 0.8;
        add(createInputPanel(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.1;
        add(createMappingButton, gbc);
        gbc.gridy++;
        gbc.weighty = 0.8;
        add(createOutputPanel(), gbc);
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 0.8;
        add(createFieldMappingListPanel(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.1;
        add(createConverterChoice(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.8;
        add(createGroovyPanel(), gbc);
        wireUp();
    }

    private void wireUp() {
        createMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FieldMapping fieldMapping = new FieldMapping((Conversion) conversionChoice.getSelectedItem());
                for (Object variable : variablesList.getSelectedValues()) {
                    fieldMapping.addFromVariable((String) variable);
                }
                for (Object field : fieldList.getSelectedValues()) {
                    fieldMapping.addToField(((EuropeanaField)field).getFieldNameString());
                }
                sipModel.addFieldMapping(fieldMapping);
            }
        });
        conversionChoice.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // todo: implement
            }
        });
        variablesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // todo: implement
            }
        });
        fieldList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // todo: implement
            }
        });
        mappingList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // todo: implement
            }
        });
    }

    private JPanel createInputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Unmapped Variables"));
        variablesList = new JList(sipModel.getUnmappedVariablesListModel());
        JScrollPane scroll = new JScrollPane(variablesList);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        p.add(scroll);
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Unmapped Fields"));
        fieldList = new JList(sipModel.getUnmappedFieldListModel());
        fieldList.setCellRenderer(new FieldListModel.CellRenderer());
        JScrollPane scroll = new JScrollPane(fieldList);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        p.add(scroll);
        return p;
    }

    private JPanel createFieldMappingListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Field Mappings"));
        mappingList = new JList(sipModel.getFieldMappingListModel());
        JScrollPane scroll = new JScrollPane(mappingList);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        p.add(scroll);
        return p;
    }

    private JPanel createConverterChoice() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Converter:", JLabel.RIGHT), BorderLayout.WEST);
        p.add(conversionChoice, BorderLayout.CENTER);
        return p;
    }

    private JPanel createGroovyPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        JScrollPane scroll = new JScrollPane(groovyCodeArea);
        p.add(scroll);
        return p;
    }
}