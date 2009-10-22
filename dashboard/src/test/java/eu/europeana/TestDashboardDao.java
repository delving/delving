/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana;

import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.EuropeanaObject;
import eu.europeana.database.domain.IndexingQueueEntry;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/dashboard-application-context.xml", "/database-application-context.xml"})
@Transactional
public class TestDashboardDao {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private DashboardDao dashboardDao;

    @Test
    public void testFindOrphans() {
        EuropeanaCollection testCol = dashboardDao.fetchCollectionByName("TestCollection", true);
        Set<String> objectUrls = new TreeSet<String>();
        // create 10 orphans
        createIds(testCol, objectUrls, 0, false);
        // update the last modified date and update collection
        testCol.setCollectionLastModified(new Date());
        dashboardDao.updateCollection(testCol);
        // create 10 records normal records
        createIds(testCol, objectUrls, 10, false);

        EuropeanaCollection updatedCollection = dashboardDao.updateCollectionCounters(testCol.getId());
        Assert.assertEquals("total number objects", 0, updatedCollection.getTotalObjects().intValue());
        Assert.assertEquals("total number records", 10, updatedCollection.getTotalRecords().intValue());
        Assert.assertEquals("total number of orphans", 10, updatedCollection.getTotalOrphans().intValue());

        int orphans = dashboardDao.findOrphans(testCol);
        Assert.assertEquals("total number of orphans", 10, orphans);

        boolean addedToQueue = dashboardDao.addToIndexQueue(testCol);
        Assert.assertTrue("check if collection is added to the indexing queue", addedToQueue);
        IndexingQueueEntry indexingQueueEntry = dashboardDao.getEntryForIndexing();
        Assert.assertEquals("Check if we are indexing the test collection", testCol.getId(), indexingQueueEntry.getCollection().getId());

        List<EuropeanaId> idsForIndexing = dashboardDao.getEuropeanaIdsForIndexing(100, indexingQueueEntry);
        Assert.assertEquals("See if number of ids to index does not include orphans", 10, idsForIndexing.size());
    }


    @Test
    public void testWithObjects() {
        long start = System.currentTimeMillis();
        EuropeanaCollection collection = dashboardDao.fetchCollectionByName("TestCollection", true);
        runWithObjects(collection);
        log.info("finished first run in: " + (System.currentTimeMillis() - start));
        EuropeanaId iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(0, collection).getEuropeanaUri());
        Assert.assertEquals(2, iddy.getEuropeanaObjects().size());

        start = System.currentTimeMillis();
        runWithObjects(collection);
        log.info("finished second run in: " + (System.currentTimeMillis() - start));
//        start = System.currentTimeMillis();
//        runWithObjects(collection);
//        log.info("finished third run in: " + (System.currentTimeMillis() - start));
        iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(99, collection).getEuropeanaUri());
        Assert.assertEquals(2, iddy.getEuropeanaObjects().size());
        for (EuropeanaObject europeanaObject : iddy.getEuropeanaObjects()) {
            log.info(String.format("object: %s", europeanaObject.getObjectUrl()));
        }
        Assert.assertEquals(iddy.getEuropeanaUri(), createEuropeanaId(99, collection).getEuropeanaUri());
        Assert.assertNotSame(iddy.getEuropeanaUri(), createEuropeanaId(0, collection).getEuropeanaUri());
    }

    @Test
    public void testWithoutObjects() {
        long start = System.currentTimeMillis();
        EuropeanaCollection collection = dashboardDao.fetchCollectionByName("TestCollection", true);
        Set<String> objectUrls = new TreeSet<String>();
        runWithoutObjects(collection, objectUrls);
        log.info("finished first run in: " + (System.currentTimeMillis() - start));
        EuropeanaId iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(0, collection).getEuropeanaUri());
        Assert.assertEquals(0, iddy.getEuropeanaObjects().size());

        start = System.currentTimeMillis();
        runWithoutObjects(collection, objectUrls);
        log.info("finished second run in: " + (System.currentTimeMillis() - start));
        iddy = dashboardDao.fetchEuropeanaId(createEuropeanaId(99, collection).getEuropeanaUri());
        Assert.assertEquals(0, iddy.getEuropeanaObjects().size());
        Assert.assertEquals(iddy.getEuropeanaUri(), createEuropeanaId(99, collection).getEuropeanaUri());
    }

    private void runWithObjects(EuropeanaCollection collection) {
        for (int walk = 0; walk < 100; walk++) {
            EuropeanaId id = createEuropeanaId(walk, collection);
            Set<String> objectList = createObjectList(walk);
            dashboardDao.saveEuropeanaId(id, objectList);
//            log.info(MessageFormat.format("saved europeanaid: ({0}) {1} with {2} objects.", walk, id.getEuropeanaUri(), objectList.size()));
        }
    }

    private void runWithoutObjects(EuropeanaCollection collection, Set<String> objectUrls) {
        for (int walk = 0; walk < 100; walk++) {
            EuropeanaId id = createEuropeanaId(walk, collection);
            dashboardDao.saveEuropeanaId(id, objectUrls);
//            log.info(MessageFormat.format("saved europeanaid: ({0}) {1} with {2} objects.", walk, id.getEuropeanaUri(), objectUrls.size()));
        }
    }

    private void createIds(EuropeanaCollection collection, Set<String> objectUrls, int offSet, Boolean createOrphan) {
        for (int walk = 0; walk < 10; walk++) {
            EuropeanaId id = createEuropeanaId(walk + offSet, collection);
            if (createOrphan) {
                id.setOrphan(true);
            }
            dashboardDao.saveEuropeanaId(id, objectUrls);
            log.info(MessageFormat.format("saved europeanaid: ({0}) {1} with {2} objects.", walk, id.getEuropeanaUri(), objectUrls.size()));
        }
    }


    private Set<String> createObjectList(int walk) {
        Set<String> objectUrls = new TreeSet<String>();
        for (int i = 0; i < 2; i++) {
            String object = new StringBuilder().append("http://host/objecturl/pathy/thing").append(walk).append(i).append(".jpg").toString();
            objectUrls.add(object);
        }
        return objectUrls;
    }

    private EuropeanaId createEuropeanaId(int number, EuropeanaCollection collection) {
        EuropeanaId europeanaId = new EuropeanaId(collection);
        europeanaId.setCreated(new Date());
        europeanaId.setLastModified(new Date());
        europeanaId.setEuropeanaUri("http://www.europeana.eu/resolve/record/92001/79F2A36A85CE59D4343770F4A560EBDF5F207735" + number);
        return europeanaId;
    }

}