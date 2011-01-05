/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.delving.core.storage.impl;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.core.storage.User;
import eu.delving.core.storage.UserRepo;
import eu.delving.domain.Language;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class UserRepoImpl implements UserRepo {
    private String databaseName = "users";

    @Autowired
    private Mongo mongo;

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    private DBCollection users() {
        return mongo.getDB(databaseName).getCollection(databaseName);
    }

    @Override
    public User createUser(String email) {
        DBObject object = new BasicDBObject(User.EMAIL, email);
        return new UserImpl(object);
    }

    @Override
    public void removeUser(String id) {
        users().remove(new BasicDBObject(User.ID, new ObjectId(id)));
    }

    @Override
    public User authenticate(String email, String password) {
        users().ensureIndex(new BasicDBObject(User.EMAIL, 1));
        DBObject query = new BasicDBObject();
        query.put(User.EMAIL, email);
        query.put(User.PASSWORD, hashPassword(password));
        DBObject userObject = users().findOne(query);
        return userObject != null ? new UserImpl(userObject) : null;
    }

    @Override
    public boolean isExistingUserName(String userName) {
        return users().findOne(new BasicDBObject(User.USER_NAME, userName)) != null;
    }

    @Override
    public boolean isProperUserName(String userName) {
        return userName != null && userName.matches("[a-z_0-9]{2,24}") && !userName.contains("__");
    }

    @Override
    public List<User> getUsers(String pattern) {
        return null;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<User>();
        for (DBObject personObject : users().find()) {
            users.add(new UserImpl(personObject));
        }
        return users;
    }

    @Override
    public User byEmail(String email) {
        users().ensureIndex(new BasicDBObject(User.EMAIL, 1));
        DBObject query = new BasicDBObject();
        query.put(User.EMAIL, email);
        DBObject personObject = users().findOne(query);
        return personObject != null ? new UserImpl(personObject) : null;
    }

    public class UserImpl implements User {
        private DBObject object;

        public UserImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public String getId() {
            return string(object.get(ID));
        }

        @Override
        public void setEmail(String email) {
            object.put(EMAIL, email);
        }

        @Override
        public String getEmail() {
            return string(object.get(EMAIL));
        }

        @Override
        public void setPassword(String password) {
            object.put(PASSWORD, hashPassword(password));
        }

        @Override
        public String getHashedPassword() {
            return string(object.get(PASSWORD));
        }

        @Override
        public void setRole(Role role) {
            object.put(ROLE, role.toString());
        }

        @Override
        public Role getRole() {
            return Role.valueOf((String) object.get(ROLE));
        }

        @Override
        public void setEnabled(boolean enabled) {
            object.put(ENABLED, enabled);
        }

        @Override
        public boolean isEnabled() {
            return (Boolean) object.get(ENABLED);
        }

        @Override
        public void setUserName(String userName) {
            if (!isProperUserName(userName)) {
                throw new RuntimeException("Must check if userName is proper first, this isn't: " + userName);
            }
            object.put(USER_NAME, userName);
        }

        @Override
        public String getUserName() {
            return string(object.get(USER_NAME));
        }

        @Override
        public void setFirstName(String firstName) {
            object.put(FIRST_NAME, firstName);
        }

        @Override
        public String getFirstName() {
            return string(object.get(FIRST_NAME));
        }

        @Override
        public void setLastName(String lastName) {
            object.put(LAST_LOGIN, lastName);
        }

        @Override
        public String getLastName() {
            return string(object.get(LAST_NAME));
        }

        @Override
        public void setLastLogin(Date lastLogin) {
            object.put(LAST_LOGIN, lastLogin);
        }

        @Override
        public Date getLastLogin() {
            return (Date) object.get(LAST_LOGIN);
        }

        @Override
        public Date getRegistrationDate() {
            ObjectId id = (ObjectId) object.get(ID);
            return new Date(id == null ? 0 : id.getTime());
        }

        @Override
        public Item addItem(String author, String title, Language language) {
            ItemImpl item = new ItemImpl(this, list(ITEMS).size());
            item.setAuthor(author);
            item.setTitle(title);
            item.setLanguage(language);
            list(ITEMS).add(item.getObject());
            return item;
        }

        @Override
        public List<Item> getItems() {
            List<Item> items = new ArrayList<Item>();
            int index = 0;
            for (Object element : list(ITEMS)) {
                items.add(new ItemImpl(this, (DBObject) element, index++));
            }
            return items;
        }

        @Override
        public Search addSearch(String query, String queryString, Language language) {
            SearchImpl search = new SearchImpl(this, list(SEARCHES).size());
            search.setQuery(query);
            search.setQueryString(queryString);
            search.setLanguage(language);
            list(SEARCHES).add(search.getObject());
            return search;
        }

        @Override
        public List<Search> getSearches() {
            List<Search> searches = new ArrayList<Search>();
            int index = 0;
            for (Object element : (BasicDBList) object.get(User.SEARCHES)) {
                searches.add(new SearchImpl(this, (DBObject) element, index++));
            }
            return searches;
        }

        @Override
        public void save() {
            users().save(object);
        }

        @Override
        public void delete() {
            users().remove(object);
        }

        public BasicDBList list(String name) {
            BasicDBList list = (BasicDBList) object.get(name);
            if (list == null) {
                object.put(name, list = new BasicDBList());
            }
            return list;
        }
    }

    public class ItemImpl implements User.Item {
        private UserImpl person;
        private DBObject object;
        private int index;

        public ItemImpl(UserImpl person, DBObject object, int index) {
            this.person = person;
            this.object = object;
            this.index = index;
        }

        public ItemImpl(UserImpl person, int index) {
            this(person, new BasicDBObject(DATE_SAVED, new Date()), index);
        }

        public void setAuthor(String author) {
            object.put(AUTHOR, author);
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public String getAuthor() {
            return string(object.get(AUTHOR));
        }

        public void setTitle(String title) {
            object.put(TITLE, title);
        }

        @Override
        public String getTitle() {
            return string(object.get(TITLE));
        }

        public void setLanguage(Language language) {
            object.put(LANGUAGE, language.toString());
        }

        @Override
        public Language getLanguage() {
            return Language.valueOf((String) object.get(LANGUAGE));
        }

        // todo: identify the actual object (was europeanaId)

        @Override
        public Date getDateSaved() {
            return (Date) object.get(DATE_SAVED);
        }

        @Override
        public void remove() {
            person.list(User.ITEMS).remove(index);
        }

        public Object getObject() {
            return object;
        }
    }

    public class SearchImpl implements User.Search {
        private UserImpl person;
        private DBObject object;
        private int index;

        public SearchImpl(UserImpl person, DBObject object, int index) {
            this.person = person;
            this.object = object;
            this.index = index;
        }

        public SearchImpl(UserImpl person, int index) {
            this(person, new BasicDBObject(DATE_SAVED, new Date()), index);
        }

        public void setQuery(String query) {
            object.put(QUERY, query);
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public String getQuery() {
            return string(object.get(QUERY));
        }

        public void setQueryString(String queryString) {
            object.put(QUERY_STRING, queryString);
        }

        @Override
        public String getQueryString() {
            return string(object.get(QUERY_STRING));
        }

        public void setLanguage(Language language) {
            object.put(LANGUAGE, language.toString());
        }

        @Override
        public Language getLanguage() {
            return Language.valueOf((String) object.get(LANGUAGE));
        }

        @Override
        public Date getDateSaved() {
            return (Date) object.get(DATE_SAVED);
        }

        @Override
        public void remove() {
            person.list(User.SEARCHES).remove(index);
        }

        public Object getObject() {
            return object;
        }
    }

    private static String string(Object object) {
        return object == null ? "" : object.toString();
    }

    private static String hashPassword(String password) {
        return encoder.encodePassword(password, null);
    }

    private static MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("SHA-1");
}
