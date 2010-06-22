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

package eu.europeana.sip.model;

import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Hold on to the recent files, maintaing their ordering
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecentFileSets {
    private static final int MAX_RECENT = 40;
    private final Logger LOG = Logger.getLogger(getClass());
    private File listFile;
    private List<FileSetImpl> recent = new ArrayList<FileSetImpl>();

    public RecentFileSets(File root) {
        listFile = new File(root, "recent-files.txt");
        if (listFile.exists()) {
            try {
                loadList();
            }
            catch (IOException e) {
                LOG.warn("Unable to load recent files list", e);
            }
        }
    }

    public FileSet select(File inputFile) {
        checkWorkerThread();
        FileSetImpl fileSet = removeFileSet(inputFile);
        if (fileSet != null) {
            recent.add(0, fileSet);
        }
        else {
            fileSet = new FileSetImpl(inputFile);
            recent.add(0, fileSet);
        }
        try {
            saveList();
        }
        catch (IOException e) {
            LOG.warn("Unable to save recent files list", e);
        }
        return fileSet;
    }

    public void setMostRecent(FileSet fileSet) {
        FileSetImpl fileSetImpl = (FileSetImpl) fileSet;
        recent.remove(fileSetImpl);
        recent.add(0, fileSetImpl);
        try {
            saveList();
        }
        catch (IOException e) {
            LOG.warn("Unable to save recent files list", e);
        }
    }

    private FileSetImpl removeFileSet(File inputFile) {
        checkWorkerThread();
        Iterator<FileSetImpl> walk = recent.iterator();
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

    public void remove(FileSet fileSet) {
        checkWorkerThread();
        Iterator<FileSetImpl> walk = recent.iterator();
        while (walk.hasNext()) {
            FileSetImpl next = walk.next();
            if (next.getName().equals(fileSet.getName())) {
                walk.remove();
            }
        }
        try {
            saveList();
        }
        catch (IOException e) {
            LOG.warn("Unable to save recent files list", e);
        }
    }

    public List<? extends FileSet> getList() {
        return recent;
    }

    public List<File> getCommonDirectories() {
        Set<File> uniqueDirectories = new HashSet<File>();
        for (int walkA = 0; walkA < recent.size(); walkA++) {
            FileSet fsA = recent.get(walkA);
            for (int walkB = walkA + 1; walkB < recent.size(); walkB++) {
                FileSet fsB = recent.get(walkB);
                uniqueDirectories.add(getDeepestCommonDirectory(fsA.getDirectory(), fsB.getDirectory()));
            }
        }
        List<File> directories = new ArrayList<File>(uniqueDirectories);
        Collections.sort(directories);
        return directories;
    }

    private File getDeepestCommonDirectory(File a, File b) {
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

    private void loadList() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(listFile));
        String line;
        while ((line = in.readLine()) != null) {
            recent.add(new FileSetImpl(new File(line)));
        }
    }

    private void saveList() throws IOException {
        checkWorkerThread();
        FileWriter out = new FileWriter(listFile);
        int count = 0;
        for (FileSetImpl set : recent) {
            out.write(set.getAbsolutePath());
            out.write('\n');
            if (count++ == MAX_RECENT) {
                break;
            }
        }
        out.close();
    }

    private static void checkWorkerThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Worker thread");
        }
    }
}
