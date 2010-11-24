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

import eu.delving.core.metadata.MappingModel;
import eu.delving.core.metadata.MetadataException;
import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.MetadataModelImpl;
import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.RecordDefinition;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.SourceDetails;
import eu.delving.core.metadata.Statistics;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Make sure the file store is working
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestFileStore {
    private static final File TARGET = new File("target");
    private static final File DIR = new File(TARGET, "file-store");
    private static final String SPEC = "spek";
    private Logger log = Logger.getLogger(getClass());
    private FileStore fileStore;

    @Before
    public void createStore() throws FileStoreException {
        if (!TARGET.exists()) {
            throw new RuntimeException("Target directory " + TARGET.getAbsolutePath() + " not found");
        }
        if (DIR.exists()) {
            deleteFiles();
        }
        else {
            if (!DIR.mkdirs()) {
                throw new RuntimeException("Unable to create directory " + DIR.getAbsolutePath());
            }
        }
        this.fileStore = new FileStoreImpl(DIR);
    }

    @After
    public void deleteStore() {
        if (DIR.exists()) {
            deleteFiles();
        }
    }

    @Test
    public void createDelete() throws IOException, FileStoreException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC, sampleFile(), null);
        Assert.assertEquals("Should be one file", 1, DIR.listFiles().length);
        Assert.assertEquals("Should be one sped", 1, fileStore.getDataSetSpecs().size());
        Assert.assertEquals("Should be one file", 1, new File(DIR, SPEC).listFiles().length);
        log.info("Created " + new File(DIR, SPEC).listFiles()[0].getAbsolutePath());
        InputStream inputStream = sampleInputStream();
        InputStream storedStream = fileStore.getDataSetStore(SPEC).createXmlInputStream();
        int input = 0, stored;
        while (input != -1) {
            input = inputStream.read();
            stored = storedStream.read();
            Assert.assertEquals("Stream discrepancy", input, stored);
        }
        store.delete();
        Assert.assertEquals("Should be zero files", 0, DIR.listFiles().length);
    }

    @Test
    public void manipulateAppConfig() throws FileStoreException {
        AppConfig appConfig = fileStore.getAppConfig();
        Assert.assertTrue("should be no access key", appConfig.getAccessKey().isEmpty());
        appConfig.setAccessKey("gumby");
        fileStore.setAppConfig(appConfig);
        appConfig = fileStore.getAppConfig();
        Assert.assertEquals("Should have saved access key", "gumby", appConfig.getAccessKey());
    }

    @Test
    public void manipulateMapping() throws IOException, FileStoreException, MetadataException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC, sampleFile(), null);
        Assert.assertEquals("Spec should be the same", SPEC, store.getSpec());
        RecordDefinition recordDefinition = getRecordDefinition();
        RecordMapping recordMapping = store.getRecordMapping(recordDefinition);
        Assert.assertEquals("Prefixes should be the same", recordDefinition.prefix, recordMapping.getPrefix());
        log.info("Mapping created with prefix "+recordMapping.getPrefix());
        MappingModel mappingModel = new MappingModel();
        mappingModel.setRecordMapping(recordMapping);
        mappingModel.setConstant("/some/path", "value");
        fileStore.getDataSetStore(SPEC).setRecordMapping(recordMapping);
        Assert.assertEquals("Should be two files", 2, new File(DIR, SPEC).listFiles().length);
        recordMapping = fileStore.getDataSetStore(SPEC).getRecordMapping(recordDefinition);
        Assert.assertEquals("Should have held constant", "value", recordMapping.getConstant("/some/path"));
    }

    @Test
    public void manipulateStatistics() throws IOException, FileStoreException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC, sampleFile(), null);
        List<Statistics> stats = store.getStatistics();
        Assert.assertEquals("Should be one files", 1, new File(DIR, SPEC).listFiles().length);
        Assert.assertNull("No stats should be here", stats);
        stats = new ArrayList<Statistics>();
        Statistics statistics = new Statistics(new Path("/stat/path"));
        statistics.recordOccurrence();
        statistics.recordValue("booger");
        stats.add(statistics);
        store.setStatistics(stats);
        Assert.assertEquals("Should be two files", 2, new File(DIR, SPEC).listFiles().length);
        stats = fileStore.getDataSetStore(SPEC).getStatistics();
        Assert.assertEquals("Should be one stat", 1, stats.size());
        Assert.assertEquals("Path discrepancy", "/stat/path", stats.get(0).getPath().toString());
    }

    @Test
    public void manipulateDetails() throws IOException, FileStoreException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC, sampleFile(), null);
        SourceDetails sourceDetails = store.getSourceDetails();
        Assert.assertEquals("source details should be empty", "", sourceDetails.get("recordPath"));
        sourceDetails.set("recordPath", "Wingy");
        store.setSourceDetails(sourceDetails);
        sourceDetails = fileStore.getDataSetStore(SPEC).getSourceDetails();
        Assert.assertEquals("source details should be restored", "Wingy", sourceDetails.get("recordPath"));
    }

    @Test
    public void pretendNormalize() throws IOException, FileStoreException, MetadataException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC, sampleFile(), null);
        RecordDefinition recordDefinition = getRecordDefinition();
        RecordMapping recordMapping = store.getRecordMapping(recordDefinition);
        FileStore.MappingOutput mo = store.createMappingOutput(recordMapping, null);
        mo.getDiscardedWriter().write("Hello");
        mo.recordDiscarded();
        mo.recordNormalized();
        mo.recordNormalized();
        Assert.assertEquals("Should be two files", 2, new File(DIR, SPEC).listFiles().length);
        mo.close(false);
        store.setRecordMapping(recordMapping);
        recordMapping = fileStore.getDataSetStore(SPEC).getRecordMapping(recordDefinition);
        Assert.assertEquals("Mapping should contain facts", 1, recordMapping.getRecordsDiscarded());
        Assert.assertEquals("Mapping should contain facts", 2, recordMapping.getRecordsNormalized());
    }

    private RecordDefinition getRecordDefinition() throws IOException, MetadataException {
        return getMetadataModel().getRecordDefinition();
    }

    private MetadataModel getMetadataModel() throws IOException, MetadataException {
        MetadataModelImpl metadataModel = new MetadataModelImpl();
        metadataModel.setRecordDefinitionResources(Arrays.asList("/abm-record-definition.xml"));
        metadataModel.setDefaultPrefix("abm");
        return metadataModel;
    }

    private File sampleFile() throws IOException {
        return new File(getClass().getResource("/sample-input.xml").getFile());
    }

    private InputStream sampleInputStream() throws IOException {
        return getClass().getResource("/sample-input.xml").openStream();
    }

    private void deleteFiles() {
        for (File dir : DIR.listFiles()) {
            if (dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    if (!file.delete()) {
                        throw new RuntimeException("File " + file.getAbsolutePath() + " could not be deleted");
                    }
                }
            }
            if (!dir.delete()) {
                throw new RuntimeException("File " + dir.getAbsolutePath() + " could not be deleted");
            }
        }
    }


}