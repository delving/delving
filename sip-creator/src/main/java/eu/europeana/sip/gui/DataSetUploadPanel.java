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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;
import java.util.TreeMap;

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
    private Map<String, JTextField> linkedFields = new TreeMap<String, JTextField>();

    public DataSetUploadPanel(SipModel sipModel) {
        super(new SpringLayout());
        this.sipModel = sipModel;
        setBorder(BorderFactory.createTitledBorder("Data Set Details"));
        addFields();
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
        linkFields();
    }

    private void addFields() {
        addField(
                "Description",
                "A human-readable description of what the dataset contains.",
                descriptionField
        );
        addField(
                "Prefix",
                "The prefix associated with the uploaded format",
                prefixField
        );
        addField(
                "Namespace",
                "The URL associated with the prefix",
                namespaceField
        );
        addField(
                "Schema",
                "An URL linking to the schema",
                schemaField
        );

        addField(
                "Data Set Spec",
                "This value is copied from the europeana_collectionName constant.",
                specField
        );
        addField(
                "Name",
                "This value is copied from the europeana_collectionTitle constant.",
                nameField
        );
        addField(
                "Provider Name",
                "This value is copied from the europeana_provider constant.",
                providerNameField
        );

        addField(
                "Record Root",
                "This field is set automatically when a record root element is chosen.",
                recordRootField
        );
        addField(
                "Unique Element",
                "This field is set automatically when a unique element is chosen.",
                uniqueElementField
        );
    }

    private void linkFields() {
        linkField("europeana_collectionName", specField );
        linkField("europeana_collectionTitle", nameField );
        linkField("europeana_provider", providerNameField );
    }

    private void linkField(String fieldName, JTextField field) {
        linkedFields.put(fieldName, field);
        field.setEditable(false);
    }

    private void addField(String prompt, String description, JTextComponent textComponent) {
        String toolTip = String.format("<html><table cellpadding=10><tr><td><h3>%s</h3><hr><p><b>%s<b></p></td></html>", prompt, description);
        JLabel label = new JLabel(prompt, JLabel.RIGHT);
        label.setToolTipText(toolTip);
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
        textComponent.setToolTipText(toolTip);
        textComponent.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                sipModel.setDataSetDetails(getDetails());
            }
        });
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
        uniqueElementField.setEditable(false);
        descriptionField.setLineWrap(true);
        recordRootField.setEditable(false);
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
        sipModel.getConstantFieldModel().addListener(new ConstantFieldModel.Listener() {
            @Override
            public void updated(String fieldName, String value) {
                JTextField field = linkedFields.get(fieldName);
                if (field != null) {
                    field.setText(value);
                }
            }
        });
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
    }
}