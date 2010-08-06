package eu.delving.metarepo.core;

import com.mongodb.DBObject;
import eu.delving.metarepo.exceptions.BadArgumentException;
import eu.delving.metarepo.exceptions.BadResumptionTokenException;
import eu.delving.metarepo.exceptions.CannotDisseminateFormatException;
import eu.delving.metarepo.exceptions.NoRecordsMatchException;
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

    DataSet createDataSet(String spec, String name, String providerName, String description, String prefix, String namespace, String schema) throws BadArgumentException;

    Map<String, ? extends DataSet> getDataSets() throws BadArgumentException;

    Set<? extends MetadataFormat> getMetadataFormats() throws BadArgumentException;

    Set<? extends MetadataFormat> getMetadataFormats(String id) throws BadArgumentException, CannotDisseminateFormatException;

    HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix) throws NoRecordsMatchException, BadArgumentException;

    HarvestStep getHarvestStep(String resumptionToken) throws NoRecordsMatchException, BadArgumentException, BadResumptionTokenException;

    void removeExpiredHarvestSteps();

    Record getRecord(String identifier, String metadataFormat) throws CannotDisseminateFormatException, BadArgumentException;

    MetaConfig getMetaRepoConfig();

    public interface DataSet {
        String setSpec();
        String setName();
        String providerName();
        String description();
        DBObject namespaces();
        QName recordRoot();

        void parseRecords(InputStream inputStream, QName recordRoot, QName uniqueElement) throws XMLStreamException, IOException;
        void setMapping(String mappingCode, String prefix, String namespace, String schema);

        MetadataFormat metadataFormat();
        Map<String,? extends Mapping> mappings() throws BadArgumentException;
        long recordCount();
        Record fetch(ObjectId id, String metadataPrefix) throws BadArgumentException, CannotDisseminateFormatException;
        List<? extends Record> records(String prefix, int start, int count, Date from, Date until) throws CannotDisseminateFormatException, BadArgumentException;

        String SPEC = "spec";
        String NAME = "name";
        String PROVIDER_NAME = "provider_name";
        String DESCRIPTION = "description";
        String NAMESPACES = "namespaces";
        String RECORD_ROOT = "record_root";
        String METADATA_FORMAT = "metadata_format";
        String MAPPINGS = "mappings";
    }

    public interface HarvestStep {

        ObjectId resumptionToken();
        Date expiration();
        long listSize();
        int cursor();
        List<? extends Record> records() throws CannotDisseminateFormatException, BadArgumentException;
        PmhRequest pmhRequest();
        boolean hasNext();
        DBObject namespaces();
        String nextResumptionToken();

        String EXPIRATION = "exp";
        String LIST_SIZE = "listSize";
        String CURSOR = "cursor";
        String PMH_REQUEST = "pmhRequest";
        String NAMESPACES = "namespaces";
    }

    public interface PmhRequest {
        PmhVerb getVerb();
        String getSet();
        Date getFrom();
        Date getUntil();
        String getMetadataPrefix();
        String getIdentifier(); // Only used GetRecord

        String VERB = "verb";
        String SET = "set";
        String FROM = "from";
        String UNTIL = "until";
        String PREFIX = "prefix";
        String IDENTIFIER = "id";
    }

    public interface Record {
        ObjectId identifier();

        PmhSet set();
        Date modified();
        boolean deleted();
        DBObject namespaces();
        String xml() throws CannotDisseminateFormatException;
        String xml(String metadataPrefix) throws CannotDisseminateFormatException;

        String MODIFIED = "mod";
        String UNIQUE = "uniq";
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


    public interface MetaConfig {
        String getRepositoryName();
        String getAdminEmail();
        String getEarliestDateStamp();
        String getRepositoryIdentifier();
        String getSampleIdentifier();
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


