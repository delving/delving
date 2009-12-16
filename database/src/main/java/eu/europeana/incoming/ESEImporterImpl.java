/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.incoming;

import com.ctc.wstx.stax.WstxInputFactory;
import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.CacheState;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.ImportFileState;
import eu.europeana.query.DocType;
import eu.europeana.query.ESERecord;
import eu.europeana.query.RecordField;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Import xml files filled with normalized metadata into the database
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ESEImporterImpl implements ESEImporter {
    private static final String RESOLVABLE_URI = "http://www.europeana.eu/resolve/record/";
    private static final DecimalFormat COUNT_FORMAT = new DecimalFormat("000000000");
    private static final String ESE_SCHEMA = "ESE-V3.2.xsd";
    private Logger log = Logger.getLogger(getClass());
    private DashboardDao dashboardDao;
    private ImportRepository importRepository;
    private SolrIndexer solrIndexer;
    private boolean normalized;
    private boolean commitImmediately;
    private int chunkSize = 1000;
    private List<Processor> processors = new CopyOnWriteArrayList<Processor>();

    private interface Processor {
        ImportFile getFile();

        ImportFile stop();

        void start();
    }

    @Autowired
    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    @Autowired
    public void setSolrIndexer(SolrIndexer solrIndexer) {
        this.solrIndexer = solrIndexer;
    }

    // npot @Autowired because there are multiple
    public void setImportRepository(ImportRepository importRepository) {
        this.importRepository = importRepository;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public void setCommitImmediately(boolean commitImmediately) {
        this.commitImmediately = commitImmediately;
    }

    public ImportRepository getImportRepository() {
        return importRepository;
    }

    public ImportFile commenceValidate(ImportFile importFile, Long collectionId) {
        importFile = importRepository.transition(importFile, ImportFileState.VALIDATING);
        if (importFile != null) {
            for (Processor processor : processors) {
                if (processor.getFile().equals(importFile)) {
                    return processor.getFile();
                }
            }
            Processor processor = new ValidationProcessor(importFile, collectionId);
            processors.add(processor);
            processor.start();
            return importFile;
        }
        return null;
    }

    public ImportFile commenceImport(ImportFile importFile, Long collectionId) {
        ImportFile importingFile = importRepository.transition(importFile, ImportFileState.IMPORTING);
        if (importingFile != null) {
            for (Processor processor : processors) {
                if (processor.getFile().equals(importingFile)) {
                    return processor.getFile();
                }
            }
            ImportProcessor importProcessor = new ImportProcessor(importingFile, dashboardDao.prepareForImport(collectionId));
            processors.add(importProcessor);
            importProcessor.start();
            return importingFile;
        }
        return null;
    }

    public ImportFile abortImport(ImportFile importingFile) {
        for (Processor processor : processors) {
            if (processor.getFile().equals(importingFile)) {
                return processor.stop();
            }
        }
        return importingFile;
    }

    public List<ImportFile> getActiveImports() {
        List<ImportFile> active = new ArrayList<ImportFile>();
        for (Processor processor : processors) {
            active.add(processor.getFile());
        }
        return active;
    }

    private class ImportProcessor implements Runnable, Processor {
        private Thread thread;
        private ImportFile importFile;
        private EuropeanaCollection collection;
        private List<SolrIndexer.Record> recordList = new ArrayList<SolrIndexer.Record>();

        private ImportProcessor(ImportFile importFile, EuropeanaCollection collection) {
            this.importFile = importFile;
            this.collection = collection;
        }

        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this);
                thread.setName(importFile.getFileName());
                thread.start();
            }
            else {
                log.warn("Import processor already started for " + importFile);
            }
        }

        public ImportFile stop() {
            if (thread != null) {
                Thread threadToJoin = thread;
                thread = null;
                try {
                    threadToJoin.join();
                }
                catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                }
            }
            return importFile;
        }

        public void run() {
            log.info("Importing " + importFile);
            try {
                InputStream inputStream = createInputStream(importFile);
                importXml(inputStream);
                if (thread != null) {
                    log.info("Finished importing " + importFile);
                    collection = dashboardDao.updateCollectionCounters(collection.getId());
                    importFile = importRepository.transition(importFile, ImportFileState.IMPORTED);
                    collection.setFileState(ImportFileState.IMPORTED);
                    collection.setCollectionState(CollectionState.ENABLED);
                    if (collection.getCacheState() == CacheState.EMPTY) {
                        collection.setCacheState(CacheState.UNCACHED);
                    }
                }
                else {
                    log.info("Aborted importing " + importFile);
                    collection.setCollectionState(CollectionState.EMPTY);
                    if (normalized) {
                        importFile = importRepository.transition(importFile, ImportFileState.UPLOADED);
                        collection.setFileState(ImportFileState.UPLOADED);
                    }
                    else {
                        importFile = importRepository.transition(importFile, ImportFileState.VALIDATED);
                        collection.setFileState(ImportFileState.VALIDATED);
                    }
                }
                collection = dashboardDao.updateCollection(collection);
                dashboardDao.removeFromIndexQueue(collection);
            }
            catch (ImportException e) {
                log.warn("Problem importing " + importFile + " to database, moving to error directory", e);
                collection = dashboardDao.setImportError(collection.getId(), exceptionToErrorString(e));
                importFile = importRepository.transition(importFile, ImportFileState.ERROR);
                collection.setFileState(ImportFileState.ERROR);
                collection = dashboardDao.updateCollection(collection);
            }
            finally {
                processors.remove(this);
                thread = null;
            }
        }

        private void importXml(InputStream inputStream) throws ImportException {
            try {
                importXmlInternal(inputStream);
            }
            catch (IOException e) {
                throw new ImportException("Problem reading the XML file", e);
            }
            catch (TransformerException e) {
                throw new ImportException("Problem transforming the XML file", e);
            }
            catch (XMLStreamException e) {
                throw new ImportException("Problem streaming the XML file", e);
            }
        }

        private void importXmlInternal(InputStream inputStream) throws TransformerException, XMLStreamException, IOException, ImportException {
            XMLInputFactory inFactory = new WstxInputFactory();
            Source source = new StreamSource(inputStream, "UTF-8");
            XMLStreamReader xml = inFactory.createXMLStreamReader(source);
            EuropeanaId europeanaId = null;
            Set<String> objectUrls = new TreeSet<String>();
            int recordCount = 0;
            boolean[] fieldFound = new boolean[RecordField.values().length];
            long time = System.currentTimeMillis();
            ESERecord eseRecord = null;
            while (thread != null) {
                switch (xml.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                        log.info("Document started");
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        if (isRecordElement(xml)) {
                            europeanaId = new EuropeanaId(collection);
                            Arrays.fill(fieldFound, false);
                            eseRecord = new ESERecord();
                        }
                        else if (europeanaId != null) {
                            RecordField field = getRecordField(xml.getPrefix(), xml.getLocalName(), recordCount);
//                            String language = fetchLanguage(xml);
                            String text = xml.getElementText();
                            if (field == RecordField.EUROPEANA_URI) {
                                europeanaId.setEuropeanaUri(text);
                            }
                            else {
                                switch (field) {
                                    case EUROPEANA_OBJECT:
                                        objectUrls.add(text);
                                        break;
                                    case EUROPEANA_TYPE:
                                        DocType.get(text); // checking if it matches one of them
                                        break;
                                }
                                fieldFound[field.ordinal()] = true;
                                if (text.length() > 10000) {
                                    text = text.substring(0, 9999);
                                }
                                // language being ignored if (language != null) {...}
                                eseRecord.put(field, text);
                            }
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (isRecordElement(xml) && europeanaId != null) {
                            if (recordCount % 500 == 0) {
                                log.info("imported " + recordCount + " records");
                            }
                            recordCount++;
                            if (normalized) {
                                if (europeanaId.getEuropeanaUri() == null) {
                                    throw new ImportException("Normalized Record must have a field " + RecordField.EUROPEANA_URI, recordCount);
                                }
                            }
                            else {
                                expectField(fieldFound, RecordField.EUROPEANA_PROVIDER, recordCount);
                                expectField(fieldFound, RecordField.EUROPEANA_TYPE, recordCount);
                                if (!(fieldFound[RecordField.EUROPEANA_IS_SHOWN_AT.ordinal()] || fieldFound[RecordField.EUROPEANA_IS_SHOWN_BY.ordinal()])) {
                                    throw new ImportException("Sandbox Record must have field " + RecordField.EUROPEANA_IS_SHOWN_AT + " or " + RecordField.EUROPEANA_IS_SHOWN_BY, recordCount);
                                }
                                // todo: maybe enable later
//                                expectNoField(fieldFound, RecordField.EUROPEANA_YEAR, recordCount);
//                                expectNoField(fieldFound, RecordField.EUROPEANA_COUNTRY, recordCount);
//                                expectNoField(fieldFound, RecordField.EUROPEANA_HAS_OBJECT, recordCount);
                                expectNoField(fieldFound, RecordField.EUROPEANA_USER_TAG, recordCount);
                                if (europeanaId.getEuropeanaUri() != null) {
                                    throw new ImportException("Sandbox Record must not have a field " + RecordField.EUROPEANA_URI, recordCount);
                                }
                                europeanaId.setEuropeanaUri(RESOLVABLE_URI + collection.getName() + "/" + COUNT_FORMAT.format(recordCount));
                            }
                            recordList.add(new SolrIndexer.Record(europeanaId, eseRecord));
                            dashboardDao.saveEuropeanaId(europeanaId, objectUrls);
                            europeanaId = null;
                            objectUrls.clear();
                            eseRecord = null;
                        }
                        break;

                    case XMLStreamConstants.END_DOCUMENT:
                        log.info("Document ended");
                        break;
                }
                if (recordList.size() >= chunkSize) {
                    indexRecordList();
                }
                if (!xml.hasNext()) {
                    if (!recordList.isEmpty()) {
                        indexRecordList();
                    }
                    break;
                }
                xml.next();
            }
            time = System.currentTimeMillis() - time;
            log.info("Processed " + recordCount + " records in " + (time / 60000.0) + " minutes");
            inputStream.close();
        }

        private boolean indexRecordList() {
            if (solrIndexer.indexRecordList(new ArrayList<SolrIndexer.Record>(recordList))) {
                recordList.clear();
                if (commitImmediately) {
                    if (!solrIndexer.commit()) {
                        log.warn("cannot commit explicitly");
                    }
                }
                return true;
            }
            else {
                return false;
            }
        }

        private void expectField(boolean[] found, RecordField recordField, int recordCount) throws ImportException {
            if (!found[recordField.ordinal()]) {
                throw new ImportException("Record missing field " + recordField, recordCount);
            }
        }

        private void expectNoField(boolean[] found, RecordField recordField, int recordCount) throws ImportException {
            if (found[recordField.ordinal()]) {
                throw new ImportException("Record may not have field " + recordField, recordCount);
            }
        }

        private RecordField getRecordField(String prefix, String localName, int recordCount) throws ImportException {
            RecordField field = null;
            for (RecordField recordField : RecordField.values()) {
                if (recordField.getPrefix().equals(prefix) && recordField.getLocalName().equals(localName)) {
                    field = recordField;
                    break;
                }
            }
            if (field == null) {
                throw new ImportException("Record field not recognized: " + prefix + ":" + localName, recordCount);
            }
            return field;
        }

        private boolean isRecordElement(XMLStreamReader xml) {
            return "record".equals(xml.getName().getLocalPart());
        }

//        private String fetchLanguage(XMLStreamReader xml) {
//            for (int walk = 0; walk < xml.getAttributeCount(); walk++) {
//                if ("xml".equals(xml.getAttributePrefix(walk)) && "lang".equals(xml.getAttributeLocalName(walk))) {
//                    return xml.getAttributeValue(walk);
//                }
//            }
//            return null;
//        }

        public ImportFile getFile() {
            return importFile;
        }
    }

    private class ValidationProcessor implements Runnable, Processor {
        private Long collectionId;
        private EuropeanaCollection collection;
        private Thread thread;
        private ImportFile importFile;

        private ValidationProcessor(ImportFile importFile, Long collectionId) {
            this.importFile = importFile;
            this.collectionId = collectionId;
        }

        public void run() {
            log.info("Validating " + importFile);
            try {
                collection = dashboardDao.fetchCollection(collectionId);
                if (collection == null) {
                    throw new ImportException("No collection found with id " + collectionId);
                }
                InputStream inputStream = createInputStream(importFile);
                validateXml(inputStream);
                if (thread != null) {
                    log.info("Finished validating " + importFile);
                    transition(ImportFileState.VALIDATED);
                }
                else {
                    log.info("Aborted validating " + importFile);
                    transition(ImportFileState.UPLOADED);
                }
            }
            catch (ImportException e) {
                log.warn("Problem validating " + importFile + ", moving to error directory", e);
                collection = dashboardDao.setImportError(collection.getId(), exceptionToErrorString(e));
                transition(ImportFileState.ERROR);
            }
            finally {
                processors.remove(this);
                thread = null;
            }
        }

        private void validateXml(InputStream inputStream) throws ImportException {
            Source source = new StreamSource(inputStream, "UTF-8");
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            ErrorHandler errorHandler = new ErrorHandler();
            try {
                Schema schema = schemaFactory.newSchema(getClass().getResource("/"+ESE_SCHEMA));
                Validator validator = schema.newValidator();
                validator.setErrorHandler(errorHandler);
                validator.validate(source);
//                try {
//                    Thread.sleep(1000);
//                }
//                catch (InterruptedException e) {
//                    throw new IOException("Just to keep the catch clause below");
//                }
                if (!errorHandler.exceptions.isEmpty()) {
                    throw new ImportException("File is invalid according to "+ESE_SCHEMA, errorHandler.exceptions.get(0));
                }
            }
            catch (SAXException e) {
                log.error("Unable to parse ESE schema!");
                throw new RuntimeException("Unable to parse schema");
            }
            catch (IOException e) {
                throw new ImportException("Problem reading file while validating", e);
            }
        }

        public ImportFile getFile() {
            return importFile;
        }

        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this);
                thread.setName(importFile.getFileName());
                thread.start();
            }
            else {
                log.warn("Validation processor already started for " + importFile);
            }
        }

        public ImportFile stop() {
            if (thread != null) {
                Thread threadToJoin = thread;
                thread = null;
                try {
                    threadToJoin.join();
                }
                catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                }
            }
            return importFile;
        }

        private class ErrorHandler extends DefaultHandler {
            private List<SAXParseException> exceptions = new ArrayList<SAXParseException>();

            public void error(SAXParseException parseException) throws SAXException {
                exceptions.add(parseException);
            }

            public void fatalError(SAXParseException parseException) throws SAXException {
                exceptions.add(parseException);
            }
        }

        private void transition(ImportFileState state) {
            importFile = importRepository.transition(importFile, state);
            collection.setFileState(state);
            collection = dashboardDao.updateCollection(collection);
        }
    }

    private InputStream createInputStream(ImportFile importFile) throws ImportException {
        try {
            InputStream inputStream;
            if (importFile.isXml()) {
                inputStream = new FileInputStream(importRepository.createFile(importFile));
            }
            else if (importFile.isGzipXml()) {
                inputStream = new GZIPInputStream(new FileInputStream(importRepository.createFile(importFile)));
            }
            else {
                throw new ImportException("File is of the wrong type");
            }
            return inputStream;
        }
        catch (FileNotFoundException e) {
            throw new ImportException("File not found: " + importFile, e);
        }
        catch (IOException e) {
            throw new ImportException("Problem while reading: " + importFile, e);
        }
    }

    private String exceptionToErrorString(ImportException exception) {
        StringBuilder out = new StringBuilder();
        out.append(exception.getMessage());
        Throwable cause = exception.getCause();
        while (cause != null) {
            out.append('\n');
            out.append(cause.toString());
            cause = cause.getCause();
        }
        return out.toString();
    }

}