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

package eu.europeana.incoming;

import eu.europeana.cache.DigitalObjectCache;
import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.CacheState;
import eu.europeana.database.domain.CacheingQueueEntry;
import eu.europeana.database.domain.EuropeanaObject;
import org.apache.log4j.Logger;

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
    private DashboardDao dashboardDao;
    private DigitalObjectCache digitalObjectCache;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public void setDigitalObjectCache(DigitalObjectCache digitalObjectCache) {
        this.digitalObjectCache = digitalObjectCache;
    }

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

        private CacheJob(CacheingQueueEntry entry) {
            this.entry = entry;
        }

        public void run() {
            while (true) {
                List<EuropeanaObject> newObjects = dashboardDao.getEuropeanaObjectsToCache(chunkSize, entry);
                if (newObjects.isEmpty()) {
                    log.debug("No new objects found for "+entry.getCollection());
                    dashboardDao.finishCaching(entry);
                    return;
                }
                log.info("Found " + newObjects.size() + "/" + chunkSize + " objects to cache from " + entry.getCollection());
                try {
                    long time = System.currentTimeMillis();
                    for (EuropeanaObject object : newObjects) {
                        try {
                            if (!digitalObjectCache.cache(object.getObjectUrl())) {
                                dashboardDao.setObjectCachedError(object);
                            }
                        }
                        catch (IOException e) {
                            log.warn("Unable to cache " + object, e);
                            dashboardDao.setObjectCachedError(object);
                        }
                    }
                    log.info("Finished cacheing " + newObjects.size() + " for " + entry.getCollection() + "  in " + (System.currentTimeMillis() - time) + " millis.");
                    entry = dashboardDao.saveObjectsCached(newObjects.size(), entry, newObjects.get(newObjects.size() - 1));
                    if (entry == null) {
                        break;
                    }
                }
                catch (Exception e) {
                    log.error("Unable to submit imported records for caching! "+entry, e);
                    entry.getCollection().setCacheState(CacheState.UNCACHED);
                    dashboardDao.removeFromCacheQueue(entry.getCollection());
                    dashboardDao.updateCollection(entry.getCollection());
                    return;
                }
            }
        }
    }
}