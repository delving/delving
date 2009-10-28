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

package eu.europeana.database;

import eu.europeana.database.domain.CacheingQueueEntry;
import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.DashboardLog;
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.EuropeanaObject;
import eu.europeana.database.domain.IndexingQueueEntry;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.QueueEntry;
import eu.europeana.database.domain.Role;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;
import eu.europeana.database.domain.User;

import java.util.List;
import java.util.Set;

/**
 * This interface represents the functions provided by an implementation of a data access object
 * for the dashboard application
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface DashboardDao {

    // collections

    List<EuropeanaCollection> fetchCollections();

    List<EuropeanaCollection> fetchCollections(String prefix);

    EuropeanaCollection fetchCollectionByName(String name, boolean create);

    EuropeanaCollection fetchCollectionByFileName(String fileName);

    EuropeanaCollection fetchCollection(Long id);

    EuropeanaCollection updateCollection(EuropeanaCollection collection);

    List<EuropeanaCollection> disableAllCollections();

    void enableAllCollections();

    // import

    EuropeanaCollection prepareForImport(Long collectionId);

    EuropeanaCollection setImportError(Long collectionId, String importError);

    EuropeanaId saveEuropeanaId(EuropeanaId europeanaId, Set<String> objectUrls);

    EuropeanaId getEuropeanaId(EuropeanaId europeanaId);

    EuropeanaId fetchEuropeanaId(String europeanaUri);

    void removeOrphanObject(String uri);

    int findOrphans(EuropeanaCollection collection);

    List<? extends QueueEntry> fetchQueueEntries();

    // cacheing

    List<EuropeanaObject> getEuropeanaObjectsToCache(int maxResults, CacheingQueueEntry queueEntry);

    List<EuropeanaObject> getEuropeanaObjectOrphans(int maxResults);

    boolean addToCacheQueue(EuropeanaCollection collection);

    void removeFromCacheQueue(EuropeanaCollection collection);

    CacheingQueueEntry getEntryForCacheing();

    CacheingQueueEntry saveObjectsCached(int cachedRecords, CacheingQueueEntry queueEntry, EuropeanaObject lastId);

    void setObjectCachedError(EuropeanaObject object);

    void finishCaching(CacheingQueueEntry entry);

    // index

    boolean addToIndexQueue(EuropeanaCollection collection);

    void removeFromIndexQueue(EuropeanaCollection collection);

    IndexingQueueEntry getIndexQueueHead();

    List<EuropeanaId> getEuropeanaIdsForIndexing(int chunkSize, IndexingQueueEntry indexingQueueEntry);

    void finishIndexing(IndexingQueueEntry indexingQueueEntry);

    void saveRecordsIndexed(int count, IndexingQueueEntry indexingQueueEntry, EuropeanaId europeanaId);

    void startIndexing(IndexingQueueEntry indexingQueueEntry);

    EuropeanaCollection updateCollectionCounters(Long collectionId);

    IndexingQueueEntry getEntryForIndexing();

    IndexingQueueEntry getIndexEntry(IndexingQueueEntry detachedEntry);

    // todo: move the implementations to UserDaoImpl
    User fetchUser(String email, String password);
    void setUserRole(Long userId, Role role);
    List<User> fetchUsers(String pattern);
    void setUserEnabled(Long userId, boolean enabled);
    void removeUser(Long userId);
    User fetchUser(Long userId);
    void setUserProjectId(Long userId, String projectId);
    void setUserProviderId(Long userId, String providerId);
    void setUserLanguages(Long userId, String languages);
    List<SavedItem> fetchSavedItems(Long userId);
    SavedItem fetchSavedItemById(Long id);
    List<SavedSearch> fetchSavedSearches(Long userId);
    SavedSearch fetchSavedSearchById(Long id);

    // todo: eliminate these, move implementations to StaticInfoDaoImpl
    List<Partner> fetchPartners();
    List<Contributor> fetchContributors();
    Partner savePartner(Partner partner);
    Contributor saveContributor(Contributor contributor);
    boolean removePartner(Long partnerId);
    boolean removeContributor(Long contributorId);
    StaticPage fetchStaticPage(StaticPageType pageType, Language language);
    StaticPage saveStaticPage(Long staticPageId, String content);
    Boolean removeCarouselItem(Long id);
    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);
    void removeFromCarousel(SavedItem savedItem);
    boolean addCarouselItem(SavedItem savedItem);
    List<CarouselItem> fetchCarouselItems();
    List<EditorPick> fetchEditorPicksItems();
    void removeFromEditorPick(SavedSearch savedSearch);
    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;
    boolean addSearchTerm(Language language, String term);
    boolean addSearchTerm(SavedSearch savedSearch);
    List<String> fetchSearchTerms(Language language);
    boolean removeSearchTerm(Language language, String term);

    // languages
    // todo: eliminate these, move implementations to LanguageDaoImpl
    void addMessagekey(String key);
    void removeMessageKey(String key);

    // dashboard log
    void log(String who, String what);
    List<DashboardLog> fetchLogEntriesFrom(Long topId, int pageSize);
    List<DashboardLog> fetchLogEntriesTo(Long bottomId, int pageSize);

}