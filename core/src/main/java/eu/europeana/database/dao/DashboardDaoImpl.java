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

package eu.europeana.database.dao;

import eu.europeana.database.DashboardDao;
import eu.europeana.database.domain.*;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.*;

/**
 * This class is an implementation of the DashboardDao using an injected JPA Entity Manager.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@SuppressWarnings("unchecked")
public class DashboardDaoImpl implements DashboardDao {
    private static final long CACHE_RESTART_MILLIS = 60000L;
    private Logger log = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

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

    @Transactional
    public List<DashboardLog> fetchLogEntriesFrom(Long bottomId, int pageSize) {
        Query query = entityManager.createQuery("select log from DashboardLog log where log.id >= :bottomId order by log.id asc");
        query.setParameter("bottomId", bottomId);
        query.setMaxResults(pageSize);
        return (List<DashboardLog>) query.getResultList();
    }

    @Transactional
    public List<DashboardLog> fetchLogEntriesTo(Long topId, int pageSize) {
        Query query = entityManager.createQuery("select log from DashboardLog log where log.id <= :bottomId order by log.id desc");
        query.setParameter("bottomId", topId);
        query.setMaxResults(pageSize);
        List<DashboardLog> entries = (List<DashboardLog>) query.getResultList();
        Collections.sort(entries, new Comparator<DashboardLog>() {
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

    @Transactional
    public List<EuropeanaCollection> fetchCollections() {
        Query query = entityManager.createQuery("select c from EuropeanaCollection c where c.collectionState = :collectionState order by c.name");
        //important because only enabled collections are available in the search engine
        query.setParameter("collectionState", CollectionState.ENABLED);
        return (List<EuropeanaCollection>) query.getResultList();
    }

    @Transactional
    public List<EuropeanaCollection> fetchCollections(String prefix) {
        Query query = entityManager.createQuery("select c from EuropeanaCollection c where c.name like :prefix");
        query.setParameter("prefix", prefix + "%");
        return (List<EuropeanaCollection>) query.getResultList();
    }

    @Transactional
    public EuropeanaCollection fetchCollectionByName(String name, boolean create) {
        Query query = entityManager.createQuery("select col from EuropeanaCollection as col where col.name = :name");
        query.setParameter("name", name);
        List<EuropeanaCollection> collections = query.getResultList();
        EuropeanaCollection collection;
        if (collections.isEmpty()) {
            if (!create) {
                return null;
            }
            log.info("collection not found, creating: " + name);
            collection = new EuropeanaCollection();
            collection.setName(name.replaceFirst(".xml", ""));
            collection.setFileName(name);
            collection.setFileState(ImportFileState.UPLOADING);
            collection.setCollectionLastModified(new Date());
            entityManager.persist(collection);
        }
        else {
            collection = collections.get(0);
        }
        return collection;
    }

    @Transactional
    public EuropeanaCollection fetchCollectionByFileName(String fileName) {
        Query query = entityManager.createQuery("select col from EuropeanaCollection as col where col.fileName = :fileName");
        query.setParameter("fileName", fileName);
        List<EuropeanaCollection> collections = query.getResultList();
        if (collections.size() != 1) { // todo: potentially dangerous because file names need not be unique!
            return null;
        }
        return collections.get(0);
    }

    @Transactional
    public EuropeanaCollection fetchCollection(Long id) {
        return entityManager.find(EuropeanaCollection.class, id);
    }

    @Transactional
    public EuropeanaCollection updateCollection(EuropeanaCollection collection) {
        if (collection.getId() != null) {
            EuropeanaCollection existing = entityManager.find(EuropeanaCollection.class, collection.getId());
            if (collection.getCacheState() != existing.getCacheState()) {
                switch (collection.getCacheState()) {
                    case EMPTY:
                    case CACHED:
                    case UNCACHED:
                        removeFromCacheQueue(collection);
                        break;
                    case QUEUED:
                        addToCacheQueue(collection);
                        break;
                }
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

    @Transactional
    public EuropeanaCollection prepareForImport(Long collectionId) {
        EuropeanaCollection collection = entityManager.find(EuropeanaCollection.class, collectionId);
        collection.setImportError(null);
        collection.setCollectionLastModified(new Date()); // so that the orphan mechanism works
        collection.setFileState(ImportFileState.IMPORTING);
        return collection;
    }

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

    @Transactional
    public List<EuropeanaCollection> disableAllCollections() {
        // find all collections that are enabled;
        Query collectionQuery = entityManager.createQuery("select coll from EuropeanaCollection as coll where collectionState = :collectionState");
        collectionQuery.setParameter("collectionState", CollectionState.ENABLED);
        List<EuropeanaCollection> enabledCollections = collectionQuery.getResultList();

        // remove all items from the IndexingQueue
        Query indexQueueQuery = entityManager.createQuery("select qi from IndexingQueueEntry as qi");
        List<QueueEntry> indexingResultList = indexQueueQuery.getResultList();
        for (QueueEntry queueEntry : indexingResultList) {
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

    @Transactional()
    public EuropeanaId saveEuropeanaId(EuropeanaId detachedId, Set<String> objectUrls) {
        EuropeanaId persistentId = getEuropeanaId(detachedId);
        Date now = new Date();
        if (persistentId == null) {
            log.debug("creating new Id");
            detachedId.setLastModified(now);
            detachedId.setCreated(now);
            entityManager.persist(detachedId);
            if (objectUrls.size() > 0) {
                for (String objectUrl : objectUrls) {
                    detachedId.getEuropeanaObjects().add(new EuropeanaObject(detachedId, objectUrl));
                }
            }
            persistentId = detachedId;
        }
        else {
            log.debug("updating Id");
            persistentId.setLastModified(now);
            persistentId.getSocialTags().size();
            persistentId.setOrphan(false);
            if (objectUrls.size() > 0) { // fix to speed up reimporting of collections without objects
                log.debug("checking for objectUrls");
                for (String objectUrl : objectUrls) { // if one is not there, add it
                    EuropeanaObject found = persistentId.getEuropeanaObject(objectUrl);
                    if (found == null) {
                        EuropeanaObject object = new EuropeanaObject(persistentId, objectUrl);
                        entityManager.persist(object);
                        persistentId.getEuropeanaObjects().add(object);
                    }
                }
                Iterator<EuropeanaObject> objectWalk = persistentId.getEuropeanaObjects().iterator();
                while (objectWalk.hasNext()) { // if it is no longer there, disconnect it
                    EuropeanaObject object = objectWalk.next();
                    if (!objectUrls.contains(object.getObjectUrl())) {
                        objectWalk.remove();
                        object.setEuropeanaId(null); // just disconnect
                        entityManager.merge(object);
                    }
                }
            }
        }
        return persistentId;
    }

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

	@Transactional
    public CacheingQueueEntry getEntryForCacheing() {
        Query query = entityManager.createQuery("select entry from CacheingQueueEntry as entry where entry.updated is null or entry.updated < :tooOld");
        Date tooOld = new Date(System.currentTimeMillis() - CACHE_RESTART_MILLIS);
        query.setParameter("tooOld", tooOld);
        List<CacheingQueueEntry> result = (List<CacheingQueueEntry>) query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        else {
            result.get(0).setUpdated(new Date());
            return result.get(0);
        }
    }

    @Transactional
    public CacheingQueueEntry saveObjectsCached(int cachedRecords, CacheingQueueEntry queueEntry, EuropeanaObject lastId) {
        CacheingQueueEntry entry = entityManager.find(CacheingQueueEntry.class, queueEntry.getId());
        if (entry == null) {
            return null;
        }
        Integer recordsCached = queueEntry.getRecordsProcessed() + cachedRecords;
        entry.setRecordsProcessed(recordsCached);
        entry.setLastProcessedRecordId(lastId.getId());
        entry.setUpdated(new Date());
        log.info(MessageFormat.format("updated CachingQueue for queueEntry: {0} ({1}/{2})", queueEntry.getCollection().getName(), recordsCached, queueEntry.getTotalRecords()));
        return entry;
    }

    @Transactional
    public List<EuropeanaObject> getEuropeanaObjectsToCache(int maxResults, CacheingQueueEntry queueEntry) {
        Long lastId = queueEntry.getLastProcessedRecordId();
        Query query;
        if (lastId != null) {
            query = entityManager.createQuery("select id from EuropeanaObject as id where id.id > :lastId and id.europeanaId.collection = :collection order by id.id asc");
            query.setParameter("lastId", lastId);
        }
        else {
            query = entityManager.createQuery("select id from EuropeanaObject as id where id.europeanaId.collection = :collection order by id.id asc");
        }
        query.setParameter("collection", queueEntry.getCollection());
        query.setMaxResults(maxResults);
        return (List<EuropeanaObject>) query.getResultList();
    }

    @Transactional
    public List<EuropeanaObject> getEuropeanaObjectOrphans(int maxResults) {
        Query query = entityManager.createQuery(
                "select obj from EuropeanaObject as obj " +
                        "where obj.europeanaId is null order by obj.date asc"
        );
        query.setMaxResults(maxResults);
        return (List<EuropeanaObject>) query.getResultList();
    }

    @Transactional
    public void setObjectCachedError(EuropeanaObject detachedObject) {
        EuropeanaObject object = entityManager.find(EuropeanaObject.class, detachedObject.getId());
        object.setError(true);
    }

    @Transactional
    public int removeOrphanObject(String objectUrl) {
        Query query = entityManager.createQuery(
                "select obj from EuropeanaObject as obj " +
                        "where obj.europeanaId is null and obj.objectUrl = :objectUrl"
        );
        query.setParameter("objectUrl", objectUrl);
        List<EuropeanaObject> objects = (List<EuropeanaObject>) query.getResultList();
        for (EuropeanaObject object : objects) {
            entityManager.remove(object);
        }
        return objects.size();
    }

    @Transactional
    public boolean addToCacheQueue(EuropeanaCollection collection) {  // todo: only used by this dao itself, so could be private or inlined
        Query query = entityManager.createQuery("select count(eo) from EuropeanaObject eo where eo.europeanaId.collection = :collection");
        query.setParameter("collection", collection);
        List resultList = query.getResultList();
        if (resultList.isEmpty()) {
            log.info("Collection is unknown.");
            return false;
        }
        Long totalNumberOfRecords = (Long) resultList.get(0);
        // create a new CacheQueueEntry
        CacheingQueueEntry queueEntry = new CacheingQueueEntry(collection);
        queueEntry.setTotalRecords(totalNumberOfRecords.intValue());
        queueEntry.setRecordsProcessed(0);
        entityManager.persist(queueEntry);
        return true;
    }

    @Transactional
    public void removeFromCacheQueue(EuropeanaCollection collection) { // todo: only used by this dao itself, so could be private or inlined
        // remove collection to cache Queue
        Query query = entityManager.createQuery("select entry from CacheingQueueEntry as entry where entry.collection = :collection");
        query.setParameter("collection", collection);
        List<CacheingQueueEntry> resultList = (List<CacheingQueueEntry>) query.getResultList();
        if (resultList.isEmpty()) {
            log.info("collection not found on cacheQueue. ");
        }
        else {
            for (CacheingQueueEntry queueEntry : resultList) {
                entityManager.remove(queueEntry);
            }
        }
    }

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
        // create a new QueueEntry
        IndexingQueueEntry queueEntry = new IndexingQueueEntry(collection);
        queueEntry.setTotalRecords(totalNumberOfRecords.intValue());
        queueEntry.setCreated(new Date());
        queueEntry.setRecordsProcessed(0);
        entityManager.persist(queueEntry);
        return true;
    }

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

    @Transactional
    public void saveRecordsIndexed(int indexedRecords, IndexingQueueEntry queueEntry, EuropeanaId lastEuropeanaId) {
        IndexingQueueEntry attached = entityManager.find(IndexingQueueEntry.class, queueEntry.getId());
        Integer recordsIndexed = queueEntry.getRecordsProcessed() + indexedRecords;
        attached.setRecordsProcessed(recordsIndexed);
        attached.setLastProcessedRecordId(lastEuropeanaId.getId());
        log.info(MessageFormat.format("updated indexQueue for queueEntry: {0} ({1}/{2})", queueEntry.getCollection().getName(), recordsIndexed, queueEntry.getTotalRecords()));
    }

    @Transactional
    public IndexingQueueEntry startIndexing(IndexingQueueEntry indexingQueueEntry) {
        IndexingQueueEntry attached = entityManager.find(IndexingQueueEntry.class, indexingQueueEntry.getId());
        attached.getCollection().setCollectionState(CollectionState.INDEXING);
        return attached;
    }

    @Transactional
    public List<? extends QueueEntry> fetchQueueEntries() {
        Query indexQuery = entityManager.createQuery("select entry from IndexingQueueEntry as entry");
        Query cacheQuery = entityManager.createQuery("select entry from CacheingQueueEntry as entry");
        List<QueueEntry> entries = new ArrayList<QueueEntry>();
        List<IndexingQueueEntry> indexResult = indexQuery.getResultList();
        List<CacheingQueueEntry> cacheResult = cacheQuery.getResultList();
        entries.addAll(indexResult);
        entries.addAll(cacheResult);
        return entries;
    }

    @Transactional
    public EuropeanaCollection updateCollectionCounters(Long collectionId) {
        EuropeanaCollection collection = entityManager.find(EuropeanaCollection.class, collectionId);
        markOrphans(collection);
        Query recordCountQuery = entityManager.createQuery("select count(id) from EuropeanaId as id where id.collection = :collection and orphan = false");
        Query cacheCountQuery = entityManager.createQuery("select count(eo) from EuropeanaObject as eo where eo.europeanaId.collection = :collection");
        Query orphanCountQuery = entityManager.createQuery("select count(id) from EuropeanaId as id where id.collection = :collection and orphan = true");
        // update recordCount
        recordCountQuery.setParameter("collection", collection);
        Long totalNumberOfRecords = (Long) recordCountQuery.getResultList().get(0);
        collection.setTotalRecords(totalNumberOfRecords.intValue());
        // update cacheCount
        cacheCountQuery.setParameter("collection", collection);
        Long totalNumberOfObjects = (Long) cacheCountQuery.getResultList().get(0);
        collection.setTotalObjects(totalNumberOfObjects.intValue());
        // update orphan count
        orphanCountQuery.setParameter("collection", collection);
        Long totalNumberOfOrphans = (Long) orphanCountQuery.getResultList().get(0);
        collection.setTotalOrphans(totalNumberOfOrphans.intValue());
        return collection;
    }

    @Transactional
    public int markOrphans(EuropeanaCollection collection) {
        int numberUpdated;
        Query orphanQountUpdate = entityManager.createQuery("update EuropeanaId id set orphan = :orphan where collection = :collection and lastModified < :lastmodified");
        orphanQountUpdate.setParameter("collection", collection);
        orphanQountUpdate.setParameter("orphan", true);
        orphanQountUpdate.setParameter("lastmodified", collection.getCollectionLastModified());
        numberUpdated = orphanQountUpdate.executeUpdate();
        log.info(String.format("Found %d orphans in collection %s", numberUpdated, collection.getName()));
        return numberUpdated;
    }

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
