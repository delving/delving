/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.delving.core.storage.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.core.storage.StaticRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static eu.delving.core.util.MongoObject.mob;

/**
 * The repository of static pages and images
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class StaticRepoImpl implements StaticRepo {

    @Autowired
    private Mongo mongo;

    @Value("#{launchProperties['portal.name']}")
    private String portalName = "";

    @Value("#{launchProperties['portal.mongo.dbName']}")
    private String databaseName;

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

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
            return new PageImpl(mob(
                    PATH, path,
                    CONTENT, String.format("<a href=\"/%s/%s\">%s</a>", portalName, path, path)
            ));
        }
    }

    public Page getPage(String path, String id) {
        Page page = getPageVersion(new ObjectId(id));
        if (page != null) {
            return page;
        }
        else {
            return new PageImpl(mob(
                    PATH, path,
                    CONTENT, String.format("<a href=\"%s\">%s</a>", path, path)
            ));
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

    public void setHidden(String path, boolean hidden) {
        for (Page page : getVersionPages(path)) {
            page.setHidden(hidden);
        }
    }

    public List<Page> getPageVersions(String path) {
        return getVersionPages(path);
    }

    public byte[] getImage(String path) {
        DBObject object = images().findOne(mob(PATH, path));
        if (object != null) {
            return (byte[]) object.get(CONTENT);
        }
        else {
            return null;
        }
    }

    public void deleteImage(String path) {
        DBObject object = images().findOne(mob(PATH, path));
        if (object != null) {
            images().remove(object);
        }
    }

    public ObjectId putPage(String path, String content, Locale locale) {
        Page page = getLatestPage(path);
        if (page != null) {
            if (content == null) {
                page.remove();
            }
            else {
                page.setContent(content, locale);
            }
        }
        else {
            page = new PageImpl(mob(PATH, path));
            page.setContent(content, locale);
        }
        return page.getId();
    }

    public boolean setPagePath(String oldPath, String newPath) {
        DBObject object = pages().findOne(mob(PATH, oldPath));
        if (object != null) {
            object.put(PATH, newPath);
            pages().save(object);
            return true;
        }
        else {
            return false;
        }
    }

    public void putImage(String path, byte[] content) {
        DBObject object = images().findOne(mob(PATH, path));
        if (object != null) {
            object.put(CONTENT, content);
            images().save(object);
        }
        else {
            images().insert(mob(
                    PATH, path,
                    CONTENT, content
            ));
        }
    }

    public boolean setImagePath(String oldPath, String newPath) {
        DBObject object = images().findOne(mob(PATH, oldPath));
        if (object != null) {
            object.put(PATH, newPath);
            images().save(object);
            return true;
        }
        else {
            return false;
        }
    }

    public class PageImpl implements Page {
        private DBObject object;

        public PageImpl(DBObject object) {
            this.object = object;
        }

        public ObjectId getId() {
            return (ObjectId) object.get(MONGO_ID);
        }

        public String getPath() {
            return "/" + portalName + "/" + object.get(PATH);
        }

        public boolean isHidden() {
            Boolean hidden = (Boolean)object.get(HIDDEN);
            return hidden != null && hidden;
        }

        public void setHidden(boolean hidden) {
            object.put(HIDDEN, hidden);
            pages().save(object);
        }

        public String getContent(Locale locale) {
            String content;
            if (locale != null) {
                content = (String) object.get(CONTENT + "_" + locale.getLanguage());
                if (content == null) {
                    content = (String) object.get(CONTENT);
                }
            }
            else {
                content = (String) object.get(CONTENT);
            }
            if (content == null) {
                content = "";
            }
            return content;
        }

        public Date getDate() {
            ObjectId id = (ObjectId) object.get(MONGO_ID);
            if (id == null) {
                throw new IllegalStateException("Object has no _id field!");
            }
            return new Date(getId().getTime());
        }

        public void setContent(String content, Locale locale) {
            BasicDBObject fresh = copyObject();
            if (locale != null) {
                fresh.put(CONTENT + "_" + locale.getLanguage(), content);
                if (DEFAULT_LANGUAGE.equals(locale.getLanguage())) {
                    fresh.put(CONTENT, content);
                }
            }
            else {
                fresh.put(CONTENT, content);

            }
            pages().insert(fresh);
            this.object = fresh;
        }

        public void remove() {
            pages().remove(object);
        }

        private BasicDBObject copyObject() {
            BasicDBObject fresh = mob();
            for (String key : object.keySet()) {
                if (!key.equals(MONGO_ID)) {
                    fresh.put(key, object.get(key));
                }
            }
            return fresh;
        }

    }

    // === private

    private Page getPageVersion(ObjectId id) {
        DBObject object = pages().findOne(mob("_id", id));
        if (object != null) {
            return new PageImpl(object);
        }
        else {
            return null;
        }
    }

    private Page getLatestPage(String path) {
        DBCursor cursor = pages().find(mob(PATH, path)).sort(mob("_id", -1)).limit(1);
        if (cursor.hasNext()) {
            return new PageImpl(cursor.next());
        }
        else {
            return null;
        }
    }

    private List<Page> getVersionPages(String path) {
        DBCursor cursor = pages().find(mob(PATH, path)).sort(mob("_id", -1));
        List<Page> list = new ArrayList<Page>();
        while (cursor.hasNext()) {
            list.add(new PageImpl(cursor.next()));
        }
        return list;
    }

    private Set<String> getPathSet(String collection) {
        DBCollection coll = db().getCollection(collection);
        coll.ensureIndex(mob(PATH, 1));
        DBCursor cursor = coll.find();
        Set<String> set = new TreeSet<String>();
        while (cursor.hasNext()) {
            DBObject pageObject = cursor.next();
            set.add("/" + portalName + "/" + pageObject.get(PATH));
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
