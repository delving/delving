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

import eu.europeana.database.domain.CarouselItem;
import eu.europeana.database.domain.EditorPick;
import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SearchTerm;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.database.integration.TagCount;

import java.util.List;

public interface UserDao {

    User fetchUserByEmail(String email);
    User addUser(User user);
    void removeUser(User user);
    void updateUser(User user);
    User refreshUser(User user);
    boolean userNameExists(String userName);
    User remove(User user, Class<?> clazz, Long id);
    List<User> fetchUsers(String pattern);
    User fetchUserWhoPickedCarouselItem(String europeanaUri);
    User fetchUserWhoPickedEditorPick(String query);
    void setUserEnabled(Long userId, boolean enabled);
    void setUserToAdministrator(Long userId, boolean administrator);
    void markAsViewed(String europeanaUri);
    User addSocialTag(User user, SocialTag socialTag);
    List<TagCount> getSocialTagCounts(String query);
    User addSavedItem(User user, SavedItem savedItem, String europeanaUri);
    User addSavedSearch(User user, SavedSearch savedSearch);

   

    // todo: add these (implementations are in DashboardDaoImpl)
//    User fetchUser(String email, String password);
//    void setUserRole(Long userId, Role role);
//    void removeUser(Long userId);
//    User fetchUser(Long userId);
//    void setUserProjectId(Long userId, String projectId);
//    void setUserProviderId(Long userId, String providerId);
//    void setUserLanguages(Long userId, String languages);
//    List<SavedItem> fetchSavedItems(Long userId);
//    SavedItem fetchSavedItemById(Long id);
//    List<SavedSearch> fetchSavedSearches(Long userId);
//    SavedSearch fetchSavedSearchById(Long id);


}
