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
 * @author Nicola Aloia   <nicola.aloia@isti.cnr.it>
 */

public interface StaticInfoDao {

    // ==== Partners

    /**
     * Return the list of all {@link Partner}s ordered by sector.  The Partner class contains the name
     * and URL of the project partner, together with the sector of activity (e.g. "Project Contributors",
     * "Research institutions", ...).
     *
     * @return a List of Partner objects.
     * @see {@link Partner}
     * @see {@link eu.europeana.database.domain.PartnerSector}
     */
    List<Partner> getAllPartnerItems();

    /**
     * Persists the given instance of the {@link Partner} class.
     *
     * @param partner an instance of the Partner class
     * @return Partner - the updated instance class
     * @see {@link Partner}
     */
    Partner savePartner(Partner partner);

    /**
     * Remove the {@link Partner} class instance having the given identifier. Return true
     * if the partner is successfully removed, false otherwise.
     *
     * @param partnerId - Long - the unique identifier of the partner to be removed.
     * @return boolean - (successfully/unsuccessfully) removed
     * @throws IllegalArgumentException if the given identifier is null.
     * @see {@link Partner}
     */
    boolean removePartner(Long partnerId);

    // ==== Contributors

    /**
     * Return the list of all Europeana {@link Contributor}s ordered by country.
     *
     * @return List - the list of all Europeana contributors.
     * @see {@link Contributor}
     */
    List<Contributor> getAllContributors();     // renamed the previous: getAllContributorItems

    /**
     * Return the list of all Europeana {@link Contributor}s ordered by id.
     *
     * @return List - the list of all Europeana contributors.
     * @see {@link Contributor}
     */
    List<Contributor> getAllContributorsByIdentifier();    // renamed the previous: fetchContributors todo: do we need this?

    /**
     * Persists the given instance of the {@link Contributor} class.
     *
     * @param contributor an instance of the Contributor class
     * @return Contributor - the update instance class
     * @see {@link Contributor}
     */
    Contributor saveContributor(Contributor contributor);

    /**
     * Remove the {@link Contributor} class instance having the given identifier. Return true
     * if the contributor is successfully removed, false otherwise.
     *
     * @param contributorId - Long - the unique identifier of the contributor to be removed.
     * @return boolean - (successfully/unsuccessfully) removed
     * @throws IllegalArgumentException if the given identifier is null.
     * @see {@link Contributor}
     */
    boolean removeContributor(Long contributorId);

    // ==== Static pages

    /**
     * Returns the Europeana portal static page with the given name for the given language. The returned instance
     * of the {@link StaticPage} class contains: the page identifier,
     * the language ISO 3166 code, the page type (e.g. ABOUT_US, PRIVACY, ..)
     * and a String for the content of the page (HTML formatted).
     *
     * @param language - an instance of the {@link Language} class containing the ISO 3166 code
     * @param pageName String - the name of the static page as defined in {@link StaticPageType}
     * @return StaticPage - the instance of the StaticPage class
     * @see {@link StaticPage}
     * @see {@link Language}
     * @see {@link StaticPageType}
     */
    StaticPage fetchStaticPage(Language language, String pageName);    // todo: do we need this and the following?

    /**
     * Returns the Europeana portal static page with the given type for the given language. The returned instance
     * of the {@link StaticPage} class contains: the page identifier,
     * the language ISO 3166 code, the page type (e.g. ABOUT_US, PRIVACY, ..)
     * and a String for the content of the page (HTML formatted).
     *
     * @param language - an instance of the {@link Language} class containing the ISO 3166 code
     * @param pageType StaticPageType - the type of the static page as defined in {@link StaticPageType}
     * @return StaticPage - the instance of the StaticPage class
     * @see {@link StaticPage}
     * @see {@link Language}
     * @see {@link StaticPageType}
     */
    StaticPage fetchStaticPage(StaticPageType pageType, Language language); // todo: I suggest to rename getStaticPage

    /**
     * Persists a new static page with the given parameters (if not exists in the DB) or update the existing
     * page with the give content.
     *
     * @param pageType StaticPageType - the type of the static page as defined in {@link StaticPageType}
     * @param language - an instance of the {@link Language} class containing the ISO 3166 code
     * @param content  - String - the page content (HTML formatted).
     * @see {@link StaticPage}
     * @see {@link Language}
     * @see {@link StaticPageType}
     */
    void setStaticPage(StaticPageType pageType, Language language, String content);

    /**
     * Update the static page with the given content for the given page identifier.
     *
     * @param staticPageId Long - the identifier of the StaticPage instance.
     * @param content      - String - the page content (HTML formatted).
     * @see {@link StaticPage}
     */
    StaticPage saveStaticPage(Long staticPageId, String content);  // todo: I suggest the same name for this and the previous (setStaticPage or updateStaticPage)

    /**
     * Returns the {@link List} of all the Europeana portal {@link StaticPage}s. Each instance
     * of the {@link StaticPage} contains: the page identifier,
     * the language ISO 3166 code, the page type (e.g. ABOUT_US, PRIVACY, ..)
     * and a String for the content of the page (HTML formatted).
     *
     * @return List - the List of all StaticPage instances.
     * @see {@link StaticPage}
     */
    List<StaticPage> getAllStaticPages();


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