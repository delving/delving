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
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implementing FileSet, handling all the files related to the original xml file.]
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FileSetImpl implements FileSet {
    private final Logger LOG = Logger.getLogger(getClass());
    private File inputFile, statisticsFile, mappingFile, outputFile, discardedFile, reportFile, zipFile;
    private UserNotifier userNotifier;

    public FileSetImpl(File inputFile) {
        this.inputFile = inputFile;
        this.statisticsFile = new File(inputFile.getParentFile(), inputFile.getName() + ".statistics");
        this.mappingFile = new File(inputFile.getParentFile(), inputFile.getName() + ".mapping");
        this.outputFile = new File(inputFile.getParentFile(), inputFile.getName() + ".normalized");
        this.discardedFile = new File(inputFile.getParentFile(), inputFile.getName() + ".discarded");
        this.reportFile = new File(inputFile.getParentFile(), inputFile.getName() + ".report");
        this.zipFile = new File(inputFile.getParentFile(), inputFile.getName() + ".zip");
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
                userNotifier.tellUser("Unable to read mapping file", e);
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
            userNotifier.tellUser("Unable to save mapping file", e);
        }
    }

    @Override
    public File createZipFile() {
        checkWorkerThread();
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                userNotifier.tellUser("Unable to delete zip file");
            }
        }
        try {
            buildZipFile();
            return zipFile;
        }
        catch (IOException e) {
            userNotifier.tellUser("Unable to build zip file", e);
            LOG.warn("Unable to build zip file "+zipFile.getAbsolutePath(), e);
        }
        return null;
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
    public Output prepareOutput() {
        return new OutputImpl();
    }

    public String toString() {
        return getName();
    }

    private class OutputImpl implements Output {
        private OutputStream outputStream, discardedStream, reportStream;
        private int recordsNormalized, recordsDiscarded;

        private OutputImpl() {
            checkWorkerThread();
            removeOutput();
            try {
                this.outputStream = new FileOutputStream(outputFile);
                this.discardedStream = new FileOutputStream(discardedFile);
                this.reportStream = new FileOutputStream(reportFile);
            }
            catch (FileNotFoundException e) {
                userNotifier.tellUser("Unable to open output file " + outputFile.getAbsolutePath(), e);
            }
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public OutputStream getDiscardedStream() {
            return discardedStream;
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
                    outputStream.close();
                    discardedStream.close();
                    Properties properties = new Properties();
                    properties.put("normalizationDate", String.valueOf(System.currentTimeMillis()));
                    properties.put("recordsNormalized", String.valueOf(recordsNormalized));
                    properties.put("recordsDiscarded", String.valueOf(recordsDiscarded));
                    properties.store(reportStream, "Normalization Report");
                    reportStream.close();
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
                return null;
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

    private void buildZipFile() throws IOException {
        OutputStream outputStream = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        // todo: create an entry for the data set details!
        stream(inputFile, zos);
        stream(mappingFile, zos);
        zos.close();
    }

    private void stream(File file, ZipOutputStream zos) throws IOException {
        InputStream in = new FileInputStream(file);
        zos.putNextEntry(new ZipEntry(file.getName()));
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
        zos.closeEntry();
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
