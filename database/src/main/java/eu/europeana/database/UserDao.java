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

import eu.europeana.database.domain.SavedItem;
import eu.europeana.database.domain.SavedSearch;
import eu.europeana.database.domain.SocialTag;
import eu.europeana.database.domain.User;
import eu.europeana.database.integration.TagCount;

import java.util.List;

public interface UserDao {

    User fetchUser(String email, String password);
    List<User> fetchUsers(String pattern);
    boolean userNameExists(String userName);
    User fetchUserByEmail(String email);
    User fetchUserWhoPickedCarouselItem(String europeanaUri);
    User fetchUserWhoPickedEditorPick(String query);
    User addUser(User user);

    void removeUser(User user); // todo: remove this one or the one below
    void removeUser(Long userId);

    User updateUser(User user); // todo: remove this one or the one below
    User refreshUser(User user);

    // ==== Social Tags

    User addSocialTag(User user, SocialTag socialTag);
    List<TagCount> getSocialTagCounts(String query);
    User removeSocialTag(User user, Long id);

    // ==== Saved Items

    User addSavedItem(User user, SavedItem savedItem, String europeanaUri);
    List<SavedItem> fetchSavedItems(Long userId);
    SavedItem fetchSavedItemById(Long id);
    User removeSavedItems(User user, Long id);

    // ==== Saved Search

    User addSavedSearch(User user, SavedSearch savedSearch);
    List<SavedSearch> fetchSavedSearches(Long userId);
    SavedSearch fetchSavedSearchById(Long id);
    User removeSavedSearch(User user, Long id);

}
