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

package eu.europeana.database;

import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.Language;
import eu.europeana.database.domain.MessageKey;
import eu.europeana.database.domain.Partner;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.StaticPage;
import eu.europeana.database.domain.StaticPageType;
import eu.europeana.database.domain.User;

import java.util.List;

/**
 * Handles all access to the information that doesn't really change very much over time.
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia
 */

public interface StaticInfoDao {

    // ==== Partners

    /**
     *
     * Return a list of Partners ordered by Sector.
     * @return  a List of Partner objects.
     */
    List<Partner> getAllPartnerItems();
    Partner savePartner(Partner partner);
    boolean removePartner(Long partnerId);

    // ==== Contributors

    List<Contributor> getAllContributorItems();     // this is ordered by country: wich one? I will prefere this
    List<Contributor> fetchContributors();         // this is ordered by providerId
    Contributor saveContributor(Contributor contributor);
    boolean removeContributor(Long contributorId);

    // ==== Static pages

    StaticPage fetchStaticPage(Language language, String pageName);    // this  or the following both do the same thing   
    StaticPage fetchStaticPage(StaticPageType pageType, Language language); // but the previous uses the pageName to get the page Type
    void setStaticPage(StaticPageType pageType, Language language, String content);
    List<StaticPage> getAllStaticPages();
    StaticPage saveStaticPage(Long staticPageId, String content);

   // === Carousel Items

    User removeCarouselItem(User user, Long savedItemId);        // one of the 3 is enough
    void removeFromCarousel(SavedItem savedItem);
    Boolean removeCarouselItem(Long id);
    User addCarouselItem(User user, CarouselItem carouselItem);
    boolean addCarouselItem(SavedItem savedItem);
    CarouselItem addCarouselItem(User user, Long savedItem);    // this is different from the following in the reurned value
    User addCarouselItem(User user, SavedItem savedItem);
    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);
    List<CarouselItem> fetchCarouselItems();


    // ==== Search Terms

    boolean addSearchTerm(Language language, String term);
    boolean addSearchTerm(SavedSearch savedSearch);
    SearchTerm addSearchTerm(Long savedSearchId);
    List<String> fetchSearchTerms(Language language);
    boolean removeSearchTerm(Language language, String term);
    List<SearchTerm> getAllSearchTerms();
    User removeSearchTerm(User user, Long savedSearchId);

    // ==== Editor picks

    User addEditorPick(User user, EditorPick editorPick);
    List<EditorPick> fetchEditorPicksItems();
    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;
    void removeFromEditorPick(SavedSearch savedSearch);
}