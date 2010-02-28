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

import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.*;
import eu.europeana.core.querymodel.query.QueryProblem;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @author Cesare Corcordia <cesare.concordia@isti.cnr.it>
 */
@SuppressWarnings("unchecked")
public class UserDaoImpl implements UserDao {

    private Logger logger = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    @Transactional
    public User fetchUserByEmail(String email) {
        Query query = entityManager.createQuery("select u from User as u where u.email = :email");
        query.setParameter("email", email);
        try {
            User user = (User) query.getSingleResult();
            user.getSavedItems().size();
            user.getSavedSearches().size();
            user.getSocialTags().size();
            return user;
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public User addUser(User user) {
        logger.info("adding user " + user.getFirstName());
        entityManager.persist(user);
        return user;
    }

    @Override
    @Transactional
    public void removeUser(User user) {
        user = entityManager.merge(user);
        for (SocialTag tag : user.getSocialTags()) {
            entityManager.remove(tag);
        }
        user.getSocialTags().clear();
        entityManager.remove(user);
    }

    @Override
    @Transactional
    public User updateUser(User fresh) {
        if (fresh.getId() != null) {
            if (fresh.getHashedPassword().isEmpty()) {
                User existing = entityManager.find(User.class, fresh.getId());
                fresh.setHashedPassword(existing.getHashedPassword());
            }
            User user = entityManager.merge(fresh);
            user.getSavedItems().size();
            user.getSavedSearches().size();
            user.getSocialTags().size();
            return user;
        }
        else {
            entityManager.persist(fresh);
            return fresh;
        }
    }

    @Override
    @Transactional
    public User addSavedSearch(User user, SavedSearch savedSearch) {
        savedSearch.setDateSaved(new Date());
        savedSearch.setUser(user);
        user.getSavedSearches().add(savedSearch);
        user = entityManager.merge(user);
        return user;
    }

    @Override
    @Transactional
    public List<SavedSearch> fetchSavedSearches(User user) {
        user = entityManager.find(User.class, user.getId());
        user.getSavedSearches().size();
        return user.getSavedSearches();
    }

    @Override
    @Transactional
    public User authenticateUser(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Parameter(s) has null value: email:" + email + ", password:" + password);
        }
        Query query = entityManager.createQuery("select u from User as u where u.email like :email");
        query.setParameter("email", email);
        try {
            User user = (User) query.getSingleResult();
            if (user.getHashedPassword().equals(User.hashPassword(password))) {
                return user;
            }
            logger.info("Password wrong for: " + email);
        }
        catch (NoResultException e) {
            logger.info("Email not found: " + email);
        }
        return null;
    }

    @Override
    @Transactional
    public List<SavedItem> fetchSavedItems(Long userId) {
        User user = entityManager.find(User.class, userId);
        user.getSavedItems().size();
        return user.getSavedItems();
    }

    @Override
    @Transactional
    public SavedItem fetchSavedItemById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter has null value: id:" + id);
        }
        Query q = entityManager.createQuery("select st from SavedItem st where st.id = :id");
        q.setParameter("id", id);
        List<SavedItem> savedItems = q.getResultList();
        return savedItems.size() == 1 ? savedItems.get(0) : null;
    }

    @Transactional
    public List<SavedSearch> fetchSavedSearches(Long userId) {
        return entityManager.find(User.class, userId).getSavedSearches();
    }

    @Override
    @Transactional
    public SavedSearch fetchSavedSearchById(Long savedSearchId) {
        return entityManager.find(SavedSearch.class, savedSearchId);
    }

    @Override
    @Transactional
    public User addSavedItem(User user, SavedItem savedItem, String europeanaUri) {
        EuropeanaId europeanaId = fetchEuropeanaId(europeanaUri);
        if (europeanaId == null) {
            throw new IllegalArgumentException("Unable to find europeana record identified by ");// + europeanaId.getEuropeanaUri());
        }
        savedItem.setDateSaved(new Date());
        savedItem.setEuropeanaId(europeanaId);
        savedItem.setUser(user);
        user.getSavedItems().add(savedItem);
        user = entityManager.merge(user);
        return user;
    }

    @Override
    @Transactional
    public User addSocialTag(User user, SocialTag socialTag) {
        user = entityManager.merge(user);
        Date now = new Date();
        socialTag.setDateSaved(now);
        EuropeanaId europeanaId = fetchEuropeanaId(socialTag.getEuropeanaUri());
        socialTag.setEuropeanaId(europeanaId);
        europeanaId.getSocialTags().add(socialTag);
        socialTag.setUser(user);
        entityManager.persist(socialTag);
        user.getSocialTags().add(socialTag);
        return user;
    }

    @Override
    @Transactional
    public boolean userNameExists(String userName) {
        Query query = entityManager.createQuery("select u from User as u where u.userName = :userName");
        query.setParameter("userName", userName);
        try {
            query.getSingleResult();
            return true;
        }
        catch (NoResultException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public User removeSocialTag(Long socialTagId) {
        SocialTag socialTag = entityManager.find(SocialTag.class, socialTagId);
        User user = socialTag.getUser();
        user.getSocialTags().remove(socialTag);
        socialTag.getEuropeanaId().setLastModified(new Date());
        entityManager.remove(socialTag);
        user.getSavedItems().size();
        user.getSavedSearches().size();
        return user;
    }

    @Override
    @Transactional
    public User removeSavedItem(Long savedItemId) {
        SavedItem savedItem = entityManager.find(SavedItem.class, savedItemId);
        User user = savedItem.getUser();
        user.getSavedItems().remove(savedItem);
        entityManager.remove(savedItem);
        return user;
    }

    @Override
    @Transactional
    public User removeSavedSearch(Long savedSearchId) {
        SavedSearch savedSearch = entityManager.find(SavedSearch.class, savedSearchId);
        User user = savedSearch.getUser();
        user.getSavedSearches().remove(savedSearch);
        entityManager.remove(savedSearch);
        return user;
    }

    @Override
    @Transactional
    public List<User> fetchUsers(String pattern) {
        Query query = entityManager.createQuery(
                "select u from User as u " +
                        "where lower(u.userName) like :searchField " +
                        "or lower(u.email) like :searchField " +
                        "or lower(u.firstName) like :searchField");
        StringBuilder cleanPattern = new StringBuilder();
        for (int walk = 0; walk < pattern.length(); walk++) {
            if (pattern.charAt(walk) != '%') {
                cleanPattern.append(pattern.charAt(walk));
            }
        }
        cleanPattern.append("%");
        query.setParameter("searchField", cleanPattern.toString().toLowerCase());
        return (List<User>) query.getResultList();
    }


    @Override
    @Transactional
    public List<TagCount> getSocialTagCounts(String pattern) {
        Query query = entityManager.createQuery(
                "select count(socialTag.tag) from SocialTag socialtag " +
                        "where socialtag.tag like :pattern " +
                        "group by socialTag.tag"
        );
        StringBuilder cleanPattern = new StringBuilder();
        for (int walk = 0; walk < pattern.length(); walk++) {
            if (pattern.charAt(walk) != '%') {
                cleanPattern.append(Character.toLowerCase(pattern.charAt(walk)));
            }
        }
        cleanPattern.append("%");
        pattern = cleanPattern.toString();
        query.setParameter("pattern", pattern);
        query.setMaxResults(100);
        List<TagCount> tagCountList = (List<TagCount>) query.getResultList();
        Collections.sort(tagCountList);
        return tagCountList;
    }

    @Override
    public EuropeanaId fetchEuropeanaId(String europeanaUri) {
        Query query = entityManager.createQuery("select id from EuropeanaId as id where id.europeanaUri = :uri");
        query.setParameter("uri", europeanaUri);
        return (EuropeanaId) query.getSingleResult();
    }

    @Override
    public QueryProblem whyIsEuropeanaIdNotFound(String europenaUri) {
        QueryProblem queryProblem;
        try {
            EuropeanaId id = fetchEuropeanaId(europenaUri);
            if (id != null && id.isOrphan()) {
                queryProblem = QueryProblem.RECORD_REVOKED;
            }
            else if (id != null && id.getCollection().getCollectionState() != CollectionState.ENABLED) {
                queryProblem = QueryProblem.RECORD_NOT_INDEXED;
            }
            else {
                queryProblem = QueryProblem.RECORD_NOT_FOUND;
            }
        } catch (Exception e) {
            queryProblem = QueryProblem.RECORD_NOT_FOUND;
        }
        return queryProblem;
    }
}