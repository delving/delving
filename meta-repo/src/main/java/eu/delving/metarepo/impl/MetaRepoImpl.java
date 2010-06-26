package eu.delving.metarepo.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.metarepo.core.MetaRepo;
import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.ToolCodeModel;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
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
    private Map<String, DataSetImpl> dataSets;
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
    ) {
        DBObject object = new BasicDBObject();
        object.put(DataSet.SPEC, spec);
        object.put(DataSet.NAME, name);
        object.put(DataSet.PROVIDER_NAME, providerName);
        object.put(DataSet.DESCRIPTION, description);
        DBObject metadataFormat = new BasicDBObject();
        metadataFormat.put(MetadataFormat.PREFIX, prefix);
        metadataFormat.put(MetadataFormat.NAMESPACE, namespace);
        metadataFormat.put(MetadataFormat.SCHEMA, schema);
        object.put(DataSet.METADATA_FORMAT, metadataFormat);
        DataSetImpl impl = new DataSetImpl(object);
        impl.saveObject();
        getDataSets();
        dataSets.put(impl.setSpec(), impl);
        return impl;
    }

    @Override
    public synchronized Map<String, ? extends DataSet> getDataSets() {
        if (dataSets == null) {
            dataSets = new TreeMap<String, DataSetImpl>();
            DBCollection collection = db().getCollection(DATASETS_COLLECTION);
            DBCursor cursor = collection.find();
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                DataSetImpl impl = new DataSetImpl(object);
                dataSets.put(impl.setSpec(), impl);
            }
        }
        return dataSets;
    }

    @Override
    public Set<MetadataFormat> getMetadataFormats() {
        Set<MetadataFormat> set = new TreeSet<MetadataFormat>();
        for (DataSet dataSet : getDataSets().values()) {
            set.add(dataSet.metadataFormat());
            for (Mapping mapping : dataSet.mappings().values()) {
                set.add(mapping.metadataFormat());
            }
        }
        return set;
    }

    @Override
    public Set<MetadataFormat> getMetadataFormats(String id) {
        Set<MetadataFormat> set = new TreeSet<MetadataFormat>();
        ObjectId objectId = new ObjectId(id);
        for (DataSet dataSet : getDataSets().values()) {
            Record record = dataSet.fetch(objectId);
            if (record != null) {
                set.add(dataSet.metadataFormat());
                for (Mapping mapping : dataSet.mappings().values()) {
                    set.add(mapping.metadataFormat());
                }
            }
        }
        return set;
    }

    @Override
    public HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix, String identifier) {
        DBCollection steps = db().getCollection(HARVEST_STEPS_COLLECTION);
        DBObject req = new BasicDBObject();
        req.put(PmhRequest.VERB, verb.toString());
        req.put(PmhRequest.SET, set);
        req.put(PmhRequest.FROM, from);
        req.put(PmhRequest.UNTIL, until);
        req.put(PmhRequest.PREFIX, metadataPrefix);
        req.put(PmhRequest.IDENTIFIER, identifier);
        DBObject firstStep = new BasicDBObject(HarvestStep.PMH_REQUEST, req);
        firstStep.put(HarvestStep.LIST_SIZE, steps.getCount());
        firstStep.put(HarvestStep.CURSOR, 0);
        firstStep.put(HarvestStep.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * harvestStepSecondsToLive));
        steps.insert(firstStep);
        return createHarvestStep(firstStep, steps);
    }

    @Override
    public HarvestStep getHarvestStep(String resumptionToken) {
        ObjectId objectId = new ObjectId(resumptionToken);
        DBCollection steps = db().getCollection(HARVEST_STEPS_COLLECTION);
        DBObject query = new BasicDBObject(MONGO_ID, objectId);
        DBObject step = steps.findOne(query);
        return createHarvestStep(step, steps);
    }

    private HarvestStep createHarvestStep(DBObject step, DBCollection steps) {
        int cursor = (Integer) step.get(HarvestStep.CURSOR);
        if (steps.getCount() > cursor + responseListSize) {
            DBObject nextStep = new BasicDBObject(HarvestStep.PMH_REQUEST, step.get(HarvestStep.PMH_REQUEST));
            nextStep.put(HarvestStep.LIST_SIZE, steps.getCount());
            nextStep.put(HarvestStep.CURSOR, cursor + responseListSize);
            nextStep.put(HarvestStep.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * harvestStepSecondsToLive));
            steps.insert(nextStep);
            return new HarvestStepImpl(step, (ObjectId) nextStep.get(MONGO_ID));
        }
        else {
            return new HarvestStepImpl(step);
        }
    }

    @Override
    public Record getRecord(String identifier, String metadataPrefix) {
        RecordIdentifier recordIdentifier = new RecordIdentifier(identifier).invoke();
        if (recordIdentifier == null) return null;
        DBObject dbObject = recordIdentifier.findOne();
        return new RecordImpl(dbObject, metadataPrefix);
    }

    @Override
    public MetaConfig getMetaRepoConfig() {
        return metaRepoConfig;
    }

    private class DataSetImpl implements DataSet {
        private DBObject object;
        private DBCollection recColl;
        private MetadataFormat metadataFormat;

        private DataSetImpl(DBObject object) {
            this.object = object;
            if (object.get(METADATA_FORMAT) != null) {
                this.metadataFormat = new MetadataFormatImpl((DBObject) object.get(METADATA_FORMAT));
            }
        }

        private DBCollection records() {
            if (recColl == null) {
                recColl = db().getCollection(RECORD_COLLECTION_PREFIX + setSpec());
            }
            return recColl;
        }

        @Override
        public String setSpec() {
            return (String) object.get(SPEC);
        }

        @Override
        public String setName() {
            return (String) object.get(NAME);
        }

        @Override
        public String providerName() {
            return (String) object.get(PROVIDER_NAME);
        }

        @Override
        public String description() {
            return (String) object.get(DESCRIPTION);
        }

        @Override
        public void parseRecords(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException, IOException {
            records().drop();
            MongoObjectParser parser = new MongoObjectParser(
                    inputStream,
                    recordRoot,
                    uniqueElement,
                    metadataFormat().prefix()
            );
            DBObject record;
            while ((record = parser.nextRecord()) != null) {
                records().insert(record);
            }
            object.put(NAMESPACES, parser.getNamespaces());
            saveObject();
        }

        @Override
        public void setMapping(String mappingCode, String prefix, String namespace, String schema) {
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
        public MetadataFormat metadataFormat() {
            return metadataFormat;
        }

        @Override
        public Map<String, ? extends Mapping> mappings() {
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
        public Record fetch(ObjectId id) { // if prefix is passed in, mapping can be done
            DBObject object = new BasicDBObject(MONGO_ID, id);
            return new RecordImpl(records().findOne(object), metadataFormat().prefix());
        }

        @Override
        public List<? extends Record> records(String prefix, int start, int count) {
            Map<String, ? extends Mapping> mappings = mappings();
            Mapping mapping = mappings.get(prefix);
            if (mapping == null && !metadataFormat().prefix().equals(prefix)) {
                throw new RuntimeException(String.format(
                        "Only original format %s and mapped formats %s supported so far",
                        metadataFormat().prefix(),
                        mappings.keySet().toString()
                ));
            }
            List<RecordImpl> list = new ArrayList<RecordImpl>();
            DBCursor cursor = records().find().skip(start).limit(count);
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                list.add(new RecordImpl(object, metadataFormat().prefix()));
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

        private void saveObject() {
            DBCollection collection = db().getCollection(DATASETS_COLLECTION);
            collection.save(object);
        }
    }

    private class MappingImpl implements Mapping, Comparable<Mapping> {
        private DataSetImpl dataSet;
        private DBObject object;
        private MetadataFormat metadataFormat;

        private MappingImpl(DataSetImpl dataSet, DBObject object) {
            this.dataSet = dataSet;
            this.object = object;
            this.metadataFormat = new MetadataFormatImpl((DBObject) object.get(FORMAT));
        }

        @Override
        public MetadataFormat metadataFormat() {
            return metadataFormat;
        }

        @Override
        public String code() {
            return (String) object.get(CODE);
        }

        @Override
        public int compareTo(Mapping o) {
            return metadataFormat().prefix().compareTo(o.metadataFormat().prefix());
        }

        private void map(List<? extends Record> records, Map<String, String> namespaces) {
            ConstantFieldModel constantFieldModel = ConstantFieldModel.fromMapping(code(), annotationProcessor);
            ToolCodeModel toolCodeModel = new ToolCodeModel();
            MappingRunner mappingRunner = new MappingRunner(toolCodeModel.getCode() + code(), constantFieldModel);
            MetadataRecord.Factory factory = new MetadataRecord.Factory(namespaces);
            for (MetaRepo.Record record : records) {
                try {
                    MetadataRecord metadataRecord = factory.fromXml(record.xml(dataSet.metadataFormat().prefix()));
                    String recordString = mappingRunner.runMapping(metadataRecord);
                    String[] lines = recordString.split("\n");
                    if (!"<record>".equals(lines[0])) {
                        throw new XMLStreamException("Expected the first line to be <record>");
                    }
                    if (!"</record>".equals(lines[lines.length - 1])) {
                        throw new XMLStreamException("Expected the last line to be </record>");
                    }
                    StringBuilder recordLines = new StringBuilder();
                    for (int walk = 1; walk < lines.length - 1; walk++) {
                        recordLines.append(lines[walk]).append('\n');
                    }
                    RecordImpl recordImpl = (RecordImpl) record;
                    recordImpl.addFormat(metadataFormat(), recordLines.toString());
                }
                catch (MappingException e) {
                    log.info("Unable to map record due to: " + e.toString());
                }
                catch (XMLStreamException e) {
                    log.warn("Unable to map record!", e);
                }
            }
        }
    }

    private static class MetadataFormatImpl implements MetadataFormat, Comparable<MetadataFormat> {
        private String prefix;
        private DBObject object;

        private MetadataFormatImpl(DBObject object) {
            this.object = object;
            if (object == null) {
                throw new RuntimeException("MetdataFormat object missing!");
            }
            this.prefix = (String) object.get(PREFIX);
            if (prefix == null || prefix.isEmpty()) {
                throw new RuntimeException("MetdataFormat with no prefix!");
            }
        }

        @Override
        public String prefix() {
            return prefix;
        }

        @Override
        public String schema() {
            return (String) object.get(SCHEMA);
        }

        @Override
        public String namespace() {
            return (String) object.get(NAMESPACE);
        }

        @Override
        public int compareTo(MetadataFormat o) {
            return prefix().compareTo(o.prefix());
        }
    }

    private static class RecordImpl implements Record {

        private DBObject object;
        private String defaultPrefix;

        private RecordImpl(DBObject object, String defaultPrefix) {
            this.object = object;
            this.defaultPrefix = defaultPrefix;
        }

        @Override
        public ObjectId identifier() {
            return (ObjectId) object.get(MONGO_ID);
        }

        @Override
        public PmhSet set() {
            return null;  //TODO: implement this
        }

        @Override
        public Date modified() {
            Date modified = (Date) object.get(MODIFIED);
            if (modified == null) {
                modified = new Date(identifier().getTime());
            }
            return modified;
        }

        @Override
        public boolean deleted() {
            return false;  //TODO: implement this
        }

        @Override
        public String xml() {
            return xml(defaultPrefix);
        }

        // todo determine if the right format is returned after on-the-fly mapping

        @Override
        public String xml(String metadataPrefix) {
            String x = (String) object.get(metadataPrefix);
            if (x == null) {
                throw new RuntimeException(String.format("No record with prefix [%s]", metadataPrefix));
            }
            return x;
        }

        void addFormat(MetadataFormat metadataFormat, String recordString) {
            object.put(metadataFormat.prefix(), recordString);
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
        public ObjectId resumptionToken() {
            return (ObjectId) object.get(MONGO_ID);
        }

        @Override
        public Date expiration() {
            return (Date) object.get(EXPIRATION);
        }

        @Override
        public int listSize() {
            return (Integer) object.get(LIST_SIZE);
        }

        @Override
        public int cursor() {
            return (Integer) object.get(CURSOR);
        }

        @Override
        public List<? extends Record> records() {
            PmhRequest request = pmhRequest();
            DataSet dataSet = getDataSets().get(request.getSet());
            return dataSet.records(request.getMetadataPrefix(), cursor(), responseListSize);
        }

        @Override
        public PmhRequest pmhRequest() {
            return new PmhRequestImpl((DBObject) object.get(PMH_REQUEST));
        }

        @Override
        public boolean hasNext() {
            return nextStepId != null;
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
        public String getFrom() {
            return (String) object.get(FROM);
        }

        @Override
        public String getUntil() {
            return (String) object.get(UNTIL);
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

    public class RecordIdentifier {

        private String identifier;
        private String collId;
        private String recordId;

        public RecordIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public RecordIdentifier(String collId, String recordId) {
            this.collId = collId;
            this.recordId = recordId;
            this.identifier = collId + ":" + recordId;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getCollId() {
            return collId;
        }

        public String getRecordId() {
            return recordId;
        }

        public RecordIdentifier invoke() {
            String[] elements = identifier.split(":");
            if (elements.length != 2) return null;
            collId = elements[0];
            recordId = elements[1];
            return this;
        }


        public ObjectId getObjectId() {
            return new ObjectId(recordId);
        }

        public DBObject getDBObject() {
            return new BasicDBObject(MONGO_ID, getObjectId());
        }

        public DBCollection getDBCollection() {
            return db().getCollection(RECORD_COLLECTION_PREFIX + getCollId());
        }

        public DBObject findOne() {
            return getDBCollection().findOne(getDBObject());
        }
    }
}
