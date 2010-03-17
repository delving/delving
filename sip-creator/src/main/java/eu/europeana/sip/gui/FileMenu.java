package eu.europeana.sip.gui;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class FileMenu extends JMenu {
    private static final File RECENT_FILES_FILE = new File("RecentFiles.xml");
    private Component parent;
    private Action loadFile = new LoadNewFileAction();
    private Map<String, Action> recentFileActions = new TreeMap<String,Action>();
    private JMenu recentFilesMenu = new JMenu("Recent Files");
    private SelectListener selectListener;

    public FileMenu(Component parent, SelectListener selectListener) {
        super("File");
        this.parent = parent;
        this.selectListener = selectListener;
        this.add(loadFile);
        this.add(recentFilesMenu);
        loadRecentFiles();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                loadFile.setEnabled(enabled);
                for (Action action : recentFileActions.values()) {
                    action.setEnabled(enabled);
                }
            }
        });
    }

    public interface SelectListener {
        void select(File file);
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
                selectListener.select(file);
                if (!recentFileActions.containsKey(file.getAbsolutePath())) {
                    final Action action = new LoadRecentFileAction(file);
                    recentFileActions.put(file.getAbsolutePath(), action);
                    recentFilesMenu.add(action);
                    Thread thread = new Thread(new RecentFileSaver());
                    thread.start();
                }
            }
        }
    }

    private class LoadRecentFileAction extends AbstractAction {
        private static final long serialVersionUID = -2107471072903742141L;
        private File file;

        private LoadRecentFileAction(File file) {
            super("Load "+file.getName());
            this.file = file;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            selectListener.select(file);
        }
    }

    private void loadRecentFiles() {
        Thread thread = new Thread(new RecentFileLoader());
        thread.start();
    }

    private class RecentFileLoader implements Runnable {
        @Override
        public void run() {
            try {
                if (!RECENT_FILES_FILE.exists()) {
                    return;
                }
                FileReader fileReader = new FileReader(RECENT_FILES_FILE);
                XStream stream = new XStream();
                stream.processAnnotations(RecentFiles.class);
                RecentFiles recentFiles = (RecentFiles) stream.fromXML(fileReader);
                File firstRecentFile = null;
                for (String fileName : recentFiles.files) {
                    File file = new File(fileName);
                    final Action action = new LoadRecentFileAction(file);
                    recentFileActions.put(file.getAbsolutePath(), action);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            recentFilesMenu.add(action);
                        }
                    });
                    if (firstRecentFile == null) {
                        firstRecentFile = file;
                    }
                }
                if (firstRecentFile != null) {
                    selectListener.select(firstRecentFile);
                }
            }
            catch (Exception e) {
                e.printStackTrace(); // todo
            }
        }
    }

    private class RecentFileSaver implements Runnable {
        @Override
        public void run() {
            try {
                FileWriter fileWriter = new FileWriter(RECENT_FILES_FILE);
                XStream stream = new XStream();
                stream.processAnnotations(RecentFiles.class);
                RecentFiles recentFiles = new RecentFiles();
                recentFiles.files = new ArrayList<String>();
                for (String fileName : recentFileActions.keySet()) {
                    recentFiles.files.add(fileName);
                }
                stream.toXML(recentFiles, fileWriter);
                fileWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace(); // todo
            }
        }
    }

    @XStreamAlias("recent-files")
    private static class RecentFiles {
        @XStreamImplicit
        List<String> files;
    }
}
