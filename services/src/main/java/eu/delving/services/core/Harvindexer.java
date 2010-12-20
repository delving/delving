package eu.delving.services.core;

import com.ctc.wstx.stax.WstxInputFactory;
import eu.delving.metadata.FieldDefinition;
import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.Path;
import eu.delving.metadata.Tag;
import eu.delving.services.exceptions.HarvindexingException;
import eu.delving.sip.AccessKey;
import eu.delving.sip.DataSetState;
import eu.europeana.core.database.ConsoleDao;
import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.CollectionState;
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.EuropeanaId;
import eu.europeana.core.database.domain.SocialTag;
import eu.europeana.core.querymodel.query.DocType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

public class Harvindexer {
    private ConsoleDao consoleDao;
    private SolrServer solrServer;
    private XMLInputFactory inFactory = new WstxInputFactory();

    private Logger log = Logger.getLogger(getClass());
    private int chunkSize = 1000;
    private HttpClient httpClient;
    private List<Processor> processors = new CopyOnWriteArrayList<Processor>();


    @Value("#{launchProperties['services.harvindexing.prefix']}")
    private String metadataPrefix;

    @Value("#{launchProperties['services.url']}")
    private String servicesUrl;

    @Autowired
    private AccessKey accessKey;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MetaRepo metaRepo;

    @Autowired
    private MetadataModel metadataModel;

    @Autowired
    public void setConsoleDao(ConsoleDao consoleDao) {
        this.consoleDao = consoleDao;
    }

    @Autowired
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Autowired
    public void setSolrServer(@Qualifier("solrUpdateServer") SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public EuropeanaCollection commenceImport(Long collectionId) {
        EuropeanaCollection collection = consoleDao.fetchCollection(collectionId);
        if (collectionId != null) {
            for (Processor processor : processors) {
                if (processor.getCollection().getId().equals(collectionId)) {
                    return processor.getCollection();
                }
            }
            Processor processor = new Processor(collection);
            processors.add(processor);
            processor.start();
            return collection;
        }
        return null;
    }

    public EuropeanaCollection abortImport(Long collectionId) {
        EuropeanaCollection collection = consoleDao.fetchCollection(collectionId);
        for (Processor processor : processors) {
            if (processor.getCollection().equals(collection)) {
                return processor.stop();
            }
        }
        return collection;
    }

    public List<EuropeanaCollection> getActiveImports() {
        List<EuropeanaCollection> active = new ArrayList<EuropeanaCollection>();
        for (Processor processor : processors) {
            active.add(processor.getCollection());
        }
        return active;
    }

    public void commitSolr() throws IOException, SolrServerException {
        solrServer.commit();
    }

    public class Processor implements Runnable {
        private Thread thread;
        private EuropeanaCollection collection;
        private List<SolrInputDocument> recordList = new ArrayList<SolrInputDocument>();

        private Processor(EuropeanaCollection collection) {
            this.collection = collection;
        }

        public void start() {
            if (this.thread == null) {
                this.thread = new Thread(this);
                thread.setName(collection.getFileName());
                thread.start();
            }
            else {
                log.warn("Import processor already started for " + collection.getName());
            }
        }

        public EuropeanaCollection stop() {
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
            return collection;
        }

        @Override
        public void run() {
            log.info("Importing " + collection);
            try {
                DateTime now = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                importPmh(collection);
                solrServer.deleteByQuery("europeana_collectionName:" + collection.getName() + " AND timestamp:[* TO " + fmt.print(now) + "]");
//                solrServer.commit(); // now you don't have to
                if (thread != null) {
                    log.info("Finished importing " + collection);
                    collection = consoleDao.updateCollectionCounters(collection.getId());
                    collection.setCollectionState(CollectionState.ENABLED);
                }
                else {
                    log.info("Aborted importing " + collection);
                    collection.setCollectionState(CollectionState.EMPTY);
                }
                collection = consoleDao.updateCollection(collection);
                enableDataSet();
            }
            catch (HarvindexingException e) {
                log.warn("Problem importing " + collection + " to database", e);
                collection = consoleDao.setImportError(collection.getId(), exceptionToErrorString(e));
                collection = consoleDao.updateCollection(collection);
                recordProblem(e);
            }
            catch (Exception e) {
                recordProblem(e);
            }
            finally {
                processors.remove(this);
                thread = null;
            }
        }

        private void enableDataSet() {
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(collection.getName());
            if (dataSet == null) {
                throw new RuntimeException("Expected to find data set for " + collection.getName());
            }
            dataSet.setState(DataSetState.ENABLED);
            dataSet.save();
        }

        private void recordProblem(Exception ex) {
            log.warn("Problem importing " + collection + ", to ERROR state.", ex);
            MetaRepo.DataSet dataSet = metaRepo.getDataSet(collection.getName());
            if (dataSet == null) {
                throw new RuntimeException("Expected to find data set for " + collection.getName());
            }
            dataSet.setErrorState(ex.getMessage());
            dataSet.save();
        }

        private void importPmh(EuropeanaCollection collection) throws HarvindexingException, IOException, TransformerException, XMLStreamException, SolrServerException {
            String accessKey = Harvindexer.this.accessKey.createKey("HARVINDEXER");
            String url = String.format(
                    "%s/oai-pmh?verb=ListRecords&metadataPrefix=%s&set=%s&accessKey=%s",
                    servicesUrl,
                    metadataPrefix,
                    collection.getName(),
                    accessKey
            );
            HttpMethod method = new GetMethod(url);
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
                log.info(String.format("Indexed %d of %d records", dataSet.getRecordsIndexed(), dataSet.getRecordCount()));
                if (dataSet.getState() != DataSetState.INDEXING) {
                    break;
                }
            }
            if (!recordList.isEmpty()) {
                indexRecordList();
            }
        }

        private String importXmlInternal(InputStream inputStream) throws TransformerException, XMLStreamException, IOException, SolrServerException, HarvindexingException {
            Source source = new StreamSource(inputStream, "UTF-8");
            XMLStreamReader xml = inFactory.createXMLStreamReader(source);
            EuropeanaId europeanaId = null;
            String pmhId = null;
            String resumptionToken = "";
            int recordCount = 0;
            boolean isInMetadataBlock = false;
            long startTime = System.currentTimeMillis();
            Path path = new Path();
            SolrInputDocument solrInputDocument = null;
            while (thread != null) {
                switch (xml.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                        log.info("Document started");
                        break;

                    case XMLStreamConstants.START_ELEMENT:
                        if (isErrorElement(xml)) {
                            throw new HarvindexingException(xml.getElementText());
                        }
                        else if (!isInMetadataBlock && isPmhIdentifier(xml)) {
                            pmhId = xml.getElementText();
                        }
                        else if (isMetadataElement(xml)) {
                            isInMetadataBlock = true;
                        }
                        else if (isResumptionToken(xml)) {
                            resumptionToken = xml.getElementText();
                        }
                        else if (isRecordElement(xml) && isInMetadataBlock) {
                            path.push(Tag.create(xml.getName().getPrefix(), xml.getName().getLocalPart()));
                            europeanaId = new EuropeanaId(collection);
                            solrInputDocument = new SolrInputDocument();
                            solrInputDocument.addField("delving_pmhId", pmhId);
                        }
                        else if (europeanaId != null) {
                            path.push(Tag.create(xml.getName().getPrefix(), xml.getName().getLocalPart()));
                            FieldDefinition fieldDefinition = getFieldDefinition(path, recordCount);
                            String text = xml.getElementText();
                            FieldDefinition.Validation validation = fieldDefinition.validation;
                            if (validation != null) {
                                if (validation.id) {
                                    europeanaId.setEuropeanaUri(text);
                                }
                                else if (validation.type) {
                                    DocType.get(text); // checking if it matches one of them
                                    SolrInputField objectField = solrInputDocument.getField("europeana_type");
                                    if (objectField != null) {
                                        break;
                                    }
                                }
                            }
                            if (text.length() > 10000) {
                                text = text.substring(0, 9999);
                            }
                            // language being ignored if (language != null) {...}
                            solrInputDocument.addField(fieldDefinition.getFieldNameString(), text);
                            if (xml.isEndElement()) {
                                path.pop();
                            }
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
                                throw new HarvindexingException("Normalized Record must have a field designated as europeana uri", recordCount);
                            }
                            Collection<Object> objectUrls = solrInputDocument.getFieldValues("europeana_object");
                            if (objectUrls != null) {
                                for (Object object : objectUrls) {
                                    String url = (String) object;
                                }
                            }
                            else if ("true".equals(solrInputDocument.getFieldValue("europeana_hasObject"))) {
                                log.warn("No object urls for " + europeanaId.getEuropeanaUri());
                            }
                            if (!solrInputDocument.containsKey("europeana_collectionName")) {
                                solrInputDocument.addField("europeana_collectionName", collection.getName()); // todo: can't just use a string field name here
                            }
                            final List<SocialTag> socialTags = userDao.fetchAllSocialTags(europeanaId.getEuropeanaUri());
                            for (SocialTag socialTag : socialTags) {
                                solrInputDocument.addField("europeana_userTag", socialTag.getTag());
                            }
                            recordList.add(solrInputDocument);
                            consoleDao.saveEuropeanaId(europeanaId);
                            europeanaId = null;
                            solrInputDocument = null;
                            path.pop();
                        }
                        else if (isMetadataElement(xml)) {
                            isInMetadataBlock = false;
                        }
                        if (europeanaId != null) {
                            path.pop();
                            log.info("eid not null end: "+path);
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
            xml.close();
            inputStream.close();
            return resumptionToken;
        }

        private void indexRecordList() throws IOException, SolrServerException {
            log.info("sending " + recordList.size() + " records to solr");
            try {
                solrServer.add(recordList);
                metaRepo.incrementRecordCount(collection.getName(), recordList.size());
            }
            catch (SolrServerException e) {
                log.error("unable to index this batch");
                log.error(recordList.toString());
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
//            solrServer.commit();       // It is better to use the  autocommit from solr
            recordList.clear();
        }

        private FieldDefinition getFieldDefinition(Path path, int recordCount) throws HarvindexingException {
            FieldDefinition fieldDefinition = metadataModel.getRecordDefinition().getFieldDefinition(path);
            if (fieldDefinition == null) {
                throw new HarvindexingException("Field not recognized: " + path, recordCount);
            }
            return fieldDefinition;
        }

        private boolean isRecordElement(XMLStreamReader xml) {
            return "record".equals(xml.getName().getLocalPart());
        }

        private boolean isMetadataElement(XMLStreamReader xml) {
            return "metadata".equals(xml.getName().getLocalPart());
        }

        private boolean isPmhIdentifier(XMLStreamReader xml) {
            return "identifier".equals(xml.getName().getLocalPart());
        }

        private boolean isErrorElement(XMLStreamReader xml) {
            return "error".equals(xml.getName().getLocalPart());
        }

        private boolean isResumptionToken(XMLStreamReader xml) {
            return "resumptionToken".equals(xml.getName().getLocalPart());
        }

        public EuropeanaCollection getCollection() {
            return collection;
        }
    }

    private static String exceptionToErrorString(HarvindexingException exception) {
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
