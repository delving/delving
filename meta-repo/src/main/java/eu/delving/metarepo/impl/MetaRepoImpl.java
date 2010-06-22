package eu.delving.metarepo.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.metarepo.core.MetaRepo;
import org.bson.types.ObjectId;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Wrap the mongo database so that what goes in and comes out is managed.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoImpl implements MetaRepo {
    private Mongo mongo;
    private DB mongoDatabase;
    private Map<String, DataSetImpl> datasets;

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    private synchronized DB db() {
        if (mongoDatabase == null) {
            mongoDatabase = mongo.getDB(DATABASE_NAME);
        }
        return mongoDatabase;
    }

    @Override
    public DataSet createDataSet(String spec, String name, String providerName, String description) {
        DBObject object = new BasicDBObject();
        object.put(DataSet.DATASET_SPEC, spec);
        object.put(DataSet.DATASET_NAME, name);
        object.put(DataSet.DATASET_PROVIDER_NAME, providerName);
        object.put(DataSet.DATASET_DESCRIPTION, description);
        DataSetImpl impl = new DataSetImpl(object);
        impl.saveObject();
        getDataSets();
        datasets.put(impl.setSpec(), impl);
        return impl;
    }

    @Override
    public synchronized Map<String, ? extends DataSet> getDataSets() {
        if (datasets == null) {
            datasets = new TreeMap<String, DataSetImpl>();
            DBCollection collection = db().getCollection(DATASETS_COLLECTION);
            DBCursor cursor = collection.find();
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                DataSetImpl impl = new DataSetImpl(object);
                datasets.put(impl.setSpec(), impl);
            }
        }
        return datasets;
    }

    @Override
    public List<MetadataFormat> getMetadataFormats() {
        return new ArrayList<MetadataFormat>();  //TODO: implement this
    }

    @Override
    public List<MetadataFormat> getMetadataFormats(String id) {
        return new ArrayList<MetadataFormat>();  //TODO: implement this
    }

    @Override
    public HarvestStep getHarvestStep(String resumptionToken) {
        return null;  //TODO: implement this
    }

    @Override
    public HarvestStep getHarvestStep(PmhRequest request) {
        return null;  //TODO: implement this
    }

    @Override
    public Record getRecord(String identifier, String metadataFormat) {
        return null;  //TODO: implement this
    }

    private class DataSetImpl implements DataSet {
        private DBObject object;
        private DBCollection recColl;

        private DataSetImpl(DBObject object) {
            this.object = object;
        }

        private DBCollection records() {
            if (recColl == null) {
                recColl = db().getCollection(RECORD_COLLECTION_PREFIX + setSpec());
            }
            return recColl;
        }

        @Override
        public String setSpec() {
            return (String)object.get(DATASET_SPEC);
        }

        @Override
        public String setName() {
            return (String)object.get(DATASET_NAME);
        }

        @Override
        public String providerName() {
            return (String)object.get(DATASET_PROVIDER_NAME);
        }

        @Override
        public String description() {
            return (String)object.get(DATASET_DESCRIPTION);
        }

        @Override
        public Record fetch(ObjectId id) {
            DBObject object = new BasicDBObject(MONGO_ID, id);
            return new RecordImpl(records().findOne(object));
        }

        @Override
        public void parseRecords(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException, IOException {
            records().drop();
            MongoObjectParser parser = new MongoObjectParser(
                    inputStream,
                    recordRoot,
                    uniqueElement
            );
            DBObject record;
            while ((record = parser.nextRecord()) != null) {
                records().insert(record);
            }
            object.put(DATASET_NAMESPACES, parser.getNamespaces());
            saveObject();
        }

        @Override
        public void setMapping(String mappingName, String mapping) {
            DBObject mappings = (DBObject)object.get(DATASET_MAPPINGS);
            if (mappings == null) {
                mappings = new BasicDBObject();
                object.put(DATASET_MAPPINGS, mappings);
            }
            mappings.put(mappingName, mapping);
            saveObject();
        }

        @Override
        public List<? extends Record> records(int start, int count) {
            List<RecordImpl> list = new ArrayList<RecordImpl>();
            DBCursor cursor = records().find(null, null, start, count);
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                list.add(new RecordImpl(object));
            }
            return list;
        }

        private void saveObject() {
            DBCollection collection = db().getCollection(DATASETS_COLLECTION);
            collection.save(object);
        }
    }

    private static class RecordImpl implements Record {

        private DBObject object;

        private RecordImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public ObjectId identifier() {
            return (ObjectId) object.get(MONGO_ID);
        }

        @Override
        public DBObject rootObject() {
            return object;
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
        public String format() {
            return null;  //TODO: implement this
        }

        @Override
        public String xml() {
            return (String) object.get(ORIGINAL);
        }
    }

    private static class MetadataFormatImpl implements MetadataFormat {

        private String metadataPrefix, schema, metadataNameSpace;

        private MetadataFormatImpl(String metadataPrefix, String schema, String metadataNameSpace) {
            this.metadataPrefix = metadataPrefix;
            this.schema = schema;
            this.metadataNameSpace = metadataNameSpace;
        }

        @Override
        public String getMetadataPrefix() {
            return metadataPrefix;
        }

        @Override
        public String getSchema() {
            return schema;
        }

        @Override
        public String getMetadataNameSpace() {
            return metadataNameSpace;
        }
    }
}
