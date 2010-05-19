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
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class FileMenu extends JMenu {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Component parent;
    private RecentFileSets recentFiles;
    private SelectListener selectListener;

    public interface SelectListener {
        boolean select(FileSet fileSet);
    }

    public FileMenu(Component parent, SelectListener selectListener) {
        super("File");
        this.parent = parent;
        this.selectListener = selectListener;
        executor.execute(new RefreshJob(new Runnable() {
            @Override
            public void run() {
                recentFiles = new RecentFileSets(new File("."));
                if (!recentFiles.getList().isEmpty()) {
                    SwingUtilities.invokeLater(new SpontaneousLoader(recentFiles.getList().get(0)));
                }
            }
        }));
    }

    private class LoadNewFileAction extends AbstractAction {
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
                final File file = chooser.getSelectedFile();
                executor.execute(new RefreshJob(new Runnable() {
                    @Override
                    public void run() {
                        final FileSet fileSet = recentFiles.select(file);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                selectListener.select(fileSet);
                            }
                        });
                    }
                }));
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
            final boolean selected = selectListener.select(fileSet);
            executor.execute(new RefreshJob(new Runnable() {
                @Override
                public void run() {
                    if (selected) {
                        recentFiles.setMostRecent(fileSet);
                    }
                    else {
                        recentFiles.remove(fileSet);
                    }
                }
            }));
        }
    }

    private class RefreshJob implements Runnable {
        private Runnable runFirst;

        private RefreshJob(Runnable runFirst) {
            this.runFirst = runFirst;
        }

        @Override
        public void run() {
            runFirst.run();
            final File commonDirectory = recentFiles.getCommonDirectory();
            final List<? extends FileSet> fileSetList = recentFiles.getList();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeAll();
                    if (commonDirectory != null) {
                        add(new LoadNewFileAction(commonDirectory));
                    }
                    else {
                        add(new LoadNewFileAction(new File("/")));
                    }
                    addSeparator();
                    for (FileSet fileSet : fileSetList) {
                        add(new LoadRecentFileSetAction(fileSet));
                    }
                }
            });
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
