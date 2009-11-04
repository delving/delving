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

import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public class StaticInfoDaoImpl implements StaticInfoDao {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Partner> getAllPartnerItems() {
        Query q = entityManager.createQuery("select pi from Partner pi order by pi.sector");
        return (List<Partner>) q.getResultList();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Contributor> getAllContributorItems() {
        Query q = entityManager.createQuery("select con from Contributor con order by con.country");
        return (List<Contributor>) q.getResultList();
    }

    @Transactional
    public void saveContributor(Contributor contributorX) {
        Query query = entityManager.createQuery("select co from Contributor as co where co.providerId = :providerId");
        query.setParameter("providerId", contributorX.getProviderId());
        Contributor contributor = null;
        try {
            contributor = (Contributor) query.getSingleResult();
            contributor.setProviderId(contributorX.getProviderId());
            contributor.setOriginalName(contributorX.getOriginalName());
            contributor.setEnglishName(contributorX.getEnglishName());
            contributor.setAcronym(contributorX.getAcronym());
            contributor.setCountry(contributorX.getCountry());
            contributor.setNumberOfPartners(contributorX.getNumberOfPartners());
            contributor.setUrl(contributorX.getUrl());
        } catch (NoResultException e) {
            if (contributorX.getProviderId() != null) {
                entityManager.persist(contributorX);
            }
        }
    }
    

    @Transactional
    public void savePartner(Partner partnerX) {
        Query query = entityManager.createQuery("select po from Partner as po where po.name = :name");
        query.setParameter("name", partnerX.getName());
        Partner partner = null;
        try {
            partner = (Partner) query.getSingleResult();
            partner.setName(partnerX.getName());
            partner.setUrl(partnerX.getUrl());
            partnerX.setSector(partnerX.getSector());
        } catch (Exception e) {
            if (partnerX.getName() != null) {
                entityManager.persist(partnerX);
            }
        }
    }
    @SuppressWarnings("unchecked")
    @Transactional
    public List<StaticPage> getAllStaticPages() {
        Query query = entityManager.createQuery("select sp from StaticPage as sp");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<MessageKey> getAllTranslationMessages() {
        Query query = entityManager.createQuery("select trans from Translation as trans");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public StaticPage fetchStaticPage (Language language, String pageName) {
        Query query = entityManager.createQuery("select sp from StaticPage as sp where sp.language = :language and sp.pageType = :pageType");
        query.setParameter("language", language);
        query.setParameter("pageType", StaticPageType.get(pageName));
        List results = query.getResultList();
        if (results.size() == 0) {
            query.setParameter("language", Language.EN);
            results = query.getResultList();
            if (results.size() == 0) {
                return new StaticPage(StaticPageType.get(pageName), language);
            }
        }
        return (StaticPage) results.get(0);
    }

    @Transactional
    public void setStaticPage(StaticPageType pageType, Language language, String content) {
        Query query = entityManager.createQuery("select sp from StaticPage sp where sp.pageType = :pageType and sp.language = :language");
        query.setParameter("pageType", pageType);
        query.setParameter("language", language);
        try {
            StaticPage page = (StaticPage)query.getSingleResult();
            page.setContent(content);
        }
        catch (NoResultException e) {
            StaticPage page = new StaticPage(pageType, language);
            page.setContent(content);
            entityManager.persist(page);
        }
    }

    @Transactional
    public User removeCarouselItem(User user, Long savedItemId) {
        // remove carousel item and give back a user
        SavedItem savedItem = fetchSavedItem(user, savedItemId);
        if (savedItem == null) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + savedItemId);
        }
        CarouselItem carouselItem = savedItem.getCarouselItem();
        savedItem.setCarouselItem(null);
        entityManager.remove(carouselItem);
        entityManager.flush();
        user = entityManager.find(User.class, user.getId());
        return user;
    }

    private SavedItem fetchSavedItem(User user, Long savedItemId) {
       // Query q = entityManager.createQuery("select o from SavedItem as o where userid = :userid and :id = id");

         // the previous instruction is incorrect. Maybe should be as follow, but is strange (id is filtered twice in the where clause)

        Query q = entityManager.createQuery("select o from SavedItem as o where o.id  = :userid and :id = id");
        q.setParameter("userid", user.getId());
        q.setParameter("id", savedItemId);
        List results = q.getResultList();
        if (results.size() != 1) {
            return null;
        }
        return (SavedItem) results.get(0);
    }

    @Transactional
    public User removeSearchTerm(User user, Long savedSearchId) {
        // remove carousel item and give back a user
        SavedSearch savedSearch = fetchSavedSearch(user, savedSearchId);
        if (savedSearch == null) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + savedSearchId);
        }
        SearchTerm searchTerm = savedSearch.getSearchTerm();
        savedSearch.setSearchTerm(null);
        entityManager.remove(searchTerm);
        entityManager.flush();
        user = entityManager.find(User.class, user.getId());
        return user;
    }

    private SavedSearch fetchSavedSearch(User user, Long savedSearchId) {
        //Query q = entityManager.createQuery("select o from SavedSearch as o where userid = :userid and :id = id");

        // the previous instruction is incorrect. Maybe should be as follow, but is strange (id is filtered twice in the where clause)

            
        Query q = entityManager.createQuery("select o from SavedSearch as o where o.id = :userid and :id = id");
        q.setParameter("userid", user.getId());
        q.setParameter("id", savedSearchId);
        List results = q.getResultList();
        if (results.size() != 1) {
            return null;
        }
        return (SavedSearch) results.get(0);
    }

    @Transactional
    public CarouselItem addCarouselItem(User user, Long savedItemId) {
//        SavedItem savedItem = fetchSavedItem(user, savedItemId);
        SavedItem savedItem = entityManager.getReference(SavedItem.class, savedItemId);
        if (savedItem == null) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + savedItemId);
        }
        CarouselItem carouselItem = savedItem.createCarouselItem();
        savedItem.setCarouselItem(carouselItem);
        return carouselItem;
    }

    @Transactional
    public User addCarouselItem(User user, SavedItem savedItem) {
        if (savedItem == null) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + savedItem.getId());
        }
        CarouselItem carouselItem = savedItem.createCarouselItem();
        savedItem.setCarouselItem(carouselItem);
        entityManager.persist(carouselItem);
        user = entityManager.merge(user);
        return user;
    }

    @Transactional
    public User addCarouselItem(User user, CarouselItem carouselItem) {
        //carouselItem.setetDateSaved(new Date());
        user = entityManager.merge(user);
        entityManager.persist(carouselItem);
        return user;
    }

    @Transactional
    public User addEditorPick(User user, EditorPick editorPick) {
        user = entityManager.merge(user);
        entityManager.persist(editorPick);
        return user;
    }

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

}