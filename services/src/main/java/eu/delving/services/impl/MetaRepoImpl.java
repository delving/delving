package eu.delving.services.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.BadResumptionTokenException;
import eu.delving.services.exceptions.CannotDisseminateFormatException;
import eu.delving.services.exceptions.NoRecordsMatchException;
import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.FieldEntry;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCodeModel;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
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
    private Logger log = Logger.getLogger(getClass());
    private Mongo mongo;
    private AnnotationProcessor annotationProcessor;
    private int responseListSize = 5;
    private int harvestStepSecondsToLive = 5;
    private DB mongoDatabase;
    private MetaConfig metaRepoConfig;

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    public void setMetaRepoConfig(MetaConfig metaConfig) {
        this.metaRepoConfig = metaConfig;
    }

    public void setResponseListSize(int responseListSize) {
        this.responseListSize = responseListSize;
    }

    public void setHarvestStepSecondsToLive(int harvestStepSecondsToLive) {
        this.harvestStepSecondsToLive = harvestStepSecondsToLive;
    }

    private synchronized DB db() {
        if (mongoDatabase == null) {
            mongoDatabase = mongo.getDB(DATABASE_NAME);
        }
        return mongoDatabase;
    }

    @Override
    public DataSet createDataSet(
            String spec,
            String name,
            String providerName,
            String description,
            String prefix,
            String namespace,
            String schema
    ) throws BadArgumentException {
        DBObject object = new BasicDBObject();
        object.put(DataSet.SPEC, spec);
        object.put(DataSet.NAME, name);
        object.put(DataSet.PROVIDER_NAME, providerName);
        object.put(DataSet.DESCRIPTION, description);
        object.put(DataSet.RECORDS_INDEXED, 0);
        object.put(DataSet.DATA_SET_STATE, DataSetState.UPLOADED.toString());
        DBObject metadataFormat = new BasicDBObject();
        metadataFormat.put(MetadataFormat.PREFIX, prefix);
        metadataFormat.put(MetadataFormat.NAMESPACE, namespace);
        metadataFormat.put(MetadataFormat.SCHEMA, schema);
        object.put(DataSet.METADATA_FORMAT, metadataFormat);
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
    public Set<MetadataFormat> getMetadataFormats(String id) throws BadArgumentException, CannotDisseminateFormatException {
        Set<MetadataFormat> set = new TreeSet<MetadataFormat>();
        ObjectId objectId = new ObjectId(id);
        for (DataSet dataSet : getDataSets()) {
            Record record = dataSet.fetch(objectId, dataSet.getMetadataFormat().getPrefix());
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
    public HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix) throws NoRecordsMatchException, BadArgumentException {
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
            steps.insert(nextStep);
            harvestStep.nextStepId = (ObjectId) nextStep.get(MONGO_ID);
        }
        return harvestStep;
    }

    @Override
    public Record getRecord(String identifier, String metadataPrefix) throws CannotDisseminateFormatException, BadArgumentException {
        RecordIdentifier recordIdentifier = createIdentifier(identifier);
        return fetch(recordIdentifier, metadataPrefix);
    }

    public Record fetch(RecordIdentifier identifier, String metadataPrefix) throws CannotDisseminateFormatException, BadArgumentException {
        DataSet dataSet = getDataSet(identifier.collectionId);
        if (dataSet == null) {
            throw new BadArgumentException(String.format("Do data set for identifier [%s]", identifier.collectionId));
        }
        return dataSet.fetch(identifier.objectId, metadataPrefix);
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
        public QName getRecordRoot() {
            return QName.valueOf((String) (object.get(RECORD_ROOT)));
        }

        @Override
        public void setRecordRoot(QName root) {
            object.put(RECORD_ROOT, root.toString());
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
        public void parseRecords(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException, IOException {
            records().drop();
            MongoObjectParser parser = new MongoObjectParser(
                    inputStream,
                    recordRoot,
                    uniqueElement,
                    getMetadataFormat().getPrefix(),
                    getMetadataFormat().getNamespace()
            );
            DBObject record;
            while ((record = parser.nextRecord()) != null) {
                records().insert(record);
            }
            object.put(NAMESPACES, parser.getNamespaces());
            object.put(RECORD_ROOT, recordRoot.toString());
            saveObject();
        }

        @Override
        public void addMapping(String mappingCode) {
            // todo: get these from the content of the mapping getGroovyCode.  maybe from annotations or their replacement?
            String prefix = "icn";
            String namespace = "http://www.icn.nl/";
            String schema = "http://www.icn.nl/schemas/ICN-V3.2.xsd";
            // todo: get these from the content of the mapping getGroovyCode.  maybe from annotations or their replacement?

            DBObject mappings = (DBObject) object.get(MAPPINGS);
            if (mappings == null) {
                mappings = new BasicDBObject();
                object.put(MAPPINGS, mappings);
            }
            DBObject format = new BasicDBObject();
            format.put(MetadataFormat.PREFIX, prefix);
            format.put(MetadataFormat.NAMESPACE, namespace);
            format.put(MetadataFormat.SCHEMA, schema);
            DBObject mapping = new BasicDBObject();
            mapping.put(Mapping.FORMAT, format);
            mapping.put(Mapping.CODE, mappingCode);
            mappings.put(prefix, mapping);
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
        public Map<String, ? extends Mapping> mappings() throws BadArgumentException {
            Map<String, MappingImpl> mappingMap = new TreeMap<String, MappingImpl>();
            DBObject mappings = (DBObject) object.get(MAPPINGS);
            if (mappings != null) {
                for (String prefix : mappings.keySet()) {
                    mappingMap.put(prefix, new MappingImpl(this, (DBObject) mappings.get(prefix)));
                }
            }
            return mappingMap;
        }

        @Override
        public int getRecordCount() {
            return (int) records().count();
        }

        @Override
        public Record fetch(ObjectId id, String prefix) throws BadArgumentException, CannotDisseminateFormatException { // if prefix is passed in, mapping can be done
            DBObject object = new BasicDBObject(MONGO_ID, id);
            DBObject rawRecord = records().findOne(object);
            if (rawRecord != null) {
                Map<String, ? extends Mapping> mappings = mappings();
                Mapping mapping = mappings.get(prefix);
                if (mapping == null && !getMetadataFormat().getPrefix().equals(prefix)) {
                    String errorMessage = String.format(
                            "Only original format %s and mapped formats %s supported so far",
                            getMetadataFormat().getPrefix(),
                            mappings.keySet().toString()
                    );
                    log.error(errorMessage);
                    throw new CannotDisseminateFormatException(errorMessage);
                }
                List<RecordImpl> list = new ArrayList<RecordImpl>();
                list.add(new RecordImpl(records().findOne(object), getMetadataFormat().getPrefix(), getNamespaces()));
                if (mapping != null) {
                    Map<String, String> namespaces = new TreeMap<String, String>();
                    DBObject namespacesObject = getNamespaces();
                    for (String nsPrefix : namespacesObject.keySet()) {
                        namespaces.put(nsPrefix, (String) namespacesObject.get(nsPrefix));
                    }
                    ((MappingImpl) mapping).map(list, namespaces);
                }
                return list.isEmpty() ? null : list.get(0);
            }
            return null;
        }

        @Override
        public List<? extends Record> records(String prefix, int start, int count, Date from, Date until) throws CannotDisseminateFormatException, BadArgumentException {
            Map<String, ? extends Mapping> mappings = mappings();
            Mapping mapping = mappings.get(prefix);
            if (mapping == null && !getMetadataFormat().getPrefix().equals(prefix)) {
                String errorMessage = String.format(
                        "Only original format %s and mapped formats %s supported so far",
                        getMetadataFormat().getPrefix(),
                        mappings.keySet().toString()
                );
                log.error(errorMessage);
                throw new CannotDisseminateFormatException(errorMessage);
            }
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
                ((MappingImpl) mapping).map(list, namespaces);
            }
            return list;
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

    private class MappingImpl implements Mapping, Comparable<Mapping> {
        private DataSetImpl dataSet;
        private DBObject object;
        private MetadataFormat metadataFormat;
        private RecordValidator recordValidator = new RecordValidator(annotationProcessor, false);

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
        public String getGroovyCode() {
            return (String) object.get(CODE);
        }

        @Override
        public int compareTo(Mapping o) {
            return getMetadataFormat().getPrefix().compareTo(o.getMetadataFormat().getPrefix());
        }

        private void map(List<? extends Record> records, Map<String, String> namespaces) throws CannotDisseminateFormatException {
            ConstantFieldModel constantFieldModel = new ConstantFieldModel(annotationProcessor, null);
            constantFieldModel.fromMapping(Arrays.asList(getGroovyCode().split("\n")));
            ToolCodeModel toolCodeModel = new ToolCodeModel();
            MappingRunner mappingRunner = new MappingRunner(toolCodeModel.getCode() + getGroovyCode(), constantFieldModel);
            MetadataRecord.Factory factory = new MetadataRecord.Factory(namespaces);
            int invalidCount = 0;
            Iterator<? extends MetaRepo.Record> walk = records.iterator();
            while (walk.hasNext()) {
                Record record = walk.next();
                try {
                    MetadataRecord metadataRecord = factory.fromXml(record.getXmlString(dataSet.getMetadataFormat().getPrefix()));
                    String recordString = mappingRunner.runMapping(metadataRecord);
                    List<FieldEntry> fieldEntries = FieldEntry.createList(recordString);
                    recordValidator.validate(metadataRecord, fieldEntries);
                    String recordLines = FieldEntry.toString(fieldEntries, false);
                    RecordImpl recordImpl = (RecordImpl) record;
                    recordImpl.addFormat(getMetadataFormat(), recordLines);
                }
                catch (MappingException e) {
                    invalidCount++;
                    walk.remove();
                }
                catch (RecordValidationException e) {
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
    }

    private static class MetadataFormatImpl implements MetadataFormat, Comparable<MetadataFormat> {
        private String prefix;
        private DBObject object;

        private MetadataFormatImpl(DBObject object) throws BadArgumentException {
            this.object = object;
            if (object == null) {
                throw new BadArgumentException("MetdataFormat object missing!");
            }
            this.prefix = (String) object.get(PREFIX);
            if (prefix == null || prefix.isEmpty()) {
                throw new BadArgumentException("MetdataFormat with no prefix!");
            }
        }

        @Override
        public String getPrefix() {
            return prefix;
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
            return dataSet.records(request.getMetadataPrefix(), getCursor(), responseListSize, request.getFrom(), request.getUntil());
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
