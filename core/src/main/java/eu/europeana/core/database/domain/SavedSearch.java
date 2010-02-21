package eu.europeana.core.database.domain;

import javax.persistence.*;
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
    @Column(length = 200)
    private String query;

    /*
     * QuerySring is the full path of query parameters.
     */
    @Column(length = 200)
    private String queryString;

    @Column(length = 3)
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
        return searchTerm == null ? false : true;
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