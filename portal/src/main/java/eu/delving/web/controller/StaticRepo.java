package eu.delving.web.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The repository of static pages and images
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class StaticRepo {
    private static final String DB_NAME = "static";
    static final String PAGES_COLLECTION = "pages";
    static final String IMAGES_COLLECTION = "images";
    static final String PATH = "path";
    static final String CONTENT = "content";

    @Autowired
    private Mongo mongo;

    private String databaseName = DB_NAME;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Set<String> getPagePaths() {
        return getPathSet(PAGES_COLLECTION);
    }

    public Set<String> getImagePaths() {
        return getPathSet(IMAGES_COLLECTION);
    }

    public Page getPage(String path) {
        Page page = getLatestPage(path);
        if (page != null) {
            return page;
        }
        else {
            BasicDBObject object = new BasicDBObject(PATH, path);
            object.put(CONTENT, String.format("<a href=\"%s\">%s</a>", path, path));
            return new Page(object);
        }
    }

    public Page getPage(String path, String id) {
        Page page = getPageVersion(new ObjectId(id));
        if (page != null) {
            return page;
        }
        else {
            BasicDBObject object = new BasicDBObject(PATH, path);
            object.put(CONTENT, String.format("<a href=\"%s\">%s</a>", path, path));
            return new Page(object);
        }
    }

    public void approve(String path, String idString) {
        ObjectId id = new ObjectId(idString);
        Page approved = getPageVersion(id);
        if (approved != null && approved.getId().equals(id)) {
            for (Page page : getVersionPages(path)) {
                if (!page.getId().equals(id)) {
                    page.remove();
                }
            }
        }
    }

    public List<Page> getPageVersions(String path) {
        return getVersionPages(path);
    }

    public byte[] getImage(String path) {
        DBObject object = images().findOne(new BasicDBObject(PATH, path));
        if (object != null) {
            return (byte[]) object.get(CONTENT);
        }
        else {
            return null;
        }
    }

    public void putPage(String path, String content) {
        Page page = getLatestPage(path);
        if (page != null) {
            if (content == null) {
                page.remove();
            }
            else {
                page.setContent(content);
            }
        }
        else {
            BasicDBObject object = new BasicDBObject(PATH, path);
            object.put(CONTENT, content);
            pages().insert(object);
        }
    }

    public void putImage(String path, byte[] content) {
        DBObject object = images().findOne(new BasicDBObject(PATH, path));
        if (object != null) {
            object.put(CONTENT, content);
            images().save(object);
        }
        else {
            object = new BasicDBObject(PATH, path);
            object.put(CONTENT, content);
            images().insert(object);
        }
    }

    public class Page {
        private DBObject object;

        public Page(DBObject object) {
            this.object = object;
        }

        public ObjectId getId() {
            return (ObjectId) object.get("_id");
        }

        public String getPath() {
            return (String) object.get(PATH);
        }

        public String getContent() {
            return (String) object.get(CONTENT);
        }

        public Date getDate() {
            ObjectId id = (ObjectId) object.get("_id");
            if (id == null) {
                throw new IllegalStateException("Object has no _id field!");
            }
            return new Date(getId().getTime());
        }

        public void setContent(String content) {
            BasicDBObject fresh = new BasicDBObject(PATH, getPath());
            fresh.put(CONTENT, content);
            pages().insert(fresh);
            this.object = fresh;
        }

        public void remove() {
            pages().remove(object);
        }
    }

    // === private

    private Page getPageVersion(ObjectId id) {
        DBObject object = pages().findOne(new BasicDBObject("_id", id));
        if (object != null) {
            return new Page(object);
        }
        else {
            return null;
        }
    }

    private Page getLatestPage(String path) {
        DBCursor cursor = pages().find(new BasicDBObject(PATH, path)).sort(new BasicDBObject("_id", -1)).limit(1);
        if (cursor.hasNext()) {
            return new Page(cursor.next());
        }
        else {
            return null;
        }
    }

    private List<Page> getVersionPages(String path) {
        DBCursor cursor = pages().find(new BasicDBObject(PATH, path)).sort(new BasicDBObject("_id", -1));
        List<Page> list = new ArrayList<Page>();
        while (cursor.hasNext()) {
            list.add(new Page(cursor.next()));
        }
        return list;
    }

    private Set<String> getPathSet(String collection) {
        DBCollection coll = db().getCollection(collection);
        coll.ensureIndex(new BasicDBObject(PATH, 1));
        DBCursor cursor = coll.find();
        Set<String> set = new TreeSet<String>();
        while (cursor.hasNext()) {
            DBObject pageObject = cursor.next();
            set.add((String) pageObject.get(PATH));
        }
        return set;
    }

    private DBCollection pages() {
        return db().getCollection(PAGES_COLLECTION);
    }

    private DBCollection images() {
        return db().getCollection(IMAGES_COLLECTION);
    }

    private DB db() {
        return mongo.getDB(databaseName);
    }


}
