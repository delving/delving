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

import eu.delving.sip.ProgressListener;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.model.CompileModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
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
    private JButton seekButton = new JButton("Seek Record #");
    private JTextField seekField = new JTextField(15);
    private JTextField countField = new JTextField(15);
    private JButton nextButton = new JButton("Next");
    private MetadataRecord currentMetadataRecord;
    private int recordCount = -1;

    public RecordPanel(SipModel sipModel, CompileModel compileModel) {
        super(new BorderLayout(5, 5));
        this.sipModel = sipModel;
        setBorder(BorderFactory.createTitledBorder("Parsed Record"));
        final JEditorPane recordView = new JEditorPane();
        recordView.setContentType("text/html");
        recordView.setDocument(compileModel.getInputDocument());
        recordView.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        recordView.setCaretPosition(0);
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
        sipModel.addParseListener(new SipModel.ParseListener() {
            @Override
            public void updatedRecord(MetadataRecord metadataRecord) {
                currentMetadataRecord = metadataRecord;
                if (metadataRecord != null) {
                    seekField.setText(String.valueOf(metadataRecord.getRecordNumber()));
                    if (metadataRecord.getRecordCount() != recordCount) {
                        countField.setText(String.valueOf(recordCount = metadataRecord.getRecordCount()));
                    }
                }
            }
        });
        recordView.setEditable(false);
        JPanel grid = new JPanel(new GridLayout(1, 0, 5, 5));
        grid.add(seekButton);
        grid.add(seekField);
        countField.setEditable(false);
        grid.add(countField);
        grid.add(nextButton);
        add(scroll(recordView), BorderLayout.CENTER);
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
        seekButton.addActionListener(seek);
        seekField.addActionListener(seek);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.nextRecord();
            }
        });
    }

    private ActionListener seek = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            int recordNumber;
            try {
                recordNumber = Integer.parseInt(seekField.getText());
                if (recordNumber <= 0) {
                    recordNumber = 1;
                }
            }
            catch (NumberFormatException e) {
                recordNumber = 1;
            }
            if (currentMetadataRecord == null || recordNumber != currentMetadataRecord.getRecordNumber()) {
                seekButton.setEnabled(false);
                final ProgressMonitor progressMonitor = new ProgressMonitor(
                        SwingUtilities.getRoot(RecordPanel.this),
                        "<html><h2>Scanning</h2>",
                        "Input Records",
                        0,100
                );
                sipModel.seekRecord(recordNumber, new ProgressListener.Adapter(progressMonitor) {
                    @Override
                    public void swingFinished(boolean success) {
                        seekButton.setEnabled(true);
                    }
                });
            }
        }
    };
}