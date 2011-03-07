/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.delving.services.core;

import com.mongodb.DBObject;
import eu.delving.metadata.RecordMapping;
import eu.delving.services.exceptions.AccessKeyException;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.DataSetNotFoundException;
import eu.delving.services.exceptions.MappingNotFoundException;
import eu.delving.services.exceptions.RecordParseException;
import eu.delving.services.exceptions.ResumptionTokenNotFoundException;
import eu.delving.sip.DataSetState;
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

    Set<? extends MetadataFormat> getMetadataFormats();

    Set<? extends MetadataFormat> getMetadataFormats(String id, String accessKey) throws MappingNotFoundException, AccessKeyException;

    HarvestStep getFirstHarvestStep(MetaRepo.PmhVerb verb, String set, Date from, Date until, String metadataPrefix, String accessKey) throws DataSetNotFoundException, MappingNotFoundException, AccessKeyException;

    HarvestStep getHarvestStep(String resumptionToken, String accessKey) throws ResumptionTokenNotFoundException, DataSetNotFoundException, MappingNotFoundException, AccessKeyException;

    void removeExpiredHarvestSteps();

    Record getRecord(String identifier, String metadataFormat, String accessKey) throws BadArgumentException, DataSetNotFoundException, MappingNotFoundException, AccessKeyException;

    MetaConfig getMetaRepoConfig();

    public interface DataSet {
        String getSpec();

        boolean hasDetails();

        Details createDetails();

        Details getDetails();

        void setFactsHash(String sourceHash);

        DBObject getNamespaces();

        DataSetState getState(boolean fresh);

        String getErrorMessage();

        void setState(DataSetState dataSetState);

        void setErrorState(String message);

        void parseRecords(InputStream inputStream) throws RecordParseException;

        void setSourceHash(String hash, boolean downloaded);

        void setMapping(RecordMapping recordMapping, boolean accessKeyRequired);

        void setMappingHash(String metadataPrefix, String hash);

        int getRecordsIndexed();

        void setRecordsIndexed(int count);

        void incrementRecordsIndexed(int increment);

        Map<String, Mapping> mappings();

        int getRecordCount();

        Record getRecord(ObjectId id, String metadataPrefix, String accessKey) throws MappingNotFoundException, AccessKeyException;

        interface RecordFetch {
            List<? extends Record> getRecords();

            ObjectId getAfterId();
        }

        RecordFetch getRecords(String prefix, int count, Date from, ObjectId afterId, Date until, String accessKey) throws MappingNotFoundException, AccessKeyException;

        List<String> getHashes();

        void save();

        void delete();

        String SPEC = "spec";
        String NAMESPACES = "namespaces";
        String MAPPINGS = "mappings";
        String MAPPING_HASH_PREFIX = "mapping_hash_";
        String DOWNLOADED_SOURCE_HASH = "downloaded_source_hash";
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

        MetadataFormat getMetadataFormat();

        byte [] getFacts();

        void setFacts(byte [] factBytes);

        int getTotalRecordCount();
        void setTotalRecordCount(int count);
        int getDeletedRecordCount();
        void setDeletedRecordCount(int count);
        int getUploadedRecordCount();
        void setUploadedRecordCount(int count);

        String NAME = "name";
        String METADATA_FORMAT = "metadata_format";
        String FACT_BYTES = "fact_bytes";
        String TOTAL_RECORDS = "total_records";
        String DELETED_RECORDS = "deleted_records";
        String UPLOADED_RECORDS = "uploaded_records";
    }

    public interface HarvestStep {

        ObjectId getId();

        Date getExpiration();

        int getListSize();

        Runnable createRecordFetcher(DataSet dataSet, String key);

        int getCursor();

        int getRecordCount();

        List<? extends Record> getRecords();

        PmhRequest getPmhRequest();

        DBObject getNamespaces();

        boolean hasNext();

        String nextResumptionToken();

        ObjectId getAfterId();

        ObjectId getNextId();

        String getErrorMessage();

        void save();

        String FIRST = "first";
        String EXPIRATION = "exporatopm";
        String LIST_SIZE = "listSize";
        String CURSOR = "cursor";
        String PMH_REQUEST = "pmhRequest";
        String NAMESPACES = "namespaces";
        String ERROR_MESSAGE = "error";
        String AFTER_ID = "afterId";
        String NEXT_ID = "nextId";
    }

    public interface PmhRequest {
        PmhVerb getVerb();

        String getSet();

        Date getFrom();

        Date getUntil();

        String getMetadataPrefix();

        String VERB = "verb";
        String SET = "set";
        String FROM = "from";
        String UNTIL = "until";
        String PREFIX = "prefix";
    }

    public interface Record {
        ObjectId getId();

        String getUnique();

        Date getModifiedDate();

        boolean isDeleted();

        DBObject getNamespaces();

        DBObject getHash();

        Map<String,Integer> getFingerprint();

        String getXmlString() throws MappingNotFoundException;

        String getXmlString(String metadataPrefix) throws MappingNotFoundException;

        String MODIFIED = "modified";
        String DELETED = "deleted";
        String UNIQUE = "uniq";
        String HASH = "hash";
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


