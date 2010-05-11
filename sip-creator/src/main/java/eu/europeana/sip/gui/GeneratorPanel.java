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

import eu.europeana.sip.groovy.CodeTemplate;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The GUI element handling code generation
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GeneratorPanel extends JPanel {
    private TemplateModel templateModel = new TemplateModel();
    private JComboBox templateBox = new JComboBox(templateModel);
    private JTextArea explanationArea = new JTextArea();

    public GeneratorPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Generator"));
        add(templateBox, BorderLayout.NORTH);
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        add(scroll(explanationArea), BorderLayout.CENTER);
        setPreferredSize(new Dimension(300,400));
    }

    public CodeTemplate getSelectedTemplate() {
        return templateModel.getSelectedTemplate();
    }

    public boolean refresh(Collection freshTemplates) {
        return templateModel.refresh(freshTemplates);
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(400, 400));
        return scroll;
    }

    private class TemplateModel extends AbstractListModel implements ComboBoxModel {
        private Object selectedObject;
        private List<CodeTemplate> templates = new ArrayList<CodeTemplate>();

        @SuppressWarnings("unchecked")
        public boolean refresh(Collection freshTemplates) {
            int sizeBefore = getSize();
            templates.clear();
            fireIntervalRemoved(this, 0, sizeBefore);
            templates.addAll(freshTemplates);
            fireIntervalAdded(this, 0, getSize());
            if (!templates.isEmpty()) {
                setSelectedItem(templates.get(0));
                return true;
            }
            else {
                setSelectedItem(null);
                return false;
            }
        }

        public CodeTemplate getSelectedTemplate() {
            return (CodeTemplate) selectedObject;
        }

        @Override
        public void setSelectedItem(Object item) {
            if ((selectedObject != null && !selectedObject.equals(item)) || selectedObject == null && item != null) {
                selectedObject = item;
                if (selectedObject == null) {
                    explanationArea.setText("");
                }
                else {
                    explanationArea.setText(((CodeTemplate) selectedObject).getExplanation());
                }
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public Object getSelectedItem() {
            return selectedObject;
        }

        @Override
        public int getSize() {
            return templates.size();
        }

        @Override
        public Object getElementAt(int index) {
            return templates.get(index);
        }
    }


//    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
//        SpringLayout layout = (SpringLayout) parent.getLayout();
//        Component c = parent.getComponent(row * cols + col);
//        return layout.getConstraints(c);
//    }
//
//    private static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
//        SpringLayout layout = (SpringLayout) parent.getLayout();
//        //Align all cells in each column and make them the same width.
//        Spring x = Spring.constant(initialX);
//        for (int c = 0; c < cols; c++) {
//            Spring width = Spring.constant(0);
//            for (int r = 0; r < rows; r++) {
//                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
//            }
//            for (int r = 0; r < rows; r++) {
//                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
//                constraints.setX(x);
//                constraints.setWidth(width);
//            }
//            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
//        }
//
//        //Align all cells in each row and make them the same height.
//        Spring y = Spring.constant(initialY);
//        for (int r = 0; r < rows; r++) {
//            Spring height = Spring.constant(0);
//            for (int c = 0; c < cols; c++) {
//                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
//            }
//            for (int c = 0; c < cols; c++) {
//                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
//                constraints.setY(y);
//                constraints.setHeight(height);
//            }
//            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
//        }
//
//        //Set the parent's size.
//        SpringLayout.Constraints pCons = layout.getConstraints(parent);
//        pCons.setConstraint(SpringLayout.EAST, x);
//    }

}