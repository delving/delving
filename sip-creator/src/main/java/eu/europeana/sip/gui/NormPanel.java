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

import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormPanel extends JPanel {
    private SipModel sipModel;
    private JCheckBox discardInvalidBox = new JCheckBox("Discard Invalid Records");
    private JButton normalizeButton = new JButton("Normalize");
    private JButton abortButton = new JButton("Abort");

    public NormPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridy = 0;
        gbc.weightx = 0.333;
        gbc.weighty = 0.99;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        add(new RecordPanel(sipModel, sipModel.getRecordMappingModel()), gbc);
        gbc.gridx++;
        add(createCodePanel(), gbc);
        gbc.gridx++;
        add(createOutputPanel(), gbc);
        gbc.weighty = 0.01;
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy++;
        add(createNormalizePanel(), gbc);
        wireUp();
    }

    private JPanel createCodePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        JTextArea area = new JTextArea(sipModel.getRecordMappingModel().getCodeDocument());
        area.setEditable(false);
        p.add(scroll(area));
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Record"));
        JTextArea area = new JTextArea(sipModel.getRecordMappingModel().getOutputDocument());
        area.setEditable(false);
        p.add(scroll(area));
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 800));
        return scroll;
    }

    private JPanel createNormalizePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        JProgressBar progressBar = new JProgressBar(sipModel.getNormalizeProgress());
        progressBar.setBorderPainted(true);
        JPanel bp = new JPanel(new GridLayout(1, 0, 8, 8));
        bp.add(normalizeButton);
        bp.add(discardInvalidBox);
        p.add(bp, BorderLayout.WEST);
        p.add(progressBar, BorderLayout.CENTER);
        p.add(abortButton, BorderLayout.EAST);
        return p;
    }

    private void wireUp() {
        normalizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.normalize(discardInvalidBox.isSelected());
            }
        });
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.abortNormalize();
            }
        });
    }
}