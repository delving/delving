package eu.delving.metarepo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.bson.types.ObjectId;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.delving.metarepo.MRConstants.*;

/**
 * Wrap the mongo database so that what goes in and comes out is managed.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetadataRepositoryImpl implements MetadataRepository {
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
    public HarvestStep getHarvestStep(ObjectId id) {
        return null;  // todo: implement
    }

    private static class CollectionImpl implements Collection {

        private DBCollection dbc;

        private CollectionImpl(DBCollection dbc) {
            this.dbc = dbc;
        }

        @Override
        public String name() {
            return dbc.getName().substring(COLLECTION_PREFIX.length());
        }

        @Override
        public Record fetch(ObjectId id) {
            DBObject object = new BasicDBObject("_id", id);
            return new RecordImpl(dbc.findOne(object));
        }

        @Override
        public void parseRecords(InputStream inputStream, QName recordRoot) throws XMLStreamException, IOException {
            MongoObjectParser parser = new MongoObjectParser(
                    inputStream,
                    recordRoot,
                    FORMAT_ORIGINAL
            );
            DBObject object;
            while ((object = parser.nextRecord()) != null) {
                dbc.insert(object);
            }
        }

        @Override
        public void setMapping(String mappingName, String mapping) {
            DBObject query = new BasicDBObject(TYPE_ATTR, TYPE_MAPPING);
            DBObject result = dbc.findOne(query);
            if (result == null) {
                DBObject mappingObject = new BasicDBObject();
                mappingObject.put(TYPE_ATTR, TYPE_MAPPING);
                mappingObject.put(FORMAT_ESE, mapping);
            }
        }

        @Override
        public List<? extends Record> records(int start, int count) {
            DBObject query = new BasicDBObject();
            query.put(TYPE_ATTR, TYPE_METADATA_RECORD);
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
            return (ObjectId) object.get("_id");
        }

        @Override
        public DBObject rootObject() {
            return object;
        }

        @Override
        public Date lastModified() {
            return null;  // todo: implement
        }

        @Override
        public String xml() {
            return (String) object.get(FORMAT_ORIGINAL);
        }
    }
}
