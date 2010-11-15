package eu.delving.services.core;

import com.mongodb.DBObject;
import eu.delving.core.metadata.MetadataException;
import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.BadResumptionTokenException;
import eu.delving.services.exceptions.CannotDisseminateFormatException;
import eu.delving.services.exceptions.NoRecordsMatchException;
import org.bson.types.ObjectId;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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

    DataSet createDataSet(String spec) throws BadArgumentException;

    Collection<? extends DataSet> getDataSets() throws BadArgumentException;

    DataSet getDataSet(String spec) throws BadArgumentException;

    DataSet getFirstDataSet(DataSetState dataSetState) throws BadArgumentException;

    void incrementRecordCount(String spec, int increment);

    Set<? extends MetadataFormat> getMetadataFormats() throws BadArgumentException;

    Set<? extends MetadataFormat> getMetadataFormats(String id, String accessKey) throws BadArgumentException, CannotDisseminateFormatException;

    HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix, String accessKey) throws NoRecordsMatchException, BadArgumentException;

    HarvestStep getHarvestStep(String resumptionToken) throws NoRecordsMatchException, BadArgumentException, BadResumptionTokenException;

    void removeExpiredHarvestSteps();

    Record getRecord(String identifier, String metadataFormat, String accessKey) throws CannotDisseminateFormatException, BadArgumentException;

    MetaConfig getMetaRepoConfig();

    public interface DataSet {
        String getSpec();
        String getName();
        void setName(String value);
        String getProviderName();
        void setProviderName(String value);
        String getDescription();
        void setDescription(String value);
        DBObject getNamespaces();
        Path getRecordRoot();
        void setRecordRoot(Path path);
        Path getUniqueElement();
        void setUniqueElement(Path path);
        int getRecordsIndexed();
        void setRecordsIndexed(int count);
        DataSetState getState();
        String getErrorMessage();
        void setState(DataSetState dataSetState);
        void setErrorState(String message);
        MetadataFormat getMetadataFormat();
        void save();

        void parseRecords(InputStream inputStream) throws XMLStreamException, IOException;
        void setMapping(RecordMapping recordMapping);

        Map<String,Mapping> mappings() throws BadArgumentException;
        int getRecordCount();
        Record fetch(ObjectId id, String metadataPrefix, String accessKey) throws BadArgumentException, CannotDisseminateFormatException;
        List<? extends Record> records(String prefix, int start, int count, Date from, Date until, String accessKey) throws CannotDisseminateFormatException, BadArgumentException;

        String SPEC = "spec";
        String NAME = "name";
        String PROVIDER_NAME = "provider_name";
        String DESCRIPTION = "description";
        String NAMESPACES = "namespaces";
        String RECORD_ROOT = "rec_root";
        String UNIQUE_ELEMENT = "unique_element";
        String METADATA_FORMAT = "metadata_format";
        String MAPPINGS = "mappings";
        String RECORDS_INDEXED = "rec_indexed";
        String DATA_SET_STATE = "state";
        String ERROR_MESSAGE = "error";
    }

    public enum DataSetState {
        EMPTY,
        DISABLED,
        UPLOADED,
        QUEUED,
        INDEXING,
        ENABLED,
        ERROR;

        public static DataSetState get(String string) {
            for (DataSetState t : values()) {
                if (t.toString().equalsIgnoreCase(string)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Did not recognize DataSetState: [" + string + "]");
        }
    }

    public interface HarvestStep {

        ObjectId getResumptionToken();
        Date getExpiration();
        int getListSize();
        int getCursor();
        List<? extends Record> getRecords() throws CannotDisseminateFormatException, BadArgumentException;
        PmhRequest getPmhRequest();
        DBObject getNamespaces();
        boolean hasNext();
        String nextResumptionToken();

        String EXPIRATION = "exp";
        String LIST_SIZE = "listSize";
        String CURSOR = "cursor";
        String PMH_REQUEST = "pmhRequest";
        String NAMESPACES = "namespaces";
        String ACCESS_KEY = "access";
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
        ObjectId getIdentifier();
        PmhSet getPmhSet();
        Date getModifiedDate();
        boolean isDeleted();
        DBObject getNamespaces();
        String getXmlString() throws CannotDisseminateFormatException;
        String getXmlString(String metadataPrefix) throws CannotDisseminateFormatException;

        String MODIFIED = "mod";
        String UNIQUE = "uniq";
    }

    public interface PmhSet {
        String getSetSpec();
        String getSetName();
    }

    public interface Mapping {
        MetadataFormat getMetadataFormat();
        RecordMapping getRecordMapping() throws MetadataException;

        String RECORD_MAPPING = "recordMapping";
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
        String getPrefix();
        void setPrefix(String value);
        String getSchema();
        void setSchema(String value);
        String getNamespace();
        void setNamespace(String value);
        boolean isAccessKeyRequired();
        void setAccessKeyRequired(boolean required);

        String PREFIX = "prefix";
        String SCHEMA = "schema";
        String NAMESPACE = "namespace";
        String ACCESS_KEY_REQUIRED = "accessKeyRequired";
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

    String RECORD_COLLECTION_PREFIX = "Records.";
    String DATASETS_COLLECTION = "Datasets";
    String HARVEST_STEPS_COLLECTION = "HarvestSteps";
    String MONGO_ID = "_id";
}


