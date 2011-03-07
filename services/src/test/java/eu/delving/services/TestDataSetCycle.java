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

package eu.delving.services;

import eu.delving.metadata.Facts;
import eu.delving.metadata.Hasher;
import eu.delving.metadata.MetadataException;
import eu.delving.metadata.SourceStream;
import eu.delving.sip.AccessKey;
import eu.delving.sip.DataSetClient;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.FileType;
import eu.delving.sip.Harvester;
import eu.delving.sip.ProgressListener;
import eu.europeana.core.util.StarterUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class TestDataSetCycle {
    private static final File FACTS_FILE = new File(TestDataSetCycle.class.getResource("/CDA1A777C398F6373F8DA8C66F1C1FDB__mock-facts.txt").getFile());
    private Logger log = Logger.getLogger(getClass());
    private MockFileStoreFactory factory;
    private HttpClient httpClient = new HttpClient();

    @Before
    public void before() throws FileStoreException {
        factory = new MockFileStoreFactory();
        factory.delete();
        MockServices.start();
    }

    @After
    public void after() {
        MockServices.stop();
    }

    @Test
    public void testHarvestCycle() throws IOException, FileStoreException, MetadataException {
        // import
        Ear importEar = new Ear("Import First Time");
        factory.getDataSetStore().importFile(MockInput.sampleFile(), importEar);
        Assert.assertTrue("import first time", importEar.getResultBoolean());
        Facts facts = Facts.read(new FileInputStream(FACTS_FILE));
        factory.getDataSetStore().setFacts(facts);
        // upload
        DataSetClient client = new DataSetClient(new ClientContext());
        Ear uploadFactsEar = new Ear("UploadFacts");
        client.uploadFile(FileType.FACTS, MockFileStoreFactory.SPEC, FACTS_FILE, uploadFactsEar);
        Assert.assertTrue("upload facts", uploadFactsEar.getResultBoolean());
        Ear uploadSourceEar = new Ear("UploadSource First Time");
        client.uploadFile(FileType.SOURCE, MockFileStoreFactory.SPEC, factory.getDataSetStore().getSourceFile(), uploadSourceEar);
        Assert.assertTrue("upload source first time", uploadSourceEar.getResultBoolean());
        // harvest
        Harvester harvester = new Harvester();
        Harvey harvey = new Harvey("first");
        harvester.perform(harvey);
        Assert.assertTrue("harvest", harvey.waitUntilFinished());
        // import again
        importEar = new Ear("Import Again");
        factory.getDataSetStore().importFile(getHarvestedFile("first"), importEar);
        Assert.assertTrue("import again", harvey.waitUntilFinished());
        // change facts
        facts = factory.getDataSetStore().getFacts();
        SourceStream.adjustPathsForHarvest(facts);
        factory.getDataSetStore().setFacts(facts);
        uploadFactsEar = new Ear("UploadFacts Again");
        client.uploadFile(FileType.FACTS, MockFileStoreFactory.SPEC, factory.getDataSetStore().getFactsFile(), uploadFactsEar);
        Assert.assertTrue("upload facts", uploadFactsEar.getResultBoolean());
        // upload source again
        uploadSourceEar = new Ear("UploadSource Again");
        client.uploadFile(FileType.SOURCE, MockFileStoreFactory.SPEC, factory.getDataSetStore().getSourceFile(), uploadSourceEar);
        Assert.assertTrue("upload source again", uploadSourceEar.getResultBoolean());
        // harvest again
        harvey = new Harvey("again");
        harvester.perform(harvey);
        Assert.assertTrue("harvest again", harvey.waitUntilFinished());
        // compare
        Assert.assertEquals("harvested files different sizes", getHarvestedFile("first").length(), getHarvestedFile("again").length());
        List<String> firstLines = FileUtils.readLines(getHarvestedFile("first"), "UTF-8");
        List<String> againLines = FileUtils.readLines(getHarvestedFile("again"), "UTF-8");
        Predicate predicate = new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return !((String)o).contains("<datestamp>");
            }
        };
        CollectionUtils.filter(firstLines, predicate);
        CollectionUtils.filter(againLines, predicate);
        Assert.assertEquals("lines remaining are different", firstLines.size(), againLines.size());
        for (int walk=0; walk<firstLines.size(); walk++) {
            Assert.assertEquals("Line "+walk+" different", firstLines.get(walk), againLines.get(walk));
        }
    }

    @Test
    public void testSipZipCycle() throws Exception {
        ClientContext clientContext = new ClientContext();
        // import
        Ear importEar = new Ear("Import First Time");
        factory.getDataSetStore().importFile(MockInput.sampleFile(), importEar);
        Assert.assertTrue("import first time", importEar.getResultBoolean());
        Facts facts = Facts.read(new FileInputStream(FACTS_FILE));
        factory.getDataSetStore().setFacts(facts);
        // upload
        DataSetClient client = new DataSetClient(clientContext);
        Ear uploadFactsEar = new Ear("Upload facts first time");
        client.uploadFile(FileType.FACTS, MockFileStoreFactory.SPEC, FACTS_FILE, uploadFactsEar);
        Assert.assertTrue("upload facts first time", uploadFactsEar.getResultBoolean());
        Ear uploadSourceEar = new Ear("Upload source First Time");
        client.uploadFile(FileType.SOURCE, MockFileStoreFactory.SPEC, factory.getDataSetStore().getSourceFile(), uploadSourceEar);
        Assert.assertTrue("upload source first time", uploadSourceEar.getResultBoolean());
        client.setListFetchingEnabled(true); /* run it once */ client.setListFetchingEnabled(false);
        // delete local store
        factory.getDataSetStore().delete();
        // download a new version
        factory.getFileStore().createDataSetStore(MockFileStoreFactory.SPEC);
        Thread.sleep(1000);
        Assert.assertNotNull("data set info missing", clientContext.dataSetInfo);
        Assert.assertEquals(14141L, (long) clientContext.dataSetInfo.recordCount);
        clientContext.dataSetInfo = null;
        HttpMethod method = new GetMethod(String.format(
                "%s/fetch/%s-sip.zip?accessKey=%s",
                clientContext.getServerUrl(),
                MockFileStoreFactory.SPEC,
                clientContext.getAccessKey()
        ));
        httpClient.executeMethod(method);
        factory.getDataSetStore().acceptSipZip(new ZipInputStream(method.getResponseBodyAsStream()), new Ear("Unzip"));
        Assert.assertTrue("Hash is wrong!", Hasher.checkHash(factory.getDataSetStore().getSourceFile()));
        // upload this new version
        uploadFactsEar = new Ear("Upload facts Again");
        client.uploadFile(FileType.FACTS, MockFileStoreFactory.SPEC, factory.getDataSetStore().getFactsFile(), uploadFactsEar);
        Assert.assertTrue("upload facts again", uploadFactsEar.getResultBoolean());
        uploadSourceEar = new Ear("Upload source Again");
        client.uploadFile(FileType.SOURCE, MockFileStoreFactory.SPEC, factory.getDataSetStore().getSourceFile(), uploadSourceEar);
        Assert.assertFalse("upload source again should have been deemed unnecessary", uploadSourceEar.getResultBoolean());
    }

    // ==================

    private class ClientContext implements DataSetClient.Context {

        private DataSetInfo dataSetInfo;

        @Override
        public String getServerUrl() {
            return String.format("http://localhost:%d/services/dataset", MockServices.PORT);
        }

        @Override
        public String getAccessKey() {
            return createAccessKey();
        }

        @Override
        public void setInfo(DataSetInfo dataSetInfo) {
            info("Single", dataSetInfo);
            this.dataSetInfo = dataSetInfo;
        }

        @Override
        public void setList(List<DataSetInfo> list) {
            if (list.size() != 1) {
                throw new RuntimeException("Expected just one!");
            }
            setInfo(list.get(0));
        }

        @Override
        public void tellUser(String message) {
            log.info("USER!: " + message);
        }

        @Override
        public void disconnected() {
            log.info("Disconnected!!");
        }

        private void info(String title, DataSetInfo info) {
            log.info(String.format("%s: DataSet(%s) = %s", title, info.spec, info.state));
        }
    }

    private File getHarvestedFile(String name) {
        return new File(StarterUtil.getEuropeanaPath() + "/core/target/TestDataCycle-" + name + ".xml");
    }

    private String createAccessKey() {
        AccessKey accessKey = new AccessKey();
        accessKey.setServicesPassword("something");
        return accessKey.createKey("testdatacycle");
    }

    private class Ear implements ProgressListener {
        private String name;
        private Boolean success;

        private Ear(String name) {
            this.name = name;
        }

        @Override
        public void setTotal(int total) {
            log.info(name + ": Total = " + total);
        }

        @Override
        public boolean setProgress(int progress) {
            log.info(name + ": Progress = " + progress);
            return true;
        }

        @Override
        public void finished(boolean success) {
            log.info(name + ": Finished " + (success ? "Successfully" : "Unsuccessfully"));
            this.success = success;
        }

        public boolean getResultBoolean() {
            while (success == null) {
                try {
                    Thread.sleep(300);
                }
                catch (InterruptedException e) {
                    System.exit(1);
                }
            }
            return success;
        }
    }

    private class Harvey implements Harvester.Harvest {
        private Boolean success;
        private OutputStream outputStream;

        private Harvey(String name) throws FileNotFoundException {
            outputStream = new FileOutputStream(getHarvestedFile(name));
        }

        @Override
        public String getUrl() {
            return String.format("http://localhost:%d/services/oai-pmh", MockServices.PORT);
        }

        @Override
        public String getMetadataPrefix() {
            return "raw";
        }

        @Override
        public String getSpec() {
            return MockFileStoreFactory.SPEC;
        }

        @Override
        public String getAccessKey() {
            return createAccessKey();
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public void success() {
            success = true;
            try {
                outputStream.close();
            }
            catch (IOException e) {
                log.error("closing", e);
            }
        }

        @Override
        public void failure(Exception e) {
            log.error("Problem harvesting", e);
            success = false;
        }

        public boolean waitUntilFinished() {
            while (success == null) {
                try {
                    Thread.sleep(300);
                }
                catch (InterruptedException e) {
                    System.exit(1);
                }
            }
            return success;
        }
    }
}
