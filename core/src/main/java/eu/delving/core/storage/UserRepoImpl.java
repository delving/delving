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

package eu.delving.core.storage;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.delving.domain.Language;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class UserRepoImpl implements UserRepo {

    private String databaseName = "persons";

    @Autowired
    private Mongo mongo;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    private DBCollection persons() {
        return mongo.getDB(databaseName).getCollection(databaseName);
    }

    @Override
    public Person createPerson(String email) {
        DBObject object = new BasicDBObject(Person.EMAIL, email);
        return new PersonImpl(object);
    }

    @Override
    public Person authenticate(String email, String password) {
        persons().ensureIndex(new BasicDBObject(Person.EMAIL, 1));
        DBObject query = new BasicDBObject();
        query.put(Person.EMAIL, email);
        query.put(Person.PASSWORD, hashPassword(password));
        DBObject personObject = persons().findOne(query);
        return personObject != null ? new PersonImpl(personObject) : null;
    }

    @Override
    public List<Person> getPeople(String pattern) {
        return null;
    }

    @Override
    public List<Person> getPeople() {
        List<Person> persons = new ArrayList<Person>();
        for (DBObject personObject : persons().find()) {
            persons.add(new PersonImpl(personObject));
        }
        return persons;
    }

    @Override
    public Person byEmail(String email) {
        persons().ensureIndex(new BasicDBObject(Person.EMAIL, 1));
        DBObject query = new BasicDBObject();
        query.put(Person.EMAIL, email);
        DBObject personObject = persons().findOne(query);
        return personObject != null ? new PersonImpl(personObject) : null;
    }

    public class PersonImpl implements Person {
        private DBObject object;

        public PersonImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public void setEmail(String email) {
            object.put(EMAIL, email);
        }

        @Override
        public String getEmail() {
            return (String) object.get(EMAIL);
        }

        @Override
        public void setPassword(String password) {
            object.put(PASSWORD, hashPassword(password));
        }

        @Override
        public String getHashedPassword() {
            return (String) object.get(PASSWORD);
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
        public void setFirstName(String firstName) {
            object.put(FIRST_NAME, firstName);
        }

        @Override
        public String getFirstName() {
            return (String) object.get(FIRST_NAME);
        }

        @Override
        public void setLastName(String lastName) {
            object.put(LAST_LOGIN, lastName);
        }

        @Override
        public String getLastName() {
            return (String) object.get(LAST_NAME);
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
            for (Object element : (BasicDBList) object.get(Person.SEARCHES)) {
                searches.add(new SearchImpl(this, (DBObject) element, index++));
            }
            return searches;
        }

        @Override
        public void save() {
            persons().save(object);
        }

        @Override
        public void delete() {
            persons().remove(object);
        }

        public BasicDBList list(String name) {
            BasicDBList list = (BasicDBList) object.get(name);
            if (list == null) {
                object.put(name, list = new BasicDBList());
            }
            return list;
        }
    }

    public class ItemImpl implements Item {
        private PersonImpl person;
        private DBObject object;
        private int index;

        public ItemImpl(PersonImpl person, DBObject object, int index) {
            this.person = person;
            this.object = object;
            this.index = index;
        }

        public ItemImpl(PersonImpl person, int index) {
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
            return (String) object.get(AUTHOR);
        }

        public void setTitle(String title) {
            object.put(TITLE, title);
        }

        @Override
        public String getTitle() {
            return (String) object.get(TITLE);
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
            person.list(Person.ITEMS).remove(index);
        }

        public Object getObject() {
            return object;
        }
    }

    public class SearchImpl implements Search {
        private PersonImpl person;
        private DBObject object;
        private int index;

        public SearchImpl(PersonImpl person, DBObject object, int index) {
            this.person = person;
            this.object = object;
            this.index = index;
        }

        public SearchImpl(PersonImpl person, int index) {
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
            return (String) object.get(QUERY);
        }

        public void setQueryString(String queryString) {
            object.put(QUERY_STRING, queryString);
        }

        @Override
        public String getQueryString() {
            return (String) object.get(QUERY_STRING);
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
            person.list(Person.SEARCHES).remove(index);
        }

        public Object getObject() {
            return object;
        }
    }

    private static String hashPassword(String password) {
        MessageDigest messageDigest = getMessageDigest();
        byte[] digest;
        try {
            digest = messageDigest.digest(password.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported!");
        }
        return new String(encode(messageDigest.digest(digest)));
    }

    private static MessageDigest getMessageDigest() throws IllegalArgumentException {
        try {
            return MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm [SHA-1]");
        }
    }

    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static char[] encode(byte[] bytes) {
        final int nBytes = bytes.length;
        char[] result = new char[2*nBytes];
        int j = 0;
        for (int i=0; i < nBytes; i++) {
            // Char for top 4 bits
            result[j++] = HEX[(0xF0 & bytes[i]) >>> 4 ];
            // Bottom 4
            result[j++] = HEX[(0x0F & bytes[i])];
        }
        return result;
    }


}
