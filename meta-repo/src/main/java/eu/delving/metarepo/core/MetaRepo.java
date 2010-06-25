package eu.delving.metarepo.core;

import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface and its sub-interfaces describe how the rest of the code interacts
 * with the metadata repository.  The interfaces also conveniently define the constants
 * used as MongoDB field names
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface MetaRepo {

    DataSet createDataSet(String spec, String name, String providerName, String description, String prefix, String namespace, String schema);

    Map<String, ? extends DataSet> getDataSets();

    Set<? extends MetadataFormat> getMetadataFormats();

    Set<? extends MetadataFormat> getMetadataFormats(String id);

    HarvestStep getHarvestStep(String resumptionToken);

    HarvestStep getHarvestStep(PmhRequest request);

    Record getRecord(String identifier, String metadataFormat);

    public interface DataSet {
        String setSpec();
        String setName();
        String providerName();
        String description();

        void parseRecords(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException, IOException;
        void setMapping(String mappingCode, String prefix, String namespace, String schema);

        MetadataFormat metadataFormat();
        Map<String,? extends Mapping> mappings();
        Record fetch(ObjectId id);
        List<? extends Record> records(String prefix, int start, int count);
        
        String SPEC = "spec";
        String NAME = "name";
        String PROVIDER_NAME = "provider_name";
        String DESCRIPTION = "description";
        String NAMESPACES = "namespaces";
        String METADATA_FORMAT = "metadata_format";
        String MAPPINGS = "mappings";
    }

    public interface HarvestStep {
        ObjectId resumptionToken(); // is resumptionToken
        Date expiration();
        int listSize();
        int cursor();
        List<? extends Record> records();
        PmhRequest pmhRequest();
        boolean hasNext();
        HarvestStep next();
    }

    public interface Record {
        ObjectId identifier();
        DBObject rootObject();
        PmhSet set();
        Date modified();
        boolean deleted();
        String xml(String metadataPrefix);

        String MODIFIED = "mod";
        String UNIQUE = "uniq";
    }

    public interface PmhRequest {
        PmhVerb getVerb();
        String getSet();
        String getFrom();
        String getUntil();
        String getMetadataPrefix();
        String getIdentifier(); // Only used GetRecord
    }

    public interface PmhSet {
        String getSetSpec();
        String getSetName();
    }

    public interface Mapping {
        MetadataFormat metadataFormat();
        String code();

        String CODE = "code";
        String FORMAT = "format";
    }

    public interface MetadataFormat {
        String prefix();
        String schema();
        String namespace();

        String PREFIX = "prefix";
        String SCHEMA = "schema";
        String NAMESPACE = "namespace";
    }

    public enum PmhVerb {
        LIST_SETS("ListSets"),
        List_METADATA_FORMATS("ListMetadataFormats"),
        LIST_IDENTIFIERS("ListIdentifiers"),
        LIST_RECORDS("ListRecords"),
        GET_RECORD("GetRecord"),
        IDENTIFY("Identify");

        private String pmhCommand;

        PmhVerb(String pmhCommand) {
            this.pmhCommand = pmhCommand;
        }

        public String getPmhCommand() {
            return pmhCommand;
        }
    }
    
    String DATABASE_NAME = "MetaRepo";
    String RECORD_COLLECTION_PREFIX = "Records_";
    String DATASETS_COLLECTION = "Datasets";
    String HARVEST_STEPS_COLLECTION = "HarvestSteps";
    String MONGO_ID = "_id";
}


