/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.incoming;

import eu.europeana.cache.DigitalObjectCache;
import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.CacheState;
import eu.europeana.database.domain.CacheingQueueEntry;
import eu.europeana.database.domain.EuropeanaObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CacheBuilder implements Runnable {
    private Logger log = Logger.getLogger(getClass());
    private int chunkSize = 10;
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private DashboardDao dashboardDao;

    @Autowired
    private DigitalObjectCache digitalObjectCache;

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void run() {
        CacheingQueueEntry entry = dashboardDao.getEntryForCacheing();
        if (entry == null) {
            log.debug("no entry found for cacheing.");
        }
        else {
            log.info("found collection to cache: " + entry.getCollection());
            entry.getCollection().setCacheState(CacheState.CACHEING);
            dashboardDao.updateCollection(entry.getCollection());
            executor.execute(new CacheJob(entry));
        }
    }

    private class CacheJob implements Runnable {
        private CacheingQueueEntry entry;
        private Histogram histogram;

        private CacheJob(CacheingQueueEntry entry) {
            this.entry = entry;
            this.histogram = new Histogram(entry.getCollection().getName());
        }

        public void run() {
            while (true) {
                List<EuropeanaObject> newObjects = dashboardDao.getEuropeanaObjectsToCache(chunkSize, entry);
                if (newObjects.isEmpty()) {
                    log.debug("No new objects found for " + entry.getCollection());
                    entry.getCollection().setCacheState(CacheState.CACHED);
                    dashboardDao.updateCollection(entry.getCollection());
                    break;
                }
                log.info("Found " + newObjects.size() + "/" + chunkSize + " objects to cache from " + entry.getCollection());
                try {
                    for (EuropeanaObject object : newObjects) {
                        try {
                            long before = System.currentTimeMillis();
                            if (!digitalObjectCache.cache(object.getObjectUrl())) {
                                dashboardDao.setObjectCachedError(object);
                            }
                            long after = System.currentTimeMillis();
                            histogram.recordDuration(after - before);
                        }
                        catch (IOException e) {
                            log.warn("Unable to cache " + object, e);
                            dashboardDao.setObjectCachedError(object);
                        }
                    }
                    entry = dashboardDao.saveObjectsCached(newObjects.size(), entry, newObjects.get(newObjects.size() - 1));
                    if (entry == null) {
                        break;
                    }
                }
                catch (Exception e) {
                    log.error("Unable to submit imported records for caching! " + entry, e);
                    entry.getCollection().setCacheState(CacheState.UNCACHED);
                    dashboardDao.updateCollection(entry.getCollection());
                    break;
                }
            }
            log.info("Cacheing Performance\n" + histogram);
        }
    }

}