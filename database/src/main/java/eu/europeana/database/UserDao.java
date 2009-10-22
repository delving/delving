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

public interface UserDao {
    User fetchUserByEmail(String email);
    User addUser(User user);
    void removeUser(User user);
    void updateUser(User user);
    User refreshUser(User user);
    User addSavedSearch(User user, SavedSearch savedSearch);
    User addSocialTag(User user, SocialTag socialTag);
    User addEditorPick(User user, EditorPick editorPick);
    User addCarouselItem(User user, CarouselItem carouselItem);
    CarouselItem addCarouselItem(User user, Long savedItem);
    SearchTerm addSearchTerm(Long savedSearchId);
    boolean userNameExists(String userName);
    User remove(User user, Class<?> clazz, Long id);
    List<User> fetchUsers(String pattern);
    User fetchUserWhoPickedCarouselItem(String europeanaUri);
    User fetchUserWhoPickedEditorPick(String query);
    void setUserEnabled(Long userId, boolean enabled);
    void setUserToAdministrator(Long userId, boolean administrator);
    void markAsViewed(String europeanaUri);
    List<TagCount> getSocialTagCounts(String query);
    User removeCarouselItem(User user, Long savedItemId);
    User removeSearchTerm(User user, Long savedSearchId);

    User addCarouselItem(User user, SavedItem savedItem);

    User addSavedItem(User user, SavedItem savedItem, String europeanaUri);
}
