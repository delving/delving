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

import eu.europeana.sip.core.FieldMapping;
import eu.europeana.sip.model.CompileModel;
import eu.europeana.sip.model.FieldMappingListModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
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

public class RefinementPanel extends JPanel {
    private SipModel sipModel;
    private JTextArea groovyCodeArea;
    private JButton removeMappingButton = new JButton("Remove Selected Mapping");
    private JButton valueMappingButton = new JButton("Edit Value Mapping");
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
        // record panel
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.3;
        gbc.gridy = gbc.gridx = 0;
        p.add(new RecordPanel(sipModel, sipModel.getFieldMappingModel()), gbc);
        // value map button
        gbc.gridy++;
        gbc.weighty = 0.05;
        valueMappingButton.setEnabled(false);
        p.add(valueMappingButton, gbc);
        // code panel
        gbc.gridy++;
        gbc.weighty = 0.3;
        p.add(createGroovyPanel(), gbc);
        // output panel
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
        valueMappingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ValueMapDialog dialog = new ValueMapDialog(
                        (Frame)SwingUtilities.getWindowAncestor(RefinementPanel.this),
                        sipModel.getFieldMappingModel().getRecordMapping()
                );
                dialog.setVisible(true);
            }
        });
        mappingList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                FieldMapping fieldMapping = (FieldMapping) mappingList.getSelectedValue();
                if (fieldMapping != null) {
                    sipModel.getFieldMappingModel().getRecordMapping().setFieldMapping(fieldMapping);
                    valueMappingButton.setEnabled(fieldMapping.getValueMap() != null);
                    removeMappingButton.setEnabled(true);
                }
                else {
                    sipModel.getFieldMappingModel().getRecordMapping().clear();
                    valueMappingButton.setEnabled(false);
                    removeMappingButton.setEnabled(false);
                }
            }
        });
        sipModel.getFieldMappingModel().getCodeDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                sipModel.getFieldMappingModel().setCode(groovyCodeArea.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                sipModel.getFieldMappingModel().setCode(groovyCodeArea.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                sipModel.getFieldMappingModel().setCode(groovyCodeArea.getText());
            }
        });
        groovyCodeArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                sipModel.getRecordMappingModel().refreshCode(); // todo: somebody else do this?
            }
        });
        sipModel.getFieldMappingModel().addListener(new ModelStateListener());
    }

    private JPanel createFieldMappingListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Field Mappings"));
        mappingList = new JList(sipModel.getFieldMappingListModel());
        mappingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mappingList.setCellRenderer(new FieldMappingListModel.CellRenderer());
        p.add(scroll(mappingList));
        return p;
    }

    private JPanel createGroovyPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        groovyCodeArea = new JTextArea(sipModel.getFieldMappingModel().getCodeDocument());
        JScrollPane scroll = new JScrollPane(groovyCodeArea);
        p.add(scroll);
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Record"));
        JTextArea outputArea = new JTextArea(sipModel.getFieldMappingModel().getOutputDocument());
        outputArea.setEditable(false);
        p.add(scroll(outputArea), BorderLayout.CENTER);
        p.add(new JLabel("Note: URLs can be launched by double-clicking them.", JLabel.CENTER), BorderLayout.SOUTH);
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 800));
        return scroll;
    }

    private class ModelStateListener implements CompileModel.Listener {

        @Override
        public void stateChanged(final CompileModel.State state) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    switch (state) {
                        case PRISTINE:
                        case UNCOMPILED:
                            groovyCodeArea.setBackground(new Color(1.0f, 1.0f, 1.0f));
                            break;
                        case EDITED:
                            groovyCodeArea.setBackground(new Color(1.0f, 1.0f, 0.9f));
                            break;
                        case ERROR:
                            groovyCodeArea.setBackground(new Color(1.0f, 0.9f, 0.9f));
                            break;
                        case COMMITTED:
                            groovyCodeArea.setBackground(new Color(0.9f, 1.0f, 0.9f));
                            break;
                    }
                }
            });
        }
    }
}