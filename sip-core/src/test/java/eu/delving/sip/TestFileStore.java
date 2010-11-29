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

import eu.delving.metadata.MappingModel;
import eu.delving.metadata.MetadataException;
import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.MetadataModelImpl;
import eu.delving.metadata.Path;
import eu.delving.metadata.RecordMapping;
import eu.delving.metadata.SourceDetails;
import eu.delving.metadata.Statistics;
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
    public static final String METADATA_PREFIX = "abm";

    @Before
    public void createStore() throws FileStoreException, IOException, MetadataException {
        if (!TARGET.exists()) {
            throw new RuntimeException("Target directory " + TARGET.getAbsolutePath() + " not found");
        }
        if (DIR.exists()) {
            delete(DIR);
        }
        if (!DIR.mkdirs()) {
            throw new RuntimeException("Unable to create directory " + DIR.getAbsolutePath());
        }
        this.fileStore = new FileStoreImpl(DIR, getMetadataModel());
    }

    @After
    public void deleteStore() {
        if (DIR.exists()) {
            delete(DIR);
        }
    }

    @Test
    public void createDelete() throws IOException, FileStoreException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC);
        Assert.assertFalse(store.hasSource());
        store.importFile(sampleFile(), null);
        Assert.assertTrue(store.hasSource());
        Assert.assertEquals("Should be one file", 1, DIR.listFiles().length);
        Assert.assertEquals("Should be one spec", 1, fileStore.getDataSetStores().size());
        Assert.assertEquals("Should be one file", 1, new File(DIR, SPEC).listFiles().length);
        log.info("Created " + new File(DIR, SPEC).listFiles()[0].getAbsolutePath());
        InputStream inputStream = sampleInputStream();
        InputStream storedStream = fileStore.getDataSetStores().get(SPEC).createXmlInputStream();
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
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC);
        store.importFile(sampleFile(), null);
        Assert.assertEquals("Spec should be the same", SPEC, store.getSpec());
        RecordMapping recordMapping = store.getRecordMapping(METADATA_PREFIX);
        Assert.assertEquals("Prefixes should be the same", METADATA_PREFIX, recordMapping.getPrefix());
        log.info("Mapping created with prefix " + recordMapping.getPrefix());
        MappingModel mappingModel = new MappingModel();
        mappingModel.setRecordMapping(recordMapping);
        mappingModel.setConstant("/some/path", "value");
        fileStore.getDataSetStores().get(SPEC).setRecordMapping(recordMapping);
        Assert.assertEquals("Should be two files", 2, new File(DIR, SPEC).listFiles().length);
        recordMapping = fileStore.getDataSetStores().get(SPEC).getRecordMapping(METADATA_PREFIX);
        Assert.assertEquals("Should have held constant", "value", recordMapping.getConstant("/some/path"));
    }

    @Test
    public void manipulateStatistics() throws IOException, FileStoreException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC);
        store.importFile(sampleFile(), null);
        List<Statistics> stats = store.getStatistics();
        Assert.assertEquals("Should be one files", 1, new File(DIR, SPEC).listFiles().length);
        Assert.assertNull("No stats should be here", stats);
        stats = new ArrayList<Statistics>();
        Statistics statistics = new Statistics(new Path("/stat/path"));
        statistics.recordOccurrence();
        statistics.recordValue("booger");
        statistics.finish();
        stats.add(statistics);
        store.setStatistics(stats);
        Assert.assertEquals("Should be one directory ", 1, new File(DIR, SPEC).listFiles().length);
        stats = fileStore.getDataSetStores().get(SPEC).getStatistics();
        Assert.assertEquals("Should be one stat", 1, stats.size());
        Assert.assertEquals("Path discrepancy", "/stat/path", stats.get(0).getPath().toString());
    }

    @Test
    public void manipulateDetails() throws IOException, FileStoreException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC);
        store.importFile(sampleFile(), null);
        SourceDetails sourceDetails = store.getSourceDetails();
        Assert.assertEquals("source details should be empty", "", sourceDetails.get("recordPath"));
        sourceDetails.set("recordPath", "Wingy");
        store.setSourceDetails(sourceDetails);
        sourceDetails = fileStore.getDataSetStores().get(SPEC).getSourceDetails();
        Assert.assertEquals("source details should be restored", "Wingy", sourceDetails.get("recordPath"));
    }

    @Test
    public void pretendNormalize() throws IOException, FileStoreException, MetadataException {
        FileStore.DataSetStore store = fileStore.createDataSetStore(SPEC);
        store.importFile(sampleFile(), null);
        RecordMapping recordMapping = store.getRecordMapping(METADATA_PREFIX);
        FileStore.MappingOutput mo = store.createMappingOutput(recordMapping, null);
        mo.recordDiscarded();
        mo.recordNormalized();
        mo.recordNormalized();
        Assert.assertEquals("Should be two files", 2, new File(DIR, SPEC).listFiles().length);
        mo.close(false);
        store.setRecordMapping(recordMapping);
        recordMapping = fileStore.getDataSetStores().get(SPEC).getRecordMapping(METADATA_PREFIX);
        Assert.assertEquals("Mapping should contain facts", 1, recordMapping.getRecordsDiscarded());
        Assert.assertEquals("Mapping should contain facts", 2, recordMapping.getRecordsNormalized());
    }

//    private RecordDefinition getRecordDefinition() throws IOException, MetadataException {
//        return getMetadataModel().getRecordDefinition();
//    }
//
    private MetadataModel getMetadataModel() throws IOException, MetadataException {
        MetadataModelImpl metadataModel = new MetadataModelImpl();
        metadataModel.setRecordDefinitionResources(Arrays.asList("/abm-record-definition.xml"));
        metadataModel.setDefaultPrefix(METADATA_PREFIX);
        return metadataModel;
    }

    private File sampleFile() throws IOException {
        return new File(getClass().getResource("/sample-input.xml").getFile());
    }

    private InputStream sampleInputStream() throws IOException {
        return getClass().getResource("/sample-input.xml").openStream();
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                delete(sub);
            }
        }
        if (!file.delete()) {
            throw new RuntimeException(String.format("Unable to delete %s", file.getAbsolutePath()));
        }
    }
}
