package eu.delving.services.indexing;

import com.ctc.wstx.stax.WstxInputFactory;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.BadArgumentException;
import eu.europeana.core.database.ConsoleDao;
import eu.europeana.core.database.domain.CollectionState;
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.EuropeanaId;
import eu.europeana.core.database.domain.ImportFileState;
import eu.europeana.core.database.incoming.ImportException;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.definitions.annotations.EuropeanaBean;
import eu.europeana.definitions.annotations.EuropeanaField;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 27, 2010 9:24:53 PM
 */

public class PmhImporterImpl implements PmhImporter {
    private ConsoleDao consoleDao;
    private SolrServer solrServer;
    private EuropeanaBean europeanaBean;

    private Logger log = Logger.getLogger(getClass());
    private AnnotationProcessor annotationProcessor;
    private Class<?> beanClass;
    private int chunkSize = 1000;
    private HttpClient httpClient;
    private List<Processor> processors = new CopyOnWriteArrayList<Processor>();


    @Value("#{launchProperties['services.url']}")
    private String servicesUrl;

    @Autowired
    private MetaRepo metaRepo;

    @Autowired
    public void setConsoleDao(ConsoleDao consoleDao) {
        this.consoleDao = consoleDao;
    }

    @Autowired
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Autowired
    @Qualifier("solrUpdateServer")
    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @Autowired
    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }


    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }


    @Override
    public EuropeanaCollection commenceImport(Long collectionId) {
        EuropeanaCollection collection = consoleDao.fetchCollection(collectionId);
        if (collectionId != null) {
            for (Processor processor : processors) {
                if (processor.getCollection().getId().equals(collectionId)) {
                    return processor.getCollection();
                }
            }
            PmhImportProcessor pmhImportProcessor = new PmhImportProcessor(collection);
            processors.add(pmhImportProcessor);
            pmhImportProcessor.start();
            return collection;
        }
        return null;
    }

    @Override
    public EuropeanaCollection abortImport(Long collectionId) {
        EuropeanaCollection collection = consoleDao.fetchCollection(collectionId);
        for (Processor processor : processors) {
            if (processor.getCollection().equals(collection)) {
                return processor.stop();
            }
        }
        return collection;
    }

    @Override
    public List<EuropeanaCollection> getActiveImports() {
        List<EuropeanaCollection> active = new ArrayList<EuropeanaCollection>();
        for (Processor processor : processors) {
            active.add(processor.getCollection());
        }
        return active;
    }

    public class PmhImportProcessor implements Runnable, Processor {
        private Thread thread;
        private EuropeanaCollection collection;
        private List<SolrInputDocument> recordList = new ArrayList<SolrInputDocument>();

        private PmhImportProcessor(EuropeanaCollection collection) {
            this.collection = collection;
        }

        @Override
        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this);
                thread.setName(collection.getFileName());
                thread.start();
            } else {
                log.warn("Import processor already started for " + collection.getName());
            }
        }

        @Override
        public EuropeanaCollection stop() {
            if (thread != null) {
                Thread threadToJoin = thread;
                thread = null;
                try {
                    threadToJoin.join();
                } catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                }
            }
            return collection;
        }

        @Override
        public void run() {
            log.info("Importing " + collection);
            try {
                importPmh(collection);
                if (thread != null) {
                    log.info("Finished importing " + collection);
                    collection = consoleDao.updateCollectionCounters(collection.getId());
                    collection.setFileState(ImportFileState.IMPORTED);
                    collection.setCollectionState(CollectionState.ENABLED);
                } else {
                    log.info("Aborted importing " + collection);
                    collection.setCollectionState(CollectionState.EMPTY);
                    collection.setFileState(ImportFileState.UPLOADED);
                }
                collection = consoleDao.updateCollection(collection);
                consoleDao.removeFromIndexQueue(collection);
                MetaRepo.DataSet dataSet = metaRepo.getDataSet(collection.getName());
                if (dataSet == null) {
                    throw new RuntimeException("Expected to find data set for "+collection.getName());
                }
                dataSet.setState(MetaRepo.DataSetState.ENABLED);
                dataSet.save();
            } catch (ImportException e) {
                log.warn("Problem importing " + collection + " to database, moving to error directory", e);
                collection = consoleDao.setImportError(collection.getId(), exceptionToErrorString(e));
                collection.setFileState(ImportFileState.ERROR);
                collection = consoleDao.updateCollection(collection);
            }
            catch (BadArgumentException e) {
                log.warn("Problem importing " + collection + " to database, moving to error directory", e);
            }
            finally {
                processors.remove(this);
                thread = null;
            }
        }

        private void importPmh(EuropeanaCollection collection) throws ImportException {
            try {
                HttpMethod method = new GetMethod(String.format("%s/oai-pmh?verb=ListRecords&metadataPrefix=ese&set=%s", servicesUrl, collection.getName()));
                httpClient.executeMethod(method);
                InputStream inputStream = method.getResponseBodyAsStream();
                String resumptionToken = importXmlInternal(inputStream);
                while (!resumptionToken.isEmpty()) {
                    method = new GetMethod(String.format("%s/oai-pmh?verb=ListRecords&resumptionToken=%s", servicesUrl, resumptionToken));
                    httpClient.executeMethod(method);
                    inputStream = method.getResponseBodyAsStream();
                    resumptionToken = importXmlInternal(inputStream);
                    MetaRepo.DataSet dataSet = metaRepo.getDataSet(collection.getName());
                    if (dataSet == null) {
                        throw new RuntimeException("Data set not found!");
                    }
                    if (dataSet.getState() != MetaRepo.DataSetState.INDEXING) {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new ImportException("Problem reading the XML file", e);
            } catch (TransformerException e) {
                throw new ImportException("Problem transforming the XML file", e);
            } catch (XMLStreamException e) {
                throw new ImportException("Problem streaming the XML file", e);
            } catch (SolrServerException e) {
                throw new ImportException("Problem sending to Solr", e);
            } catch (Exception e) {
                throw new ImportException("Unknown problem", e);
            }
        }


        private String importXmlInternal(InputStream inputStream) throws TransformerException, XMLStreamException, IOException, ImportException, SolrServerException {
            XMLInputFactory inFactory = new WstxInputFactory();
            Source source = new StreamSource(inputStream, "UTF-8");
            XMLStreamReader xml = inFactory.createXMLStreamReader(source);
            EuropeanaId europeanaId = null;
            String resumptionToken = "";
            int recordCount = 0;
            int objectCount = 0;
            boolean isInMetadataBlock = false;
            long startTime = System.currentTimeMillis();
            SolrInputDocument solrInputDocument = null;
            while (thread != null) {
                switch (xml.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                        log.info("Document started");
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        if (isMetadataElement(xml)) {
                            isInMetadataBlock = true;
                        } else if (isRecordElement(xml) && isInMetadataBlock) {
                            europeanaId = new EuropeanaId(collection);
                            solrInputDocument = new SolrInputDocument();
                        } else if (isResumptionToken(xml)) {
                            resumptionToken = xml.getElementText();
                        } else if (europeanaId != null) {
                            EuropeanaField field = getEuropeanaField(xml.getPrefix(), xml.getLocalName(), recordCount);
//                            String language = fetchLanguage(xml);
                            String text = xml.getElementText();
                            if (field.europeana().id()) {
                                europeanaId.setEuropeanaUri(text);
                            } else if (field.europeana().object()) {
                                objectCount++;
                            } else if (field.europeana().type()) {
                                DocType.get(text); // checking if it matches one of them
                                SolrInputField objectField = solrInputDocument.getField("europeana_type");
                                if (objectField != null) {
                                    break;
                                }
                            }
                            if (text.length() > 10000) {
                                text = text.substring(0, 9999);
                            }
                            // language being ignored if (language != null) {...}
                            solrInputDocument.addField(field.getFieldNameString(), text);
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if (isRecordElement(xml) && isInMetadataBlock && europeanaId != null) {
                            isInMetadataBlock = false;
                            if (recordCount > 0 && recordCount % 500 == 0) {
                                log.info(String.format("imported %d records in %s", recordCount, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime)));
                            }
                            recordCount++;
                            if (europeanaId.getEuropeanaUri() == null) {
                                throw new ImportException("Normalized Record must have a field designated as europeana uri", recordCount);
                            }
                            Collection<Object> objectUrls = solrInputDocument.getFieldValues("europeana_object");
                            if (objectUrls != null) {
                                for (Object object : objectUrls) {
                                    String url = (String) object;
                                }
                            } else if ("true".equals(solrInputDocument.getFieldValue("europeana_hasObject"))) {
                                log.warn("No object urls for " + europeanaId.getEuropeanaUri());
                            }
                            if (!solrInputDocument.containsKey("europeana_collectionName")) {
                                solrInputDocument.addField("europeana_collectionName", collection.getName()); // todo: can't just use a string field name here
                            }
                            recordList.add(solrInputDocument);
                            consoleDao.saveEuropeanaId(europeanaId);
                            europeanaId = null;
                            solrInputDocument = null;
                        }
                        else if (isMetadataElement(xml)) {
                            isInMetadataBlock = false;
                        }
                        break;

                    case XMLStreamConstants.END_DOCUMENT:
                        log.info(String.format("Document ended, imported %d records", recordCount));
                        break;
                }
                if (recordList.size() >= chunkSize) {
                    indexRecordList();
                }
                if (!xml.hasNext()) {
                    break;
                }
                xml.next();
            }
            if (!recordList.isEmpty()) {
                indexRecordList();
            }
            inputStream.close();
            return resumptionToken;
        }

        private void indexRecordList() throws IOException, SolrServerException {
            log.info("sending " + recordList.size() + " records to solr");
            try {
                solrServer.add(recordList);
                metaRepo.incrementRecordCount(collection.getName(), recordList.size());
            } catch (SolrServerException e) {
                log.error("unable to index this batch");
                log.error(recordList.toString());
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            solrServer.commit();       // It is better to use the  autocommit from solr
            recordList.clear();
        }

        private EuropeanaField getEuropeanaField(String prefix, String localName, int recordCount) throws ImportException {
            EuropeanaField field = null;
            for (EuropeanaField recordField : getEuropeanaBean().getFields()) {
                if (recordField.getPrefix().equals(prefix) && recordField.getLocalName().equals(localName)) {
                    field = recordField;
                    break;
                }
            }
            if (field == null) {
                throw new ImportException("Field not recognized: " + prefix + ":" + localName, recordCount);
            }
            return field;
        }



        private boolean isRecordElement(XMLStreamReader xml) {
            return "record".equals(xml.getName().getLocalPart());
        }

        private boolean isMetadataElement(XMLStreamReader xml) {
            return "metadata".equals(xml.getName().getLocalPart());
        }

        private boolean isResumptionToken(XMLStreamReader xml) {
            return "resumptionToken".equals(xml.getName().getLocalPart());
        }

        @Override
        public EuropeanaCollection getCollection() {
            return collection;
        }

        private EuropeanaBean getEuropeanaBean() {
            if (europeanaBean == null) {
                europeanaBean = annotationProcessor.getEuropeanaBean(beanClass);
                if (europeanaBean == null) {
                    throw new RuntimeException("Expected to find bean for class " + beanClass);
                }
            }
            return europeanaBean;
        }


    }

    private static String exceptionToErrorString(ImportException exception) {
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

    /**
     * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
     * @since Sep 27, 2010 9:35:41 PM
     */
    public static interface Processor {
        EuropeanaCollection getCollection();

        EuropeanaCollection stop();

        void start();
    }
}
