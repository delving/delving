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
import java.util.Iterator;
import java.util.List;

/**
 * The menu for handling files
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class FileMenu extends JMenu {
    private static final File RECENT_FILES_FILE = new File("RecentFiles.xml");
    private Component parent;
    private Action loadFile = new LoadNewFileAction();
    private RecentFiles recentFiles;
    private JMenu recentFilesMenu = new JMenu("Recent Files");
    private SelectListener selectListener;

    public interface Enablement {
        void enable(boolean enabled);
    }

    public FileMenu(Component parent, SelectListener selectListener) {
        super("File");
        this.parent = parent;
        this.selectListener = selectListener;
        this.add(loadFile);
        this.add(recentFilesMenu);
        Thread thread = new Thread(new RecentFileLoader());
        thread.start();
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
                select(file.getAbsolutePath());
            }
        }
    }

    private class LoadRecentFileAction extends AbstractAction {
        private static final long serialVersionUID = -2107471072903742141L;
        private String fileName;

        private LoadRecentFileAction(String fileName) {
            super("Load " + fileName);
            this.fileName = fileName;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            select(fileName);
        }
    }

    private class RecentFileLoader implements Runnable {
        @Override
        public void run() {
            try {
                if (RECENT_FILES_FILE.exists()) {
                    FileReader fileReader = new FileReader(RECENT_FILES_FILE);
                    XStream stream = new XStream();
                    stream.processAnnotations(RecentFiles.class);
                    recentFiles = (RecentFiles) stream.fromXML(fileReader);
                }
                if (recentFiles == null) {
                    recentFiles = new RecentFiles();
                    recentFiles.files = new ArrayList<String>();
                }
                if (!recentFiles.files.isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            select(recentFiles.files.get(0));
                        }
                    });
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
                stream.toXML(recentFiles, fileWriter);
                fileWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace(); // todo
            }
        }
    }

    private void moveRecentFilesToMenu() {
        recentFilesMenu.removeAll();
        for (String fileName : recentFiles.files) {
            recentFilesMenu.add(new LoadRecentFileAction(fileName));
        }
    }

    private void select(String absolutePath) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected event thread");
        }
        recentFiles.add(absolutePath);
        moveRecentFilesToMenu();
        Thread thread = new Thread(new RecentFileSaver());
        thread.start();
        selectListener.select(new File(absolutePath));
    }

    @XStreamAlias("recent-files")
    private static class RecentFiles {
        @XStreamImplicit
        List<String> files;

        void add(String fileName) {
            Iterator<String> walk = files.iterator();
            while (walk.hasNext()) {
                String existing = walk.next();
                if (existing.equals(fileName)) {
                    walk.remove();
                }
            }
            files.add(0, fileName);
        }
    }
}
