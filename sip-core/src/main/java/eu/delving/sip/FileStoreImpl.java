/*
 * Copyright 2010 DELVING BV
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

package eu.delving.sip;

import com.thoughtworks.xstream.XStream;
import eu.delving.core.metadata.MetadataException;
import eu.delving.core.metadata.RecordDefinition;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.SourceDetails;
import eu.delving.core.metadata.Statistics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This interface describes how files are stored by the sip-creator
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FileStoreImpl implements FileStore {

    private File home;

    public FileStoreImpl(File home) {
        this.home = home;
    }

    @Override
    public AppConfig getAppConfig() throws FileStoreException {
        File appConfigFile = new File(home, APP_CONFIG_FILE_NAME);
        AppConfig config = null;
        if (appConfigFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(appConfigFile);
                config = (AppConfig) getAppConfigStream().fromXML(fis);
                fis.close();
            }
            catch (Exception e) {
                throw new FileStoreException(String.format("Unable to read application configuration from %s", appConfigFile.getAbsolutePath()));
            }
        }
        if (config == null) {
            config = new AppConfig();
        }
        return config;
    }

    @Override
    public void setAppConfig(AppConfig appConfig) throws FileStoreException {
        File sourceDetailsFile = new File(home, APP_CONFIG_FILE_NAME);
        try {
            FileOutputStream fos = new FileOutputStream(sourceDetailsFile);
            getAppConfigStream().toXML(appConfig, fos);
            fos.close();
        }
        catch (IOException e) {
            throw new FileStoreException(String.format("Unable to save application configuration file to %s", sourceDetailsFile.getAbsolutePath()), e);
        }
    }

    @Override
    public Set<String> getDataSetSpecs() {
        Set<String> specs = new TreeSet<String>();
        for (File file : home.listFiles()) {
            if (file.isDirectory()) {
                specs.add(file.getName());
            }
        }
        return specs;
    }

    @Override
    public DataSetStore getDataSetStore(String spec) throws FileStoreException {
        File directory = new File(home, spec);
        if (!directory.exists()) {
            throw new FileStoreException(String.format("Data store directory %s not found", directory.getAbsolutePath()));
        }
        return new DataSetStoreImpl(directory);
    }

    @Override
    public DataSetStore createDataSetStore(String spec, InputStream xmlInputStream) throws FileStoreException {
        File directory = new File(home, spec);
        if (directory.exists()) {
            throw new FileStoreException(String.format("Data store directory %s already exists", directory.getAbsolutePath()));
        }
        if (!directory.mkdirs()) {
            throw new FileStoreException(String.format("Unable to create data store directory %s", directory.getAbsolutePath()));
        }
        File source = new File(directory, SOURCE_FILE_PREFIX + "new" + SOURCE_FILE_SUFFIX);
        MessageDigest digest = getDigest();
        try {
            OutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(source));
            byte[] buffer = new byte[4096];
            int bytesRead;
            while (-1 != (bytesRead = xmlInputStream.read(buffer))) {
                gzipOutputStream.write(buffer, 0, bytesRead);
                digest.digest(buffer, 0, bytesRead);
            }
            xmlInputStream.close();
            gzipOutputStream.close();
        }
        catch (Exception e) {
            throw new FileStoreException("Unable to capture XML input into " + source.getAbsolutePath(), e);
        }
        String hash = toHexadecimal(digest.digest());
        File hashedSource = new File(directory, String.format(SOURCE_FILE_PREFIX + "%s" + SOURCE_FILE_SUFFIX, hash));
        if (!source.renameTo(hashedSource)) {
            throw new FileStoreException(String.format("Unable to rename %s to %s", source.getAbsolutePath(), hashedSource.getAbsolutePath()));
        }
        return new DataSetStoreImpl(directory);
    }

    public class DataSetStoreImpl implements DataSetStore {

        private File directory;

        public DataSetStoreImpl(File directory) {
            this.directory = directory;
        }

        @Override
        public String getSpec() {
            return directory.getName();
        }

        @Override
        public InputStream createXmlInputStream() throws FileStoreException {
            File source = getSourceFile();
            try {
                return new GZIPInputStream(new FileInputStream(source));
            }
            catch (IOException e) {
                throw new FileStoreException(String.format("Unable to create input stream from %s", source.getAbsolutePath()), e);
            }
        }

        @Override
        public List<Statistics> getStatistics() throws FileStoreException {
            File statisticsFile = new File(directory, STATISTICS_FILE_NAME);
            if (statisticsFile.exists()) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(statisticsFile)));
                    @SuppressWarnings("unchecked")
                    List<Statistics> statisticsList = (List<Statistics>) in.readObject();
                    in.close();
                    return statisticsList;
                }
                catch (Exception e) {
                    statisticsFile.delete();
                    throw new FileStoreException("Unable to read statistics file.", e);
                }
            }
            return null;
        }

        @Override
        public void setStatistics(List<Statistics> statisticsList) throws FileStoreException {
            File statisticsFile = new File(directory, STATISTICS_FILE_NAME);
            try {
                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(statisticsFile)));
                out.writeObject(statisticsList);
                out.close();
            }
            catch (IOException e) {
                throw new FileStoreException(String.format("Unable to save statistics file to %s", statisticsFile.getAbsolutePath()), e);
            }
        }

        @Override
        public RecordMapping getRecordMapping(RecordDefinition recordDefinition) throws FileStoreException {
            File mappingFile = new File(directory, MAPPING_FILE_PREFIX + recordDefinition.prefix);
            if (mappingFile.exists()) {
                try {
                    FileInputStream is = new FileInputStream(mappingFile);
                    return RecordMapping.read(is, recordDefinition);
                }
                catch (Exception e) {
                    throw new FileStoreException(String.format("Unable to read mapping from %s", mappingFile.getAbsolutePath()), e);
                }
            }
            else {
                return new RecordMapping(recordDefinition.prefix);
            }
        }

        @Override
        public void setRecordMapping(RecordMapping recordMapping) throws FileStoreException {
            File mappingFile = new File(directory, MAPPING_FILE_PREFIX + recordMapping.getPrefix());
            try {
                FileOutputStream out = new FileOutputStream(mappingFile);
                RecordMapping.write(recordMapping, out);
                out.close();
            }
            catch (IOException e) {
                throw new FileStoreException(String.format("Unable to save mapping to %s", mappingFile.getAbsolutePath()), e);
            }
        }

        @Override
        public SourceDetails getSourceDetails() throws FileStoreException {
            File sourceDetailsFile = new File(directory, SOURCE_DETAILS_FILE_NAME);
            SourceDetails details = null;
            if (sourceDetailsFile.exists()) {
                try {
                    details = SourceDetails.read(new FileInputStream(sourceDetailsFile));
                }
                catch (Exception e) {
                    throw new FileStoreException(String.format("Unable to read source details from %s", sourceDetailsFile.getAbsolutePath()));
                }
            }
            if (details == null) {
                details = new SourceDetails();
            }
            return details;
        }

        @Override
        public void setSourceDetails(SourceDetails details) throws FileStoreException {
            File sourceDetailsFile = new File(directory, SOURCE_DETAILS_FILE_NAME);
            try {
                SourceDetails.write(details, new FileOutputStream(sourceDetailsFile));
            }
            catch (IOException e) {
                throw new FileStoreException(String.format("Unable to save source details file to %s", sourceDetailsFile.getAbsolutePath()), e);
            }
            catch (MetadataException e) {
                throw new FileStoreException("Unable to set source details", e);
            }
        }

        @Override
        public MappingOutput createMappingOutput(RecordMapping recordMapping, File normalizedFile) throws FileStoreException {
            return new MappingOutputImpl(directory, recordMapping, normalizedFile);
        }

        @Override
        public void delete() throws FileStoreException {
            if (directory.exists()) {
                for (File file : directory.listFiles()) {
                    if (!file.delete()) {
                        throw new FileStoreException(String.format("Unable to delete %s", file.getAbsolutePath()));
                    }
                }
                if (!directory.delete()) {
                    throw new FileStoreException(String.format("Unable to delete %s", directory.getAbsolutePath()));
                }
            }
        }

        private File getSourceFile() throws FileStoreException {
            File[] sources = directory.listFiles(new SourceFileFilter());
            if (sources.length != 1) {
                throw new FileStoreException("Expected exactly one file named source.???.xml.gz");
            }
            return sources[0];
        }
    }

    private static class MappingOutputImpl implements MappingOutput {
        private RecordMapping recordMapping;
        private File discardedFile, normalizedFile;
        private Writer outputWriter, discardedWriter;
        private int recordsNormalized, recordsDiscarded;

        private MappingOutputImpl(File directory, RecordMapping recordMapping, File normalizedFile) throws FileStoreException {
            this.recordMapping = recordMapping;
            try {
                if (normalizedFile != null) {
                    this.outputWriter = new OutputStreamWriter(new FileOutputStream(normalizedFile), "UTF-8");
                }
                this.discardedFile = new File(directory, DISCARDED_FILE_PREFIX + recordMapping.getPrefix());
                this.discardedWriter = new OutputStreamWriter(new FileOutputStream(discardedFile), "UTF-8");
            }
            catch (FileNotFoundException e) {
                throw new FileStoreException("Unable to create output files", e);
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Writer getNormalizedWriter() {
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
        public void close(boolean abort) throws FileStoreException {
            try {
                if (abort) {
                    recordMapping.setRecordsNormalized(0);
                    recordMapping.setRecordsDiscarded(0);
                    recordMapping.setNormalizeTime(0);
                    discardedWriter.close();
                    discardedFile.delete();
                    if (normalizedFile != null) {
                        normalizedFile.delete();
                    }
                }
                else {
                    if (outputWriter != null) {
                        outputWriter.close();
                    }
                    discardedWriter.close();
                    recordMapping.setRecordsNormalized(recordsNormalized);
                    recordMapping.setRecordsDiscarded(recordsDiscarded);
                    recordMapping.setNormalizeTime(System.currentTimeMillis());
                }
            }
            catch (IOException e) {
                throw new FileStoreException("Unable to close output", e);
            }
        }
    }

    private XStream getAppConfigStream() {
        XStream stream = new XStream();
        stream.processAnnotations(AppConfig.class);
        return stream;
    }

    private class SourceFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isFile() && file.getName().startsWith(SOURCE_FILE_PREFIX) && file.getName().endsWith(SOURCE_FILE_SUFFIX);
        }
    }

    private static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available??");
        }
    }

    static final String HEXES = "0123456789ABCDEF";

    private static String toHexadecimal(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
