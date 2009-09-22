/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.database.migration.incoming;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.ImportFileState;
import eu.europeana.database.migration.outgoing.DatabaseToSolrIndexer;
import eu.europeana.query.DocType;
import eu.europeana.query.RecordField;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.stream.*;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 29, 2009: 9:13:49 PM
 */
public class DataImporter {
    private static final Logger log = Logger.getLogger(DataImporter.class);
    private static final String RESOLVABLE_URI = "http://www.europeana.eu/resolve/record/";
    private static final DecimalFormat COUNT_FORMAT = new DecimalFormat("000000000");

    private boolean normalized = true;
    private EuropeanaCollection collection;

    private DashboardDao dashboardDao;
    private DatabaseToSolrIndexer solrIndexer;

    public DataImporter() {
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "/database-application-context.xml",
                "/test-application-context.xml"
        });
        dashboardDao = (DashboardDao) context.getBean("dashboardDao");
        solrIndexer = (DatabaseToSolrIndexer) context.getBean("databaseToSolrIndexer");
    }

    public String importFile(String importingFile) {
        log.info("Processing " + importingFile);
        String bareFileName = new File(importingFile).getName();
        try {
            collection = dashboardDao.fetchCollectionByFileName(bareFileName);
            if (collection == null) {
                collection = dashboardDao.fetchCollectionByName(bareFileName, true);
//                throw new ImportException("No collection found for file name " + importingFile);
            }
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(importingFile);
            }
            catch (FileNotFoundException e) {
                throw new ImportException("File not found: " + importingFile, e);
            }
            catch (IOException e) {
                throw new ImportException("Problem while reading: " + importingFile, e);
            }
            collection = dashboardDao.prepareForImport(collection.getId());
            importXml(collection, inputStream);
            log.info("Finished processing " + importingFile);
            collection = dashboardDao.updateCollectionCounters(collection.getId());
//            transition(ImportFileState.IMPORTED);
        }
        catch (ImportException e) {
            log.warn("Problem importing " + importingFile + " to database, moving to error directory", e);
            collection = dashboardDao.setImportError(collection.getId(), exceptionToErrorString(e));
        }
        return bareFileName;
    }

    public void prepareCollectionForIndexing(String fileName) {
        collection = dashboardDao.fetchCollectionByFileName(fileName);
        dashboardDao.addToIndexQueue(collection);
        // todo maybe add later
//        dashboardDao.addToCacheQueue(collection);
    }

    public void runIndexer() {
        do {
            solrIndexer.run();
        }
        while (dashboardDao.getIndexQueueHead() != null);
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


    private void transition(ImportFileState state) {
        collection.setFileState(state);
        collection = dashboardDao.updateCollection(collection);
    }

    private void importXml(EuropeanaCollection collection, InputStream inputStream) throws ImportException {
        try {
            importXmlInternal(collection, inputStream);
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

    private void importXmlInternal(EuropeanaCollection collection, InputStream inputStream) throws TransformerException, XMLStreamException, IOException, ImportException {
        XMLInputFactory inFactory = new WstxInputFactory();
        XMLOutputFactory outFactory = new WstxOutputFactory();
        XMLStreamReader xml = inFactory.createXMLStreamReader(inputStream, "UTF-8");
        EuropeanaId europeanaId = null;
        Set<String> objectUrls = new TreeSet<String>();
        int recordCount = 0;
        boolean[] fieldFound = new boolean[RecordField.values().length];
        long time = System.currentTimeMillis();
        ByteArrayOutputStream out = null;
        XMLStreamWriter writer = null;
//        while (thread != null) {
        while (xml.hasNext()) {
            switch (xml.getEventType()) {
                case XMLStreamConstants.START_DOCUMENT:
                    log.info("Document started");
                    break;

                case XMLStreamConstants.START_ELEMENT:
                    if (isRecordElement(xml)) {
                        europeanaId = new EuropeanaId(collection);
                        Arrays.fill(fieldFound, false);
                        out = new ByteArrayOutputStream();
                        writer = outFactory.createXMLStreamWriter(out, "UTF-8");
                        writer.writeStartDocument();
                        writer.writeCharacters("\n");
                        writer.writeStartElement("doc");
                        writer.writeCharacters("\n");
                    }
                    else if (europeanaId != null) {
                        RecordField field = getRecordField(xml.getPrefix(), xml.getLocalName(), recordCount);
                        String language = fetchLanguage(xml);
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
                            writer.writeCharacters("\t");
                            writer.writeStartElement("field");
                            if (field.getFacetType() != null) {
                                writer.writeAttribute("name", field.getFacetType().toString());
                            }
                            else {
                                writer.writeAttribute("name", field.toFieldNameString());
                            }
                            if (language != null) {
                                writer.writeAttribute("lang", language);
                            }
                            if (text.length() > 10000) {
                                text = text.substring(0, 9999);
                            }
                            writer.writeCharacters(text);
                            writer.writeEndElement();
                            writer.writeCharacters("\n");
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (isRecordElement(xml) && europeanaId != null) {
                        if (recordCount % 100 == 0) {
                            log.info("imported " + recordCount + " records");
                        }
                        recordCount++;
                        writer.writeEndElement();
                        writer.writeCharacters("\n");
                        writer.close();
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
                        europeanaId.setSolrRecords(out.toString());
                        europeanaId.setOrphan(false);
                        dashboardDao.saveEuropeanaId(europeanaId, objectUrls);
                        europeanaId = null;
                        objectUrls.clear();
                        writer = null;
                        out = null;
                    }
                    break;

                case XMLStreamConstants.END_DOCUMENT:
                    log.info("Document ended");
                    break;
            }
            if (!xml.hasNext()) {
                break;
            }
            xml.next();
        }
        time = System.currentTimeMillis() - time;
        log.info("Processed " + recordCount + " records in " + (time / 60000.0) + " minutes");
        inputStream.close();
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

    private String fetchLanguage(XMLStreamReader xml) {
        for (int walk = 0; walk < xml.getAttributeCount(); walk++) {
            if ("xml".equals(xml.getAttributePrefix(walk)) && "lang".equals(xml.getAttributeLocalName(walk))) {
                return xml.getAttributeValue(walk);
            }
        }
        return null;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }
}


