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

import eu.europeana.sip.groovy.FieldMapping;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

public class RefinementPanel extends JPanel {
    private static final Dimension PREFERRED_SIZE = new Dimension(300, 700);
    private SipModel sipModel;
    private JButton removeMappingButton = new JButton("Remove Selected Mapping");
    private JList mappingList;

    public RefinementPanel(SipModel sipModel) {
        super(new BorderLayout());
        this.sipModel = sipModel;
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(createLeftSide());
        split.setRightComponent(createRightSide());
        split.setDividerLocation(0.5);
        add(split, BorderLayout.CENTER);
        wireUp();
    }

    private JPanel createLeftSide() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        // input panel
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1; 
        gbc.weighty = 0.95;
        p.add(createFieldMappingListPanel(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.05;
        p.add(removeMappingButton, gbc);
        p.setPreferredSize(new Dimension(600, 800));
        return p;
    }

    private JPanel createRightSide() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        // output panel
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.3;
        gbc.gridy = gbc.gridx = 0;
        p.add(new RecordPanel(sipModel, sipModel.getFilteredMappingModel()), gbc);
        // converter choice
        gbc.gridy++;
        p.add(createGroovyPanel(), gbc);
        // create mapping button
        gbc.gridy++;
        p.add(createOutputPanel(), gbc);
        p.setPreferredSize(new Dimension(600, 800));
        return p;
    }


    private void wireUp() {
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
//                    groovyCodeArea.setText(fieldMapping.getCodeForDisplay());
                    sipModel.setFieldMapping(fieldMapping);
                    removeMappingButton.setEnabled(true);
                }
                else {
                    sipModel.setFieldMapping(null);
//                    groovyCodeArea.setText("");
                    removeMappingButton.setEnabled(false);
                }
            }
        });
    }

    private JPanel createFieldMappingListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Field Mappings"));
        mappingList = new JList(sipModel.getFieldMappingListModel());
        mappingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(scroll(mappingList));
        return p;
    }

    private JPanel createGroovyPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        JTextArea groovyCodeArea = new JTextArea(sipModel.getFilteredMappingModel().getCodeDocument());
        groovyCodeArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(groovyCodeArea);
        p.add(scroll);
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Record"));
        JTextArea outputArea = new JTextArea(sipModel.getFilteredMappingModel().getOutputDocument());
        outputArea.setEditable(false);
        p.add(scroll(outputArea));
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 800));
        return scroll;
    }

}