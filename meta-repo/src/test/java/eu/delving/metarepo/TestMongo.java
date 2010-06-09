package eu.delving.metarepo;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ObjectId;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMongo {
    private static final Logger LOG = Logger.getLogger(TestMongo.class);
    private static final File MONGO = new File("meta-repo/target/mongo");
    private static DaemonRunner daemonRunner;
    private DBCollection collection;

    @BeforeClass
    public static void startDaemon() throws IOException, InterruptedException {
        if (!MONGO.mkdirs()) {
            throw new IOException("couldn't make directories");
        }
        daemonRunner = new DaemonRunner();
        daemonRunner.start();
    }

    @AfterClass
    public static void stopDaemon() {
        daemonRunner.kill();
    }

    @Before
    public void createMongo() throws UnknownHostException, InterruptedException {
        LOG.info("Setting up the database and the collection");
        Mongo mongo = new Mongo();
        DB delvingDB = mongo.getDB("delving");
        delvingDB.dropDatabase();
        this.collection = delvingDB.getCollection("test-collection");
        LOG.info("Collection ready");
    }

    @Test
    public void insertSomething() {
        LOG.info("Testing testing");
        SetMultimap<String, String> map = TreeMultimap.create();
        map.put("key1", "val1");
        map.put("key1", "second of number one");
        map.put("key2", "valsy3");
        map.put("key3", "newergoo");
        collection.insert(createDBObject(map));
        DBCursor cursor = collection.find();
        int count = 0;
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            System.out.format("Retrieved %s %s\n", object, new Date(((ObjectId) object.get("_id")).getTime()));
            count++;
        }
        Assert.assertEquals("Should be one document", 1, count);
    }

    public void updateSomething() {
//        DBObject criteria = new BasicDBObject("_id", new ObjectId(id));
//        collection.update(criteria, object);
//        System.out.format("updated object's id is [%s]\n", object.get("_id"));
    }

    private static DBObject createDBObject(Multimap<String, String> record) {
        BasicDBObject object = new BasicDBObject();
        for (Map.Entry<String, String> entry : record.entries()) {
            BasicDBList list = (BasicDBList) object.get(entry.getKey());
            if (list == null) {
                list = new BasicDBList();
                object.put(entry.getKey(), list);
            }
            list.add(entry.getValue());
        }
        return object;
    }

//    private void insert(String id, DBObject object) throws UnknownHostException {
//        DB db = mongo.getDB("delving");
//        DBCollection collection = db.getCollection("metadata");
//        object.put("_id", new ObjectId(id));
//        collection.insert(object);
//        System.out.format("inserted object's id is [%s] error [%s]\n", object.get("_id"), db.getLastError());
//    }
//
    private static class DaemonRunner extends Thread {
        private Process process;
        private boolean killed;

        @Override
        public void run() {
            try {
                LOG.info("Firing up the daemon");
                process = Runtime.getRuntime().exec(new String[]{
                        "/opt/local/bin/mongod",
                        "--dbpath",
                        MONGO.getAbsolutePath()
                });
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    LOG.info(line);
                }
                LOG.info("Daemon is dead");
            }
            catch (IOException e) {
                if (!killed) {
                    e.printStackTrace();
                }
            }
        }

        public void kill() {
            killed = true;
            process.destroy();
        }
    }

}
