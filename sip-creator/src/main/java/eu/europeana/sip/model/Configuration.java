package eu.europeana.sip.model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Handle recent files and any other items of configuration that the SipCreatorGUI needs
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Configuration {
    private static final File FILE = new File("SipCreatorGUI.xml");
    private final Logger LOG = Logger.getLogger(getClass());
    private Root root;
    private FileSetRecent fileSetRecent = new FileSetRecent();
    private List<FileSetImpl> fileSetImplList = new ArrayList<FileSetImpl>();

    public Configuration() {
        loadRoot();
    }

    public String getServerAccessKey() {
        return root.getAccessKey();
    }

    public void setServerAccessKey(String value) {
        root.setAccessKey(value);
        saveRoot();
    }

    public FileSet.Recent getRecentFileSets() {
        return fileSetRecent;
    }

    private class FileSetRecent implements FileSet.Recent {

        @Override
        public List<File> getCommonDirectories() {
            Set<File> uniqueDirectories = new HashSet<File>();
            for (int walkA = 0; walkA < fileSetImplList.size(); walkA++) {
                FileSet fsA = fileSetImplList.get(walkA);
                for (int walkB = walkA + 1; walkB < fileSetImplList.size(); walkB++) {
                    FileSet fsB = fileSetImplList.get(walkB);
                    uniqueDirectories.add(getDeepestCommonDirectory(fsA.getDirectory(), fsB.getDirectory()));
                }
            }
            List<File> directories = new ArrayList<File>(uniqueDirectories);
            Collections.sort(directories);
            return directories;
        }

        @Override
        public List<? extends FileSet> getList() {
            return fileSetImplList;
        }

        @Override
        public FileSet select(File inputFile) {
            FileSetImpl fileSet = removeFileSet(inputFile);
            if (fileSet != null) {
                fileSetImplList.add(0, fileSet);
            }
            else {
                fileSet = new FileSetImpl(inputFile);
                fileSetImplList.add(0, fileSet);
            }
            save();
            return fileSet;
        }

        @Override
        public void setMostRecent(FileSet fileSet) {
            FileSetImpl fileSetImpl = (FileSetImpl) fileSet;
            fileSetImplList.remove(fileSetImpl);
            fileSetImplList.add(0, fileSetImpl);
            save();
        }

        @Override
        public void remove(FileSet fileSet) {
            Iterator<FileSetImpl> walk = fileSetImplList.iterator();
            while (walk.hasNext()) {
                FileSetImpl next = walk.next();
                if (next.getName().equals(fileSet.getName())) {
                    walk.remove();
                }
            }
            save();
        }

        private FileSetImpl removeFileSet(File inputFile) {
            Iterator<FileSetImpl> walk = fileSetImplList.iterator();
            FileSetImpl fileSet = null;
            while (walk.hasNext()) {
                FileSetImpl next = walk.next();
                if (next.getAbsolutePath().equals(inputFile.getAbsolutePath())) {
                    fileSet = next;
                    walk.remove();
                }
            }
            return fileSet;
        }

        private void save() {
            root = new Root(root.accessKey, fileSetImplList);
            saveRoot();
        }
    }

    @XStreamAlias("sip-creator-configuration")
    public static class Root {
        private String accessKey;
        private List<String> recentFiles;

        public Root() {
        }

        public Root(String accessKey, List<FileSetImpl> fileSetImplList) {
            this.accessKey = accessKey;
            this.recentFiles = new ArrayList<String>();
            for (FileSetImpl fileSet : fileSetImplList) {
                this.recentFiles.add(fileSet.getAbsolutePath());
            }
        }

        public String getAccessKey() {
            if (accessKey == null) {
                accessKey = "";
            }
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public List<String> getRecentFiles() {
            if (recentFiles == null) {
                recentFiles = new ArrayList<String>();
            }
            return recentFiles;
        }
    }

    private static File getDeepestCommonDirectory(File a, File b) {
        while (!a.equals(b)) {
            String pathA = a.getAbsolutePath();
            String pathB = b.getAbsolutePath();
            if (pathA.length() > pathB.length()) {
                a = a.getParentFile();
            }
            else if (pathB.length() > pathA.length()) {
                b = b.getParentFile();
            }
            else {
                a = a.getParentFile();
                b = b.getParentFile();
            }
        }
        return a; // or b
    }

    private void loadRoot() {
        try {
            InputStream is = new FileInputStream(FILE);
            XStream xstream = new XStream();
            xstream.processAnnotations(Root.class);
            this.root = (Root) xstream.fromXML(is);
            List<FileSetImpl> freshList = new ArrayList<FileSetImpl>();
            for (String absolutePath : root.getRecentFiles()) {
                freshList.add(new FileSetImpl(new File(absolutePath)));
            }
            fileSetImplList = freshList;
            is.close();
        }
        catch (Exception e) {
            LOG.warn("Unable to load " + FILE.getAbsolutePath(), e);
            this.root = new Root();
        }
    }

    private void saveRoot() {
        try {
            OutputStream os = new FileOutputStream(FILE);
            XStream xstream = new XStream();
            xstream.processAnnotations(Root.class);
            xstream.toXML(this.root, os);
            os.close();
        }
        catch (Exception e) {
            LOG.warn("Unable to save to " + FILE.getAbsolutePath(), e);
        }
    }
}
