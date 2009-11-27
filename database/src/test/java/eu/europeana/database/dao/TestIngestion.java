/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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
package eu.europeana.database.dao;

import eu.europeana.database.DashboardDao;
import eu.europeana.database.dao.fixture.IngestionFixture;
import eu.europeana.database.domain.CacheState;
import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.ImportFileState;
import eu.europeana.database.domain.QueueEntry;
import eu.europeana.incoming.ImportFile;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import org.apache.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * This class tests the process of ingesting and indexing metadata
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/database-application-context.xml",
        "/test-application-context.xml"
})
public class TestIngestion {
    private static Logger log = Logger.getLogger(TestIngestion.class);

    @Autowired
    private IngestionFixture ingestionFixture;

    @Autowired
    private DashboardDao dashboardDao;

    private static EuropeanaCollection collection;
    private static ImportFile importFile;

    @Before
    public void init() throws Exception {
        if (collection == null) {                                                                          
            ingestionFixture.deleteImportRepository();
            log.info("=== initializing test ingestion ===");
            List<ImportFile> importFiles = ingestionFixture.getImportRepository().getAllFiles();
            assertEquals(1, importFiles.size());
            importFile = importFiles.get(0);
            log.info(importFile);
            collection = dashboardDao.fetchCollectionByName(importFile.deriveCollectionName(), true);
            log.info(collection.getName());
            ingestionFixture.startSolr();
        }
    }

    @After
    public void cleanup() throws Exception {
//        ingestionFixture.stopSolr();
    }

    @Test
    public void indexStateChange() {
        collection.setCollectionState(CollectionState.QUEUED);
        collection = dashboardDao.updateCollection(collection);
        assertEquals(CollectionState.QUEUED, collection.getCollectionState());
        List<? extends QueueEntry> queue = dashboardDao.fetchQueueEntries();
        assertEquals(1, queue.size());
    }

    @Test
    public void doImport() throws IOException, InterruptedException {
        importFile = ingestionFixture.getESEImporter().commenceImport(importFile, collection.getId());
        assertEquals(ImportFileState.IMPORTING, importFile.getState());
        collection = dashboardDao.fetchCollection(collection.getId());
        assertTrue(collection.getCollectionLastModified().compareTo(new Date()) < 0);
        do {
            log.info("Importing..");
            if (ingestionFixture.isSolrProblem()) {
                fail("Solr problem");
            }
            Thread.sleep(500);
            collection = dashboardDao.fetchCollection(collection.getId());
        }
        while (collection.getFileState() == ImportFileState.IMPORTING);
        // todo: test to see if stuff is in the index!
    }

    @Test
    public void cacheStateChange() {
        collection.setCacheState(CacheState.QUEUED);
        collection = dashboardDao.updateCollection(collection);
        assertEquals(CacheState.QUEUED, collection.getCacheState());
        List<? extends QueueEntry> queue = dashboardDao.fetchQueueEntries();
        assertEquals(1, queue.size());
    }
}