package eu.delving.services.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.core.metadata.FieldDefinition;
import eu.delving.core.metadata.MetadataException;
import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.MetadataNamespace;
import eu.delving.core.metadata.NamespaceDefinition;
import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.Tag;
import eu.delving.core.rest.ServiceAccessToken;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.BadResumptionTokenException;
import eu.delving.services.exceptions.CannotDisseminateFormatException;
import eu.delving.services.exceptions.NoRecordsMatchException;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCodeResource;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Wrap the mongo database so that what goes in and comes out is managed.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoImpl implements MetaRepo {
    private static final MetadataNamespace MAPPED_NAMESPACE = MetadataNamespace.ABM;
    private static final boolean MAPPED_NAMESPACE_ACCESS_KEY_REQUIRED = true;
    private Logger log = Logger.getLogger(getClass());
    private int responseListSize = 5;
    private int harvestStepSecondsToLive = 5;
    private DB mongoDatabase;

    @Autowired
    private MetaConfig metaRepoConfig;

    @Autowired
    private ServiceAccessToken serviceAccessToken;

    @Autowired
    private Mongo mongo;

    @Autowired
    private MetadataModel metadataModel;

    @Value("#{launchProperties['services.mongo.dbName']}")
    private String mongoDatabaseName = null;

    public void setResponseListSize(int responseListSize) {
        this.responseListSize = responseListSize;
    }

    public void setHarvestStepSecondsToLive(int harvestStepSecondsToLive) {
        this.harvestStepSecondsToLive = harvestStepSecondsToLive;
    }

    private synchronized DB db() {
        if (mongoDatabase == null) {
            mongoDatabase = mongo.getDB(mongoDatabaseName);
        }
        return mongoDatabase;
    }

    @Override
    public DataSet createDataSet(String spec) throws BadArgumentException {
        DBObject object = new BasicDBObject();
        object.put(DataSet.SPEC, spec);
        object.put(DataSet.DATA_SET_STATE, DataSetState.EMPTY.toString());
        DataSetImpl impl = new DataSetImpl(object);
        impl.saveObject();
        return impl;
    }

    @Override
    public synchronized Collection<? extends DataSet> getDataSets() throws BadArgumentException {
        List<DataSetImpl> sets = new ArrayList<DataSetImpl>();
        DBCollection collection = db().getCollection(DATASETS_COLLECTION);
        DBCursor cursor = collection.find();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            sets.add(new DataSetImpl(object));
        }
        return sets;
    }

    @Override
    public DataSet getDataSet(String spec) throws BadArgumentException {
        DBCollection collection = db().getCollection(DATASETS_COLLECTION);
        DBObject object = collection.findOne(new BasicDBObject(DataSet.SPEC, spec));
        if (object == null) {
            return null;
        }
        return new DataSetImpl(object);
    }

    @Override
    public DataSet getFirstDataSet(DataSetState dataSetState) throws BadArgumentException {
        DBCollection collection = db().getCollection(DATASETS_COLLECTION);
        DBObject object = collection.findOne(new BasicDBObject(DataSet.DATA_SET_STATE, dataSetState.toString()));
        if (object == null) {
            return null;
        }
        return new DataSetImpl(object);
    }

    @Override
    public void incrementRecordCount(String spec, int increment) {
        DBCollection collection = db().getCollection(DATASETS_COLLECTION);
        collection.update(
                new BasicDBObject(
                        DataSet.SPEC,
                        spec
                ),
                new BasicDBObject(
                        "$inc",
                        new BasicDBObject(
                                DataSet.RECORDS_INDEXED,
                                increment
                        )
                )
        );
    }

    @Override
    public Set<MetadataFormat> getMetadataFormats() throws BadArgumentException {
        Set<MetadataFormat> set = new TreeSet<MetadataFormat>();
        for (DataSet dataSet : getDataSets()) {
            set.add(dataSet.getMetadataFormat());
            for (Mapping mapping : dataSet.mappings().values()) {
                set.add(mapping.getMetadataFormat());
            }
        }
        return set;
    }

    @Override
    public Set<MetadataFormat> getMetadataFormats(String id, String accessKey) throws BadArgumentException, CannotDisseminateFormatException {
        Set<MetadataFormat> set = new TreeSet<MetadataFormat>();
        ObjectId objectId = new ObjectId(id);
        for (DataSet dataSet : getDataSets()) {
            Record record = dataSet.fetch(objectId, dataSet.getMetadataFormat().getPrefix(), accessKey);
            if (record != null) {
                set.add(dataSet.getMetadataFormat());
                for (Mapping mapping : dataSet.mappings().values()) {
                    set.add(mapping.getMetadataFormat());
                }
            }
        }
        return set;
    }

    @Override
    public HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix, String accessKey) throws NoRecordsMatchException, BadArgumentException {
        DBCollection steps = db().getCollection(HARVEST_STEPS_COLLECTION);
        DBObject req = new BasicDBObject();
        req.put(PmhRequest.VERB, verb.toString());
        req.put(PmhRequest.SET, set);
        req.put(PmhRequest.FROM, from);
        req.put(PmhRequest.UNTIL, until);
        req.put(PmhRequest.PREFIX, metadataPrefix);
        DBObject firstStep = new BasicDBObject(HarvestStep.PMH_REQUEST, req);
        DataSet dataSet = getDataSet(set);
        if (dataSet == null) {
            String errorMessage = String.format("Cannot find set [%s]", set);
            log.error(errorMessage);
            throw new NoRecordsMatchException(errorMessage);
        }
        firstStep.put(HarvestStep.LIST_SIZE, dataSet.getRecordCount());
        firstStep.put(HarvestStep.NAMESPACES, dataSet.getNamespaces());
        firstStep.put(HarvestStep.CURSOR, 0);
        firstStep.put(HarvestStep.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * harvestStepSecondsToLive));
        firstStep.put(HarvestStep.ACCESS_KEY, accessKey);
        steps.insert(firstStep);
        return createHarvestStep(firstStep, steps);
    }

    @Override
    public HarvestStep getHarvestStep(String resumptionToken) throws NoRecordsMatchException, BadArgumentException, BadResumptionTokenException {
        ObjectId objectId;
        // otherwise a illegal resumptionToken from the mongodb perspective throws a general exception
        try {
            objectId = new ObjectId(resumptionToken);
        }
        catch (Exception e) {
            throw new BadResumptionTokenException("Unable to find resumptionToken: " + resumptionToken);
        }
        DBCollection steps = db().getCollection(HARVEST_STEPS_COLLECTION);
        DBObject query = new BasicDBObject(MONGO_ID, objectId);
        DBObject step = steps.findOne(query);
        if (step == null) {
            throw new BadResumptionTokenException("Unable to find resumptionToken: " + resumptionToken);
        }
        return createHarvestStep(step, steps);
    }

    @Override
    public void removeExpiredHarvestSteps() {
        DBCollection steps = db().getCollection(HARVEST_STEPS_COLLECTION);
        Date now = new Date();
        DBObject query = new BasicDBObject(HarvestStep.EXPIRATION, new BasicDBObject("$lt", now));
        steps.remove(query);
    }

    private HarvestStep createHarvestStep(DBObject step, DBCollection steps) throws NoRecordsMatchException, BadArgumentException {
        HarvestStepImpl harvestStep = new HarvestStepImpl(step);
        String set = harvestStep.getPmhRequest().getSet();
        DataSet dataSet = getDataSet(set);
        if (dataSet == null) {
            String errorMessage = String.format("Cannot find set [%s]", set);
            log.error(errorMessage);
            throw new NoRecordsMatchException(errorMessage);
        }
        if (harvestStep.getListSize() > harvestStep.getCursor() + responseListSize) {
            DBObject nextStep = new BasicDBObject(HarvestStep.PMH_REQUEST, step.get(HarvestStep.PMH_REQUEST));
            nextStep.put(HarvestStep.NAMESPACES, step.get(HarvestStep.NAMESPACES));
            nextStep.put(HarvestStep.LIST_SIZE, step.get(HarvestStep.LIST_SIZE));
            nextStep.put(HarvestStep.CURSOR, harvestStep.getCursor() + responseListSize);
            nextStep.put(HarvestStep.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * harvestStepSecondsToLive));
            nextStep.put(HarvestStep.ACCESS_KEY, step.get(HarvestStep.ACCESS_KEY));
            steps.insert(nextStep);
            harvestStep.nextStepId = (ObjectId) nextStep.get(MONGO_ID);
        }
        return harvestStep;
    }

    @Override
    public Record getRecord(String identifier, String metadataPrefix, String accessKey) throws CannotDisseminateFormatException, BadArgumentException {
        RecordIdentifier recordIdentifier = createIdentifier(identifier);
        return fetch(recordIdentifier, metadataPrefix, accessKey);
    }

    public Record fetch(RecordIdentifier identifier, String metadataPrefix, String accessKey) throws CannotDisseminateFormatException, BadArgumentException {
        DataSet dataSet = getDataSet(identifier.collectionId);
        if (dataSet == null) {
            throw new BadArgumentException(String.format("Do data set for identifier [%s]", identifier.collectionId));
        }
        return dataSet.fetch(identifier.objectId, metadataPrefix, accessKey);
    }

    @Override
    public MetaConfig getMetaRepoConfig() {
        return metaRepoConfig;
    }

    private class DataSetImpl implements DataSet {
        private DBObject object;
        private DBCollection recColl;
        private MetadataFormat metadataFormat;

        private DataSetImpl(DBObject object) throws BadArgumentException {
            this.object = object;
            if (object.get(METADATA_FORMAT) != null) {
                this.metadataFormat = new MetadataFormatImpl((DBObject) object.get(METADATA_FORMAT));
            }
            else {
                DBObject formatObject = new BasicDBObject();
                object.put(METADATA_FORMAT, formatObject);
                this.metadataFormat = new MetadataFormatImpl(formatObject);
            }
        }

        private DBCollection records() {
            if (recColl == null) {
                recColl = db().getCollection(RECORD_COLLECTION_PREFIX + getSpec());
            }
            return recColl;
        }

        @Override
        public String getSpec() {
            return (String) object.get(SPEC);
        }

        @Override
        public String getName() {
            return (String) object.get(NAME);
        }

        @Override
        public void setName(String value) {
            object.put(NAME, value);
        }

        @Override
        public String getProviderName() {
            return (String) object.get(PROVIDER_NAME);
        }

        @Override
        public void setProviderName(String value) {
            object.put(PROVIDER_NAME, value);
        }

        @Override
        public String getDescription() {
            return (String) object.get(DESCRIPTION);
        }

        @Override
        public void setDescription(String value) {
            object.put(DESCRIPTION, value);
        }

        @Override
        public DBObject getNamespaces() {
            return (DBObject) object.get(NAMESPACES);
        }

        @Override
        public Path getRecordRoot() {
            return new Path((String) (object.get(RECORD_ROOT)));
        }

        @Override
        public void setRecordRoot(Path path) {
            object.put(RECORD_ROOT, path.toString());
        }

        @Override
        public int getRecordsIndexed() {
            return (Integer) object.get(RECORDS_INDEXED);
        }

        @Override
        public void setRecordsIndexed(int count) {
            object.put(RECORDS_INDEXED, count);
        }

        @Override
        public Path getUniqueElement() {
            return new Path((String) (object.get(UNIQUE_ELEMENT)));
        }

        @Override
        public void setUniqueElement(Path path) {
            object.put(UNIQUE_ELEMENT, path.toString());
        }

        @Override
        public DataSetState getState() {
            return DataSetState.get((String) object.get(DATA_SET_STATE));
        }

        @Override
        public String getErrorMessage() {
            return (String) object.get(ERROR_MESSAGE);
        }

        @Override
        public void setState(DataSetState dataSetState) {
            object.put(DATA_SET_STATE, dataSetState.toString());
            object.removeField(ERROR_MESSAGE);
        }

        @Override
        public void setErrorState(String message) {
            setState(DataSetState.ERROR);
            object.put(ERROR_MESSAGE, message);
        }

        @Override
        public void parseRecords(InputStream inputStream) throws XMLStreamException, IOException {
            records().drop();
            MongoObjectParser parser = new MongoObjectParser(
                    inputStream,
                    getRecordRoot(),
                    getUniqueElement(),
                    getMetadataFormat().getPrefix(),
                    getMetadataFormat().getNamespace()
            );
            DBObject record;
            while ((record = parser.nextRecord()) != null) {
                records().insert(record);
            }
            object.put(NAMESPACES, parser.getNamespaces());
            saveObject();
        }

        @Override
        public void addMapping(String mappingXML) {
            DBObject mappings = (DBObject) object.get(MAPPINGS);
            if (mappings == null) {
                mappings = new BasicDBObject();
                object.put(MAPPINGS, mappings);
            }
            DBObject format = new BasicDBObject();
            format.put(MetadataFormat.PREFIX, MAPPED_NAMESPACE.getPrefix());
            format.put(MetadataFormat.NAMESPACE, MAPPED_NAMESPACE.getUri());
            format.put(MetadataFormat.SCHEMA, MAPPED_NAMESPACE.getSchema());
            format.put(MetadataFormat.ACCESS_KEY_REQUIRED, MAPPED_NAMESPACE_ACCESS_KEY_REQUIRED);
            DBObject mapping = new BasicDBObject();
            mapping.put(Mapping.FORMAT, format);
            mapping.put(Mapping.RECORD_MAPPING, mappingXML);
            mappings.put(MAPPED_NAMESPACE.getPrefix(), mapping);
            saveObject();
        }

        @Override
        public MetadataFormat getMetadataFormat() {
            return metadataFormat;
        }

        @Override
        public void save() {
            saveObject();
        }

        @Override
        public Map<String, Mapping> mappings() throws BadArgumentException {
            Map<String, Mapping> mappingMap = new TreeMap<String, Mapping>();
            DBObject mappingsObject = (DBObject) object.get(MAPPINGS);
            if (mappingsObject != null) {
                for (String prefix : mappingsObject.keySet()) {
                    mappingMap.put(prefix, new MappingImpl(this, (DBObject) mappingsObject.get(prefix)));
                }
                // todo: workaround for faking ESE harvesting
                Mapping originalMapping = mappingMap.get(MAPPED_NAMESPACE.getPrefix());
                if (originalMapping != null) {
                    mappingMap.put(MetadataNamespace.ESE.getPrefix(), new FakeESEMappingImpl((MappingInternal) originalMapping));
                }
                // todo: workaround for faking ESE harvesting
            }
            return mappingMap;
        }

        @Override
        public int getRecordCount() {
            return (int) records().count();
        }

        @Override
        public Record fetch(ObjectId id, String prefix, String accessKey) throws BadArgumentException, CannotDisseminateFormatException { // if prefix is passed in, mapping can be done
            DBObject object = new BasicDBObject(MONGO_ID, id);
            DBObject rawRecord = records().findOne(object);
            if (rawRecord != null) {
                Mapping mapping = getMapping(prefix, accessKey);
                List<RecordImpl> list = new ArrayList<RecordImpl>();
                list.add(new RecordImpl(records().findOne(object), getMetadataFormat().getPrefix(), getNamespaces()));
                if (mapping != null) {
                    Map<String, String> namespaces = new TreeMap<String, String>();
                    DBObject namespacesObject = getNamespaces();
                    for (String nsPrefix : namespacesObject.keySet()) {
                        namespaces.put(nsPrefix, (String) namespacesObject.get(nsPrefix));
                    }
                    ((MappingInternal) mapping).map(list, namespaces);
                }
                return list.isEmpty() ? null : list.get(0);
            }
            return null;
        }

        @Override
        public List<? extends Record> records(String prefix, int start, int count, Date from, Date until, String accessKey) throws CannotDisseminateFormatException, BadArgumentException {
            Mapping mapping = getMapping(prefix, accessKey);
            List<RecordImpl> list = new ArrayList<RecordImpl>();
            DBCursor cursor = createCursor(from, until).skip(start).limit(count);
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                list.add(new RecordImpl(object, getMetadataFormat().getPrefix(), getNamespaces()));
            }
            if (mapping != null) {
                Map<String, String> namespaces = new TreeMap<String, String>();
                DBObject namespacesObject = (DBObject) object.get(NAMESPACES);
                for (String nsPrefix : namespacesObject.keySet()) {
                    namespaces.put(nsPrefix, (String) namespacesObject.get(nsPrefix));
                }
                ((MappingInternal) mapping).map(list, namespaces);
            }
            return list;
        }

        private Mapping getMapping(String prefix, String accessKey) throws CannotDisseminateFormatException, BadArgumentException {
            Mapping mapping;
            if (getMetadataFormat().getPrefix().equals(prefix)) {
                mapping = null;
                if (getMetadataFormat().isAccessKeyRequired() && !serviceAccessToken.checkKey(accessKey)) {
                    log.warn("Access key violation for raw format " + prefix);
                    throw new CannotDisseminateFormatException(String.format("Raw metadata format requires access key, but %s is not valid", accessKey));
                }
            }
            else {
                mapping = mappings().get(prefix);
                if (mapping == null) {
                    throw new CannotDisseminateFormatException(String.format("No mapping found to prefix %s", prefix));
                }
                if (mapping.getMetadataFormat().isAccessKeyRequired() && !serviceAccessToken.checkKey(accessKey)) {
                    log.warn("Access key violation for mapped format " + prefix);
                    throw new CannotDisseminateFormatException(String.format("Mapping to metadata format requires access key, but %s is not valid", accessKey));
                }
            }
            return mapping;
        }

        private DBCursor createCursor(Date from, Date until) {
            DBObject query = new BasicDBObject();
            if (from != null) {
                query.put(Record.MODIFIED, new BasicDBObject("$gte", from));
            }
            if (until != null) {
                query.put(Record.MODIFIED, new BasicDBObject("$lte", until));
            }
            return records().find(query);
        }

        private void saveObject() {
            DBCollection collection = db().getCollection(DATASETS_COLLECTION);
            collection.save(object);
        }
    }

    private interface MappingInternal {
        void map(List<? extends Record> records, Map<String, String> namespaces) throws CannotDisseminateFormatException;
    }

    // for now we pretend that we have an ESE mapping

    private class FakeESEMappingImpl implements Mapping, MappingInternal, Comparable<Mapping> {
        private final Set<String> ELIMINATE = new TreeSet<String>(Arrays.asList(
                "uri",
                "collectionName",
                "collectionTitle",
                "hasObject",
                "language",
                "country"
        ));
        private MappingInternal icnMapping;
        private ESEStripper eseStripper = new ESEStripper();
        private ESEMetadataFormat eseMetadataFormat = new ESEMetadataFormat();

        private FakeESEMappingImpl(MappingInternal icnMapping) {
            this.icnMapping = icnMapping;
        }

        @Override
        public MetadataFormat getMetadataFormat() {
            return eseMetadataFormat;
        }

        @Override
        public RecordMapping getRecordMapping() {
            return null;  // there is none!
        }

        @Override
        public int compareTo(Mapping o) {
            return getMetadataFormat().getPrefix().compareTo(o.getMetadataFormat().getPrefix());
        }

        @Override
        public void map(List<? extends Record> records, Map<String, String> namespaces) throws CannotDisseminateFormatException {
            icnMapping.map(records, namespaces);
            Iterator<? extends Record> recordWalk = records.iterator();
            while (recordWalk.hasNext()) {
                Record record = recordWalk.next();
                String stripped = eseStripper.strip(record.getXmlString(MAPPED_NAMESPACE.getPrefix()));
                if (!stripped.contains("<europeana:object>")) {
                    recordWalk.remove();
                }
                else {
                    ((RecordImpl) record).addFormat(getMetadataFormat(), stripped);
                }
            }
        }

        private class ESEStripper {
            private String context;
            private int contextBegin, contextEnd;

            private ESEStripper() {
                StringBuilder contextString = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n\n<strip\n");
                for (NamespaceDefinition ns : metadataModel.getRecordDefinition().namespaces) {
                    contextString.append(String.format("xmlns:%s=\"%s\"\n", ns.prefix, ns.uri));
                }
                contextString.append(">\n%s</strip>\n");
                this.context = contextString.toString();
                this.contextBegin = this.context.indexOf("%s");
                this.contextEnd = this.context.length() - (this.contextBegin + 2);
            }

            public String strip(String recordString) {
                String contextualizedRecord = String.format(context, recordString);
                StringWriter out = new StringWriter();
                try {
                    Document document = DocumentHelper.parseText(contextualizedRecord);
                    stripDocument(document);
                    OutputFormat format = OutputFormat.createPrettyPrint();
                    XMLWriter writer = new XMLWriter(out, format);
                    writer.write(document);
                }
                catch (Exception e) {
                    throw new RuntimeException("Unable to strip for ESE");
                }
                out.getBuffer().delete(0, contextBegin);
                out.getBuffer().delete(out.getBuffer().length() - contextEnd, out.getBuffer().length());
                return out.toString();
            }

            private void stripDocument(Document document) {
                Element validateElement = document.getRootElement();
                Element recordElement = validateElement.element("record");
                if (recordElement == null) {
                    throw new RuntimeException("Cannot find record element");
                }
                stripElement(recordElement, new Path());
            }

            private boolean stripElement(Element element, Path path) {
                path.push(Tag.create(element.getNamespacePrefix(), element.getName()));
                boolean hasElements = false;
                Iterator walk = element.elementIterator();
                while (walk.hasNext()) {
                    Element subelement = (Element) walk.next();
                    boolean remove = stripElement(subelement, path);
                    if (remove) {
                        walk.remove();
                    }
                    hasElements = true;
                }
                if (!hasElements) {
                    FieldDefinition fieldDefinition = metadataModel.getRecordDefinition().getFieldDefinition(path);
                    if (fieldDefinition == null) {
                        throw new RuntimeException("Should have found field definition");
                    }
                    path.pop();
                    return fieldDefinition.getPrefix().equals(MAPPED_NAMESPACE.getPrefix()) ||
                            fieldDefinition.getPrefix().equals("europeana") && ELIMINATE.contains(fieldDefinition.getLocalName());
                }
                path.pop();
                return false;

            }
        }

        private class ESEMetadataFormat implements MetadataFormat {

            @Override
            public String getPrefix() {
                return "ese";
            }

            @Override
            public void setPrefix(String value) {
                throw new RuntimeException();
            }

            @Override
            public String getSchema() {
                return "http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd";
            }

            @Override
            public void setSchema(String value) {
                throw new RuntimeException();
            }

            @Override
            public String getNamespace() {
                return "http://www.europeana.eu/schemas/ese/";
            }

            @Override
            public void setNamespace(String value) {
                throw new RuntimeException();
            }

            @Override
            public boolean isAccessKeyRequired() {
                return false;  // ESE is free-for-all
            }

            @Override
            public void setAccessKeyRequired(boolean required) {
                // do nothing
            }
        }
    }

    private class MappingImpl implements Mapping, MappingInternal, Comparable<Mapping> {
        private DataSetImpl dataSet;
        private DBObject object;
        private MetadataFormat metadataFormat;
        private RecordValidator recordValidator = new RecordValidator(metadataModel, false);
        private MappingRunner mappingRunner;

        private MappingImpl(DataSetImpl dataSet, DBObject object) throws BadArgumentException {
            this.dataSet = dataSet;
            this.object = object;
            this.metadataFormat = new MetadataFormatImpl((DBObject) object.get(FORMAT));
        }

        @Override
        public MetadataFormat getMetadataFormat() {
            return metadataFormat;
        }

        @Override
        public RecordMapping getRecordMapping() throws MetadataException {
            return RecordMapping.read((String) object.get(RECORD_MAPPING), metadataModel.getRecordDefinition());
        }

        @Override
        public int compareTo(Mapping o) {
            return getMetadataFormat().getPrefix().compareTo(o.getMetadataFormat().getPrefix());
        }

        @Override
        public void map(List<? extends Record> records, Map<String, String> namespaces) throws CannotDisseminateFormatException {
            try {
                MappingRunner mappingRunner = getMappingRunner();
                MetadataRecord.Factory factory = new MetadataRecord.Factory(namespaces);
                int invalidCount = 0;
                Iterator<? extends MetaRepo.Record> walk = records.iterator();
                while (walk.hasNext()) {
                    Record record = walk.next();
                    try {
                        MetadataRecord metadataRecord = factory.fromXml(record.getXmlString(dataSet.getMetadataFormat().getPrefix()));
                        String recordString = mappingRunner.runMapping(metadataRecord);
                        List<String> problems = new ArrayList<String>();
                        String validated = recordValidator.validateRecord(recordString, problems);
                        if (problems.isEmpty()) {
                            RecordImpl recordImpl = (RecordImpl) record;
                            recordImpl.addFormat(getMetadataFormat(), validated);
                        }
                        else {
                            log.info("invalid record: " + recordString);
                            invalidCount++;
                            walk.remove();
                        }
                    }
                    catch (MappingException e) {
                        log.warn("mapping exception: " + e);
                        invalidCount++;
                        walk.remove();
                    }
                    catch (XMLStreamException e) {
                        log.warn("Unable to map record!", e);
                    }
                }
                if (invalidCount > 0) {
                    log.info(String.format("%d invalid records discarded", invalidCount));
                }
            }
            catch (MetadataException e) {
                log.error("Metadata exception!", e);
                throw new CannotDisseminateFormatException("Internal Problem");
            }
            // todo break here if mapping is consistently invalidating all records.
        }

        private MappingRunner getMappingRunner() throws MetadataException {
            if (mappingRunner == null) {
                ToolCodeResource toolCodeResource = new ToolCodeResource();
                RecordMapping recordMapping = getRecordMapping();
                recordMapping.toCompileCode(metadataModel.getRecordDefinition());
                mappingRunner = new MappingRunner(toolCodeResource.getCode() + getRecordMapping());
            }
            return mappingRunner;
        }
    }

    private static class MetadataFormatImpl implements MetadataFormat, Comparable<MetadataFormat> {
        private DBObject object;

        private MetadataFormatImpl(DBObject object) throws BadArgumentException {
            this.object = object;
            if (object == null) {
                throw new BadArgumentException("MetdataFormat object missing!");
            }
            String prefix = (String) object.get(PREFIX);
            if (prefix == null || prefix.isEmpty()) {
                throw new BadArgumentException("MetdataFormat with no prefix!");
            }
        }

        @Override
        public String getPrefix() {
            return (String) object.get(PREFIX);
        }

        @Override
        public void setPrefix(String value) {
            object.put(PREFIX, value);
        }

        @Override
        public String getSchema() {
            return (String) object.get(SCHEMA);
        }

        @Override
        public void setSchema(String value) {
            object.put(SCHEMA, value);
        }

        @Override
        public String getNamespace() {
            return (String) object.get(NAMESPACE);
        }

        @Override
        public void setNamespace(String value) {
            object.put(NAMESPACE, value);
        }

        @Override
        public boolean isAccessKeyRequired() {
            Boolean isIt = (Boolean) object.get(ACCESS_KEY_REQUIRED);
            return isIt == null ? Boolean.FALSE : isIt;
        }

        @Override
        public void setAccessKeyRequired(boolean required) {
            object.put(ACCESS_KEY_REQUIRED, required);
        }

        @Override
        public int compareTo(MetadataFormat o) {
            return getPrefix().compareTo(o.getPrefix());
        }
    }

    private static class RecordImpl implements Record {
        private Logger log = Logger.getLogger(getClass());

        private DBObject object;
        private String defaultPrefix;
        private DBObject namespaces;

        private RecordImpl(DBObject object, String defaultPrefix, DBObject namespaces) {
            this.object = object;
            this.defaultPrefix = defaultPrefix;
            this.namespaces = namespaces;
        }

        @Override
        public ObjectId getIdentifier() {
            return (ObjectId) object.get(MONGO_ID);
        }

        @Override
        public PmhSet getPmhSet() {
            return null;  //TODO: implement this
        }

        @Override
        public Date getModifiedDate() {
            return (Date) object.get(MODIFIED);
        }

        @Override
        public boolean isDeleted() {
            return false;  //TODO: implement this
        }

        @Override
        public DBObject getNamespaces() {
            return namespaces;
        }

        @Override
        public String getXmlString() throws CannotDisseminateFormatException {
            return getXmlString(defaultPrefix);
        }

        // todo determine if the right format is returned after on-the-fly mapping

        @Override
        public String getXmlString(String metadataPrefix) throws CannotDisseminateFormatException {
            String x = (String) object.get(metadataPrefix);
            if (x == null) {
                String errorMessage = String.format("No record with prefix [%s]", metadataPrefix);
                log.error(errorMessage);
                throw new CannotDisseminateFormatException(errorMessage);
            }
            return x;
        }

        void addFormat(MetadataFormat metadataFormat, String recordString) {
            object.put(metadataFormat.getPrefix(), recordString);
        }
    }


    private class HarvestStepImpl implements HarvestStep {

        private DBObject object;
        private ObjectId nextStepId;

        private HarvestStepImpl(DBObject object, ObjectId nextStepId) {
            this.object = object;
            this.nextStepId = nextStepId;
        }

        private HarvestStepImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public ObjectId getResumptionToken() {
            return (ObjectId) object.get(MONGO_ID);
        }

        @Override
        public Date getExpiration() {
            return (Date) object.get(EXPIRATION);
        }

        @Override
        public int getListSize() {
            return (Integer) object.get(LIST_SIZE);
        }

        @Override
        public int getCursor() {
            return (Integer) object.get(CURSOR);
        }

        @Override
        public List<? extends Record> getRecords() throws CannotDisseminateFormatException, BadArgumentException {
            PmhRequest request = getPmhRequest();
            DataSet dataSet = getDataSet(request.getSet());
            if (dataSet == null) {
                throw new BadArgumentException("No data set found by the name " + request.getSet());
            }
            String accessKey = (String) object.get(ACCESS_KEY);
            return dataSet.records(request.getMetadataPrefix(), getCursor(), responseListSize, request.getFrom(), request.getUntil(), accessKey);
        }

        @Override
        public PmhRequest getPmhRequest() {
            return new PmhRequestImpl((DBObject) object.get(PMH_REQUEST));
        }

        @Override
        public boolean hasNext() {
            return nextStepId != null;
        }

        @Override
        public DBObject getNamespaces() {
            return (DBObject) object.get(NAMESPACES);
        }

        @Override
        public String nextResumptionToken() {
            if (!hasNext()) {
                throw new RuntimeException("Should have checked hasNext()");
            }
            return nextStepId.toString();
        }
    }

    private static class PmhRequestImpl implements PmhRequest {

        private DBObject object;

        private PmhRequestImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public PmhVerb getVerb() {
            return PmhVerb.valueOf((String) object.get(VERB));
        }

        @Override
        public String getSet() {
            return (String) object.get(SET);
        }

        @Override
        public Date getFrom() {
            return (Date) object.get(FROM);
        }

        @Override
        public Date getUntil() {
            return (Date) object.get(UNTIL);
        }

        @Override
        public String getMetadataPrefix() {
            return (String) object.get(PREFIX);
        }

        @Override
        public String getIdentifier() {
            return (String) object.get(IDENTIFIER);
        }
    }

    private RecordIdentifier createIdentifier(String delimitedString) throws BadArgumentException {
        String[] parts = delimitedString.split(":");
        if (parts.length != 2) {
            throw new BadArgumentException("Identifier must have format <collection-id>:<object-id>");
        }
        try {
            String collectionId = parts[0];
            ObjectId objectId = new ObjectId(parts[1]);
            return new RecordIdentifier(collectionId, objectId);
        }
        catch (IllegalArgumentException e) {
            log.warn("Bad object id", e);
            throw new BadArgumentException("Bad object id " + parts[1]);
        }
        catch (Exception e) {
            throw new BadArgumentException(String.format("Unable to create identifier from [%s]", delimitedString));
        }
    }

    private static class RecordIdentifier {
        private String collectionId;
        private ObjectId objectId;

        private RecordIdentifier(String collectionId, ObjectId objectId) {
            this.collectionId = collectionId;
            this.objectId = objectId;
        }

        public String toString() {
            return collectionId + ":" + objectId.toString();
        }
    }
}
