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
import eu.europeana.sip.model.SipModel;

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
    private SipModel sipModel;
    private FieldComponent[] fieldComponent;

    public ConstantFieldPanel(SipModel sipModel) {
        super(new SpringLayout());
        this.sipModel = sipModel;
        fieldComponent = new FieldComponent[sipModel.getConstantFieldModel().getFields().size()];
        setBorder(BorderFactory.createTitledBorder("Constant Fields"));
        int index = 0;
        for (ConstantFieldModel.FieldSpec fieldSpec : sipModel.getConstantFieldModel().getFields()) {
            if (fieldSpec.getEnumValues() == null) {
                fieldComponent[index++] = new FieldComponent(fieldSpec);
            }
            else {
                fieldComponent[index++] = new FieldComponent(fieldSpec);
            }
        }
        LayoutUtil.makeCompactGrid(this, getComponentCount() / 2, 2, 5, 5, 5, 5);
        setPreferredSize(new Dimension(400, 400));
    }

    public void refresh() {
        ConstantFieldModel model = sipModel.getConstantFieldModel();
        int index = 0;
        for (ConstantFieldModel.FieldSpec fieldSpec : model.getFields()) {
            fieldComponent[index++].setText(model.get(fieldSpec.getName()));
        }
    }

    private class FieldComponent {
        private ConstantFieldModel.FieldSpec fieldSpec;
        private JTextField textField;
        private JComboBox comboBox;

        private FieldComponent(ConstantFieldModel.FieldSpec fieldSpec) {
            this.fieldSpec = fieldSpec;
            if (fieldSpec.getEnumValues() == null) {
                createTextField();
            }
            else {
                createComboBox();
            }
        }

        private void createComboBox() {
            comboBox = new JComboBox(fieldSpec.getEnumValues().toArray());
            comboBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    Object selected = comboBox.getSelectedItem();
                    if (selected != null) {
                        setValue();
                    }
                }
            });
            JLabel label = new JLabel(fieldSpec.getName(), JLabel.RIGHT);
            label.setLabelFor(comboBox);
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
            JLabel label = new JLabel(fieldSpec.getName(), JLabel.RIGHT);
            label.setLabelFor(textField);
            ConstantFieldPanel.this.add(label);
            ConstantFieldPanel.this.add(textField);
        }

        private void setValue() {
            if (textField != null) {
                sipModel.setGlobalField(fieldSpec.getName(), textField.getText());
            }
            else {
                sipModel.setGlobalField(fieldSpec.getName(), comboBox.getSelectedItem().toString());
            }
        }

        public void setText(String text) {
            if (textField != null) {
                textField.setText(text);
            }
            else {
                comboBox.getModel().setSelectedItem(text);
            }
        }
    }
}
