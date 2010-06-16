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

import eu.europeana.sip.model.ConstantFieldModel;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import java.awt.Component;
import java.awt.Container;
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
        fieldComponent = new FieldComponent[sipModel.getGlobalFieldModel().getFields().size()];
        setBorder(BorderFactory.createTitledBorder("Constant Fields"));
        int index = 0;
        for (ConstantFieldModel.FieldSpec fieldSpec : sipModel.getGlobalFieldModel().getFields()) {
            if (fieldSpec.getEnumValues() == null) {
                fieldComponent[index++] = new FieldComponent(fieldSpec);
            }
            else {
                fieldComponent[index++] = new FieldComponent(fieldSpec);
            }
        }
        makeCompactGrid(this, getComponentCount() / 2, 2, 5, 5, 5, 5);
        setPreferredSize(new Dimension(400, 400));
    }

    public void refresh() {
        ConstantFieldModel model = sipModel.getGlobalFieldModel();
        int index = 0;
        for (ConstantFieldModel.FieldSpec fieldSpec : model.getFields()) {
            fieldComponent[index++].setText(model.get(fieldSpec.getName()));
        }
    }

    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    private static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.EAST, x);
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
