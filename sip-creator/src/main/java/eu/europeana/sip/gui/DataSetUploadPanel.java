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

import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.DataSetDetails;
import eu.europeana.sip.core.RecordRoot;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Present a number of fields in a form which can be used as global
 * values during mapping/normalization
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetUploadPanel extends JPanel {
    private static final int FIELD_SIZE = 80;
    private SipModel sipModel;
    private JTextField specField = new JTextField(FIELD_SIZE);
    private JTextField nameField = new JTextField(FIELD_SIZE);
    private JTextField providerNameField = new JTextField(FIELD_SIZE);
    private JTextArea descriptionField = new JTextArea(3, 30);
    private JTextField prefixField = new JTextField(FIELD_SIZE);
    private JTextField namespaceField = new JTextField(FIELD_SIZE);
    private JTextField schemaField = new JTextField(FIELD_SIZE);
    private JTextField recordRootField = new JTextField(FIELD_SIZE);
    private JTextField uniqueElementField = new JTextField(FIELD_SIZE);
    private JButton createUploadZipButton = new JButton("Create and upload ZIP File");

    public DataSetUploadPanel(SipModel sipModel) {
        super(new SpringLayout());
        this.sipModel = sipModel;
        setBorder(BorderFactory.createTitledBorder("Data Set Details"));
        addField("Data Set Spec", specField);
        specField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                noSpaces();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                noSpaces();
            }

            private void noSpaces() {
                String with = specField.getText();
                final String without = with.replaceAll("\\s+","");
                if (!with.equals(without)) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            int dot = specField.getCaret().getDot();
                            specField.setText(without);
                            specField.getCaret().setDot(dot);
                        }
                    });
                }
            }
        });
        addField("Name", nameField);
        addField("Provider Name", providerNameField);
        descriptionField.setLineWrap(true);
        addField("Description", descriptionField);
        addField("Prefix", prefixField);
        addField("Namespace", namespaceField);
        addField("Schema", schemaField);
        recordRootField.setEditable(false);
        addField("Record Root", recordRootField);
        uniqueElementField.setEditable(false);
        addField("Unique Element", uniqueElementField);
        // button line
        add(new JLabel(""));
        add(createUploadZipButton);
        // progress bars
        add(new JLabel("Create ZIP File:", JLabel.RIGHT));
        add(new JProgressBar(sipModel.getZipProgress()));
        add(new JLabel("Upload ZIP File:", JLabel.RIGHT));
        add(new JProgressBar(sipModel.getUploadProgress()));
        // finish up
        LayoutUtil.makeCompactGrid(this, getComponentCount() / 2, 2, 5, 5, 5, 5);
        setPreferredSize(new Dimension(480, 500));
        wireUp();
    }

    private void addField(String prompt, JTextComponent textComponent) {
        JLabel label = new JLabel(prompt, JLabel.RIGHT);
        label.setLabelFor(textComponent);
        add(label);
        if (textComponent instanceof JTextArea) {
            JScrollPane scroll = new JScrollPane(textComponent);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            add(scroll);
        }
        else {
            add(textComponent);
        }
    }

    private void setDetails(DataSetDetails details) {
        specField.setText(details.getSpec());
        nameField.setText(details.getName());
        providerNameField.setText(details.getProviderName());
        descriptionField.setText(details.getDescription());
        prefixField.setText(details.getPrefix());
        namespaceField.setText(details.getNamespace());
        schemaField.setText(details.getSchema());
        recordRootField.setText(details.getRecordRoot());
        uniqueElementField.setText(details.getUniqueElement());
    }

    private DataSetDetails getDetails() {
        DataSetDetails details = new DataSetDetails();
        details.setSpec(specField.getText().trim());
        details.setName(nameField.getText().trim());
        details.setProviderName(providerNameField.getText().trim());
        details.setDescription(descriptionField.getText().trim());
        details.setPrefix(prefixField.getText().trim());
        details.setNamespace(namespaceField.getText().trim());
        details.setSchema(schemaField.getText().trim());
        details.setRecordRoot(recordRootField.getText().trim());
        details.setUniqueElement(uniqueElementField.getText().trim());
        return details;
    }

    private void wireUp() {
        createUploadZipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.setDataSetDetails(getDetails());
                sipModel.createUploadZipFile();
            }
        });
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedFileSet(FileSet fileSet) {
                createUploadZipButton.setEnabled(fileSet.getReport() != null);
            }

            @Override
            public void updatedDetails(DataSetDetails dataSetDetails) {
                setDetails(sipModel.getDataSetDetails());
            }

            @Override
            public void updatedRecordRoot(RecordRoot recordRoot) {
            }

            @Override
            public void updatedConstantFieldModel(ConstantFieldModel constantFieldModel) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
                createUploadZipButton.setEnabled(complete);
            }
        });
    }
}