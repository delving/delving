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

import eu.europeana.sip.io.FileSet;
import eu.europeana.sip.io.RecentFileSets;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
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

public class FileMenu extends JMenu {
    private Component parent;
    private Action loadFile = new LoadNewFileAction();
    private RecentFileSets recentFiles = new RecentFileSets(new File("."));
    private JMenu recentFilesMenu = new JMenu("Recent Files");
    private SelectListener selectListener;

    public interface SelectListener {
        void select(FileSet fileSet);
    }

    public interface Enablement {
        void enable(boolean enabled);
    }

    public FileMenu(Component parent, SelectListener selectListener) {
        super("File");
        this.parent = parent;
        this.selectListener = selectListener;
        this.add(loadFile);
        refreshRecentFilesMenu();
        this.add(recentFilesMenu);
    }

    public Enablement getEnablement() {
        return new Enablement() {
            @Override
            public void enable(final boolean enabled) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loadFile.setEnabled(enabled);
                        recentFilesMenu.setEnabled(enabled);
                    }
                });
            }
        };
    }

    private class LoadNewFileAction extends AbstractAction {

        private static final long serialVersionUID = -6398521298905842613L;
        private JFileChooser chooser = new JFileChooser("XML File");

        private LoadNewFileAction() {
            super("Load");
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
                selectListener.select(fileSet);
            }
        }
    }

    private class LoadRecentFileSetAction extends AbstractAction {
        private static final long serialVersionUID = -2107471072903742141L;
        private FileSet fileSet;

        private LoadRecentFileSetAction(FileSet fileSet) {
            super("Load " + fileSet.getName());
            this.fileSet = fileSet;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            fileSet.setMostRecent();
            refreshRecentFilesMenu();
            selectListener.select(fileSet);
        }
    }

    private void refreshRecentFilesMenu() {
        recentFilesMenu.removeAll();
        for (FileSet fileSet : recentFiles.getList()) {
            recentFilesMenu.add(new LoadRecentFileSetAction(fileSet));
        }
    }
}
