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

package eu.europeana.database.migration.outgoing;

import eu.europeana.database.domain.CollectionState;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.IndexingQueueEntry;
import eu.europeana.query.ESERecord;
import eu.europeana.query.EuropeanaQueryException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Export chunks of fresh data from the database to the SOLR search engine located at the
 * URL specified.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DatabaseToSolrIndexer extends AbstractSolrIndexer implements Runnable {
    private ExecutorService executor = Executors.newCachedThreadPool();

    public void run() {
        long time = System.currentTimeMillis();
        log.debug("Looking for imported ids to export to solr");
        IndexingQueueEntry indexingQueueEntry = dashboardDao.getIndexQueueHead();
        if (indexingQueueEntry == null) {
            log.debug("no collection found for indexing.");
            return;
        }
        List<EuropeanaId> newIds = dashboardDao.getEuropeanaIdsForIndexing(chunkSize, indexingQueueEntry);
        if (newIds.isEmpty()) {
            log.info("No new imported ids found. Enabled " + indexingQueueEntry.getCollection().getName());
            dashboardDao.finishIndexing(indexingQueueEntry);
            return;
        }
        if (indexingQueueEntry.getCollection().getCollectionState() != CollectionState.INDEXING) {
            dashboardDao.startIndexing(indexingQueueEntry);
        }
        indexRecords(newIds, time, indexingQueueEntry);
    }

    private void indexRecords(List<EuropeanaId> newIds, long time, IndexingQueueEntry indexingQueueEntry) {
        long start = time;
        log.info(MessageFormat.format("Found {0}/{1} ids to index from {2} in {3}",
                newIds.size(),
                chunkSize,
                indexingQueueEntry.getCollection().getName(),
                System.currentTimeMillis() - start));
        try {
            start = System.currentTimeMillis();
            Map<String, ESERecord> records = new TreeMap<String, ESERecord>();
            for (EuropeanaId id : newIds) {
                records.put(id.getEuropeanaUri(), fetchRecordFromSolr(id.getEuropeanaUri()));
            }
            // todo: where do the records come from?
            String xml = createAddRecordsXML(newIds, records);
            postUpdate(xml);
            log.info("Finished submitting " + newIds.size() + " ids to index in " + (System.currentTimeMillis() - start) + " millis.");
            start = System.currentTimeMillis();
            dashboardDao.saveRecordsIndexed(newIds.size(), indexingQueueEntry, newIds.get(newIds.size() - 1));
            log.info("Database updated in " + (System.currentTimeMillis() - start) + " millis.");
        }
        catch (IOException e) {
            log.error("Unable to submit imported records for indexing!", e);
            // todo: this must somehow stop the process!
        }
        catch (EuropeanaQueryException e) {
            log.error("Unable to find record for indexing!", e);
        }
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
                List<EuropeanaId> newIds = dashboardDao.getEuropeanaIdsForIndexing(chunkSize, queueEntry);
                if (newIds.isEmpty()) {
                    log.info("No new Europeana ids found. Enabled collection: " + queueEntry.getCollection().getName());
                    dashboardDao.finishIndexing(queueEntry);
                    return;
                }
                Long time = System.currentTimeMillis();
                indexRecords(newIds, time, queueEntry);
            }
        }
    }
}