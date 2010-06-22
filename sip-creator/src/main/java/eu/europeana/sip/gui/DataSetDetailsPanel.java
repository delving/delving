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

import eu.europeana.sip.model.DataSetDetails;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;

/**
 * Present a number of fields in a form which can be used as global
 * values during mapping/normalization
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetDetailsPanel extends JPanel {
    private JTextField specField = new JTextField();
    private JTextField nameField = new JTextField();
    private JTextField providerNameField = new JTextField();
    private JTextArea descriptionField = new JTextArea(3, 60);
    private JTextField namespaceField = new JTextField();
    private JTextField schemaField = new JTextField();
    private JTextField recordRootField = new JTextField();
    private JTextField uniqueElementField = new JTextField();

    public DataSetDetailsPanel() {
        super(new SpringLayout());
        setBorder(BorderFactory.createTitledBorder("Data Set Details"));
        addField("Data Set Spec", specField);
        addField("Name", nameField);
        addField("Provider Name", providerNameField);
        descriptionField.setLineWrap(true);
        addField("Description", descriptionField);
        addField("Namespace", namespaceField);
        addField("Schema", schemaField);
        recordRootField.setEditable(false);
        addField("Record Root", recordRootField);
        uniqueElementField.setEditable(false);
        addField("Unique Element", uniqueElementField);
        LayoutUtil.makeCompactGrid(this, getComponentCount() / 2, 2, 5, 5, 5, 5);
        setPreferredSize(new Dimension(400, 400));
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

    public void setDetails(DataSetDetails details) {
        specField.setText(details.getSpec());
        nameField.setText(details.getName());
        providerNameField.setText(details.getProviderName());
        descriptionField.setText(details.getDescription());
        namespaceField.setText(details.getNamespace());
        schemaField.setText(details.getSchema());
        recordRootField.setText(details.getRecordRoot());
        uniqueElementField.setText(details.getUniqueElement());
    }

    public DataSetDetails getDetails() {
        DataSetDetails details = new DataSetDetails();
        details.setSpec(specField.getText().trim());
        details.setName(nameField.getText().trim());
        details.setProviderName(providerNameField.getText().trim());
        details.setDescription(descriptionField.getText().trim());
        details.setNamespace(namespaceField.getText().trim());
        details.setSchema(schemaField.getText().trim());
        details.setRecordRoot(recordRootField.getText().trim());
        details.setUniqueElement(uniqueElementField.getText().trim());
        return details;
    }

}