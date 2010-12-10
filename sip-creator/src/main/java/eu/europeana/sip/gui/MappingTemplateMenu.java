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

import eu.delving.metadata.RecordMapping;
import eu.europeana.sip.model.SipModel;
import org.apache.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class MappingTemplateMenu extends JMenu {
    private Logger log = Logger.getLogger(getClass());
    private Component parent;
    private SipModel sipModel;

    public MappingTemplateMenu(Component parent, SipModel sipModel) {
        super("Template");
        this.parent = parent;
        this.sipModel = sipModel;
        refresh();
    }

    private void refresh() {
        this.removeAll();
        this.add(new SaveAsTemplateAction());
        this.addSeparator();
        JMenu deleteMenu = new JMenu("Delete a template");
        this.add(deleteMenu);
        this.addSeparator();
        Map<String, RecordMapping> templates = sipModel.getFileStore().getTemplates();
        for (Map.Entry<String,RecordMapping> entry : templates.entrySet()) {
            this.add(new ApplyTemplateAction(entry.getKey(), entry.getValue()));
            deleteMenu.add(new DeleteTemplateAction(entry.getKey()));
        }
    }

    private class SaveAsTemplateAction extends AbstractAction {

        private SaveAsTemplateAction() {
            super("Save current mapping as template");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String name = JOptionPane.showInputDialog(parent, "What name should this template be given");
            if (name != null && !name.isEmpty()) {
                sipModel.saveAsTemplate(name);
                refresh();
            }
        }
    }

    private class ApplyTemplateAction extends AbstractAction {
        private String name;
        private RecordMapping recordMapping;

        private ApplyTemplateAction(String name, RecordMapping recordMapping) {
            super(String.format("Apply the %s template", name));
            this.name = name;
            this.recordMapping = recordMapping;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            sipModel.applyTemplate(recordMapping);
        }
    }

    private class DeleteTemplateAction extends AbstractAction {
        private String name;

        private DeleteTemplateAction(String name) {
            super(String.format("Delete %s", name));
            this.name = name;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            sipModel.getFileStore().deleteTemplate(name);
            refresh();
        }
    }
}