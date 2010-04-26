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

import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class MappingPanel extends JPanel {
    private SipModel sipModel;
    private JButton createMappingButton = new JButton("Create Mapping");
    private JComboBox converterChoice = new JComboBox(new Object[]{"Converter One", "Converter Two"});
    private JTextArea groovyCodeArea = new JTextArea();
    private GroovyEditor groovyEditor = new GroovyEditor(groovyCodeArea);

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
        add(createMappingListPanel(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.1;
        add(createConverterChoice(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.8;
        add(createGroovyPanel(), gbc);
    }

    public void setFileSet(final FileSet fileSet) {
        groovyEditor.setFileSet(fileSet);
    }

    private JPanel createInputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Input Variables"));
        JList list = new JList(new Object[]{"input", "variables"});
        JScrollPane scroll = new JScrollPane(list);
        p.add(scroll);
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Variables"));
        JList list = new JList(new Object[]{"output", "variables"});
        JScrollPane scroll = new JScrollPane(list);
        p.add(scroll);
        return p;
    }

    private JPanel createMappingListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Mappings"));
        JList list = new JList(new Object[]{"one mapping", "another"});
        JScrollPane scroll = new JScrollPane(list);
        p.add(scroll);
        return p;
    }

    private JPanel createConverterChoice() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Converter:", JLabel.RIGHT), BorderLayout.WEST);
        p.add(converterChoice, BorderLayout.CENTER);
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