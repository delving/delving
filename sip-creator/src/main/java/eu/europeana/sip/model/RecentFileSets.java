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
import java.io.Writer;
import java.util.ArrayList;
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

    private FileSetImpl removeFileSet(File inputFile) {
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

    public Set<File> getDirectories() {
        Set<File> directories = new HashSet<File>();
        for (FileSet set : recent) {
            directories.add(set.getDirectory());
        }
        return directories;
    }

    private void loadList() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(listFile));
        String line;
        while ((line = in.readLine()) != null) {
            recent.add(new FileSetImpl(new File(line)));
        }
    }

    private void saveList() throws IOException {
        FileWriter out = new FileWriter(listFile);
        for (FileSetImpl set : recent) {
            out.write(set.getAbsolutePath());
            out.write('\n');
        }
        out.close();
    }

    private class FileSetImpl implements FileSet {
        private File inputFile, statisticsFile, mappingFile, recordRootFile, outputFile;
        private ExceptionHandler exceptionHandler;

        private FileSetImpl(File inputFile) {
            this.inputFile = inputFile;
            this.statisticsFile = new File(inputFile.getParentFile(), inputFile.getName() + ".statistics");
            this.mappingFile = new File(inputFile.getParentFile(), inputFile.getName() + ".mapping");
            this.recordRootFile = new File(inputFile.getParentFile(), inputFile.getName() + ".record");
            this.outputFile = new File(inputFile.getParentFile(), inputFile.getName() + ".normalized.xml");
            if (outputFile.exists()) {
                outputFile.delete();
            }
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
        public boolean isValid() {
            return inputFile.exists();
        }

        @Override
        public void setMostRecent() {
            recent.remove(this);
            recent.add(0, this);
            try {
                saveList();
            }
            catch (IOException e) {
                LOG.warn("Unable to save recent files list", e);
            }
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
            try {
                return new FileOutputStream(outputFile, true);
            }
            catch (FileNotFoundException e) {
                exceptionHandler.failure(e);
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Statistics> getStatistics() {
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
            try {
                FileWriter out = new FileWriter(mappingFile);
                out.write(mapping);
                out.close();
            }
            catch (IOException e) {
                exceptionHandler.failure(e);
            }
        }

        @Override
        public RecordRoot getRecordRoot() {
            if (recordRootFile.exists()) {
                StringBuilder contents = new StringBuilder();
                try {
                    BufferedReader in = new BufferedReader(new FileReader(recordRootFile));
                    int ch;
                    while ((ch = in.read()) >= 0) {
                        contents.append((char) ch);
                    }
                    in.close();
                    return new RecordRoot(contents.toString());
                }
                catch (IOException e) {
                    recordRootFile.delete();
                    exceptionHandler.failure(e);
                }
            }
            return null;
        }

        @Override
        public void setRecordRoot(RecordRoot recordRoot) {
            if (recordRoot == null) {
                recordRootFile.delete();
            }
            else {
                try {
                    Writer out = new FileWriter(recordRootFile);
                    out.write(recordRoot.toString());
                    out.close();
                }
                catch (IOException e) {
                    exceptionHandler.failure(e);
                }
            }
        }

        public String toString() {
            return getName();
        }

        public String getAbsolutePath() {
            return inputFile.getAbsolutePath();
        }
    }
}