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

public class PersonStorageImpl implements PersonStorage {

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

    public Person authenticate(String email, String password) {
        persons().ensureIndex(new BasicDBObject(Person.EMAIL, 1));
        DBObject query = new BasicDBObject();
        query.put(Person.EMAIL, email);
        query.put(Person.PASSWORD, hashPassword(password));
        DBObject personObject = persons().findOne(query);
        return personObject != null ? new PersonImpl(personObject) : null;
    }

    public List<Person> getPeople(String pattern) {
        return null;
    }

    public List<Person> getPeople() {
        List<Person> persons = new ArrayList<Person>();
        for (DBObject personObject : persons().find()) {
            persons.add(new PersonImpl(personObject));
        }
        return persons;
    }

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

        public void setEmail(String email) {
            object.put(EMAIL, email);
        }

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

        public void setEnabled(boolean enabled) {
            object.put(ENABLED, enabled);
        }

        public boolean isEnabled() {
            return (Boolean) object.get(ENABLED);
        }

        public void setFirstName(String firstName) {
            object.put(FIRST_NAME, firstName);
        }

        public String getFirstName() {
            return (String) object.get(FIRST_NAME);
        }

        public void setLastName(String lastName) {
            object.put(LAST_LOGIN, lastName);
        }

        public String getLastName() {
            return (String) object.get(LAST_NAME);
        }

        public void setLastLogin(Date lastLogin) {
            object.put(LAST_LOGIN, lastLogin);
        }

        public Date getLastLogin() {
            return (Date) object.get(LAST_LOGIN);
        }

        public Item addItem(String author, String title, Language language) {
            ItemImpl item = new ItemImpl(this);
            item.setAuthor(author);
            item.setTitle(title);
            item.setLanguage(language);
            return item;
        }

        public List<Item> getItems() {
            List<Item> items = new ArrayList<Item>();
            for (Object element : (BasicDBList) object.get(Person.ITEMS)) {
                items.add(new ItemImpl(this, (DBObject) element));
            }
            return items;
        }

        public Search addSearch(String query, String queryString, Language language) {
            SearchImpl search = new SearchImpl(this);
            search.setQuery(query);
            search.setQueryString(queryString);
            search.setLanguage(language);
            return search;
        }

        public List<Search> getSearches() {
            List<Search> searches = new ArrayList<Search>();
            for (Object element : (BasicDBList) object.get(Person.SEARCHES)) {
                searches.add(new SearchImpl(this, (DBObject) element));
            }
            return searches;
        }

        public void save() {
            persons().save(object);
        }

        public void delete() {
            persons().remove(object);
        }
    }

    public class ItemImpl implements Item {
        private PersonImpl person;
        private DBObject object;

        public ItemImpl(PersonImpl person, DBObject object) {
            this.person = person;
            this.object = object;
        }

        public ItemImpl(PersonImpl person) {
            this(person, new BasicDBObject(DATE_SAVED, new Date()));
        }

        public void setAuthor(String author) {
            object.put(AUTHOR, author);
        }

        public String getAuthor() {
            return (String) object.get(AUTHOR);
        }

        public void setTitle(String title) {
            object.put(TITLE, title);
        }

        public String getTitle() {
            return (String) object.get(TITLE);
        }

        public void setLanguage(Language language) {
            object.put(LANGUAGE, language.toString());
        }

        public Language getLanguage() {
            return Language.valueOf((String) object.get(LANGUAGE));
        }

        // todo: identify the actual object (was europeanaId)

        public Date getDateSaved() {
            return (Date) object.get(DATE_SAVED);
        }

        public void add() {
            BasicDBList list = (BasicDBList) person.object.get(Person.ITEMS);
            list.add(object);
        }

        public void remove() {
            BasicDBList list = (BasicDBList) person.object.get(Person.ITEMS);
            list.remove(object); // todo: will this work?
        }
    }

    public class SearchImpl implements Search {
        private PersonImpl person;
        private DBObject object;

        public SearchImpl(PersonImpl person, DBObject object) {
            this.person = person;
            this.object = object;
        }

        public SearchImpl(PersonImpl person) {
            this(person, new BasicDBObject(DATE_SAVED, new Date()));
        }

        public void setQuery(String query) {
            object.put(QUERY, query);
        }

        public String getQuery() {
            return (String) object.get(QUERY);
        }

        public void setQueryString(String queryString) {
            object.put(QUERY_STRING, queryString);
        }

        public String getQueryString() {
            return (String) object.get(QUERY_STRING);
        }

        public void setLanguage(Language language) {
            object.put(LANGUAGE, language.toString());
        }

        public Language getLanguage() {
            return Language.valueOf((String) object.get(LANGUAGE));
        }

        public Date getDateSaved() {
            return (Date) object.get(DATE_SAVED);
        }

        public void add() {
            BasicDBList list = (BasicDBList) person.object.get(Person.SEARCHES);
            list.add(object);
        }

        public void remove() {
            BasicDBList list = (BasicDBList) person.object.get(Person.SEARCHES);
            // todo: remove
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
