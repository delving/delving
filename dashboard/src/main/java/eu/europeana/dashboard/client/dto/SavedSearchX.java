package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Interesting stuff from the saved search
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SavedSearchX implements IsSerializable {
    private Long id;
    private String queryString;
    private LanguageX language;

    public SavedSearchX(Long id, String queryString, LanguageX language) {
        this.id = id;
        this.queryString = queryString;
        this.language = language;
    }

    public SavedSearchX() {
    }

    public Long getId() {
        return id;
    }

    public String getQueryString() {
        return queryString;
    }

    public LanguageX getLanguage() {
        return language;
    }
}