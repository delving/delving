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

package eu.delving.sip;

import eu.delving.core.metadata.RecordDefinition;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.SourceDetails;
import eu.delving.core.metadata.Statistics;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import java.util.Set;

/**
 * This interface describes how files are stored by the sip-creator
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface FileStore {

    String SOURCE_FILE_PREFIX = "source.";
    String SOURCE_FILE_SUFFIX = ".xml.gz";
    String STATISTICS_FILE_NAME = "statistics.ser";
    String SOURCE_DETAILS_FILE_NAME = "source-details.xml";
    String MAPPING_FILE_PREFIX = "mapping.";
    String DISCARDED_FILE_PREFIX = "discarded.";

    Set<String> getDataSetSpecs();

    DataSetStore getDataSetStore(String spec) throws FileStoreException;

    DataSetStore createDataSetStore(String spec, InputStream xmlInput) throws FileStoreException;

    public interface DataSetStore {
        String getSpec();

        InputStream createXmlInputStream() throws FileStoreException;

        List<Statistics> getStatistics() throws FileStoreException;

        void setStatistics(List<Statistics> statisticsList) throws FileStoreException;

        RecordMapping getRecordMapping(RecordDefinition recordDefinition) throws FileStoreException;

        void setRecordMapping(RecordMapping recordMapping) throws FileStoreException;

        SourceDetails getSourceDetails() throws FileStoreException;

        void setSourceDetails(SourceDetails details) throws FileStoreException;

        MappingOutput createMappingOutput(RecordMapping recordMapping, File normalizedFile) throws FileStoreException;

        void delete() throws FileStoreException;
    }

    public interface MappingOutput {

        Writer getNormalizedWriter();

        Writer getDiscardedWriter();

        void recordNormalized();

        void recordDiscarded();

        void close(boolean abort) throws FileStoreException;
    }

}
