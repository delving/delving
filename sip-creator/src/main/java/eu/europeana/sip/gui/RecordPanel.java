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

import eu.europeana.sip.model.CompileModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
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

    public RecordPanel(SipModel sipModel, CompileModel compileModel) {
        super(new BorderLayout(5, 5));
        this.sipModel = sipModel;
        setBorder(BorderFactory.createTitledBorder("Parsed Record"));
        final JTextArea area = new JTextArea(compileModel.getInputDocument());
        area.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        area.setCaretPosition(0);
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
            }
        });
        area.setEditable(false);
        JPanel grid = new JPanel(new GridLayout(1, 0, 5, 5));
        grid.add(nextButton);
        grid.add(rewindButton);
        add(scroll(area), BorderLayout.CENTER);
        add(grid, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(240, 500));
        wireUp();
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(240, 300));
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