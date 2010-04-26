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

import eu.europeana.sip.xml.AnalysisParser;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
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
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hold on to the recent files, maintaing their ordering
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecentFileSets {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
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
        Iterator<FileSetImpl> walk = recent.iterator();
        FileSetImpl fileSet = null;
        while (walk.hasNext()) {
            FileSetImpl next = walk.next();
            if (next.getName().equals(inputFile.getAbsolutePath())) {
                fileSet = next;
                walk.remove();
            }
        }
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

    public List<? extends FileSet> getList() {
        return recent;
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
            out.write(set.getName());
            out.write('\n');
        }
        out.close();
    }

    private class FileSetImpl implements FileSet {
        private File inputFile, statisticsFile, mappingFile, recordRootFile, outputFile;

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
        public String getName() {
            return inputFile.getAbsolutePath();
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
        public InputStream getInputStream() throws FileNotFoundException {
            return new FileInputStream(inputFile);
        }

        @Override
        public OutputStream getOutputStream() throws FileNotFoundException {
            return new FileOutputStream(outputFile, true);
        }

        @Override
        public void analyze(AnalysisListener analysisListener) {
            AnalysisParser parser = new AnalysisParser(this, analysisListener);
            executor.submit(parser);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Statistics> getStatistics() throws IOException {
            if (statisticsFile.exists()) {
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(statisticsFile)));
                List<Statistics> statisticsList = null;
                try {
                    statisticsList = (List<Statistics>) in.readObject();
                }
                catch (ClassNotFoundException e) {
                    throw new IOException("Cannot read the statistics list", e);
                }
                in.close();
                return statisticsList;
            }
            else {
                return null;
            }
        }

        @Override
        public void setStatistics(List<Statistics> statisticsList) throws IOException {
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(statisticsFile)));
            out.writeObject(statisticsList);
            out.close();
        }

        @Override
        public String getMapping() throws IOException {
            if (mappingFile.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(mappingFile));
                StringBuilder mapping = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    mapping.append(line).append('\n');
                }
                in.close();
                return mapping.toString();
            }
            return "";
        }

        @Override
        public void setMapping(String mapping) throws IOException {
            FileWriter out = new FileWriter(mappingFile);
            out.write(mapping);
            out.close();
        }

        @Override
        public QName getRecordRoot() throws IOException {
            if (recordRootFile.exists()) {
                Reader in = new FileReader(recordRootFile);
                StringBuilder qNameString = new StringBuilder();
                int ch;
                while ((ch = in.read()) >= 0) {
                    qNameString.append((char) ch);
                }
                in.close();
                try {
                    return QName.valueOf(qNameString.toString());
                }
                catch (Exception e) {
                    recordRootFile.delete();
                }
            }
            return null;
        }

        @Override
        public void setRecordRoot(QName qname) throws IOException {
            Writer out = new FileWriter(recordRootFile);
            out.write(qname.toString());
            out.close();
        }

        public String toString() {
            return getName();
        }
    }
}
