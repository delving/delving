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

import eu.europeana.database.domain.*;

import java.util.List;

/**
 * @author vitali
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia
 */
public interface StaticInfoDao {

    List<Partner> getAllPartnerItems();

    List<Contributor> getAllContributorItems();

    //void saveContributor(Contributor contributor);      todo: this or the following?
    Contributor saveContributor(Contributor contributor);

    //void savePartner(Partner partner);         todo: this or the following?
    Partner savePartner(Partner partner);

    StaticPage fetchStaticPage(Language language, String pageName);

    void setStaticPage(StaticPageType pageType, Language language, String content);

    List<StaticPage> getAllStaticPages();

    List<MessageKey> getAllTranslationMessages();

    // todo: add these (implementations in DashboardDaoImpl)       DONE!
//    List<Partner> fetchPartners();
//    List<Contributor> fetchContributors();
//    Partner savePartner(Partner partner); // todo: this is a better impl than above
//    Contributor saveContributor(Contributor contributor);  // todo: this is a better impl than above
//    boolean removePartner(Long partnerId);
//    boolean removeContributor(Long contributorId);
//    StaticPage fetchStaticPage(StaticPageType pageType, Language language);
//    StaticPage saveStaticPage(Long staticPageId, String content);
//    Boolean removeCarouselItem(Long id);
//    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);
//    void removeFromCarousel(SavedItem savedItem);
//    boolean addCarouselItem(SavedItem savedItem);
//    List<CarouselItem> fetchCarouselItems();
//    List<EditorPick> fetchEditorPicksItems();
//    void removeFromEditorPick(SavedSearch savedSearch);
//    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;
//    boolean addSearchTerm(Language language, String term);
//    boolean addSearchTerm(SavedSearch savedSearch);
//    List<String> fetchSearchTerms(Language language);
//    boolean removeSearchTerm(Language language, String term);


    // NEW ADD
    /**
     * Remove the given Item from the Carousel
     *
     * @param user        - an instance of the User class
     * @param savedItemId - Long - the identifier of the saved Item
     * @return - an instance of the User class
     * @throws IllegalArgumentException for null input parameter(s) or the user doesn't own the object.
     */
    User removeCarouselItem(User user, Long savedItemId);

    /**
     * Remove a saved Search and return a User
     *
     * @param user          - an instance of the User class
     * @param savedSearchId - Long - the identifier of the saved Item
     * @return - an instance of the User class
     * @throws IllegalArgumentException for null input parameter(s) or the user doesn't own the object.
     */
    User removeSearchTerm(User user, Long savedSearchId);

    /**
     * Remove the partner with the given Identifier.
     *
     * @param partnerId - Long - the identifier of the partner
     * @return boolean - true if the partner has been correctly removed, false in the partner doesn't exists.
     * @throws IllegalArgumentException
     */
    boolean removePartner(Long partnerId);

    /**
     * Remove the contributor with the given Identifier.
     *
     * @param contributorId - Long - the identifier of the contributor
     * @return boolean - true if the contributor has been correctly removed, false in the contributor doesn't exists.
     * @throws IllegalArgumentException
     */
    boolean removeContributor(Long contributorId);

    /**
     * Add an Item to the carousel
     *
     * @param user      - an instance of the User class
     * @param savedItem - Long - the identifier of the saved Item
     * @return - an instance of the User class
     * @throws IllegalArgumentException
     */
    User addCarouselItem(User user, SavedItem savedItem);

    /**
     * @param user       - an instance of the User class
     * @param editorPick - an instance of the EditorPick class
     * @return - an instance of the User class
     */
    User addEditorPick(User user, EditorPick editorPick);

    /**
     * @param user
     * @param carouselItem
     * @return
     */
    User addCarouselItem(User user, CarouselItem carouselItem);

    /**
     * @param user
     * @param savedItem
     * @return
     */
    CarouselItem addCarouselItem(User user, Long savedItem);

    /**
     * @param savedSearchId
     * @return
     */
    SearchTerm addSearchTerm(Long savedSearchId);

    /**
     * @return
     */
    List<Partner> fetchPartners();

    /**
     * @return
     */
    List<Contributor> fetchContributors();

    /**
     * @param pageType
     * @param language
     * @return
     */
    StaticPage fetchStaticPage(StaticPageType pageType, Language language);

    /**
     * @param staticPageId
     * @param content
     * @return
     */
    StaticPage saveStaticPage(Long staticPageId, String content);

    /**
     * @param id
     * @return
     */
    Boolean removeCarouselItem(Long id);

    /**
     * @return
     */
    List<CarouselItem> fetchCarouselItems();

    /**
     * @param europeanaUri
     * @param savedItemId
     * @return
     */
    CarouselItem createCarouselItem(String europeanaUri, Long savedItemId);

    /**
     * @param savedItem
     */
    void removeFromCarousel(SavedItem savedItem);

    /**
     * @param savedItem
     * @return
     */
    boolean addCarouselItem(SavedItem savedItem);

    /**
     * @return
     */
    List<EditorPick> fetchEditorPicksItems();

    /**
     * @param savedSearch
     * @return
     * @throws Exception
     */
    EditorPick createEditorPick(SavedSearch savedSearch) throws Exception;

    /**
     * @param savedSearch
     */
    void removeFromEditorPick(SavedSearch savedSearch);

    /**
     * @param language
     * @param term
     * @return
     */
    boolean addSearchTerm(Language language, String term);

    /**
     * @param savedSearch
     * @return
     */
    boolean addSearchTerm(SavedSearch savedSearch);

    /**
     * @param language
     * @return
     */
    List<String> fetchSearchTerms(Language language);

    /**
     * @param language
     * @param term
     * @return
     */
    boolean removeSearchTerm(Language language, String term);

    /**
     * Get the list of all searched terms
     *
     * @return LIST - a List of searched terms
     */
    List<SearchTerm> getAllSearchTerms();

}