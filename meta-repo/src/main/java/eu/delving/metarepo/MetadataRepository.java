package eu.delving.metarepo;

import com.mongodb.DBObject;
import com.mongodb.ObjectId;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface MetadataRepository {

    List<String> getCollectionNames();

    Collection getCollection(String name);

    HarvestStep getHarvestStep(ObjectId id);

    public interface Collection {
        String name();
        Details details();
        Record fetch(ObjectId id);
        Record insert(String xml);
        List<? extends Record> insert(InputStream inputStream, QName recordRoot);
        Record update(ObjectId id, String xml);
        void addMapping(String mappingName, String mapping);
        View view(String mappingName);
    }

    public interface Details {
        String providerName();
        String description();
    }
    
    public interface View {
        List<? extends Record> records(int start, int count);
        List<? extends Record> records(Date startTime, int count);
    }

    public interface HarvestStep {
        ObjectId identifier();
        Date expiration();
        int listSize();
        List<? extends Record> records();
        HarvestStep next();
    }

    public interface Record {
        ObjectId identifier();
        DBObject rootObject();
        Date lastModified();
        String toXML();
    }
}
