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

import static eu.delving.metarepo.core.Constant.COLLECTION_PREFIX;
import static eu.delving.metarepo.core.Constant.DATABASE_NAME;
import static eu.delving.metarepo.core.Constant.MODIFIED;
import static eu.delving.metarepo.core.Constant.MONGO_ID;
import static eu.delving.metarepo.core.Constant.ORIGINAL;
import static eu.delving.metarepo.core.Constant.TYPE;
import static eu.delving.metarepo.core.Constant.TYPE_MAPPING;
import static eu.delving.metarepo.core.Constant.TYPE_METADATA_RECORD;

/**
 * Wrap the mongo database so that what goes in and comes out is managed.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoImpl implements MetaRepo {
    private Mongo mongo;
    private DB mongoDatabase;

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    private DB db() {
        if (mongoDatabase == null) {
            mongoDatabase = mongo.getDB(DATABASE_NAME);
        }
        return mongoDatabase;
    }

    @Override
    public List<String> getCollectionNames() {
        List<String> names = new ArrayList<String>();
        for (String name : db().getCollectionNames()) {
            if (name.startsWith(COLLECTION_PREFIX)) {
                names.add(name.substring(COLLECTION_PREFIX.length()));
            }
        }
        return names;
    }

    @Override
    public Collection getCollection(String name) {
        return new CollectionImpl(db().getCollection(COLLECTION_PREFIX + name));
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

    private static class CollectionImpl implements Collection {

        private DBCollection dbc;

        private CollectionImpl(DBCollection dbc) {
            this.dbc = dbc;
        }

        @Override
        public String setSpec() {
            return dbc.getName().substring(COLLECTION_PREFIX.length());
        }

        @Override
        public String nameOfSet() {
            return "nameOfSet";  //TODO: implement this
        }

        @Override
        public Record fetch(ObjectId id) {
            DBObject object = new BasicDBObject(MONGO_ID, id);
            return new RecordImpl(dbc.findOne(object));
        }

        @Override
        public void parseRecords(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException, IOException {
            Map<String,String> namespaceMap = new TreeMap<String,String>();
            MongoObjectParser parser = new MongoObjectParser(
                    inputStream,
                    recordRoot,
                    uniqueElement,
                    namespaceMap
            );
            DBObject object;
            while ((object = parser.nextRecord()) != null) {
                dbc.insert(object);
            }
            // todo: save the namespaces!
        }

        @Override
        public void setMapping(String mappingName, String mapping) {
            DBObject query = new BasicDBObject(TYPE, TYPE_MAPPING);
            DBObject result = dbc.findOne(query);
            if (result == null) {
                DBObject mappingObject = new BasicDBObject();
                mappingObject.put(TYPE, TYPE_MAPPING);
                mappingObject.put(mappingName, mapping);
            }
        }

        @Override
        public List<? extends Record> records(int start, int count) {
            DBObject query = new BasicDBObject();
            query.put(TYPE, TYPE_METADATA_RECORD);
            List<RecordImpl> list = new ArrayList<RecordImpl>();
            DBCursor cursor = dbc.find(query, null, start, count);
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                list.add(new RecordImpl(object));
            }
            return list;
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

}
