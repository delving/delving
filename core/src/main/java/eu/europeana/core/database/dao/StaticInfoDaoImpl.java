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

import eu.europeana.core.database.StaticInfoDao;
import eu.europeana.core.database.domain.*;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */
public class StaticInfoDaoImpl implements StaticInfoDao {
    private Logger log = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    @Transactional
    public User removeCarouselItemFromSavedItem(Long savedItemId) {
        SavedItem savedItem = entityManager.find(SavedItem.class, savedItemId);
        CarouselItem carouselItem = savedItem.getCarouselItem();
        savedItem.setCarouselItem(null);
        entityManager.remove(carouselItem);
        return savedItem.getUser();
    }

    @Override
    @Transactional
    public Boolean removeCarouselItem(Long carouselItemId) {
        CarouselItem carouselItem = entityManager.getReference(CarouselItem.class, carouselItemId);
        if (carouselItem == null) {
            throw new IllegalArgumentException("Unable to find saved item: " + carouselItemId);
        }
        SavedItem savedItem = carouselItem.getSavedItem();
        savedItem.setCarouselItem(null);
        EuropeanaId europeanaId = carouselItem.getEuropeanaId();
        europeanaId.getCarouselItems().remove(carouselItem);
        entityManager.remove(carouselItem);
        return true;
    }

    @Override
    @Transactional
    public User removeSearchTerm(Long savedSearchId) {
        SavedSearch savedSearch = entityManager.find(SavedSearch.class, savedSearchId);
        SearchTerm searchTerm = savedSearch.getSearchTerm();
        savedSearch.setSearchTerm(null);
        entityManager.remove(searchTerm);
        return savedSearch.getUser();
    }

    @Override
    @Transactional
    public SearchTerm addSearchTerm(Long savedSearchId) {
        SavedSearch savedSearch = entityManager.getReference(SavedSearch.class, savedSearchId);
        if (savedSearch == null) {
            throw new IllegalArgumentException("The user doesn't own the object. user:  object: " + savedSearchId);
        }
        SearchTerm searchTerm = savedSearch.createSearchTerm();
        savedSearch.setSearchTerm(searchTerm);
        return searchTerm;
    }

    @Override
    @Transactional
    public boolean addSearchTerm(Language language, String term) {
        SearchTerm searchTerm = new SearchTerm();
        searchTerm.setLanguage(language);
        searchTerm.setProposedSearchTerm(term);
        searchTerm.setDate(new Date());
        entityManager.persist(searchTerm);
        return true; // maybe check for existence first?
    }

    @Override
    @Transactional
    public boolean addSearchTerm(SavedSearch savedSearch) {
        SearchTerm searchTerm = savedSearch.createSearchTerm();
        entityManager.persist(searchTerm);
        return true;
    }

    @Override
    @Transactional
    public boolean removeSearchTerm(Language language, String term) {
        // todo remove back reference to saved item
        Query query = entityManager.createQuery("delete from SearchTerm as term where term.language = :language and term.proposedSearchTerm = :term");
        query.setParameter("term", term);
        query.setParameter("language", language);
        boolean success = query.executeUpdate() == 1;
        if (!success) {
            log.warn("Not there to remove from search terms: " + term);
        }
        return success;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<String> fetchSearchTerms(Language language) {
        Query query = entityManager.createQuery("select term.proposedSearchTerm from SearchTerm as term where term.language = :language");
        query.setParameter("language", language);
        return (List<String>) query.getResultList();
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<CarouselItem> fetchCarouselItems() {
        Query q = entityManager.createQuery("select ci from CarouselItem ci");
        List<CarouselItem> results = (List<CarouselItem>) q.getResultList();
        Iterator<CarouselItem> walk = results.iterator();
        while (walk.hasNext()) {
            CarouselItem item = walk.next();
            EuropeanaId id = item.getEuropeanaId();
            if (id != null && id.isOrphan()) { // remove null check later
                walk.remove();
            }
        }
        return results;
    }

    @Override
    @Transactional
    public CarouselItem createCarouselItem(Long savedItemId) {
        SavedItem savedItem = entityManager.find(SavedItem.class, savedItemId);
        // create the carouselItem
        CarouselItem carouselItem = new CarouselItem();
        carouselItem.setEuropeanaUri(savedItem.getEuropeanaId().getEuropeanaUri());
        carouselItem.setTitle(savedItem.getTitle());
        carouselItem.setCreator(savedItem.getAuthor());
        carouselItem.setType(savedItem.getDocType());
        carouselItem.setThumbnail(savedItem.getEuropeanaObject());
        carouselItem.setLanguage(savedItem.getLanguage());
        // add the relations
        carouselItem.setSavedItem(savedItem);
        carouselItem.setEuropeanaId(savedItem.getEuropeanaId());
        savedItem.getEuropeanaId().getCarouselItems().add(carouselItem);
        savedItem.setCarouselItem(carouselItem);
        entityManager.persist(carouselItem);
        return carouselItem;
    }

    @Override
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

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<SearchTerm> getAllSearchTerms() {
        Query q = entityManager.createQuery("select st from SearchTerm st");
        return (List<SearchTerm>) q.getResultList();
    }
}