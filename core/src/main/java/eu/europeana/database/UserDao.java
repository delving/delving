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

/**
 * This interface describes the database access to users and all the things that they can save, such as
 * searches, items, tags.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface UserDao {

    /**
     * A user logs in to the system using this method, and returns null if there is no match
     * between email and password.
     *
     * @param email address
     * @param password unhashed
     * @return the user if authenticated, otherwise null
     */

    User authenticateUser(String email, String password);

    /**
     * Fetch a number of user objects that contain the given pattern in either their user name
     * first name, or email address, all case-insensitive.  Used for suggestion list.
     *
     * @param pattern a few letters
     * @return a list of matching users
     */

    List<User> fetchUsers(String pattern);

    /**
     * Check purely to see if a particular username exists.  This is to prevent duplication.
     *
     * @param userName to check
     * @return true if it exists
     */

    boolean userNameExists(String userName);

    /**
     * Fetch the user having the given email address
     *
     * @param email address
     * @return the user object, or null if nothing was found
     */

    User fetchUserByEmail(String email);

    /**
     * Add a user to the system, presumably all filled in with the necessary stuff
     *
     * @param user the new user
     * @return the stored version of it (with id!)
     */

    User addUser(User user);

    /**
     * Remove a user from the system, using its id to find it.
     *
     * @param user who is to be removed?
     */

    void removeUser(User user);

    /**
     * Change the fields of this user using the new values in the object passed in.
     * <p>
     * Note that special care is taken of the password.  If it is empty, the existing hashed password
     * is used but if a value is present in this field, it is hashed and persisted, becoming the new password.
     *
     * @param user the user containing some changes
     * @return the changed version as it is now persisted
     */

    User updateUser(User user);

    /**
     * Add a social tag for the given user
     *
     * @param user the user getting the social tag
     * @param socialTag the social tag being added
     * @return the user with its new social tag
     */

    User addSocialTag(User user, SocialTag socialTag);

    /**
     * Get a list of all the social tags used which match the pattern along with how frequently they appear.
     *
     * @param query the pattern, sanitized and surrounded by % in this method
     * @return the list of counts
     */

    List<TagCount> getSocialTagCounts(String query);

    /**
     * Remove a social tag from the user
     *
     * @param socialTagId the internal id of the social tag
     * @return the user with the tag removed
     */

    User removeSocialTag(Long socialTagId);

    /**
     * Add a saved item to the given user.  Ths saved item needs to be linked to a particular EuropeanaID so
     * the URI of that is passed in.
     *
     * @param user who is getting this added?
     * @param savedItem the thing to add
     * @param europeanaUri identify the EuropeanaId to which the saved item refers
     * @return the user with the item added
     */

    User addSavedItem(User user, SavedItem savedItem, String europeanaUri);

    /**
     * Get all the saved items for the given user
     *
     * @param userId which user
     * @return their list of saved items
     */

    List<SavedItem> fetchSavedItems(Long userId);

    /**
     * Fetch a single saved item when its id is known.  Used for creating carousel items
     *
     * @param savedItemId the internal id
     * @return the whole saved item
     */

    SavedItem fetchSavedItemById(Long savedItemId);

    /**
     * Removed a specific saved item from a user object.
     *
     * @param savedItemId internal id
     * @return the user with the saved item removed
     */

    User removeSavedItem(Long savedItemId);

    /**
     * Add a saved search to a user
     *
     * @param user who to add to?
     * @param savedSearch what to add
     * @return the user with the saved search added
     */

    User addSavedSearch(User user, SavedSearch savedSearch);

    /**
     * Fetch the saved searches for the given user
     *
     * @param user whose saved searches do we want
     * @return the list of saved searches
     */

    List<SavedSearch> fetchSavedSearches(User user);

    /**
     * Fetch a particular saved search by its id
     * @param savedSearchId internal id
     * @return the object
     */

    SavedSearch fetchSavedSearchById(Long savedSearchId);

    /**
     * Remove a saved search when its id is known
     *
     * @param savedSearchId which saved search, internal id
     * @return the User without the saved search
     */

    User removeSavedSearch(Long savedSearchId);

}
