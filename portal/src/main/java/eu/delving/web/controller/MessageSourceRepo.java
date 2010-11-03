package eu.delving.web.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * The repository of i18n messages
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MessageSourceRepo extends AbstractMessageSource {
    static final String MESSAGES_COLLECTION = "messages";
    static final String KEY = "path";
    static final String CONTENT = "content";
    static final String DEFAULT_LANGUAGE = "en";

    @Autowired
    private Mongo mongo;

    @Value("#{launchProperties['portal.mongo.dbName']}")
    private String databaseName;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Set<String> getMessageKeys() {
        messages().ensureIndex(new BasicDBObject(KEY, 1));
        DBCursor cursor = messages().find();
        Set<String> set = new TreeSet<String>();
        while (cursor.hasNext()) {
            DBObject pageObject = cursor.next();
            set.add((String) pageObject.get(KEY));
        }
        return set;
    }

    public Translation getTranslation(String key) {
        DBObject object = messages().findOne(new BasicDBObject(KEY, key));
        if (object != null) {
            return new Translation(object);
        }
        else {
            return new Translation(new BasicDBObject(KEY, key));
        }
    }

    @Override
    protected String resolveCodeWithoutArguments(String key, Locale locale) {
        return getTranslation(key).getContent(locale);
    }

    @Override
    protected MessageFormat resolveCode(String key, Locale locale) {
        return createMessageFormat(resolveCodeWithoutArguments(key, locale), locale);
    }

    public class Translation {
        private DBObject object;

        public Translation(DBObject object) {
            this.object = object;
        }

        public ObjectId getId() {
            return (ObjectId) object.get("_id");
        }

        public String getKey() {
            return (String) object.get(KEY);
        }

        public String getContent(Locale locale) {
            String content;
            if (locale != null) {
                content = (String) object.get(CONTENT + "_" + locale.getLanguage());
                if (content == null) {
                    content = getParentMessageSource().getMessage(getKey(), null, locale);
                    if (content == null) {
                        content = (String) object.get(CONTENT);
                    }
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
            messages().insert(fresh);
            this.object = fresh;
        }

        public void remove() {
            messages().remove(object);
        }

        public void save() {
            messages().save(object);
        }

        private BasicDBObject copyObject() {
            BasicDBObject fresh = new BasicDBObject();
            for (String key : object.keySet()) {
                if (!key.equals("_id")) {
                    fresh.put(key, object.get(key));
                }
            }
            return fresh;
        }

    }

    // === private

    private DBCollection messages() {
        return db().getCollection(MESSAGES_COLLECTION);
    }

    private DB db() {
        return mongo.getDB(databaseName);
    }
}
