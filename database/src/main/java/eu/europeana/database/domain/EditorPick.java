package eu.europeana.database.domain;

import org.hibernate.annotations.Index;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
public class EditorPick implements Serializable {
    private static final long serialVersionUID = 6318923149012810813L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @Column(length = 200)
    private String query;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaved;

    @ManyToOne
    @JoinColumn(name = "userid", insertable = false, updatable = false)
    private User user;

    @OneToOne(cascade = CascadeType.PERSIST, mappedBy = "editorPick", optional = true)
    private SavedSearch savedSearch;


    public Long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query.toLowerCase();
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

		public SavedSearch getSavedSearch() {
			return savedSearch;
		}

		public void setSavedSearch(SavedSearch savedSearch) {
			this.savedSearch = savedSearch;
		}

}