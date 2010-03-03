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

package eu.europeana.core.database.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;


/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @author Cesare Corcordia <cesare.concordia@isti.cnr.it>
 */

@Entity
public class SavedSearch implements Serializable {
    private static final long serialVersionUID = 667805541628354454L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /*
     * query is the just the input from the search box.
     */
    @Column(length = FieldSize.QUERY)
    private String query;

    /*
     * QuerySring is the full path of query parameters.
     */
    @Column(length = FieldSize.QUERY_STRING)
    private String queryString;

    @Column(length = FieldSize.LANGUAGE)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaved;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "searchtermid")
    private SearchTerm searchTerm;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "userid", insertable = false, updatable = false)
    private User user;


    public Long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }

    public SearchTerm getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(SearchTerm searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Boolean hasSearchTerm() {
        return searchTerm != null;
    }

    public SearchTerm createSearchTerm() {
        SearchTerm searchTerm = new SearchTerm();
        searchTerm.setDate(new Date());
        searchTerm.setLanguage(language);
        searchTerm.setProposedSearchTerm(queryString);
        searchTerm.setUriQueryString(query);
        searchTerm.setSavedSearch(this);
        this.setSearchTerm(searchTerm);
        return searchTerm;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}