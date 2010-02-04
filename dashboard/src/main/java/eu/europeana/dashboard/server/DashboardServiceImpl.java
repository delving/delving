/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

package eu.europeana.dashboard.server;

import eu.europeana.cache.DigitalObjectCache;
import eu.europeana.dashboard.client.DashboardService;
import eu.europeana.dashboard.client.dto.*;
import eu.europeana.database.DashboardDao;
import eu.europeana.database.StaticInfoDao;
import eu.europeana.database.UserDao;
import eu.europeana.database.domain.*;
import eu.europeana.incoming.ESEImporter;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardServiceImpl implements DashboardService {
    private static final long SESSION_TIMEOUT = 1000L * 60 * 30;
    private static long lastAgeCheck;
    private Logger log = Logger.getLogger(getClass());
    private Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();
    private String cacheUrl;
    private ESEImporter normalizedImporter;
    private ESEImporter sandboxImporter;
    private DashboardDao dashboardDao;
    private DigitalObjectCache digitalObjectCache;
    private StaticInfoDao staticInfoDao;
    private UserDao userDao;
    private SolrServer solrServer;

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public void setStaticInfoDao(StaticInfoDao staticInfoDao) {
        this.staticInfoDao = staticInfoDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setCacheUrl(String cacheUrl) {
        this.cacheUrl = cacheUrl;
    }

    public void setNormalizedImporter(ESEImporter normalizedImporter) {
        this.normalizedImporter = normalizedImporter;
    }

    public void setSandboxImporter(ESEImporter sandboxImporter) {
        this.sandboxImporter = sandboxImporter;
    }

    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public void setDigitalObjectCache(DigitalObjectCache digitalObjectCache) {
        this.digitalObjectCache = digitalObjectCache;
    }

    public UserX login(String email, String password) {
        User user = userDao.authenticateUser(email, password);
        if (user != null && user.isEnabled()) {
            log.info("User " + user.getEmail() + " (id=" + user.getId() + ") logged in with cookie " + UserCookie.get());
            String cookie = UserCookie.get();
            if (cookie == null) {
                log.error("No Cookie!!");
                throw new RuntimeException("Missing cookie");
            }
            sessions.put(cookie, new Session(user));
            audit("logged in");
            return DataTransfer.convert(user);
        }
        else {
            return null;
        }
    }

    public List<EuropeanaCollectionX> fetchCollections() {
        List<EuropeanaCollection> collectionList = dashboardDao.fetchCollections();
        List<EuropeanaCollectionX> collections = new ArrayList<EuropeanaCollectionX>();
        for (EuropeanaCollection collection : collectionList) {
            collections.add(DataTransfer.convert(collection));
        }
        return collections;
    }

    public List<EuropeanaCollectionX> fetchCollections(String prefix) {
        List<EuropeanaCollection> collectionList = dashboardDao.fetchCollections(prefix);
        List<EuropeanaCollectionX> collections = new ArrayList<EuropeanaCollectionX>();
        for (EuropeanaCollection collection : collectionList) {
            collections.add(DataTransfer.convert(collection));
        }
        return collections;
    }

    public EuropeanaCollectionX fetchCollection(String name, boolean create) {
        EuropeanaCollection collection = dashboardDao.fetchCollectionByName(name, create);
        if (collection == null) {
            return null;
        }
        else {
            return DataTransfer.convert(collection);
        }
    }

    public EuropeanaCollectionX updateCollection(EuropeanaCollectionX collectionX) {
        String cookie = UserCookie.get();
        if (cookie == null) throw new RuntimeException("Expected cookie!");
        Session session = sessions.get(cookie);
        collectionX.setFileUserName(session.getUser().getUserName()); // record whodunnit
        if (collectionX.getCollectionState() == CollectionStateX.DISABLED) {
            if (!deleteCollectionByName(collectionX.getName())) {
                log.warn("Unable to delete from the index!");
            }
        }
        EuropeanaCollection collection = dashboardDao.updateCollection(DataTransfer.convert(collectionX));
        audit("update collection: " + collection.getName());
        return DataTransfer.convert(collection);
    }

    public List<QueueEntryX> fetchQueueEntries() {
        List<? extends QueueEntry> fetchedEntries = dashboardDao.fetchQueueEntries();
        List<QueueEntryX> entries = new ArrayList<QueueEntryX>();
        for (QueueEntry entry : fetchedEntries) {
            entries.add(new QueueEntryX(
                    entry.getId(),
                    entry.isCache() ? QueueEntryX.Type.CACHE : QueueEntryX.Type.INDEX,
                    DataTransfer.convert(entry.getCollection()),
                    entry.getRecordsProcessed(),
                    entry.getTotalRecords()
            ));
        }
        return entries;
    }

    public EuropeanaCollectionX updateCollectionCounters(EuropeanaCollectionX collection) {
        audit("update collection counters: " + collection);
        return DataTransfer.convert(dashboardDao.updateCollectionCounters(collection.getId()));
    }

    public List<UserX> fetchUsers(String pattern) {
        List<UserX> users = new ArrayList<UserX>();
        List<User> userList = userDao.fetchUsers(pattern);
        for (User user : userList) {
            users.add(DataTransfer.convert(user));
        }
        return users;
    }

    public UserX updateUser(UserX user) {
        return DataTransfer.convert(userDao.updateUser(DataTransfer.convert(user)));
    }

    public List<SavedItemX> fetchSavedItems(Long userId) {
        List<SavedItem> savedItems = userDao.fetchSavedItems(userId);
        List<SavedItemX> items = new ArrayList<SavedItemX>();
        for (SavedItem item : savedItems) {
            items.add(new SavedItemX(item.getTitle(), item.getEuropeanaId().getEuropeanaUri(), item.getId()));
        }

        return items;
    }

    public void removeUser(UserX user) {
        audit("remove user " + user.getUserName());
        userDao.removeUser(DataTransfer.convert(user));
    }

    public List<ImportFileX> fetchImportFiles(boolean normalized) {
        return DataTransfer.convert(importer(normalized).getImportRepository().getAllFiles());
    }

    public ImportFileX commenceValidate(ImportFileX importFile, Long collectionId) {
        audit("commence validate " + importFile.getFileName());
        return DataTransfer.convert(importer(false).commenceValidate(DataTransfer.convert(importFile), collectionId));
    }

    public ImportFileX commenceImport(ImportFileX importFile, Long collectionId, boolean normalized) {
        audit("commence import " + importFile.getFileName());
        return DataTransfer.convert(importer(normalized).commenceImport(DataTransfer.convert(importFile), collectionId));
    }

    public ImportFileX abortImport(ImportFileX importFile, boolean normalized) {
        audit("abort import of " + importFile.getFileName());
        return DataTransfer.convert(importer(normalized).abortImport(DataTransfer.convert(importFile)));
    }

    public ImportFileX checkImportFileStatus(String fileName, boolean normalized) {
        return DataTransfer.convert(importer(normalized).getImportRepository().checkStatus(fileName));
    }

    private ESEImporter importer(boolean normalized) {
        return normalized ? normalizedImporter : sandboxImporter;
    }

    public List<LanguageX> fetchLanguages() {
        List<LanguageX> languages = new ArrayList<LanguageX>(Language.values().length);
        for (Language active : Language.values()) {
            languages.add(DataTransfer.convert(active));
        }
        return languages;
    }


    public String fetchCacheUrl() {
        return cacheUrl;
    }

    public List<CarouselItemX> fetchCarouselItems() {
        List<CarouselItemX> items = new ArrayList<CarouselItemX>();
        for (CarouselItem item : staticInfoDao.fetchCarouselItems()) {
            items.add(DataTransfer.convert(item));
        }
        return items;
    }

    public CarouselItemX createCarouselItem(SavedItemX savedItemX) {
        String europeanaUri = savedItemX.getUri();
        audit("create carousel item: " + europeanaUri);
        CarouselItem item = staticInfoDao.createCarouselItem(savedItemX.getId());
        if (item == null) {
            return null;
        }
        return DataTransfer.convert(item);
    }

    public boolean removeCarouselItem(CarouselItemX item) {
        audit("remove carousel item: " + item.getEuropeanaUri());
        return staticInfoDao.removeCarouselItem(item.getId());
    }

    public boolean addSearchTerm(String language, String term) {
        audit("add search term: " + language + "/" + term);
        return staticInfoDao.addSearchTerm(Language.findByCode(language), term);
    }

    public List<String> fetchSearchTerms(String language) {
        return staticInfoDao.fetchSearchTerms(Language.findByCode(language));
    }

    public boolean removeSearchTerm(String language, String term) {
        audit("remove search term: " + language + "/" + term);
        return staticInfoDao.removeSearchTerm(Language.findByCode(language), term);
    }

    public List<String> getObjectOrphans() {
        List<EuropeanaObject> orphans = dashboardDao.getEuropeanaObjectOrphans(50);
        List<String> uris = new ArrayList<String>();
        for (EuropeanaObject object : orphans) {
            uris.add(object.getObjectUrl());
        }
        return uris;
    }

    public boolean deleteObjectOrphan(String uri) {
        audit("delete object orphan: " + uri);
        dashboardDao.removeOrphanObject(uri);
        return digitalObjectCache.remove(uri);
    }

    public void deleteAllOrphans() {
        while (true) {
            List<EuropeanaObject> orphans = dashboardDao.getEuropeanaObjectOrphans(1000);
            if (orphans.isEmpty()) {
                break;
            }
            for (EuropeanaObject orphan : orphans) {
                deleteObjectOrphan(orphan.getObjectUrl());
            }
        }
    }

    public EuropeanaIdX fetchEuropeanaId(String uri) {
        return DataTransfer.convert(dashboardDao.fetchEuropeanaId(uri));
    }

    public void disableAllCollections() {
        List<EuropeanaCollection> collections = dashboardDao.disableAllCollections();
        for (EuropeanaCollection collection : collections) {
            deleteCollectionByName(collection.getName());
        }
    }

    public void enableAllCollections() {
        disableAllCollections();
        dashboardDao.enableAllCollections();
    }

    public List<SavedSearchX> fetchSavedSearches(UserX user) {
        List<SavedSearch> savedSearches = userDao.fetchSavedSearches(DataTransfer.convert(user));
        List<SavedSearchX> result = new ArrayList<SavedSearchX>();
        for (SavedSearch search : savedSearches) {
            result.add(DataTransfer.convert(search));
        }
        return result;
    }

    public List<DashboardLogX> fetchLogEntriesFrom(Long topId, int pageSize) {
        List<DashboardLogX> result = new ArrayList<DashboardLogX>();
        for (DashboardLog log : dashboardDao.fetchLogEntriesFrom(topId, pageSize)) {
            result.add(DataTransfer.convert(log));
        }
        return result;
    }

    public List<DashboardLogX> fetchLogEntriesTo(Long bottomId, int pageSize) {
        List<DashboardLogX> result = new ArrayList<DashboardLogX>();
        for (DashboardLog log : dashboardDao.fetchLogEntriesTo(bottomId, pageSize)) {
            result.add(DataTransfer.convert(log));
        }
        return result;
    }

    private void audit(String what) {
        String cookie = UserCookie.get();
        if (cookie == null) throw new RuntimeException("Expected cookie!");
        Session session = sessions.get(cookie);
        if (session == null) throw new RuntimeException("Expected to find user");
        dashboardDao.log(session.getUser().getEmail(), what);
        if (System.currentTimeMillis() - lastAgeCheck > SESSION_TIMEOUT / 10) {
            Iterator<Map.Entry<String, Session>> walk = sessions.entrySet().iterator();
            while (walk.hasNext()) {
                if (walk.next().getValue().isTooOld()) {
                    walk.remove();
                }
            }
            lastAgeCheck = System.currentTimeMillis();
        }
    }

    public boolean deleteCollectionByName(String collectionName) {
        try {
            log.info(String.format("Delete collection %s from Solr Index", collectionName));
            solrServer.deleteByQuery("europeana_collectionName:\""+collectionName+"\"");
            solrServer.commit();
            return true;
        }
        catch (Exception e) {
            log.error("Unable to delete collection", e);
        }
        return false;
    }


    private static class Session {
        private User user;
        private long lastAccess;

        private Session(User user) {
            this.user = user;
            lastAccess = System.currentTimeMillis();
        }

        public User getUser() {
            lastAccess = System.currentTimeMillis();
            return user;
        }

        public boolean isTooOld() {
            return System.currentTimeMillis() - lastAccess > SESSION_TIMEOUT;
        }
    }
}
