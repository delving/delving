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

package eu.delving.services.core;

import com.mongodb.DBObject;
import eu.delving.metadata.Path;
import eu.delving.metadata.RecordMapping;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.DataSetNotFoundException;
import eu.delving.services.exceptions.MappingNotFoundException;
import eu.delving.services.exceptions.RecordParseException;
import eu.delving.services.exceptions.ResumptionTokenNotFoundException;
import org.bson.types.ObjectId;

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

    DataSet createDataSet(String spec);

    Collection<? extends DataSet> getDataSets();

    DataSet getDataSet(String spec);

    DataSet getFirstDataSet(DataSetState dataSetState);

    void incrementRecordCount(String spec, int increment);

    Set<? extends MetadataFormat> getMetadataFormats();

    Set<? extends MetadataFormat> getMetadataFormats(String id, String accessKey) throws MappingNotFoundException, AccessKeyException;

    HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix, String accessKey) throws DataSetNotFoundException;

    HarvestStep getHarvestStep(String resumptionToken) throws ResumptionTokenNotFoundException, DataSetNotFoundException;

    void removeExpiredHarvestSteps();

    Record getRecord(String identifier, String metadataFormat, String accessKey) throws BadArgumentException, DataSetNotFoundException, MappingNotFoundException, AccessKeyException;

    MetaConfig getMetaRepoConfig();

    public interface DataSet {
        String getSpec();
        boolean hasHash(String hash);

        boolean hasDetails();
        Details createDetails();
        Details getDetails();
        void setFactsHash(String sourceHash);
        DBObject getNamespaces();

        DataSetState getState();
        String getErrorMessage();
        void setState(DataSetState dataSetState);
        void setErrorState(String message);

        void parseRecords(InputStream inputStream) throws RecordParseException;
        void setSourceHash(String hash);

        void setMapping(RecordMapping recordMapping);
        void setMappingHash(String metadataPrefix, String hash);

        int getRecordsIndexed();
        void setRecordsIndexed(int count);

        Map<String,Mapping> mappings();
        int getRecordCount();
        Record fetch(ObjectId id, String metadataPrefix, String accessKey) throws MappingNotFoundException, AccessKeyException;
        List<? extends Record> records(String prefix, int start, int count, Date from, Date until, String accessKey) throws MappingNotFoundException, AccessKeyException;

        void save();

        String SPEC = "spec";
        String NAMESPACES = "namespaces";
        String MAPPINGS = "mappings";
        String MAPPING_HASH_PREFIX = "mapping_hash_";
        String SOURCE_HASH = "source_hash";
        String FACTS_HASH = "facts_hash";
        String ERROR_MESSAGE = "error";
        String DATA_SET_STATE = "state";
        String RECORDS_INDEXED = "rec_indexed";
        String DETAILS = "details";
    }

    public interface Details {
        String getName();
        void setName(String value);
        String getProviderName();
        void setProviderName(String value);
        String getDescription();
        void setDescription(String value);
        Path getRecordRoot();
        void setRecordRoot(Path path);
        Path getUniqueElement();
        void setUniqueElement(Path path);
        MetadataFormat getMetadataFormat();


        String NAME = "name";
        String PROVIDER_NAME = "provider_name";
        String DESCRIPTION = "description";
        String RECORD_ROOT = "rec_root";
        String UNIQUE_ELEMENT = "unique_element";
        String METADATA_FORMAT = "metadata_format";
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
        List<? extends Record> getRecords() throws DataSetNotFoundException, MappingNotFoundException, AccessKeyException;
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
        String getXmlString() throws MappingNotFoundException;
        String getXmlString(String metadataPrefix) throws MappingNotFoundException;

        String MODIFIED = "mod";
        String UNIQUE = "uniq";
    }

    public interface PmhSet {
        String getSetSpec();
        String getSetName();
    }

    public interface Mapping {
        MetadataFormat getMetadataFormat();
        RecordMapping getRecordMapping();

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


