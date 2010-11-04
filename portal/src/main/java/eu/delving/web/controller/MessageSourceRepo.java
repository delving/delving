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
    static final String KEY = "key";
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

    public void setTranslation(String key, String message, Locale locale) {
        getTranslation(key).setContent(message, locale).save();
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
            String content = (String) object.get(objectKey(locale));
            if (content == null) {
                content = getParentMessageSource().getMessage(getKey(), null, locale);
//                setContent(content, locale).save();
            }
            return content;
        }

        public Translation setContent(String content, Locale locale) {
            object.put(objectKey(locale), content);
            if (locale != null && DEFAULT_LANGUAGE.equals(locale.getLanguage())) {
                object.put(objectKey(null), content);
            }
            return this;
        }

        public void remove() {
            messages().remove(object);
        }

        public void save() {
            messages().save(object);
        }
    }

    // === private

    private String objectKey(Locale locale) {
        if (locale != null) {
            return CONTENT + "_" + locale.getLanguage();
        }
        else {
            return CONTENT;
        }
    }

    private DBCollection messages() {
        return db().getCollection(MESSAGES_COLLECTION);
    }

    private DB db() {
        return mongo.getDB(databaseName);
    }
}
