package eu.delving.services.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.metadata.MetadataModel;
import eu.delving.services.core.impl.ImplFactory;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.DataSetNotFoundException;
import eu.delving.services.exceptions.MappingNotFoundException;
import eu.delving.services.exceptions.ResumptionTokenNotFoundException;
import eu.delving.sip.AccessKey;
import eu.delving.sip.DataSetState;
import eu.europeana.sip.core.GroovyCodeResource;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Wrap the mongo database so that what goes in and comes out is managed.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoImpl implements MetaRepo {
    private Logger log = Logger.getLogger(getClass());
    private ImplFactory factory;
    private int responseListSize = 5;
    private int harvestStepSecondsToLive = 5;
    private DB mongoDatabase;

    @Autowired
    private MetaConfig metaRepoConfig;

    @Autowired
    private AccessKey accessKey;

    @Autowired
    private Mongo mongo;

    @Autowired
    private MetadataModel metadataModel;

    @Autowired
    private GroovyCodeResource groovyCodeResource;

    @Value("#{launchProperties['services.mongo.dbName']}")
    private String mongoDatabaseName = null;

    public MetaRepoImpl() {
        factory = new ImplFactory(this, db(), metadataModel, groovyCodeResource, accessKey);
    }

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
    public DataSet createDataSet(String spec) {
        DBObject object = new BasicDBObject();
        object.put(DataSet.SPEC, spec);
        object.put(DataSet.DATA_SET_STATE, DataSetState.EMPTY.toString());

        DataSet dataSet = factory.createDataSet(object);
        dataSet.save();
        return dataSet;
    }

    @Override
    public synchronized Collection<? extends DataSet> getDataSets() {
        List<DataSet> sets = new ArrayList<DataSet>();
        DBCollection collection = db().getCollection(DATASETS_COLLECTION);
        DBCursor cursor = collection.find();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            DataSet dataSet = factory.createDataSet(object);
            if (!dataSet.hasDetails()) continue; // todo: add to query
            sets.add(dataSet);
        }
        return sets;
    }

    @Override
    public DataSet getDataSet(String spec) {
        DBCollection collection = db().getCollection(DATASETS_COLLECTION);
        DBObject object = collection.findOne(new BasicDBObject(DataSet.SPEC, spec));
        if (object == null) {
            return null;
        }
        return factory.createDataSet(object);
    }

    @Override
    public DataSet getFirstDataSet(DataSetState dataSetState) {
        DBCollection collection = db().getCollection(DATASETS_COLLECTION);
        DBObject object = collection.findOne(new BasicDBObject(DataSet.DATA_SET_STATE, dataSetState.toString()));
        if (object == null) {
            return null;
        }
        return factory.createDataSet(object);
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
    public Set<MetadataFormat> getMetadataFormats() {
        Set<MetadataFormat> set = new TreeSet<MetadataFormat>();
        for (DataSet dataSet : getDataSets()) {
            set.add(dataSet.getDetails().getMetadataFormat());
            for (Mapping mapping : dataSet.mappings().values()) {
                set.add(mapping.getMetadataFormat());
            }
        }
        return set;
    }

    @Override
    public Set<MetadataFormat> getMetadataFormats(String id, String accessKey) throws MappingNotFoundException, AccessKeyException {
        Set<MetadataFormat> set = new TreeSet<MetadataFormat>();
        ObjectId objectId = new ObjectId(id);
        for (DataSet dataSet : getDataSets()) {
            Record record = dataSet.getRecord(objectId, dataSet.getDetails().getMetadataFormat().getPrefix(), accessKey);
            if (record != null) {
                set.add(dataSet.getDetails().getMetadataFormat());
                for (Mapping mapping : dataSet.mappings().values()) {
                    set.add(mapping.getMetadataFormat());
                }
            }
        }
        return set;
    }

    @Override
    public HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix, String accessKey) throws DataSetNotFoundException {
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
            throw new DataSetNotFoundException(errorMessage);
        }
        firstStep.put(HarvestStep.LIST_SIZE, dataSet.getRecordCount()); // todo: not if some records don't validate
        firstStep.put(HarvestStep.NAMESPACES, dataSet.getNamespaces());
        firstStep.put(HarvestStep.CURSOR, 0);
        firstStep.put(HarvestStep.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * harvestStepSecondsToLive));
        steps.insert(firstStep);
        return createHarvestStep(firstStep, steps, accessKey);
    }

    @Override
    public HarvestStep getHarvestStep(String resumptionToken, String accessKey) throws ResumptionTokenNotFoundException, DataSetNotFoundException {
        ObjectId objectId;
        // otherwise a illegal resumptionToken from the mongodb perspective throws a general exception
        try {
            objectId = new ObjectId(resumptionToken);
        }
        catch (Exception e) {
            throw new ResumptionTokenNotFoundException("Unable to find resumptionToken: " + resumptionToken);
        }
        DBCollection steps = db().getCollection(HARVEST_STEPS_COLLECTION);
        DBObject query = new BasicDBObject(MONGO_ID, objectId);
        DBObject step = steps.findOne(query);
        if (step == null) {
            throw new ResumptionTokenNotFoundException("Unable to find resumptionToken: " + resumptionToken);
        }
        return createHarvestStep(step, steps, accessKey);
    }

    @Override
    public void removeExpiredHarvestSteps() {
        DBCollection steps = db().getCollection(HARVEST_STEPS_COLLECTION);
        Date now = new Date();
        DBObject query = new BasicDBObject(HarvestStep.EXPIRATION, new BasicDBObject("$lt", now));
        steps.remove(query);
    }

    private HarvestStep createHarvestStep(DBObject object, DBCollection steps, String accessKey) throws DataSetNotFoundException {
        HarvestStep harvestStep = factory.createHarvestStep(object);
        String set = harvestStep.getPmhRequest().getSet();
        DataSet dataSet = getDataSet(set);
        if (dataSet == null) {
            String errorMessage = String.format("Cannot find set [%s]", set);
            log.error(errorMessage);
            throw new DataSetNotFoundException(errorMessage);
        }
        if (harvestStep.getListSize() > harvestStep.getCursor() + responseListSize) {
            DBObject nextStep = new BasicDBObject(HarvestStep.PMH_REQUEST, object.get(HarvestStep.PMH_REQUEST));
            nextStep.put(HarvestStep.NAMESPACES, object.get(HarvestStep.NAMESPACES));
            nextStep.put(HarvestStep.LIST_SIZE, object.get(HarvestStep.LIST_SIZE));
            nextStep.put(HarvestStep.CURSOR, harvestStep.getCursor() + responseListSize);
            nextStep.put(HarvestStep.EXPIRATION, new Date(System.currentTimeMillis() + 1000 * harvestStepSecondsToLive));
            steps.insert(nextStep);
            harvestStep.setNextStepId((ObjectId) nextStep.get(MONGO_ID));
        }
        return harvestStep;
    }

    @Override
    public Record getRecord(String identifier, String metadataPrefix, String accessKey) throws BadArgumentException, DataSetNotFoundException, MappingNotFoundException, AccessKeyException {
        RecordIdentifier recordIdentifier = createIdentifier(identifier);
        return fetch(recordIdentifier, metadataPrefix, accessKey);
    }

    public Record fetch(RecordIdentifier identifier, String metadataPrefix, String accessKey) throws DataSetNotFoundException, MappingNotFoundException, AccessKeyException {
        DataSet dataSet = getDataSet(identifier.collectionId);
        if (dataSet == null) {
            throw new DataSetNotFoundException(String.format("Do data set for identifier [%s]", identifier.collectionId));
        }
        return dataSet.getRecord(identifier.objectId, metadataPrefix, accessKey);
    }

    @Override
    public MetaConfig getMetaRepoConfig() {
        return metaRepoConfig;
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
