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
import eu.europeana.core.querymodel.query.DocType;

import java.util.Date;
import java.util.List;

/**
 * A platform user
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface User {
    String getId();
    void setEmail(String email);
    String getEmail();
    void setPassword(String password);
    String getHashedPassword();
    void setRole(Role role);
    Role getRole();
    void setEnabled(boolean enabled);
    boolean isEnabled();
    void setUserName(String userName);
    String getUserName();
    void setFirstName(String firstName);
    String getFirstName();
    void setLastName(String lastName);
    String getLastName();
    void setLastLogin(Date lastLogin);
    Date getLastLogin();
    Date getRegistrationDate();
    Item addItem(String author, String title, Language language, String delvingId, String europeanaId, DocType docType, String thumbnail);
    void removeItem(int index);
    List<Item> getItems();
    Search addSearch(String query, String queryString, Language language);
    void removeSearch(int index);
    List<Search> getSearches();
    void save();
    void delete();

    String ID = "_id";
    String EMAIL = "email";
    String PASSWORD = "password";
    String ROLE = "role";
    String ENABLED = "enabled";
    String USER_NAME = "user_name";
    String FIRST_NAME = "first_name";
    String LAST_NAME = "last_name";
    String LAST_LOGIN = "last_login";
    String ITEMS = "items";
    String SEARCHES = "searches";


    public enum Role {
        NONE,
        ROLE_USER,
        ROLE_RESEARCH_USER,
        ROLE_ADMINISTRATOR,
        ROLE_GOD
    }

    public interface Item {
        int getIndex();
        String getAuthor();
        String getTitle();
        Language getLanguage();
        String getDelvingId();
        String getEuropeanaId();
        DocType getDocType();
        String getThumbnail();
        Date getDateSaved();
        void remove();

        String AUTHOR = "author";
        String TITLE = "title";
        String LANGUAGE = "lang";
        String DELVING_ID = "delving_id";
        String EUROPEANA_ID = "europeana_id";
        String DOC_TYPE = "doc_type";
        String THUMBNAIL = "thumb";
        String DATE_SAVED = "date_saved";
    }

    public interface Search {
        int getIndex();
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
