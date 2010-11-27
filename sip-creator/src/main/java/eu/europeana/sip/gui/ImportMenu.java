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

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class ImportMenu extends JMenu {
    private Component parent;
    private SipModel sipModel;
    private SelectListener selectListener;

    public interface SelectListener {
        boolean selectInputFile(File inputFile);
    }

    public ImportMenu(Component parent, SipModel sipModel, SelectListener selectListener) {
        super("Import");
        this.parent = parent;
        this.sipModel = sipModel;
        this.selectListener = selectListener;
        refresh();
    }

    private class LoadNewFileAction extends AbstractAction {
        private JFileChooser chooser = new JFileChooser("XML File");

        private LoadNewFileAction(File directory) {
            super("From " + directory.getAbsolutePath());
            chooser.setCurrentDirectory(directory);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".xml") || file.getName().endsWith(".xml.gz");
                }

                @Override
                public String getDescription() {
                    return "XML or GZIP/XML Files";
                }
            });
            chooser.setMultiSelectionEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int choiceMade = chooser.showOpenDialog(parent);
            if (choiceMade == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                sipModel.setRecentDirectory(file);
                selectListener.selectInputFile(file);
                refresh();
            }
        }
    }

    private void refresh() {
        removeAll();
        File directory = new File(sipModel.getRecentDirectory());
        while (directory != null) {
            add(new LoadNewFileAction(directory));
            directory = directory.getParentFile();
        }
    }
}
