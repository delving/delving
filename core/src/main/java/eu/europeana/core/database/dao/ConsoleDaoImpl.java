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

import eu.europeana.core.database.ConsoleDao;
import eu.europeana.core.database.domain.CollectionState;
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.EuropeanaId;
import eu.europeana.core.database.domain.SocialTag;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * This class is an implementation of the ConsoleDao using an injected JPA Entity Manager.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

@SuppressWarnings("unchecked")
public class ConsoleDaoImpl implements ConsoleDao {
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
    public List<SocialTag> fetchSocialTags(String europeanaUri) {
        Query query = entityManager.createQuery("select st from SocialTag st where st.europeanaUri = :europeanaUri order by st.tag");
        //important because only enabled collections are available in the search engine
        query.setParameter("europeanaUri", europeanaUri);
        return (List<SocialTag>) query.getResultList();
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
//            if (collection.getCollectionState() != existing.getCollectionState()) {
//                switch (collection.getCollectionState()) {
//                    case QUEUED:
//                        addToIndexQueue(collection);
//                        break;
//                    case EMPTY:
//                    case DISABLED:
//                        removeFromIndexQueue(collection);
//                        break;
//                }
//            }
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

}
