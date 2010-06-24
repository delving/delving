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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Wrap the mongo database so that what goes in and comes out is managed.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoImpl implements MetaRepo {
    private Mongo mongo;
    private DB mongoDatabase;
    private Map<String, DataSetImpl> dataSets;

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
            for (Mapping mapping : dataSet.mappings()) {
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
                for (Mapping mapping : dataSet.mappings()) {
                    set.add(mapping.metadataFormat());
                }
            }
        }
        return set;
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
        private MetadataFormat metadataFormat;

        private DataSetImpl(DBObject object) {
            this.object = object;
            if (object.get(METADATA_FORMAT) != null) {
                this.metadataFormat = new MetadataFormatImpl((DBObject)object.get(METADATA_FORMAT));
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
            return (String)object.get(SPEC);
        }

        @Override
        public String setName() {
            return (String)object.get(NAME);
        }

        @Override
        public String providerName() {
            return (String)object.get(PROVIDER_NAME);
        }

        @Override
        public String description() {
            return (String)object.get(DESCRIPTION);
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
            object.put(NAMESPACES, parser.getNamespaces());
            saveObject();
        }

        @Override
        public void setMapping(String mappingCode, String prefix, String namespace, String schema) {
            DBObject mappings = (DBObject)object.get(MAPPINGS);
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
        public Set<? extends Mapping> mappings() {
            Set<MappingImpl> mappingList = new TreeSet<MappingImpl>();
            DBObject mappings = (DBObject) object.get(MAPPINGS);
            if (mappings != null) {
                for (String prefix : mappings.keySet()) {
                    mappingList.add(new MappingImpl((DBObject) mappings.get(prefix)));
                }
            }
            return mappingList;
        }

        @Override
        public Record fetch(ObjectId id) {
            DBObject object = new BasicDBObject(MONGO_ID, id);
            return new RecordImpl(records().findOne(object), metadataFormat);
        }

        @Override
        public List<? extends Record> records(String prefix, int start, int count) {
            if (!"abm".equals(prefix)) {
                throw new RuntimeException("Not yet ready for prefixes and on-the-fly mapping");
            }
            List<RecordImpl> list = new ArrayList<RecordImpl>();
            DBCursor cursor = records().find(null, null, start, count);
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                list.add(new RecordImpl(object, metadataFormat));
                if (count-- <= 0) { // todo: damn, why isn't count parameter working in the find() above?
                    break;
                }
            }
            return list;
        }

        private void saveObject() {
            DBCollection collection = db().getCollection(DATASETS_COLLECTION);
            collection.save(object);
        }
    }

    private static class MappingImpl implements Mapping, Comparable<Mapping> {
        private DBObject object;
        private MetadataFormat metadataFormat;

        private MappingImpl(DBObject object) {
            this.object = object;
            this.metadataFormat = new MetadataFormatImpl((DBObject)object.get(FORMAT));
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
    }

    private static class MetadataFormatImpl implements MetadataFormat, Comparable<MetadataFormat> {
        private String prefix;
        private DBObject object;

        private MetadataFormatImpl(DBObject object) {
            this.object = object;
            if (object == null) {
                throw new RuntimeException("MetdataFormat object missing!");
            }
            this.prefix = (String)object.get(PREFIX);
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
            return (String)object.get(SCHEMA);
        }

        @Override
        public String namespace() {
            return (String)object.get(NAMESPACE);
        }

        @Override
        public int compareTo(MetadataFormat o) {
            return prefix().compareTo(o.prefix());
        }
    }

    private static class RecordImpl implements Record {

        private DBObject object;
        private MetadataFormat metadataFormat;

        private RecordImpl(DBObject object, MetadataFormat metadataFormat) {
            this.object = object;
            this.metadataFormat = metadataFormat;
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
        public MetadataFormat metadataFormat() {
            return metadataFormat;
        }

        @Override
        public String xml() {
            return (String) object.get(ORIGINAL);
        }
    }
}
