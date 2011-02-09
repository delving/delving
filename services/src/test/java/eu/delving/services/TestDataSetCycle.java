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

package eu.delving.services;

import eu.delving.sip.AccessKey;
import eu.delving.sip.DataSetClient;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.FileType;
import eu.delving.sip.Harvester;
import eu.delving.sip.ProgressListener;
import eu.europeana.core.util.StarterUtil;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class TestDataSetCycle {
    private Logger log = Logger.getLogger(getClass());
    private MockFileStoreFactory factory;
    private File harvestedFile;

    @Before
    public void before() throws FileStoreException {
        factory = new MockFileStoreFactory();
        MockServices.start();
        harvestedFile = new File(StarterUtil.getEuropeanaPath() + "/core/target/TestDataCycleHarvest.xml");
    }

    @After
    public void after() {
        MockServices.stop();
        factory.delete();
    }

    @Test
    public void test() throws IOException, FileStoreException{
        Ear importEar = new Ear("Import");
        factory.getDataSetStore().importFile(MockInput.sampleFile(), importEar);
        Assert.assertTrue("import", importEar.waitUntilFinished());
        DataSetClient client = new DataSetClient(new ClientContext());
        File factsFile = new File(getClass().getResource("/mock-facts.txt").getFile());
        Ear uploadFactsEar = new Ear("UploadFacts");
        client.uploadFile(FileType.FACTS, MockFileStoreFactory.SPEC, factsFile, uploadFactsEar);
        Assert.assertTrue("upload facts", uploadFactsEar.waitUntilFinished());
        Ear uploadSourceEar = new Ear("UploadSource");
        client.uploadFile(FileType.SOURCE, MockFileStoreFactory.SPEC, factory.getDataSetStore().getSourceFile(), uploadSourceEar);
        Assert.assertTrue("upload source", uploadSourceEar.waitUntilFinished());
        Harvester harvester = new Harvester();
        Harvey harvey = new Harvey();
        harvester.perform(harvey);
        Assert.assertTrue("harvest", harvey.waitUntilFinished());
    }

    // ==================

    private class ClientContext implements DataSetClient.Context {
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
        }

        @Override
        public void setList(List<DataSetInfo> list) {
            int index = 0;
            for (DataSetInfo info : list) {
                info(String.format("#%d", index++), info);
            }
        }

        @Override
        public void tellUser(String message) {
            log.info("USER!: "+message);
        }

        @Override
        public void disconnected() {
            log.info("Disconnected!!");
        }

        private void info(String title, DataSetInfo info) {
            log.info(String.format("%s: DataSet(%s) = %s", title, info.spec, info.state));
        }
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
            log.info(name+": Total = "+total);
        }

        @Override
        public boolean setProgress(int progress) {
            log.info(name+": Progress = "+progress);
            return true;
        }

        @Override
        public void finished(boolean success) {
            log.info(name+": Finished "+(success?"Successfully":"Unsuccessfully"));
            this.success = success;
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

    private class Harvey implements Harvester.Harvest {
        private Boolean success;
        private OutputStream outputStream;

        private Harvey() throws FileNotFoundException {
            outputStream = new FileOutputStream(harvestedFile);
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
