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

import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.RecentFileSets;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Set;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class FileMenu extends JMenu {
    private Component parent;
    private RecentFileSets recentFiles = new RecentFileSets(new File("."));
    private SelectListener selectListener;

    public interface SelectListener {
        boolean select(FileSet fileSet);
    }

    public FileMenu(Component parent, SelectListener selectListener) {
        super("File");
        this.parent = parent;
        this.selectListener = selectListener;
        refresh();
    }

    private void refresh() {
        removeAll();
        add(new LoadNewFileAction(new File("/")));
        addSeparator();
        Set<File> directories = recentFiles.getDirectories();
        for (File directory : directories) {
            add(new LoadNewFileAction(directory));
        }
        addSeparator();
        for (FileSet fileSet : recentFiles.getList()) {
            add(new LoadRecentFileSetAction(fileSet));
        }
    }

    private class LoadNewFileAction extends AbstractAction {

        private static final long serialVersionUID = -6398521298905842613L;
        private JFileChooser chooser = new JFileChooser("XML File");

        private LoadNewFileAction(File directory) {
            super("Open File from "+directory.getAbsolutePath());
            chooser.setCurrentDirectory(directory);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".xml");
                }

                @Override
                public String getDescription() {
                    return "XML Files";
                }
            });
            chooser.setMultiSelectionEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int choiceMade = chooser.showOpenDialog(parent);
            if (choiceMade == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                FileSet fileSet = recentFiles.select(file);
                refresh();
                selectListener.select(fileSet);
            }
        }
    }

    private class LoadRecentFileSetAction extends AbstractAction {
        private static final long serialVersionUID = -2107471072903742141L;
        private FileSet fileSet;

        private LoadRecentFileSetAction(FileSet fileSet) {
            super(fileSet.getName());
            this.fileSet = fileSet;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (selectListener.select(fileSet)) {
                fileSet.setMostRecent();
            }
            else {
                recentFiles.remove(fileSet);
            }
            refresh();
        }
    }

}
