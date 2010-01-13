package eu.europeana.database.domain;

import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class EuropeanaObject implements Serializable {
    private static final long serialVersionUID = -736621601394073339L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @Column(length = 512, nullable = false)
    @Index(name = "objecturl")
    private String objectUrl;

    @Column
    private boolean error;

    @ManyToOne
    @JoinColumn(name = "europeanaid")
    @Index(name = "object_europeanaid_index")
    private EuropeanaId europeanaId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    public EuropeanaObject(EuropeanaId europeanaId, String objectUrl) {
        this.europeanaId = europeanaId;
        this.objectUrl = objectUrl;
        this.date = new Date();
    }

    public EuropeanaObject() {
    }

    public Long getId() {
        return id;
    }

    public String getObjectUrl() {
        return objectUrl;
    }

    public void setObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public EuropeanaId getEuropeanaId() {
        return europeanaId;
    }

    public void setEuropeanaId(EuropeanaId europeanaId) {
        this.europeanaId = europeanaId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String toString() {
        return objectUrl;
    }
}