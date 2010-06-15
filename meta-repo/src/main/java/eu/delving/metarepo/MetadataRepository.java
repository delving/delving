package eu.delving.metarepo;

import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
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
//        Details details();
        Record fetch(ObjectId id);
//        Record insert(String xml);
        void parseRecords(InputStream inputStream, QName recordRoot) throws XMLStreamException, IOException;
//        Record update(ObjectId id, String xml);
        void setMapping(String mappingName, String mapping);
//        View view(String mappingName);

        List<? extends Record> records(int start, int count);
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
        String xml();
    }
}
