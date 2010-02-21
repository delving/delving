package eu.europeana.core.database.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @author Cesare Corcordia <cesare.concordia@isti.cnr.it>
 */

@Entity
public class SearchTerm implements Serializable {
    private static final long serialVersionUID = -7489418085843563526L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @Column(nullable = false, length = 64)
    private String proposedSearchTerm;

    @Column(nullable = true, length = 256)
    private String uriQueryParameters;

    @Column(length = 3)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, mappedBy = "searchTerm")
    private SavedSearch savedSearch;

    public Long getId() {
        return id;
    }

    public String getProposedSearchTerm() {
        return proposedSearchTerm;
    }

    public void setProposedSearchTerm(String proposedSearchTerm) {
        this.proposedSearchTerm = proposedSearchTerm;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SavedSearch getSavedSearch() {
        return savedSearch;
    }

    public void setSavedSearch(SavedSearch savedSearch) {
        this.savedSearch = savedSearch;
    }

    public String getUriQueryParameters() {
        return uriQueryParameters;
    }

    public void setUriQueryString(String uriQueryParameters) {
        this.uriQueryParameters = uriQueryParameters;
    }


}