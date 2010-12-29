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
import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.EuropeanaId;
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

    @Transactional(readOnly = true)
    private EuropeanaId getEuropeanaId(EuropeanaId id) {
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
