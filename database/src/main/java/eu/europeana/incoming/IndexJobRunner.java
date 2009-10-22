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

import eu.europeana.database.dao.DashboardDao;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.IndexingQueueEntry;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class IndexJobRunner {
    private Logger log = Logger.getLogger(getClass());
    private ExecutorService executor = Executors.newCachedThreadPool();
    private DashboardDao dashboardDao;
    private ESEImporter eseImporter;

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public void setEseImporter(ESEImporter eseImporter) {
        this.eseImporter = eseImporter;
    }

    public void runParallel() {
        IndexingQueueEntry entry = dashboardDao.getEntryForIndexing();
        if (entry == null) {
            log.debug("no collection found for indexing");
        }
        else {
            log.info("found collection to index: " + entry.getCollection().getName());
            dashboardDao.startIndexing(entry);
            executor.execute(new IndexJob(entry));
        }
    }

    private class IndexJob implements Runnable {
        private IndexingQueueEntry entry;

        private IndexJob(IndexingQueueEntry entry) {
            this.entry = entry;
        }

        public void run() {
            while (true) {
                IndexingQueueEntry queueEntry = dashboardDao.getIndexEntry(entry);
                EuropeanaCollection collection = queueEntry.getCollection();
                Long time = System.currentTimeMillis();
                ImportFile importFile = new ImportFile(collection.getFileName(), collection.getFileState());
                eseImporter.commenceImport(importFile, queueEntry.getCollection().getId());
            }
        }
    }

}