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

import eu.delving.core.metadata.ConstantInputDefinition;
import eu.europeana.sip.model.ConstantFieldModel;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Present a number of fields in a form which can be used as global
 * values during mapping/normalization
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ConstantFieldPanel extends JPanel {
    private ConstantFieldModel constantFieldModel;
    private FieldComponent[] fieldComponent;

    public ConstantFieldPanel(ConstantFieldModel constantFieldModel) {
        super(new SpringLayout());
        setBorder(BorderFactory.createTitledBorder("Constant Fields"));
        this.constantFieldModel = constantFieldModel;
        constantFieldModel.addListener(new ModelAdapter());
        refreshStructure();
    }

    private void refreshStructure() {
        removeAll();
        fieldComponent = new FieldComponent[constantFieldModel.getDefinitions().size()];
        int index = 0;
        for (ConstantInputDefinition cid : constantFieldModel.getDefinitions()) {
            fieldComponent[index++] = new FieldComponent(cid);
        }
        LayoutUtil.makeCompactGrid(this, getComponentCount() / 2, 2, 5, 5, 5, 5);
        setPreferredSize(new Dimension(400, 400));
    }

    public void refreshContent() {
        for (FieldComponent field : fieldComponent) {
            field.getValue();
        }
    }

    private class FieldComponent {
        private ConstantInputDefinition inputDefinition;
        private JTextField textField;
        private JComboBox comboBox;

        private FieldComponent(ConstantInputDefinition inputDefinition) {
            this.inputDefinition = inputDefinition;
            if (inputDefinition.fieldDefinition == null || inputDefinition.fieldDefinition.options == null) {
                createTextField();
            }
            else {
                createComboBox();
            }
        }

        private void createComboBox() {
            comboBox = new JComboBox(inputDefinition.fieldDefinition.options.toArray());
            comboBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    Object selected = comboBox.getSelectedItem();
                    if (selected != null) {
                        setValue();
                    }
                }
            });
            JLabel label = new JLabel(inputDefinition.prompt, JLabel.RIGHT);
            label.setLabelFor(comboBox);
            comboBox.setToolTipText(inputDefinition.toolTip);
            label.setToolTipText(inputDefinition.toolTip);
            ConstantFieldPanel.this.add(label);
            ConstantFieldPanel.this.add(comboBox);
        }

        private void createTextField() {
            textField = new JTextField();
            textField.setText("");
            textField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    setValue();
                }
            });
            textField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    setValue();
                }
            });
            if (inputDefinition.automatic != null && inputDefinition.automatic) {
                textField.setEditable(false);
            }
            JLabel label = new JLabel(inputDefinition.prompt, JLabel.RIGHT);
            label.setLabelFor(textField);
            textField.setToolTipText(inputDefinition.toolTip);
            label.setToolTipText(inputDefinition.toolTip);
            ConstantFieldPanel.this.add(label);
            ConstantFieldPanel.this.add(textField);
        }

        private void setValue() {
            if (textField != null) {
                constantFieldModel.set(inputDefinition, textField.getText());
            }
            else {
                constantFieldModel.set(inputDefinition, comboBox.getSelectedItem().toString());
            }
        }

        public void getValue() {
            String text = constantFieldModel.get(inputDefinition);
            if (textField != null) {
                textField.setText(text);
            }
            else {
                comboBox.getModel().setSelectedItem(text);
            }
        }
    }

    private class ModelAdapter implements ConstantFieldModel.Listener {
        @Override
        public void updatedDefinitions(ConstantFieldModel constantFieldModel) {
            refreshStructure();
        }

        @Override
        public void updatedConstant(ConstantFieldModel constantFieldModel, boolean interactive) {
            refreshContent();
        }
    }
}
