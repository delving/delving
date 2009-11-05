/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or as soon they
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
import eu.europeana.database.LanguageDao;
//import eu.europeana.database.MessageDao;
import eu.europeana.database.domain.*;
import eu.europeana.incoming.ESEImporter;
import eu.europeana.incoming.SolrIndexer;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardServiceImpl implements DashboardService {
    private static final long SESSION_TIMEOUT = 1000L*60*30;
    private static long lastAgeCheck;
    private Logger log = Logger.getLogger(getClass());
    private Map<String, Session> sessions = new ConcurrentHashMap<String,Session>();
    private String cacheUrl;
    private ESEImporter normalizedImporter;
    private ESEImporter sandboxImporter;
    private DashboardDao dashboardDao;
    //private MessageDao messageDao;
    private LanguageDao languageDao;
    private SolrIndexer indexer;
    private DigitalObjectCache digitalObjectCache;

    public void setCacheUrl(String cacheUrl) {
        this.cacheUrl = cacheUrl;
    }

    public void setNormalizedImporter(ESEImporter normalizedImporter) {
        this.normalizedImporter = normalizedImporter;
    }

    public void setSandboxImporter(ESEImporter sandboxImporter) {
        this.sandboxImporter = sandboxImporter;
    }

    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }
    /*
    public void setMessageDao(MessageDao messageDao) {
        this.messageDao = messageDao;
    }
             */
    public void setLanguageDao(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

    public void setIndexDeleter(SolrIndexer indexer) {
        this.indexer = indexer;
    }

    public void setDigitalObjectCache(DigitalObjectCache digitalObjectCache) {
        this.digitalObjectCache = digitalObjectCache;
    }

    public UserX login(String email, String password) {
        User user = dashboardDao.fetchUser(email, password);
        if (user != null && user.isEnabled()) {
            log.info("User "+user.getEmail()+" (id="+user.getId()+") logged in with cookie "+UserCookie.get());
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
        EuropeanaCollection collection;
        if (collectionX.getId() != null) {
            collection = dashboardDao.fetchCollection(collectionX.getId());
            handleCacheStateTransition(collectionX, collection);
            handleCollectionStateTransition(collectionX, collection);
        }
        else {
            collection = new EuropeanaCollection();
        }
        collection.setName(collectionX.getName());
        collection.setDescription(collectionX.getDescription());
        collection.setFileName(collectionX.getFileName());
        collection.setCollectionLastModified(collectionX.getCollectionLastModified());
        String cookie = UserCookie.get();
        if (cookie == null) throw new RuntimeException("Expected cookie!");
        Session session = sessions.get(cookie);
        collection.setFileUserName(session.getUser().getUserName());
        collection.setFileState(ImportFileState.valueOf(collectionX.getFileState().toString()));
        collection.setCacheState(CacheState.valueOf(collectionX.getCacheState().toString()));
        collection.setCollectionState(CollectionState.valueOf(collectionX.getCollectionState().toString()));
        collection = dashboardDao.updateCollection(collection);
        audit("update collection: "+collection.getName());
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
        audit("update collection counters: "+collection);
        return DataTransfer.convert(dashboardDao.updateCollectionCounters(collection.getId()));
    }

    public List<UserX> fetchUsers(String pattern) {
        List<UserX> users = new ArrayList<UserX>();
        List<User> userList = dashboardDao.fetchUsers(pattern);
        for (User user : userList) {
            users.add(DataTransfer.convert(user));
        }
        return users;
    }

    public UserX updateUser(UserX user) {
        return DataTransfer.convert(dashboardDao.updateUser(DataTransfer.convert(user)));
    }

    public List<SavedItemX> fetchSavedItems(Long userId) {
        List<SavedItem> savedItems = dashboardDao.fetchSavedItems(userId);
        List<SavedItemX> items = new ArrayList<SavedItemX>();
        for (SavedItem item : savedItems) {
            items.add(new SavedItemX(item.getTitle(), item.getEuropeanaId().getEuropeanaUri(), item.getId()));
        }

        return items;
    }

    public void removeUser(Long userId) {
        audit("remove user "+userId);
        dashboardDao.removeUser(userId);
    }

    public List<ImportFileX> fetchImportFiles(boolean normalized) {
        return DataTransfer.convert(importer(normalized).getImportRepository().getAllFiles());
    }

    public ImportFileX commenceValidate(ImportFileX importFile, Long collectionId) {
        audit("commence validate "+importFile.getFileName());
        return DataTransfer.convert(importer(false).commenceValidate(DataTransfer.convert(importFile), collectionId));
    }

    public ImportFileX commenceImport(ImportFileX importFile, Long collectionId, boolean normalized) {
        audit("commence import "+importFile.getFileName());
        return DataTransfer.convert(importer(normalized).commenceImport(DataTransfer.convert(importFile), collectionId));
    }

    public ImportFileX abortImport(ImportFileX importFile, boolean normalized) {
        audit("abort import of "+importFile.getFileName());
        return DataTransfer.convert(importer(normalized).abortImport(DataTransfer.convert(importFile)));
    }

    public ImportFileX checkImportFileStatus(String fileName, boolean normalized) {
        return DataTransfer.convert(importer(normalized).getImportRepository().checkStatus(fileName));
    }

    private ESEImporter importer(boolean normalized) {
        return normalized ? normalizedImporter : sandboxImporter;
    }

    public List<String> fetchMessageKeys() {
        //return messageDao.fetchMessageKeyStrings();
         return languageDao.fetchMessageKeyStrings();
    }

    public List<LanguageX> fetchLanguages() {
        EnumSet<Language> activeLanguages = languageDao.getActiveLanguages();
        List<LanguageX> languages = new ArrayList<LanguageX>(Language.values().length);
        for (Language active : activeLanguages) {
            languages.add(DataTransfer.convert(active));
        }
        return languages;
    }

    public Map<String,List<TranslationX>> fetchTranslations(Set<String> languageCodes) {
       // Map<String, List<Translation>> preconvert = messageDao.fetchTranslations(languageCodes);
        Map<String, List<Translation>> preconvert = languageDao.fetchTranslations(languageCodes);
        Map<String, List<TranslationX>> translations = new HashMap<String,List<TranslationX>>();
        for (Map.Entry<String,List<Translation>> entry : preconvert.entrySet()) {
            List<TranslationX> value = new ArrayList<TranslationX>();
            translations.put(entry.getKey(), value);
            for (Translation translation : entry.getValue()) {
                value.add(DataTransfer.convert(translation));
            }
        }
        return translations;
    }

    public TranslationX setTranslation(String key, String languageCode, String value) {
        audit("set translation "+key+"/"+languageCode+"="+value);
        //return DataTransfer.convert(messageDao.setTranslation(key, Language.findByCode(languageCode), value));
        return DataTransfer.convert(languageDao.setTranslation(key, Language.findByCode(languageCode), value));
    }

    public String fetchCacheUrl() {
        return cacheUrl;
    }

    public List<CarouselItemX> fetchCarouselItems() {
        List<CarouselItemX> items = new ArrayList<CarouselItemX>();
        for (CarouselItem item : dashboardDao.fetchCarouselItems()) {
            items.add(DataTransfer.convert(item));
        }
        return items;
    }

    public CarouselItemX createCarouselItem(SavedItemX savedItemX) {
        String europeanaUri = savedItemX.getUri();
        audit("create carousel item: "+ europeanaUri);
        CarouselItem item = dashboardDao.createCarouselItem(europeanaUri, savedItemX.getId());
        if (item == null) {
            return null;
        }
        return DataTransfer.convert(item);
    }

    public boolean removeCarouselItem(CarouselItemX item) {
        audit("remove carousel item: "+item.getEuropeanaUri());
        return dashboardDao.removeCarouselItem(item.getId());
    }

    public boolean addSearchTerm(String language, String term) {
        audit("add search term: "+language+"/"+term);
        return dashboardDao.addSearchTerm(Language.findByCode(language), term);
    }

    public List<String> fetchSearchTerms(String language) {
        return dashboardDao.fetchSearchTerms(Language.findByCode(language));
    }

    public boolean removeSearchTerm(String language, String term) {
        audit("remove search term: "+language+"/"+term);
        return dashboardDao.removeSearchTerm(Language.findByCode(language), term);
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
        audit("delete object orphan: "+uri);
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

    public void disableAllCollections () {
        List<EuropeanaCollection> collections = dashboardDao.disableAllCollections();
        for (EuropeanaCollection collection : collections) {
            indexer.deleteCollectionByName(collection.getName());
        }
    }

    public void enableAllCollections () {
        disableAllCollections();
        dashboardDao.enableAllCollections();
    }

    public List<SavedSearchX> fetchSavedSearches(Long id) {
        List<SavedSearch> savedSearches = dashboardDao.fetchSavedSearches(id);
        List<SavedSearchX> result = new ArrayList<SavedSearchX>();
        for (SavedSearch search : savedSearches) {
            result.add(DataTransfer.convert(search));
        }
        return result;
    }

    public List<String> fetchPartnerSectors() {
        List<String> sectors = new ArrayList<String>();
        for (PartnerSector ps : PartnerSector.values()) {
            sectors.add(ps.toString());
        }
        return sectors;
    }

    public List<PartnerX> fetchPartners() {
        List<PartnerX> results = new ArrayList<PartnerX>();
        for (Partner partner : dashboardDao.fetchPartners()) {
            results.add(DataTransfer.convert(partner));
        }
        return results;
    }

    public List<CountryX> fetchCountries() {
        List<CountryX> countries = new ArrayList<CountryX>();
        for (Country c : Country.values()) {
            countries.add(DataTransfer.convert(c));
        }
        return countries;
    }

    public List<ContributorX> fetchContributors() {
        List<ContributorX> results = new ArrayList<ContributorX>();
        for (Contributor contributor: dashboardDao.fetchContributors()) {
            results.add(DataTransfer.convert(contributor));
        }
        return results;
    }

    public PartnerX savePartner(PartnerX partnerX) {
        audit("save partner: "+partnerX.getName());
        Partner partner = DataTransfer.convert(partnerX);
        partner = dashboardDao.savePartner(partner);
        return DataTransfer.convert(partner);
    }

    public ContributorX saveContributor(ContributorX contributorX) {
        audit("save contributor: "+ contributorX.getOriginalName());
        Contributor contributor = DataTransfer.convert(contributorX);
        contributor = dashboardDao.saveContributor(contributor);
        return DataTransfer.convert(contributor);
    }

    public boolean removePartner(Long partnerId) {
        audit("remove partner: "+partnerId);
        return dashboardDao.removePartner(partnerId);
    }

    public boolean removeContributor(Long contributorId) {
        audit("remove contributor: "+contributorId);
        return dashboardDao.removeContributor(contributorId);
    }

    public List<String> fetchStaticPageTypes() {
        List<String> pageTypes = new ArrayList<String>();
        for (StaticPageType pageType : StaticPageType.values()) {
            pageTypes.add(pageType.toString());
        }
        return pageTypes;
    }

    public StaticPageX fetchStaticPage(String pageType, LanguageX language) {
        StaticPage page = dashboardDao.fetchStaticPage(StaticPageType.valueOf(pageType), Language.findByCode(language.getCode()));
        return DataTransfer.convert(page);
    }

    public StaticPageX saveStaticPage(Long staticPageId, String content) {
        StaticPage page = dashboardDao.saveStaticPage(staticPageId, content);
        audit("save static page: "+staticPageId);
        return DataTransfer.convert(page);
    }

    public void removeMessageKey(String key) {
        audit("remove message key: "+key);
        dashboardDao.removeMessageKey(key);
    }

    public void addMessageKey(String key) {
        audit("add message key: "+key);
        dashboardDao.addMessagekey(key);
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

    private void handleCacheStateTransition(EuropeanaCollectionX next, EuropeanaCollection current) {
        switch (current.getCacheState()) {
            case EMPTY:
                return;
            case UNCACHED:
                switch (next.getCacheState()) {
                    case EMPTY:
                    case UNCACHED:
                        return;
                    case QUEUED:
                        dashboardDao.addToCacheQueue(current);
                        return;
                }
                break;
            case QUEUED:
                switch (next.getCacheState()) {
                    case EMPTY:
                    case UNCACHED:
                        dashboardDao.removeFromCacheQueue(current);
                        return;
                    case QUEUED:
                        return;
                }
                break;
            case CACHEING:
                switch (next.getCacheState()) {
                    case EMPTY:
                    case UNCACHED:
                        dashboardDao.removeFromCacheQueue(current);
                        return;
                    case CACHEING:
                        return;
                }
                break;
            case CACHED:
                switch (next.getCacheState()) {
                    case QUEUED:
                        dashboardDao.addToCacheQueue(current);
                        return;
                }
                return;
        }
        throw new RuntimeException("Illegal transition: "+current.getCacheState()+" to "+next.getCacheState());
    }

    private void handleCollectionStateTransition(EuropeanaCollectionX next, EuropeanaCollection current) {
        switch (current.getCollectionState()) {
            case EMPTY:
                return;
            case DISABLED:
                switch (next.getCollectionState()) {
                    case EMPTY:
                    case DISABLED:
                        return;
                    case QUEUED:
                        dashboardDao.addToIndexQueue(current);
                        return;
                }
                break;
            case QUEUED:
                switch (next.getCollectionState()) {
                    case EMPTY:
                    case DISABLED:
                        dashboardDao.removeFromIndexQueue(current);
                        return;
                    case QUEUED:
                        return;
                }
                break;
            case INDEXING:
                switch (next.getCollectionState()) {
                    case EMPTY:
                    case DISABLED:
                        dashboardDao.removeFromIndexQueue(current);
                        return;
                    case INDEXING:
                        return;
                }
                break;
            case ENABLED:
                switch (next.getCollectionState()) {
                    case EMPTY:
                    case DISABLED:
                        dashboardDao.removeFromIndexQueue(current);
                        if (!indexer.deleteCollectionByName(current.getName())) {
                            log.warn("Unable to delete from the index!");
                        }
                        return;
                    case ENABLED:
                        return;
                }
                break;
        }
        throw new RuntimeException("Illegal transition: "+current.getCollectionState()+" to "+next.getCollectionState());
    }

    private void audit(String what) {
        String cookie = UserCookie.get();
        if (cookie == null) throw new RuntimeException("Expected cookie!");
        Session session = sessions.get(cookie);
        if (session == null) throw new RuntimeException("Expected to find user");
        dashboardDao.log(session.getUser().getEmail(), what);
        if (System.currentTimeMillis() - lastAgeCheck > SESSION_TIMEOUT/10) {
            Iterator<Map.Entry<String,Session>> walk = sessions.entrySet().iterator();
            while (walk.hasNext()) {
                if (walk.next().getValue().isTooOld()) {
                    walk.remove();
                }
            }
            lastAgeCheck = System.currentTimeMillis();
        }
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
