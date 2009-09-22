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

import java.util.List;
import java.util.Set;

/**
 * This interface represents the functions provided by an implementation of a data access object
 * for the dashboard application
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface DashboardDao {

    User fetchUser(String email, String password);

    void setUserRole(Long userId, Role role);

    List<User> fetchUsers(String pattern);

    void setUserEnabled(Long userId, boolean enabled);

    List<EuropeanaCollection> fetchCollections();

    List<EuropeanaCollection> fetchCollections(String prefix);

    EuropeanaCollection fetchCollectionByName(String name, boolean create);

    EuropeanaCollection fetchCollectionByFileName(String fileName);

    EuropeanaCollection fetchCollection(Long id);

    EuropeanaCollection updateCollection(EuropeanaCollection collection);

    EuropeanaCollection prepareForImport(Long collectionId);

    EuropeanaCollection setImportError(Long collectionId, String importError);

    EuropeanaId saveEuropeanaId(EuropeanaId europeanaId, Set<String> objectUrls);

    EuropeanaId getEuropeanaId(EuropeanaId europeanaId);

    List<EuropeanaObject> getEuropeanaObjectsToCache(int maxResults, CacheingQueueEntry queueEntry);

    List<EuropeanaObject> getEuropeanaObjectOrphans(int maxResults);

    CacheingQueueEntry getEntryForCacheing();

    CacheingQueueEntry saveObjectsCached(int cachedRecords, CacheingQueueEntry queueEntry, EuropeanaObject lastId);

    void setObjectCachedError(EuropeanaObject object);

    void removeOrphanObject(String uri);

    boolean addToIndexQueue(EuropeanaCollection collection);

    void removeFromIndexQueue(EuropeanaCollection collection);

    boolean addToCacheQueue(EuropeanaCollection collection);

    void removeFromCacheQueue(EuropeanaCollection collection);

    IndexingQueueEntry getIndexQueueHead();

    List<EuropeanaId> getEuropeanaIdsForIndexing(int chunkSize, IndexingQueueEntry indexingQueueEntry);

    void finishIndexing(IndexingQueueEntry indexingQueueEntry);

    void saveRecordsIndexed(int count, IndexingQueueEntry indexingQueueEntry, EuropeanaId europeanaId);

    void startIndexing(IndexingQueueEntry indexingQueueEntry);

    List<? extends QueueEntry> fetchQueueEntries();

    EuropeanaCollection updateCollectionCounters(Long collectionId);

    boolean addSearchTerm(Language language, String term);

    boolean addSearchTerm(SavedSearch savedSearch);

    List<String> fetchSearchTerms(Language language);

    boolean removeSearchTerm(Language language, String term);

    List<SavedItem> fetchSavedItems(Long userId);

    SavedItem fetchSavedItemById(Long id);

    List<SavedSearch> fetchSavedSearches(Long userId);

    SavedSearch fetchSavedSearchById(Long id);

    void removeUser(Long userId);

    User fetchUser(Long userId);

    EuropeanaId fetchEuropeanaId(String europeanaUri);

    EuropeanaId updateEuropeanaId(Long id, float boostFactor, String solrRecords);

    List<Partner> fetchPartners();

    List<Contributor> fetchContributors();

    Partner savePartner(Partner partner);

    Contributor saveContributor(Contributor contributor);

    boolean removePartner(Long partnerId);

    boolean removeContributor(Long contributorId);

    void log(String who, String what);

    StaticPage fetchStaticPage(StaticPageType pageType, Language language);

    StaticPage saveStaticPage(Long staticPageId, String content);

    void setUserLanguages(Long userId, String languages);

    void addMessagekey(String key);

    void removeMessageKey(String key);

    List<DashboardLog> fetchLogEntriesFrom(Long topId, int pageSize);

    List<DashboardLog> fetchLogEntriesTo(Long bottomId, int pageSize);

    IndexingQueueEntry getEntryForIndexing();

    void finishCaching(CacheingQueueEntry entry);

    IndexingQueueEntry getIndexEntry(IndexingQueueEntry detachedEntry);

    List<EuropeanaCollection> disableAllCollections();

    void enableAllCollections();

    void setUserProjectId(Long userId, String projectId);

    void setUserProviderId(Long userId, String providerId);

    int findOrphans(EuropeanaCollection collection);

    /*
     * Carousel
     */
    List<CarouselItem> fetchCarouselItems();

    Boolean removeCarouselItem(Long id);

    CarouselItem createCarouselItem(String europeanaUri);

    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);

    void removeFromCarousel(SavedItem savedItem);

    boolean addCarouselItem(SavedItem savedItem);

    /*
     *  People Are Currently Thinking About, or editor picks
     */
    List<EditorPick> fetchEditorPicksItems();

    void removeFromEditorPick(SavedSearch savedSearch);

    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;

    List<CarouselItem> getAllCarouselItems();
}