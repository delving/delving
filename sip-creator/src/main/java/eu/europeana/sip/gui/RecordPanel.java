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

import eu.europeana.sip.model.MappingModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Show the current parsed record, and allow for moving to next, and rewinding
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordPanel extends JPanel {
    private SipModel sipModel;
    private JButton rewindButton = new JButton("Rewind");
    private JButton nextButton = new JButton("Next");

    public RecordPanel(SipModel sipModel, MappingModel mappingModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        setBorder(BorderFactory.createTitledBorder("Parsed Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.99;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea area = new JTextArea(mappingModel.getInputDocument());
        area.setEditable(false);
        add(scroll(area), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.weighty = 0.01;
        add(nextButton, gbc);
        gbc.gridx++;
        add(rewindButton, gbc);
        setPreferredSize(new Dimension(400, 500));
        wireUp();
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scroll;
    }

    private void wireUp() {
        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.firstRecord();
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.nextRecord();
            }
        });
    }
}