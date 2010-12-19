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

import eu.delving.domain.Language;

import java.util.Date;
import java.util.List;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public interface PersonStorage {

    Person createPerson(String email);
    Person authenticate(String email, String password);
    List<Person> getPeople(String pattern);
    List<Person> getPeople();
    Person byEmail(String email);

    public interface Person {
        void setEmail(String email);
        String getEmail();
        void setPassword(String password);
        String getHashedPassword();
        void setEnabled(boolean enabled);
        boolean isEnabled();
        void setFirstName(String firstName);
        String getFirstName();
        void setLastName(String lastName);
        String getLastName();
        void setLastLogin(Date lastLogin);
        Date getLastLogin();
        Item addItem(String author, String title, Language language);
        List<Item> getItems();
        Search addSearch(String query, String queryString, Language language);
        List<Search> getSearches();
        void save();
        void delete();

        String EMAIL = "email";
        String PASSWORD = "password";
        String ENABLED = "enabled";
        String FIRST_NAME = "first_name";
        String LAST_NAME = "last_name";
        String LAST_LOGIN = "last_login";
        String ITEMS = "items";
        String SEARCHES = "searches";
    }

    public interface Item {
        String getAuthor();
        String getTitle();
        Language getLanguage();
        // todo: identify the actual object (was europeanaId
        Date getDateSaved();
        void remove();

        String AUTHOR = "author";
        String TITLE = "title";
        String LANGUAGE = "lang";
        String DATE_SAVED = "date_saved";
    }

    public interface Search {
        String getQuery();
        String getQueryString();
        Language getLanguage();
        Date getDateSaved();
        void remove();

        String QUERY = "query";
        String QUERY_STRING = "query_string";
        String LANGUAGE = "lang";
        String DATE_SAVED = "date_saved";
    }
}
