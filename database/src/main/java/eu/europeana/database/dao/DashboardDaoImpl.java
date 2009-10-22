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

package eu.europeana.database.dao;

import eu.europeana.database.domain.*;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
	public User fetchUser(String email, String password) {
		Query query = entityManager.createQuery("select u from User as u where u.email like :email");
		query.setParameter("email", email);
		try {
			User user = (User) query.getSingleResult();
			if (user.getHashedPassword().equals(User.hashPassword(password))) {
				return user;
			}
			log.info("Password wrong for: " + email);
		}
		catch (NoResultException e) {
			log.info("Email not found: " + email);
		}
		return null;
	}

	@Transactional
	public void setUserRole(Long userId, Role role) {
		User user = entityManager.find(User.class, userId);
		user.setRole(role);
	}

	@Transactional
	public List<User> fetchUsers(String pattern) {
		Query query = entityManager.createQuery(
				"select u from User as u " +
				"where u.userName like :searchField " +
				"or u.email like :searchField " +
		"or u.firstName like :searchField");
		StringBuilder cleanPattern = new StringBuilder();
		for (int walk = 0; walk < pattern.length(); walk++) {
			if (pattern.charAt(walk) != '%') {
				cleanPattern.append(pattern.charAt(walk));
			}
		}
		cleanPattern.append("%");
		query.setParameter("searchField", cleanPattern.toString());
		return (List<User>) query.getResultList();
	}

	@Transactional
	public void setUserEnabled(Long userId, boolean enabled) {
		User user = entityManager.getReference(User.class, userId);
		user.setEnabled(enabled);
	}

	@Transactional
	public void setUserLanguages(Long userId, String languages) {
		User user = entityManager.getReference(User.class, userId);
		user.setLanguages(languages);
	}

	@Transactional
	public void addMessagekey(String key) {
		MessageKey messageKey = new MessageKey(key);
		entityManager.persist(messageKey);
	}

	@Transactional
	public void removeMessageKey(String key) {
		Query query = entityManager.createQuery("select k from MessageKey k where k.key = :key");
		query.setParameter("key", key);
		try {
			MessageKey messageKey = (MessageKey) query.getSingleResult();
			entityManager.remove(messageKey);
		}
		catch (NoResultException e) {
			log.warn("Unable to remove message key " + key);
		}
	}

	@Transactional
	public List<DashboardLog> fetchLogEntriesFrom(Long topId, int pageSize) {
		Query query = entityManager.createQuery("select log from DashboardLog log where log.id >= :id order by log.id asc");
		query.setParameter("id", topId);
		query.setMaxResults(pageSize);
		return (List<DashboardLog>) query.getResultList();
	}

	@Transactional
	public List<DashboardLog> fetchLogEntriesTo(Long bottomId, int pageSize) {
		Query query = entityManager.createQuery("select log from DashboardLog log where log.id <= :id order by log.id desc");
		query.setParameter("id", bottomId);
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
		Query query = entityManager.createQuery("select log from DashboardLog log where log.when > :from order by log.when ");
		// todo: sjoerd added this. check if correct
		query.setParameter("from", from);
		query.setMaxResults(count);
		return (List<DashboardLog>) query.getResultList();
	}

	@Transactional
	public List<EuropeanaCollection> fetchCollections() {
		Query query = entityManager.createQuery("select c from EuropeanaCollection c");
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
		return entityManager.merge(collection);
	}

	@Transactional
	public EuropeanaCollection prepareForImport(Long collectionId) {
		EuropeanaCollection collection = entityManager.find(EuropeanaCollection.class, collectionId);
		collection.setImportError(null);
		collection.setCollectionLastModified(new Date()); // so that the orphan mechanism works
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

	@Transactional
	public void setUserProjectId(Long userId, String projectId) {
		User user = entityManager.getReference(User.class, userId);
		user.setProjectId(projectId);
	}

	@Transactional
	public void setUserProviderId(Long userId, String providerId) {
		User user = entityManager.getReference(User.class, userId);
		user.setProviderId(providerId);
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
			for (String objectUrl : objectUrls) {
				detachedId.getEuropeanaObjects().add(new EuropeanaObject(detachedId, objectUrl));
			}
			persistentId = detachedId;
		}
		else {
			log.debug("updating Id");
			persistentId.setLastModified(now);
			persistentId.getSocialTags().size();
			persistentId.setOrphan(false);
			persistentId.getEditorPicks().size();
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

	@Transactional(readOnly = true)
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
	public List<EuropeanaObject> getEuropeanaObjectsToCache(int maxResults, CacheingQueueEntry collection) {
		Long lastId = collection.getLastProcessedRecordId();
		Query query;
		if (lastId != null) {
			query = entityManager.createQuery("select id from EuropeanaObject as id where id.id > :lastId and id.europeanaId.collection = :collection order by id.id asc");
			query.setParameter("lastId", lastId);
		}
		else {
			query = entityManager.createQuery("select id from EuropeanaObject as id where id.europeanaId.collection = :collection order by id.id asc");
		}
		query.setParameter("collection", collection.getCollection());
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
	public void removeOrphanObject(String uri) {
		Query query = entityManager.createQuery(
				"select obj from EuropeanaObject as obj " +
				"where obj.europeanaId is null and obj.objectUrl = :objectUrl"
		);
		query.setParameter("objectUrl", uri);
		List<EuropeanaObject> objects = (List<EuropeanaObject>) query.getResultList();
		for (EuropeanaObject object : objects) {
			entityManager.remove(object);
		}
	}

	@Transactional
	public boolean addToCacheQueue(EuropeanaCollection collection) {
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
	public void removeFromCacheQueue(EuropeanaCollection collection) {
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
	public boolean addToIndexQueue(EuropeanaCollection collection) {
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
	public void removeFromIndexQueue(EuropeanaCollection collection) {
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

	@Transactional
	public IndexingQueueEntry getIndexQueueHead() {
		Query query = entityManager.createQuery("select entry from IndexingQueueEntry as entry order by entry.created asc");
		query.setMaxResults(1);
		List<IndexingQueueEntry> result = (List<IndexingQueueEntry>) query.getResultList();
		if (result.isEmpty()) {
			return null;
		}
		else {
			return result.get(0);
		}
	}

	@Transactional
	public IndexingQueueEntry getIndexEntry(IndexingQueueEntry detachedEntry) {
		return entityManager.find(IndexingQueueEntry.class, detachedEntry.getId());
	}

	// todo: this function has not been fully tested yet
	@Transactional
	public IndexingQueueEntry getEntryForIndexing() {
		Query query = entityManager.createQuery("select entry from IndexingQueueEntry as entry where entry.collection.collectionState != :collectionState");
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
	public void finishCaching(CacheingQueueEntry entry) {
		entry.getCollection().setCacheState(CacheState.CACHED);
		removeFromCacheQueue(entry.getCollection());
		updateCollection(entry.getCollection());
	}

	@Transactional
	public void finishIndexing(IndexingQueueEntry indexingQueueEntry) {
		IndexingQueueEntry entry = entityManager.find(IndexingQueueEntry.class, indexingQueueEntry.getId());
		entry.getCollection().setCollectionState(CollectionState.ENABLED);
		entityManager.remove(entry);
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
			id.getEditorPicks().size();
		}
		return result;
	}

	@Transactional
	public void saveRecordsIndexed(int indexedRecords, IndexingQueueEntry queueEntry, EuropeanaId lastId) {
		IndexingQueueEntry attached = entityManager.find(IndexingQueueEntry.class, queueEntry.getId());
		Integer recordsIndexed = queueEntry.getRecordsProcessed() + indexedRecords;
		attached.setRecordsProcessed(recordsIndexed);
		attached.setLastProcessedRecordId(lastId.getId());
		log.info(MessageFormat.format("updated indexQueue for queueEntry: {0} ({1}/{2})", queueEntry.getCollection().getName(), recordsIndexed, queueEntry.getTotalRecords()));
	}

	@Transactional
	public void startIndexing(IndexingQueueEntry indexingQueueEntry) {
		IndexingQueueEntry attached = entityManager.find(IndexingQueueEntry.class, indexingQueueEntry.getId());
		attached.getCollection().setCollectionState(CollectionState.INDEXING);
	}

	@Transactional
	public SavedItem fetchSavedItemById(Long id) {
		Query q = entityManager.createQuery("select st from SavedItem st where st.id = :id");
		q.setParameter("id", id);
		List<SavedItem> savedItems = q.getResultList();
		return savedItems.size() == 1 ? savedItems.get(0) : null;
	}

	@Transactional
	public void removeFromCarousel(SavedItem savedItem) {
		CarouselItem carouselItem = savedItem.getCarouselItem();
		if (carouselItem != null) {
			savedItem = entityManager.getReference(SavedItem.class, savedItem.getId());
			savedItem.setCarouselItem(null);
			entityManager.persist(savedItem);
			carouselItem = entityManager.getReference(CarouselItem.class, carouselItem.getId());
			entityManager.remove(carouselItem);
		}
	}

	@Transactional
	public boolean addCarouselItem(SavedItem savedItem) {
		CarouselItem carouselItem = savedItem.createCarouselItem();
		//        carouselItem.setSavedItem(savedItem);
		savedItem.setCarouselItem(carouselItem);
		entityManager.persist(carouselItem);
		return true;
	}

	@Transactional
	public void removeFromEditorPick(SavedSearch savedSearch) {
		EditorPick editorPick = savedSearch.getEditorPick();
		if (editorPick != null) {
			savedSearch = entityManager.getReference(SavedSearch.class, savedSearch.getId());
			savedSearch.setEditorPick(null);
			entityManager.persist(savedSearch);
			editorPick = entityManager.getReference(EditorPick.class, editorPick.getId());
			entityManager.remove(editorPick);
		}
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
		findOrphans(collection);
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
	public int findOrphans(EuropeanaCollection collection) {
		// find all orphans by query
		//        Query orphanCountQuery = entityManager.createQuery("select id from EuropeanaId as id where collection = :collection and collection.collectionLastModified > id.lastModified");
		//        orphanCountQuery.setParameter("collection", collection);
		//        orphanCountQuery.setMaxResults(1);
		//        // update them all to boolean orphan true
		//        List resultList = orphanCountQuery.getResultList();
		//        log.info(String.format("Found %d orphans in collection %s", resultList.size(), collection.getName()));
		////        for (Object id : resultList) {
		//            EuropeanaId newId = (EuropeanaId) id;
		//            newId.setOrphan(true);
		//        }
		// todo: see if update query is more effecient.
		int numberUpdated = 0;
		Query orphanQountUpdate = entityManager.createQuery("update EuropeanaId id set orphan = :orphan where collection = :collection and lastModified < :lastmodified");
		orphanQountUpdate.setParameter("collection", collection);
		orphanQountUpdate.setParameter("orphan", true);
		orphanQountUpdate.setParameter("lastmodified", collection.getCollectionLastModified());
		numberUpdated = orphanQountUpdate.executeUpdate();
		log.info(String.format("Found %d orphans in collection %s", numberUpdated, collection.getName()));
		return numberUpdated;
		//        return resultList.size();
	}

	@Transactional
	public boolean addSearchTerm(Language language, String term) {
		SearchTerm searchTerm = new SearchTerm();
		searchTerm.setLanguage(language);
		searchTerm.setProposedSearchTerm(term);
		searchTerm.setDate(new Date());
		entityManager.persist(searchTerm);
		return true; // maybe check for existence first?
	}

	@Transactional
	public boolean addSearchTerm(SavedSearch savedSearch) {
		SearchTerm searchTerm = savedSearch.createSearchTerm();
		entityManager.persist(searchTerm);
		return true;
	}

	@Transactional
	public List<String> fetchSearchTerms(Language language) {
		Query query = entityManager.createQuery("select term.proposedSearchTerm from SearchTerm as term where term.language = :language");
		query.setParameter("language", language);
		return (List<String>) query.getResultList();
	}

	@Transactional
	public boolean removeSearchTerm(Language language, String term) {
		Query query = entityManager.createQuery("delete from SearchTerm as term where term.language = :language and term.proposedSearchTerm = :term");
		query.setParameter("term", term);
		query.setParameter("language", language);
		boolean success = query.executeUpdate() == 1;
		if (!success) {
			log.warn("Not there to remove from search terms: " + term);
		}
		return success;
	}

	@Transactional
	public List<SavedItem> fetchSavedItems(Long userId) {
		User user = entityManager.find(User.class, userId);
		user.getSavedItems().size();
		return user.getSavedItems();
	}

	@Transactional
	public List<SavedSearch> fetchSavedSearches(Long userId) {
		User user = entityManager.find(User.class, userId);
		user.getSavedSearches().size();
		return user.getSavedSearches();
	}

	@Transactional
	public SavedSearch fetchSavedSearchById(Long id) {
		Query q = entityManager.createQuery("select st from SavedSearch st where st.id = :id");
		q.setParameter("id", id);
		List<SavedSearch> savedSearches = q.getResultList();
		return savedSearches.size() == 1 ? savedSearches.get(0) : null;
	}

	@Transactional
	public void removeUser(Long userId) {
		User user = entityManager.find(User.class, userId);
		if (user != null) {
			entityManager.remove(user);
		}
	}

	@Transactional
	public User fetchUser(Long userId) {
		return entityManager.find(User.class, userId);
	}

	@Transactional
	public EuropeanaId updateEuropeanaId(Long id, float boostFactor, String solrRecords) {
		EuropeanaId europeanaId = entityManager.find(EuropeanaId.class, id);
		if (europeanaId == null) {
			return null;
		}
		europeanaId.setBoostFactor(boostFactor);
		europeanaId.setSolrRecords(solrRecords);
		europeanaId.getSocialTags().size();
		europeanaId.getEuropeanaObjects().size();
		europeanaId.getEditorPicks().size();
		return europeanaId;
	}

	@Transactional
	public List<Partner> fetchPartners() {
		Query query = entityManager.createQuery("select p from Partner p order by p.sector");
		return (List<Partner>) query.getResultList();
	}

	@Transactional
	public List<Contributor> fetchContributors() {
		Query query = entityManager.createQuery("select c from Contributor c order by c.providerId");
		return (List<Contributor>) query.getResultList();
	}

	@Transactional
	public Partner savePartner(Partner partner) {
		return entityManager.merge(partner);
	}

	@Transactional
	public Contributor saveContributor(Contributor contributor) {
		return entityManager.merge(contributor);
	}

	@Transactional
	public boolean removePartner(Long partnerId) {
		Partner partner = entityManager.find(Partner.class, partnerId);
		if (partner != null) {
			entityManager.remove(partner);
			return true;
		}
		return false;
	}

	@Transactional
	public boolean removeContributor(Long contributorId) {
		Contributor contributor = entityManager.find(Contributor.class, contributorId);
		if (contributor != null) {
			entityManager.remove(contributor);
			return true;
		}
		return false;
	}

	@Transactional
	public void log(String who, String what) {
		DashboardLog log = new DashboardLog(who, new Date(), what);
		entityManager.persist(log);
	}

	@Transactional
	public StaticPage fetchStaticPage(StaticPageType pageType, Language language) {
		Query query = entityManager.createQuery("select sp from StaticPage sp where sp.pageType = :pageType and sp.language = :language");
		query.setParameter("pageType", pageType);
		query.setParameter("language", language);
		try {
			return (StaticPage) query.getSingleResult();
		}
		catch (NoResultException e) {
			StaticPage page = new StaticPage(pageType, language);
			entityManager.persist(page);
			return page;
		}
	}

	@Transactional
	public StaticPage saveStaticPage(Long staticPageId, String content) {
		StaticPage page = entityManager.find(StaticPage.class, staticPageId);
		page.setContent(content);
		return page;
	}

//	@Transactional
//	public boolean removeCarouselItem(Long id) {
//		Query query = entityManager.createQuery("delete from CarouselItem as item where item.id = :id");
//		query.setParameter("id", id);
//		boolean success = query.executeUpdate() == 1;
//		if (!success) {
//			log.warn("Not there to remove from carousel items: " + id);
//		}
//		return success;
//	}

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

    @Transactional
	public CarouselItem createCarouselItem(String europeanaUri, Long savedItemId) {
		// check if this Europeana Id item does exist
		EuropeanaId europeanaId = (EuropeanaId) fetchEuropeanaId(europeanaUri);
		if (europeanaId == null) {
			return null;
		}
        SavedItem savedItem = entityManager.getReference(SavedItem.class, savedItemId);
        CarouselItem carouselItem = savedItem.createCarouselItem();
        carouselItem.setEuropeanaId(europeanaId);
        carouselItem.setSavedItem(savedItem);
		savedItem.setCarouselItem(carouselItem);
		return carouselItem;
	}

    @Transactional
    public Boolean removeCarouselItem(Long carouselItemId) {
        CarouselItem carouselItem = entityManager.getReference(CarouselItem.class, carouselItemId);
        if (carouselItem == null) {
            throw new IllegalArgumentException("Unable to find saved item: " + carouselItemId);
        }
        SavedItem savedItem = entityManager.getReference(SavedItem.class, carouselItem.getSavedItem().getId());
        if (savedItem == null) {
            throw new IllegalArgumentException("Unable to find saved item: " + carouselItemId);
        }
        savedItem.setCarouselItem(null);
        entityManager.remove(carouselItem);
        entityManager.flush();
        return true;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<CarouselItem> fetchCarouselItems() {
        Query q = entityManager.createQuery("select ci from CarouselItem ci");
        List<CarouselItem> results = (List<CarouselItem>) q.getResultList();
        for (CarouselItem item : results) {
            EuropeanaId id = item.getEuropeanaId();
            if (id != null && id.isOrphan()) { // remove null check later
                results.remove(item);
                removeCarouselItem(item.getSavedItem().getId());
            }
        }
        return results;
    }

	/*
	 *  People Are Currently Thinking About, or editor picks
	 */
	@Transactional
	public List<EditorPick> fetchEditorPicksItems() {
		Query query = entityManager.createQuery("select item from EditorPick item");
		return (List<EditorPick>) query.getResultList();
	}

	@Transactional
	public EditorPick createEditorPick(SavedSearch savedSearch) throws Exception {
		EditorPick editorPick = new EditorPick();
		editorPick.setDateSaved(savedSearch.getDateSaved());
		editorPick.setQuery(savedSearch.getQuery());
		editorPick.setUser(savedSearch.getUser());

		SavedSearch savedSearch2 = entityManager.getReference(SavedSearch.class, savedSearch.getId());
		editorPick.setSavedSearch(savedSearch2);
		savedSearch2.setEditorPick(editorPick);
		return editorPick;
	}

}
