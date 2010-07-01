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

import eu.europeana.sip.groovy.FieldMapping;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Handle checkboxes for a list of field mappings
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ObviousMappingDialog extends JDialog {

    private List<MappingCheckBox> boxes = new ArrayList<MappingCheckBox>();

    public interface Creator {
        void createMapping(FieldMapping mapping);
    }

    public ObviousMappingDialog(Frame owner, List<FieldMapping> mappings, final Creator creator) {
        super(owner, "Obvious Mappings");
        JPanel boxPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        boxPanel.setBorder(BorderFactory.createTitledBorder("Fields"));
        for (FieldMapping mapping : mappings) {
            MappingCheckBox box = new MappingCheckBox(mapping);
            boxPanel.add(box);
            boxes.add(box);
        }
        getContentPane().add(boxPanel, BorderLayout.CENTER);
        JButton okButton = new JButton("Generate selected obvious mappings");
        getContentPane().add(okButton, BorderLayout.SOUTH);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (MappingCheckBox box : boxes) {
                    if (box.isSelected()) {
                        creator.createMapping(box.mapping);
                    }
                }
                setVisible(false);
            }
        });
        pack();
        setLocationRelativeTo(null);
    }

    private class MappingCheckBox extends JCheckBox {
        private FieldMapping mapping;

        private MappingCheckBox(FieldMapping mapping) {
            super(mapping.getEuropeanaField().getFieldNameString(), true);
            this.mapping = mapping;
        }
    }
}
