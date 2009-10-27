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

import eu.europeana.database.domain.Contributor;
import eu.europeana.database.domain.Partner;

import java.util.List;

/**
 * @author vitali
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */
public interface StaticInfoDao {

    List<Partner> getAllPartnerItems();
    List<Contributor> getAllContributorItems();
    void saveContributor(Contributor contributor);
    void savePartner(Partner partner);


    // todo: add these (implementations in DashboardDaoImpl)
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

    // todo: add these: (implementations in UserDaoImpl)
//    User removeCarouselItem(User user, Long savedItemId);
//    User removeSearchTerm(User user, Long savedSearchId);
//    User addCarouselItem(User user, SavedItem savedItem);
//    User addEditorPick(User user, EditorPick editorPick);
//    User addCarouselItem(User user, CarouselItem carouselItem);
//    CarouselItem addCarouselItem(User user, Long savedItem);
//    SearchTerm addSearchTerm(Long savedSearchId);

    // todo: add these (implementations in MessageDao)
//    StaticPage fetchStaticPage (Language language, String pageName);
//    void setStaticPage(StaticPageType pageType, Language language, String content);
//    List<StaticPage> getAllStaticPages();
//    List<MessageKey> getAllTranslationMessages();

    // todo: add this (impl in SearchTermDaoImpl
//    List<SearchTerm> getAllSearchTerms();

}