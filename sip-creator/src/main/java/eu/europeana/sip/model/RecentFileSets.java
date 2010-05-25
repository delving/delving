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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
    private static final int MAX_RECENT = 20;
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
        FileSetImpl fileSetImpl = (FileSetImpl)fileSet;
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
            if (next.getName().equals(inputFile.getName())) {
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

    public File getCommonDirectory() {
        Set<File> directories = new HashSet<File>();
        for (FileSet fileSet : recent) {
            directories.add(fileSet.getDirectory());
        }
        File highest = null;
        for (File directory : directories) {
            if (highest == null) {
                highest = directory;
            }
            else {
                highest = getDeepestCommonDirectory(highest, directory);
            }
        }
        return highest;
    }

    public List<File> getCommonDirectories() {
        Set<File> uniqueDirectories = new HashSet<File>();
        for (int walkA = 0; walkA < recent.size(); walkA++) {
            FileSet fsA = recent.get(walkA);
            for (int walkB = walkA+1; walkB < recent.size(); walkB++) {
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

    private class FileSetImpl implements FileSet {
        private File inputFile, statisticsFile, mappingFile, outputFile;
        private ExceptionHandler exceptionHandler;

        private FileSetImpl(File inputFile) {
            this.inputFile = inputFile;
            this.statisticsFile = new File(inputFile.getParentFile(), inputFile.getName() + ".statistics");
            this.mappingFile = new File(inputFile.getParentFile(), inputFile.getName() + ".mapping");
            this.outputFile = new File(inputFile.getParentFile(), inputFile.getName() + ".normalized.xml");
        }

        @Override
        public void setExceptionHandler(ExceptionHandler handler) {
            this.exceptionHandler = handler;
        }

        @Override
        public String getName() {
            return inputFile.getName();
        }

        @Override
        public String getAbsolutePath() {
            return inputFile.getAbsolutePath();
        }

        @Override
        public boolean isValid() {
            return inputFile.exists();
        }

        @Override
        public void remove() {
            recent.remove(this);
            try {
                saveList();
            }
            catch (IOException e) {
                LOG.warn("Unable to save recent files list", e);
            }
        }

        @Override
        public File getDirectory() {
            return inputFile.getParentFile();
        }

        @Override
        public InputStream getInputStream() {
            checkWorkerThread();
            try {
                return new FileInputStream(inputFile);
            }
            catch (FileNotFoundException e) {
                exceptionHandler.failure(e);
            }
            return null;
        }

        @Override
        public OutputStream getOutputStream() {
            checkWorkerThread();
            try {
                return new FileOutputStream(outputFile, true);
            }
            catch (FileNotFoundException e) {
                exceptionHandler.failure(e);
            }
            return null;
        }

        @Override
        public boolean hasOutputFile() {
            return outputFile.exists();
        }

        @Override
        public void removeOutputFile() {
            checkWorkerThread();
            if (!outputFile.delete()) {
                LOG.warn("Unable to delete "+outputFile);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Statistics> getStatistics() {
            checkWorkerThread();
            if (statisticsFile.exists()) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(statisticsFile)));
                    List<Statistics> statisticsList = (List<Statistics>) in.readObject();
                    in.close();
                    return statisticsList;
                }
                catch (Exception e) {
                    exceptionHandler.failure(e);
                }
            }
            return null;
        }

        @Override
        public void setStatistics(List<Statistics> statisticsList) {
            checkWorkerThread();
            try {
                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(statisticsFile)));
                out.writeObject(statisticsList);
                out.close();
            }
            catch (IOException e) {
                exceptionHandler.failure(e);
            }
        }

        @Override
        public String getMapping() {
            checkWorkerThread();
            if (mappingFile.exists()) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(mappingFile));
                    StringBuilder mapping = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        mapping.append(line).append('\n');
                    }
                    in.close();
                    return mapping.toString();
                }
                catch (IOException e) {
                    exceptionHandler.failure(e);
                }
            }
            return "";
        }

        @Override
        public void setMapping(String mapping) {
            checkWorkerThread();
            try {
                FileWriter out = new FileWriter(mappingFile);
                out.write(mapping);
                out.close();
            }
            catch (IOException e) {
                exceptionHandler.failure(e);
            }
        }

        public String toString() {
            return getName();
        }
    }

    private static void checkWorkerThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Worker thread");
        }
    }
}
