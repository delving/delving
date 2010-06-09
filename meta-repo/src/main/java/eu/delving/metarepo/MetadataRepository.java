package eu.delving.metarepo;

import com.mongodb.DBObject;
import com.mongodb.ObjectId;

import javax.xml.namespace.QName;
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

    Resumption getResumption(ObjectId id);

    public interface Collection {
        String name();
        Details details();
        Record insertRecord(String xml);
        Record updateRecord(ObjectId id, String xml);
        View getView(String mappingName);
    }

    public interface Details {
        QName recordSeparator();
        String providerName();
        String description();
    }
    
    public interface View {
        List<? extends Record> records(int start, int count);
        List<? extends Record> records(Date startTime, int count);
    }

    public interface Resumption {
        Date expiration();
        int listSize();
        List<? extends Record> records();
    }

    public interface Record {
        ObjectId identifier();
        DBObject rootObject();
        Date lastModified();
        String toXML();
    }
}
