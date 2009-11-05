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

import eu.europeana.database.UserDao;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.database.integration.TagCount;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
public class UserDaoImpl implements UserDao {

    private Logger logger = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public User fetchUserByEmail(String email) {
        Query query = entityManager.createQuery("select u from User as u where u.email = :email");
        query.setParameter("email", email);
        try {
            User user = (User) query.getSingleResult();
            // make sure the collections are greedily loaded
            user.getSavedItems().size();
            user.getSavedSearches().size();
            user.getSocialTags().size();
            return user;
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public User addUser(User user) {
        logger.info("adding user " + user.getFirstName());
        entityManager.persist(user);
        return user;
    }

    @Transactional
    public void removeUser(User user) {
        user = entityManager.merge(user);
        for (SocialTag tag : user.getSocialTags()) {
            entityManager.remove(tag);
        }
        user.getSocialTags().clear();
        entityManager.remove(user);
    }

    @Transactional
    public void updateUser(User user) {
        entityManager.merge(user);
    }

    @Transactional
    public User refreshUser(User user) {
        user = entityManager.find(User.class, user.getId());
        user.getSavedItems().size();
        user.getSavedSearches().size();
        user.getSocialTags().size();
        return user;
    }

    @Transactional
    public User addSavedSearch(User user, SavedSearch savedSearch) {
        savedSearch.setDateSaved(new Date());
        savedSearch.setUser(user);
        user.getSavedSearches().add(savedSearch);
        user = entityManager.merge(user);
        return user;
    }


    @Transactional
    public User addSavedItem(User user, SavedItem savedItem, String europeanaUri) {
        EuropeanaId europeanaId = fetchEuropeanaId(europeanaUri);
        if (europeanaId == null) {
            throw new IllegalArgumentException("Unable to find europeana record identified by ");// + europeanaId.getEuropeanaUri());
        }
        savedItem.setDateSaved(new Date());
        savedItem.setEuropeanaId(europeanaId);
        savedItem.setUser(user);
//        entityManager.persist(savedItem);
        //        user = merge(user);
        user.getSavedItems().add(savedItem);
        user = entityManager.merge(user);
        return user;
    }

    @Transactional
    public User addSocialTag(User user, SocialTag socialTag) {
        user = entityManager.merge(user);
        //        user = merge(user);
        Date now = new Date();
        socialTag.setDateSaved(now);
        EuropeanaId europeanaId = fetchEuropeanaId(socialTag.getEuropeanaUri());
        socialTag.setEuropeanaId(europeanaId);
        socialTag.setUser(user);
        entityManager.persist(socialTag);
        user.getSocialTags().add(socialTag);
        return user;
    }

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

    @Transactional
    public User removeSocialTag(User user, Long id) {
        Query q = entityManager.createQuery("select o from SocialTag  as o where user = :user and :id = id");
        q.setParameter("user", user);
        q.setParameter("id", id);
        List objects = q.getResultList();
        if (objects.size() != 1) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + id);
        }
        Object object = objects.get(0);
        SocialTag socialTag = (SocialTag) object;
        socialTag.getEuropeanaId().setLastModified(new Date());
        entityManager.remove(object);
        entityManager.flush();
        user = entityManager.find(User.class, user.getId());
        user.getSavedSearches().size();
        return user;
    }

    @Transactional
    public User removeSavedItems(User user, Long id) {
        Query q = entityManager.createQuery("select o from SavedItem  as o where user = :user and :id = id");
        q.setParameter("user", user.getId());
        q.setParameter("id", id);
        List objects = q.getResultList();
        if (objects.size() != 1) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + id);
        }
        Object object = objects.get(0);

        entityManager.remove(object);
        entityManager.flush();
        user = entityManager.find(User.class, user.getId());
        user.getSavedItems().size();
        return user;
    }

    @Transactional
    public User removeSavedSearch(User user, Long id) {
        Query q = entityManager.createQuery("select o from SavedSearch  as o where user = :user and :id = id");
        q.setParameter("user", user.getId());
        q.setParameter("id", id);
        List objects = q.getResultList();
        if (objects.size() != 1) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + id);
        }
        Object object = objects.get(0);
        entityManager.remove(object);
        entityManager.flush();
        user = entityManager.find(User.class, user.getId());
        user.getSavedSearches().size();
        return user;
    }

    //    public Object findObject(User user, Class<?> clazz, Long id) {
    //        Query q = entityManager.createQuery("select o from ")
    //    }
  /*
    @Transactional
    public User remove(User user, Class<?> clazz, Long id) {
        Query q = entityManager.createQuery("select o from " + clazz.getSimpleName() + " as o where userid = :userid and :id = id");
        q.setParameter("userid", user.getId());
        q.setParameter("id", id);
        List objects = q.getResultList();
        if (objects.size() != 1) {
            throw new IllegalArgumentException("The user doesn't own the object. user: " + user.getId() + ", object: " + id);
        }
        Object object = objects.get(0);
        if (clazz == SocialTag.class) {
            SocialTag socialTag = (SocialTag) object;
            socialTag.getEuropeanaId().setLastModified(new Date());
        }
        entityManager.remove(object);
        entityManager.flush();
        user = entityManager.find(User.class, user.getId());
        user.getSavedItems().size();
        user.getSavedSearches().size();
        user.getSocialTags().size();
        return user;
    }


    private SavedItem fetchSavedItem(User user, Long savedItemId) {
        Query q = entityManager.createQuery("select o from SavedItem as o where userid = :userid and :id = id");
        q.setParameter("userid", user.getId());
        q.setParameter("id", savedItemId);
        List results = q.getResultList();
        if (results.size() != 1) {
            return null;
        }
        return (SavedItem) results.get(0);
    }

    private SavedSearch fetchSavedSearch(User user, Long savedSearchId) {
        Query q = entityManager.createQuery("select o from SavedSearch as o where userid = :userid and :id = id");
        q.setParameter("userid", user.getId());
        q.setParameter("id", savedSearchId);
        List results = q.getResultList();
        if (results.size() != 1) {
            return null;
        }
        return (SavedSearch) results.get(0);
    }
         */
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
    public User fetchUserWhoPickedCarouselItem(String europeanaUri) {
        Query query = entityManager.createQuery(
                "select users from User users, CarouselItem ci, IN(users.savedItems) si, IN(si.europeanaId) ei " +
                        "where ei.europeanaUri = ci.europeanaUri " +
                        "and si.carouselItem = ci " +
                        "and ei.europeanaUri = :europeanaUri");
        query.setParameter("europeanaUri", europeanaUri);
        List<User> users = query.getResultList();

        if (users.isEmpty())
            return null;

        // multiple users have picked it up
        if (users.size() > 1)
            throw new RuntimeException("Illegal state exception: multiple users managed to pick the same carousel item for europeana uri " + europeanaUri);

        return users.get(0);
    }


    public User fetchUserWhoPickedEditorPick(String aQuery) {
        Query query = entityManager.createQuery(
                "select users from User users, IN(users.savedSearches) si, IN(si.editorPick) epick " +
                        "where epick.query = :query ");
        query.setParameter("query", aQuery);
        List<User> users = query.getResultList();

        if (users.isEmpty())
            return null;

        // multiple users have picked it up
        if (users.size() > 1)
            throw new RuntimeException("Illegal state exception: multiple users managed to pick the same query " + aQuery);

        return users.get(0);
    }

    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        User user = entityManager.getReference(User.class, userId);
        user.setEnabled(enabled);
    }

    @Transactional
    public void setUserToAdministrator(Long userId, boolean administrator) {
        User user = entityManager.getReference(User.class, userId);
        if (administrator) {
            user.setRole(Role.ROLE_ADMINISTRATOR);
        }
        else {
            user.setRole(Role.ROLE_USER);
        }
    }

    @Transactional
    public void markAsViewed(String europeanaUri) {
        Query query = entityManager.createQuery("select id from EuropeanaId as id where id.europeanaUri = :uri");
        query.setParameter("uri", europeanaUri);
        EuropeanaId europeanaId = (EuropeanaId) query.getSingleResult();
        europeanaId.setLastViewed(new Date());
        europeanaId.setTimesViewed(europeanaId.getTimesViewed() + 1);
    }

    @Transactional
    public List<TagCount> getSocialTagCounts(String pattern) {
        Query query = entityManager.createQuery(
                "select new eu.europeana.database.integration.TagCount(socialTag.tag, count(socialTag.tag)) from SocialTag socialtag " +
                        "where socialtag.tag like :pattern " +
                        "group by socialTag.tag"
        );
        StringBuilder cleanPattern = new StringBuilder();
        for (int walk = 0; walk < pattern.length(); walk++) {
            if (pattern.charAt(walk) != '%') {
                cleanPattern.append(pattern.charAt(walk));
            }
        }
        cleanPattern.append("%");
        query.setParameter("pattern", cleanPattern.toString());
        query.setMaxResults(100);
        List<TagCount> tagCountList = (List<TagCount>) query.getResultList();
        Collections.sort(tagCountList);
        return tagCountList;
    }

    private EuropeanaId fetchEuropeanaId(String europeanaUri) {
        Query query = entityManager.createQuery("select id from EuropeanaId as id where id.europeanaUri = :uri");
        query.setParameter("uri", europeanaUri);
        return (EuropeanaId) query.getSingleResult();
    }
}