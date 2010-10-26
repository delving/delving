package eu.europeana.sip.model;

import com.thoughtworks.xstream.XStream;
import eu.delving.core.metadata.RecordDefinition;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.Statistics;
import eu.europeana.sip.core.DataSetDetails;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Implementing FileSet, handling all the files related to the original xml file.]
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FileSetImpl implements FileSet {
    private final Logger LOG = Logger.getLogger(getClass());
    private File inputFile, statisticsFile, mappingFile, outputFile, discardedFile, reportFile, dataSetDetailsFile;
    private UserNotifier userNotifier;

    public FileSetImpl(File inputFile) {
        this.inputFile = inputFile;
        this.statisticsFile = new File(inputFile.getParentFile(), inputFile.getName() + ".statistics");
        this.mappingFile = new File(inputFile.getParentFile(), inputFile.getName() + ".mapping");
        this.outputFile = new File(inputFile.getParentFile(), inputFile.getName() + ".normalized");
        this.discardedFile = new File(inputFile.getParentFile(), inputFile.getName() + ".discarded");
        this.reportFile = new File(inputFile.getParentFile(), inputFile.getName() + ".report");
        this.dataSetDetailsFile = new File(inputFile.getParentFile(), inputFile.getName() + ".details");
    }

    @Override
    public void setExceptionHandler(UserNotifier handler) {
        this.userNotifier = handler;
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
            userNotifier.tellUser("Unable to open input file", e);
        }
        return null;
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
                userNotifier.tellUser("Unable to read statistics, please re-analyze", e);
                if (statisticsFile.delete()) {
                    LOG.warn("Cannot delete statistics file");
                }
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
            userNotifier.tellUser("Unable to save statistics file", e);
        }
    }

    @Override
    public RecordMapping getMapping(RecordDefinition recordDefinition) {
        checkWorkerThread();
        if (mappingFile.exists()) {
            try {
                FileInputStream is = new FileInputStream(mappingFile);
                return RecordMapping.read(is, recordDefinition);
            }
            catch (Exception e) {
                userNotifier.tellUser("Unable to read mapping file", e);
            }
        }
        return null;
    }

    @Override
    public void setMapping(RecordMapping mapping) {
        checkWorkerThread();
        if (mapping == null) {
            mappingFile.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(mappingFile);
            RecordMapping.write(mapping, out);
            out.close();
        }
        catch (IOException e) {
            userNotifier.tellUser("Unable to save mapping file", e);
        }
    }

    @Override
    public DataSetDetails getDataSetDetails() {
        checkWorkerThread();
        DataSetDetails details = null;
        if (dataSetDetailsFile.exists()) {
            XStream stream = new XStream();
            stream.processAnnotations(DataSetDetails.class);
            try {
                FileInputStream fis = new FileInputStream(dataSetDetailsFile);
                details = (DataSetDetails) stream.fromXML(fis);
                fis.close();
            }
            catch (Exception e) {
                userNotifier.tellUser("Unable to load dataset details file", e);
            }
        }
        if (details == null) {
            details = new DataSetDetails();
        }
        return details;
    }

    @Override
    public void setDataSetDetails(DataSetDetails details) {
        checkWorkerThread();
        try {
            XStream stream = new XStream();
            stream.processAnnotations(DataSetDetails.class);
            FileOutputStream fos = new FileOutputStream(dataSetDetailsFile);
            stream.toXML(details, fos);
            fos.close();
        }
        catch (IOException e) {
            userNotifier.tellUser("Unable to save dataset details file", e);
        }
    }

    @Override
    public List<File> getUploadFiles() {
        List<File> files = new ArrayList<File>();
        files.add(dataSetDetailsFile);
        files.add(inputFile);
        files.add(mappingFile);
        return files;
    }

    @Override
    public Report getReport() {
        if (reportFile.exists()) {
            try {
                return new ReportImpl();
            }
            catch (Exception e) {
                removeOutput();
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public Output prepareOutput(boolean storeNormalizedFile) {
        return new OutputImpl(storeNormalizedFile);
    }

    public String toString() {
        return getName();
    }

    private class OutputImpl implements Output {
        private Writer outputWriter, discardedWriter, reportWriter;
        private int recordsNormalized, recordsDiscarded;

        private OutputImpl(boolean storeNormalizedFile) {
            checkWorkerThread();
            removeOutput();
            try {
                if (storeNormalizedFile) {
                    this.outputWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
                }
                this.discardedWriter = new OutputStreamWriter(new FileOutputStream(discardedFile), "UTF-8");
                this.reportWriter = new OutputStreamWriter(new FileOutputStream(reportFile), "UTF-8");
            }
            catch (FileNotFoundException e) {
                userNotifier.tellUser("Unable to open output file " + outputFile.getAbsolutePath(), e);
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Writer getOutputWriter() {
            if (outputWriter == null) {
                throw new RuntimeException("Normalized file was not to be stored");
            }
            return outputWriter;
        }

        @Override
        public Writer getDiscardedWriter() {
            return discardedWriter;
        }

        @Override
        public void recordNormalized() {
            recordsNormalized++;
        }

        @Override
        public void recordDiscarded() {
            recordsDiscarded++;
        }

        @Override
        public void close(boolean abort) {
            if (abort) {
                removeOutput();
            }
            else {
                try {
                    if (outputWriter != null) {
                        outputWriter.close();
                    }
                    discardedWriter.close();
                    Properties properties = new Properties();
                    properties.put("normalizationDate", String.valueOf(System.currentTimeMillis()));
                    properties.put("recordsNormalized", String.valueOf(recordsNormalized));
                    properties.put("recordsDiscarded", String.valueOf(recordsDiscarded));
                    properties.store(reportWriter, "Normalization Report");
                    reportWriter.close();
                }
                catch (IOException e) {
                    userNotifier.tellUser("Unable to close output files", e);
                }
            }
        }
    }

    private class ReportImpl implements Report {
        private Properties properties = new Properties();

        private ReportImpl() throws Exception {
            try {
                InputStream reportStream = new FileInputStream(reportFile);
                properties.load(reportStream);
            }
            catch (Exception e) {
                userNotifier.tellUser("Unable to load report file", e);
                throw e;
            }
        }

        @Override
        public Date getNormalizationDate() {
            String s = (String) properties.get("normalizationDate");
            if (s != null) {
                return new Date(Long.parseLong(s));
            }
            else {
                return new Date();
            }
        }

        @Override
        public int getRecordsNormalized() {
            String s = (String) properties.get("recordsNormalized");
            if (s != null) {
                return Integer.parseInt(s);
            }
            else {
                return 0;
            }
        }

        @Override
        public int getRecordsDiscarded() {
            String s = (String) properties.get("recordsDiscarded");
            if (s != null) {
                return Integer.parseInt(s);
            }
            else {
                return 0;
            }
        }

        @Override
        public void clear() {
            checkWorkerThread();
            removeOutput();
        }

    }

    private void removeOutput() {
        if (outputFile.exists() && !outputFile.delete()) {
            LOG.warn("Unable to delete " + outputFile);
        }
        if (discardedFile.exists() && !discardedFile.delete()) {
            LOG.warn("Unable to delete " + discardedFile);
        }
        if (reportFile.exists() && !reportFile.delete()) {
            LOG.warn("Unable to delete " + reportFile);
        }
    }

    private static void checkWorkerThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Worker thread");
        }
    }
}
