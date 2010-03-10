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

package eu.europeana.core.database.dao;

import eu.europeana.core.database.DashboardDao;
import eu.europeana.core.database.domain.CarouselItem;
import eu.europeana.core.database.domain.CollectionState;
import eu.europeana.core.database.domain.DashboardLog;
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.EuropeanaId;
import eu.europeana.core.database.domain.ImportFileState;
import eu.europeana.core.database.domain.IndexingQueueEntry;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * This class is an implementation of the DashboardDao using an injected JPA Entity Manager.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

@SuppressWarnings("unchecked")
public class DashboardDaoImpl implements DashboardDao {
    private Logger log = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    @Transactional
    public EuropeanaId fetchEuropeanaId(String europeanaUri) {
        Query query = entityManager.createQuery("select id from EuropeanaId as id where id.europeanaUri = :europeanaUri");
        query.setParameter("europeanaUri", europeanaUri);
        query.setMaxResults(1);
        List<EuropeanaId> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    @Transactional
    public List<DashboardLog> fetchLogEntriesFrom(Long bottomId, int pageSize) {
        Query query = entityManager.createQuery("select log from DashboardLog log where log.id >= :bottomId order by log.id asc");
        query.setParameter("bottomId", bottomId);
        query.setMaxResults(pageSize);
        return (List<DashboardLog>) query.getResultList();
    }

    @Override
    @Transactional
    public List<DashboardLog> fetchLogEntriesTo(Long topId, int pageSize) {
        Query query = entityManager.createQuery("select log from DashboardLog log where log.id <= :bottomId order by log.id desc");
        query.setParameter("bottomId", topId);
        query.setMaxResults(pageSize);
        List<DashboardLog> entries = (List<DashboardLog>) query.getResultList();
        Collections.sort(entries, new Comparator<DashboardLog>() {
            @Override
            public int compare(DashboardLog a, DashboardLog b) {
                if (a.getId() > b.getId()) {
                    return 1;
                }
                else if (a.getId() < b.getId()) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });
        return entries;
    }

    public List<DashboardLog> fetchLogEntries(java.util.Date from, int count) {
        // Query query = entityManager.createQuery("select log from DashboardLog log where log.when > :from order by log.when ");
        Query query = entityManager.createQuery("select log from DashboardLog log where log.time > :from order by log.time ");
        // todo: sjoerd added this. check if correct
        query.setParameter("from", from);
        query.setMaxResults(count);
        return (List<DashboardLog>) query.getResultList();
    }

    @Override
    @Transactional
    public List<EuropeanaCollection> fetchCollections() {
        Query query = entityManager.createQuery("select c from EuropeanaCollection c order by c.name");
        return (List<EuropeanaCollection>) query.getResultList();
    }

    @Override
    @Transactional
    public List<EuropeanaCollection> fetchEnabledCollections() {
        Query query = entityManager.createQuery("select c from EuropeanaCollection c where c.collectionState = :collectionState order by c.name");
        //important because only enabled collections are available in the search engine
        query.setParameter("collectionState", CollectionState.ENABLED);
        return (List<EuropeanaCollection>) query.getResultList();
    }

    @Override
    @Transactional
    public List<EuropeanaCollection> fetchCollections(String prefix) {
        Query query = entityManager.createQuery("select c from EuropeanaCollection c where c.name like :prefix");
        query.setParameter("prefix", prefix + "%");
        return (List<EuropeanaCollection>) query.getResultList();
    }

    @Override
    @Transactional
    public EuropeanaCollection fetchCollection(String collectionName, String collectionFileName, boolean createIfAbsent) {
        Query query;
        List<EuropeanaCollection> collections;
        if (collectionFileName != null) {
            query = entityManager.createQuery("select col from EuropeanaCollection as col where col.fileName = :collectionFileName");
            query.setParameter("collectionFileName", collectionFileName);
            collections = query.getResultList();
            if (collections.size() == 1) {
                return collections.get(0);
            }
        }
        query = entityManager.createQuery("select col from EuropeanaCollection as col where col.name = :collectionName");
        query.setParameter("collectionName", collectionName);
        collections = query.getResultList();
        if (collections.size() == 1) {
            return collections.get(0);
        }
        else if (createIfAbsent) {
            log.info("collection not found, creating: " + collectionName);
            EuropeanaCollection collection = new EuropeanaCollection();
            collection.setName(collectionName);
            collection.setFileName(collectionFileName);
            collection.setFileState(ImportFileState.UPLOADING);
            collection.setCollectionLastModified(new Date());
            entityManager.persist(collection);
            return collection;
        }
        else {
            return null;
        }
    }

    @Override
    @Transactional
    public EuropeanaCollection fetchCollection(Long id) {
        return entityManager.find(EuropeanaCollection.class, id);
    }

    @Override
    @Transactional
    public EuropeanaCollection updateCollection(EuropeanaCollection collection) {
        if (collection.getId() != null) {
            EuropeanaCollection existing = entityManager.find(EuropeanaCollection.class, collection.getId());
            if (existing == null) {
                throw new RuntimeException("cannot find existing collection");
            }
            if (collection.getCollectionState() != existing.getCollectionState()) {
                switch (collection.getCollectionState()) {
                    case QUEUED:
                        addToIndexQueue(collection);
                        break;
                    case EMPTY:
                    case DISABLED:
                        removeFromIndexQueue(collection);
                        break;
                }
            }
            return entityManager.merge(collection);
        }
        else {
            entityManager.persist(collection);
            return collection;
        }
    }

    @Override
    @Transactional
    public EuropeanaCollection prepareForImport(Long collectionId) {
        EuropeanaCollection collection = entityManager.find(EuropeanaCollection.class, collectionId);
        collection.setImportError(null);
        collection.setCollectionLastModified(new Date()); // so that the orphan mechanism works
        collection.setFileState(ImportFileState.IMPORTING);
        return collection;
    }

    @Override
    @Transactional
    public EuropeanaCollection setImportError(Long collectionId, String importError) {
        EuropeanaCollection collection = entityManager.find(EuropeanaCollection.class, collectionId);
        collection.setImportError(importError);
        return collection;
    }

    /**
     * This method is used to disable all collections in the index and remove any indexing collections from the
     * IndexingQueue.
     * <p/>
     * Note: the collections are returned so that the caller can also make delete calls to the lucene index
     */

    @Override
    @Transactional
    public List<EuropeanaCollection> disableAllCollections() {
        // find all collections that are enabled;
        Query collectionQuery = entityManager.createQuery("select coll from EuropeanaCollection as coll where collectionState = :collectionState");
        collectionQuery.setParameter("collectionState", CollectionState.ENABLED);
        List<EuropeanaCollection> enabledCollections = collectionQuery.getResultList();

        // remove all items from the IndexingQueue
        Query indexQueueQuery = entityManager.createQuery("select qi from IndexingQueueEntry as qi");
        List<IndexingQueueEntry> indexingResultList = indexQueueQuery.getResultList();
        for (IndexingQueueEntry queueEntry : indexingResultList) {
            enabledCollections.add(queueEntry.getCollection());
            entityManager.remove(queueEntry);
        }
        // set CollectionState to Disabled for all collections in the list
        for (EuropeanaCollection enabledCollection : enabledCollections) {
            // delete collection from Solr Index
            log.info(enabledCollection.getName());
            enabledCollection.setCollectionState(CollectionState.DISABLED);
        }
        return enabledCollections;
    }


    /**
     * This method is used to enable all collections for indexing and add them to the indexing queue.
     * <p/>
     * Note: probably a good idea to disable all collections first, for certainty.
     */

    @Override
    @Transactional
    public void enableAllCollections() {
        // find imported collections
        Query query = entityManager.createQuery("select coll from EuropeanaCollection as coll where fileState = :fileState");
        query.setParameter("fileState", ImportFileState.IMPORTED);
        List<EuropeanaCollection> resultList = query.getResultList();
        // add collections to the indexing queue
        for (EuropeanaCollection collection : resultList) {
            log.info(collection.getName());
            addToIndexQueue(collection);
        }
    }

    @Override
    @Transactional()
    public EuropeanaId saveEuropeanaId(EuropeanaId detachedId) {
        EuropeanaId persistentId = getEuropeanaId(detachedId);
        Date now = new Date();
        if (persistentId == null) {
            log.debug("creating new Id");
            detachedId.setLastModified(now);
            detachedId.setCreated(now);
            entityManager.persist(detachedId);
            persistentId = detachedId;
        }
        else {
            log.debug("updating Id");
            persistentId.setLastModified(now);
            persistentId.getSocialTags().size();
            persistentId.setOrphan(false);
        }
        return persistentId;
    }

    @Override
    @Transactional(readOnly = true)
    public EuropeanaId getEuropeanaId(EuropeanaId id) {
        Query query = entityManager.createQuery("select id from EuropeanaId as id where id.europeanaUri = :uri and id.collection = :collection");
        query.setParameter("uri", id.getEuropeanaUri());
        query.setParameter("collection", id.getCollection());
        query.setMaxResults(1);
        List<EuropeanaId> result = (List<EuropeanaId>) query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        else {
            return result.get(0);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EuropeanaId> fetchCollectionObjects(EuropeanaCollection collection) {
        Query query = entityManager.createQuery("select id from EuropeanaId as id where id.collection = :collection");
        query.setParameter("collection", collection);
        return (List<EuropeanaId>) query.getResultList();
    }

    @Override
    @Transactional
    public boolean addToIndexQueue(EuropeanaCollection collection) { // todo: only used by this dao itself, so could be private or inlined
        Query query = entityManager.createQuery("select count(id) from EuropeanaId id where id.collection = :collection and id.orphan = false");
        query.setParameter("collection", collection);
        List resultList = query.getResultList();
        if (resultList.isEmpty()) {
            log.info("Collection is unknown.");
            return false;
        }
        Long totalNumberOfRecords = (Long) resultList.get(0);
        IndexingQueueEntry queueEntry = new IndexingQueueEntry(collection);
        queueEntry.setTotalRecords(totalNumberOfRecords.intValue());
        queueEntry.setCreated(new Date());
        queueEntry.setRecordsProcessed(0);
        entityManager.persist(queueEntry);
        return true;
    }

    @Override
    @Transactional
    public void removeFromIndexQueue(EuropeanaCollection collection) {  // todo: only used by this dao itself, so could be private or inlined
        // remove collection to index Queue
        Query query = entityManager.createQuery("select entry from IndexingQueueEntry as entry where entry.collection = :collection");
        query.setParameter("collection", collection);
        List<IndexingQueueEntry> resultList = (List<IndexingQueueEntry>) query.getResultList();
        if (resultList.isEmpty()) {
            log.info("collection not found on indexQueue. ");
        }
        else {
            for (IndexingQueueEntry queueEntry : resultList) {
                entityManager.remove(queueEntry);
            }
        }
    }

    // todo: this function has not been fully tested yet

    @Override
    @Transactional
    public IndexingQueueEntry getEntryForIndexing() {
        Query query = entityManager.createQuery("select entry from IndexingQueueEntry as entry where entry.collection.collectionState <> :collectionState");
        query.setParameter("collectionState", CollectionState.INDEXING);
        List<IndexingQueueEntry> result = (List<IndexingQueueEntry>) query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        else {
            result.get(0).setUpdated(new Date());
            return result.get(0);
        }
    }

    @Override
    @Transactional
    public List<EuropeanaId> getEuropeanaIdsForIndexing(int maxResults, IndexingQueueEntry collection) {
        Long lastId = collection.getLastProcessedRecordId();
        Query query;
        if (lastId != null) {
            query = entityManager.createQuery("select id from EuropeanaId as id where id.id > :lastId and id.collection = :collection and id.orphan = :orphan order by id.id asc");
            query.setParameter("lastId", lastId);
        }
        else {
            query = entityManager.createQuery("select id from EuropeanaId as id where id.collection = :collection and id.orphan = :orphan order by id.id asc");
        }
        query.setParameter("collection", collection.getCollection());
        query.setParameter("orphan", false);
        query.setMaxResults(maxResults);
        List<EuropeanaId> result = (List<EuropeanaId>) query.getResultList();
        for (EuropeanaId id : result) {
            id.getSocialTags().size();
        }
        return result;
    }

    @Override
    @Transactional
    public void saveRecordsIndexed(int indexedRecords, IndexingQueueEntry queueEntry, EuropeanaId lastEuropeanaId) {
        IndexingQueueEntry attached = entityManager.find(IndexingQueueEntry.class, queueEntry.getId());
        Integer recordsIndexed = queueEntry.getRecordsProcessed() + indexedRecords;
        attached.setRecordsProcessed(recordsIndexed);
        attached.setLastProcessedRecordId(lastEuropeanaId.getId());
        log.info(MessageFormat.format("updated indexQueue for queueEntry: {0} ({1}/{2})", queueEntry.getCollection().getName(), recordsIndexed, queueEntry.getTotalRecords()));
    }

    @Override
    @Transactional
    public IndexingQueueEntry startIndexing(IndexingQueueEntry indexingQueueEntry) {
        IndexingQueueEntry attached = entityManager.find(IndexingQueueEntry.class, indexingQueueEntry.getId());
        attached.getCollection().setCollectionState(CollectionState.INDEXING);
        return attached;
    }

    @Override
    @Transactional
    public List<IndexingQueueEntry> fetchQueueEntries() {
        Query indexQuery = entityManager.createQuery("select entry from IndexingQueueEntry as entry");
        List<IndexingQueueEntry> entries = new ArrayList<IndexingQueueEntry>();
        List<IndexingQueueEntry> indexResult = indexQuery.getResultList();
        entries.addAll(indexResult);
        return entries;
    }

    @Override
    @Transactional
    public EuropeanaCollection updateCollectionCounters(Long collectionId) {
        EuropeanaCollection collection = entityManager.find(EuropeanaCollection.class, collectionId);
        markOrphans(collection);
        Query recordCountQuery = entityManager.createQuery("select count(id) from EuropeanaId as id where id.collection = :collection and orphan = false");
        Query orphanCountQuery = entityManager.createQuery("select count(id) from EuropeanaId as id where id.collection = :collection and orphan = true");
        // update recordCount
        recordCountQuery.setParameter("collection", collection);
        Long totalNumberOfRecords = (Long) recordCountQuery.getResultList().get(0);
        collection.setTotalRecords(totalNumberOfRecords.intValue());
        // update orphan count
        orphanCountQuery.setParameter("collection", collection);
        Long totalNumberOfOrphans = (Long) orphanCountQuery.getResultList().get(0);
        collection.setTotalOrphans(totalNumberOfOrphans.intValue());
        return collection;
    }

    /**
     * Find and mark the orphan objects associated with the given collection, by checking for europeanaIds which have not
     * been modified since the collection was re-imported, indicating that they were no longer present.
     * <p/>
     * todo: apparently this method's implementation is in transition, unit tests required
     *
     * @param collection use id and last modified value
     * @return the number of IDs with
     */

    @Transactional
    private int markOrphans(EuropeanaCollection collection) {
        Query orphanQountUpdate = entityManager.createQuery("update EuropeanaId id set orphan = :orphan where collection = :collection and lastModified < :lastmodified");
        orphanQountUpdate.setParameter("collection", collection);
        orphanQountUpdate.setParameter("orphan", true);
        orphanQountUpdate.setParameter("lastmodified", collection.getCollectionLastModified());
        int numberUpdated = orphanQountUpdate.executeUpdate();
        log.info(String.format("Found %d orphans in collection %s", numberUpdated, collection.getName()));
        return numberUpdated;
    }

    @Override
    @Transactional
    public void log(String who, String what) {
        DashboardLog log = new DashboardLog(who, new Date(), what);
        entityManager.persist(log);
    }

    @Transactional
    public boolean removeCarouselItem(CarouselItem carouselItem) {
        Query query = entityManager.createQuery("delete from CarouselItem as item where item.id = :id");
        query.setParameter("id", carouselItem.getId());
        boolean success = query.executeUpdate() == 1;
        if (!success) {
            log.warn("Not there to remove from carousel items: " + carouselItem.getId());
        }
        return success;
    }
}
