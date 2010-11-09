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
import eu.europeana.sip.model.SipModel;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class FileMenu extends JMenu {
    private Component parent;
    private SipModel sipModel;
    private SelectListener selectListener;

    public interface SelectListener {
        boolean select(FileSet fileSet);
    }

    public FileMenu(Component parent, SipModel sipModel, SelectListener selectListener) {
        super("File");
        this.parent = parent;
        this.sipModel = sipModel;
        this.selectListener = selectListener;
        List<? extends FileSet> fileSets = fileSets().getList();
        if (!fileSets.isEmpty()) {
            SwingUtilities.invokeLater(new SpontaneousLoader(fileSets.get(0)));
        }
        refresh();
    }

    private class LoadNewFileAction extends AbstractAction {
        private JFileChooser chooser = new JFileChooser("XML File");

        private LoadNewFileAction(File directory) {
            super("Open File from " + directory.getAbsolutePath());
            chooser.setCurrentDirectory(directory);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".xml") || file.getName().endsWith(".gz");
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
                FileSet fileSet = fileSets().select(file);
                selectListener.select(fileSet);
                refresh();
            }
        }
    }

    private class LoadRecentFileSetAction extends AbstractAction {
        private FileSet fileSet;

        private LoadRecentFileSetAction(FileSet fileSet) {
            super(fileSet.getAbsolutePath());
            this.fileSet = fileSet;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            boolean selected = selectListener.select(fileSet);
            if (selected) {
                fileSets().setMostRecent(fileSet);
            }
            else {
                fileSets().remove(fileSet);
            }
            refresh();
        }
    }
    
    private FileSet.Recent fileSets() {
        return sipModel.getRecentFileSets();
    }

    private void refresh() {
        removeAll();
        List<File> commonDirectories = fileSets().getCommonDirectories();
        if (commonDirectories.isEmpty()) {
            if (!fileSets().getList().isEmpty()) {
                File directory = fileSets().getList().get(0).getDirectory();
                int count = 3;
                while (directory != null && count-- > 0) {
                    add(new LoadNewFileAction(directory));
                    directory = directory.getParentFile();
                }
            }
        }
        else {
            for (File directory : commonDirectories) {
                add(new LoadNewFileAction(directory));
            }
        }
        add(new LoadNewFileAction(new File("/")));
        addSeparator();
        List<? extends FileSet> fileSetList = fileSets().getList();
        for (FileSet fileSet : fileSetList) {
            add(new LoadRecentFileSetAction(fileSet));
        }
    }

    private class SpontaneousLoader implements Runnable {
        private FileSet fileSet;

        private SpontaneousLoader(FileSet fileSet) {
            this.fileSet = fileSet;
        }

        @Override
        public void run() {
            selectListener.select(fileSet);
        }
    }
}
