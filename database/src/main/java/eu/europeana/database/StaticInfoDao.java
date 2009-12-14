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

import eu.europeana.database.domain.*;

import java.util.List;

/**
 * Handles all access to the information that doesn't really change very much over time.
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia   <nicola.aloia@isti.cnr.it>
 */

public interface StaticInfoDao {

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

    /**
     * Return the list of all Europeana {@link Contributor}s ordered by country.
     *
     * @return List - the list of all Europeana contributors.
     * @see {@link Contributor}
     */

    List<Contributor> getAllContributors();

    /**
     * Return the list of all Europeana {@link Contributor}s ordered by id.
     * <p/>
     * // todo: is this method still necessary, given the above method?
     *
     * @return List - the list of all Europeana contributors.
     * @see {@link Contributor}
     */

    List<Contributor> getAllContributorsByIdentifier();

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

    StaticPage getStaticPage(StaticPageType pageType, Language language);

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
     * @return the new version of the page
     * @see {@link StaticPage}
     */

    StaticPage updateStaticPage(Long staticPageId, String content);

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

    /**
     * Remove the {@link SavedItem} instance with the given identifier from the Carousel of the given {@link User}.
     * It return an instance of the {@link User} class.
     *
     * @param user        - User - the instance of the User class to which the item belongs
     * @param savedItemId - Long - the identifier of the item to remove
     * @return User - the instance of the User class
     * @throws IllegalArgumentException if input parameter(s) is/are null or the user doesn't own the object.
     * @see {@link User}
     * @see {@link CarouselItem}
     * @see {@link SavedItem}
     */

    User removeCarouselItem(User user, Long savedItemId);        // one of the 3 is enough

    /**
     * Remove the given {@link SavedItem} from the Carousel.
     *
     * @param savedItem - SavedItem - the instance of the SavedItem to remove from Carousel
     * @see {@link CarouselItem}
     * @see {@link SavedItem}
     */
    void removeFromCarousel(SavedItem savedItem);


    /**
     * Remove the {@link CarouselItem} instance with the given identifier from the Carousel.
     *
     * @param id - Long - the identifier of the item to remove
     * @return Boolean - always true       todo: NA: why it returns a Boolean? !
     * @throws IllegalArgumentException if the input parameter is null or carousel item isn't in the SaveItem class too.
     * @see {@link CarouselItem}
     */
    Boolean removeCarouselItem(Long id);


    /**
     * Create a new {@link CarouselItem}, with a reference to the Europeana object, with the given identifier,
     * for the {@link SavedItem}, with the given identifier.
     *
     * @param savedItemId  - Long - the identifier of the item to be added
     * @return CarouselItem - the new instance of the CarouselItem class
     * @see {@link CarouselItem}
     * @see {@link SavedItem}
     * @see {@link eu.europeana.database.domain.EuropeanaId}
     */
    CarouselItem createCarouselItem(Long savedItemId);

    /**
     * Get all the instances on the {@link CarouselItem} class.
     *
     * @return List - all the CarouselItems
     * @see {@link CarouselItem}
     * @see {@link List}
     */
    List<CarouselItem> fetchCarouselItems();


    // ==== Search Terms

    /**
     * Persists a new instance of {@link SearchTerm} with the given term for the given language.
     *
     * @param language - the instance of the Language  (conform to ISO 3166)
     * @param term     - String the term to be added
     * @return boolean - true todo: always true?
     * @see {@link Language}
     */
    boolean addSearchTerm(Language language, String term);

    /**
     * Persists a new instance of {@link SearchTerm} using the given savedSearch.
     *
     * @param savedSearch - the instance of SavedSearch used to initialize the new SearchTerm instance.
     * @return boolean - true todo: always true?
     * @see {@link SavedSearch}
     */
    boolean addSearchTerm(SavedSearch savedSearch);

    /**
     * Persists a new instance of {@link SearchTerm} using the SavedSearch instance with the given identifier.
     *
     * @param savedSearchId - Long - the identifier of the instance of SavedSearch used to initialize the new SearchTerm instance.
     * @return SearchTerm - the created instance of SearchTerm.
     * @see {@link SavedSearch}
     */
    SearchTerm addSearchTerm(Long savedSearchId);

    /**
     * Get all terms for the given language.
     *
     * @param language - the instance of the Language  (conform to ISO 3166)
     * @return LIST of String
     * @see {@link List}
     * @see {@link Language}
     */
    List<String> fetchSearchTerms(Language language);

    /**
     * Remove the SearchTerm instance of the given term for the given language.
     *
     * @param language - the instance of the Language  (conform to ISO 3166)
     * @param term     - String the term to be removed
     * @return boolean - success/unsuccess
     */
    boolean removeSearchTerm(Language language, String term);

    /**
     * Get the whole content of the SearchTerm class.
     *
     * @return List -  of the retrieved SearchTerm
     * @see {@link List}
     * @see {@link SearchTerm}
     */
    List<SearchTerm> getAllSearchTerms();


    /**
     * Remove the {@link SearchTerm} instance referenced by the SavedSearch with the given identifier
     * and the given {@link User}. It return an instance of the {@link User} class.
     *
     * @param user          - User - the instance of the User class to which the item belongs
     * @param savedSearchId - Long - the identifier of SavedSearch instance
     * @return User - the instance of the User class
     * @throws IllegalArgumentException if input parameter(s) is/are null or the user doesn't own the object.
     * @see {@link User}
     * @see {@link SavedSearch}
     * @see {@link SearchTerm}
     */
    User removeSearchTerm(User user, Long savedSearchId);
}