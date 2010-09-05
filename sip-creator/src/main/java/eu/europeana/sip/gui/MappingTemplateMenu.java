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

import eu.europeana.sip.model.SipModel;
import org.apache.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class MappingTemplateMenu extends JMenu {
    private static final File DIRECTORY = new File(".");
    private static final String EXTENSION = ".mapping.template";
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
        for (File file : DIRECTORY.listFiles(new ExtensionFilter())) {
            this.add(new UseTemplateAction(file));
            deleteMenu.add(new DeleteTemplateAction(file));
        }
    }

    private class SaveAsTemplateAction extends AbstractAction {

        private SaveAsTemplateAction() {
            super("Save current mapping as template");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String name = JOptionPane.showInputDialog(parent, "Template Name?");
            if (name != null && !name.isEmpty()) {
                String templateCode = sipModel.getMappingTemplate();
                saveTemplate(name, templateCode);
            }
        }
    }

    private class UseTemplateAction extends AbstractAction {
        private File file;

        private UseTemplateAction(File file) {
            super("Apply the \"" + getTemplateName(file) +"\" template");
            this.file = file;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            sipModel.loadMappingTemplate(file);
        }
    }

    private class DeleteTemplateAction extends AbstractAction {
        private File file;

        private DeleteTemplateAction(File file) {
            super("Delete " + getTemplateName(file));
            this.file = file;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (file.delete()) {
                refresh();
            }
        }
    }

    private void saveTemplate(String name, String templateCode) {
        FileWriter out = null;
        try {
            out = new FileWriter(name + EXTENSION);
            out.write(templateCode);
            out.close();
            log.info("Created template: " + name);
            refresh(); // pretty lazy
        }
        catch (IOException e) {
            log.warn("Couldn't create the template: " + name, e);
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ee) {
                    log.warn("Couldn't close", ee);
                }
            }
        }
    }

    private static String getTemplateName(File file) {
        if (!file.getName().endsWith(EXTENSION)) {
            throw new RuntimeException("File name does not end in " + EXTENSION);
        }
        return file.getName().substring(0, file.getName().length() - EXTENSION.length());
    }

    private class ExtensionFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(EXTENSION);
        }
    }

}