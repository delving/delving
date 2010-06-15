package eu.delving.metarepo;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * Create a Mongo DB 0bject
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DbFactoryBean implements FactoryBean<DB> {
    private Mongo mongo;
    private String name;

    @Override
    public DB getObject() throws Exception {
        return mongo.getDB(name);
    }

    @Override
    public Class<?> getObjectType() {
        return DB.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Required
    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    @Required
    public void setName(String name) {
        this.name = name;
    }
}
