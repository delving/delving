package eu.europeana.core.database.domain;

import eu.europeana.core.querymodel.query.DocType;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @author Cesare Corcordia <cesare.concordia@isti.cnr.it>
 */

@Entity
public class SocialTag implements Serializable {
    private static final long serialVersionUID = -3635227115883742004L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @Column(length = 60)
    private String tag;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaved;

    @Column(length = 256)
    private String europeanaUri;

    @Column(length = 120)
    private String title;

    @Column(length = 256)
    private String europeanaObject;

    @ManyToOne
    @JoinColumn(name = "europeanaid", nullable = false)
    @Index(name = "socialtag_europeanaid_index")
    private EuropeanaId europeanaId;

    @Column(length = 3)
    @Enumerated(EnumType.STRING)
    private Language language;


    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private DocType docType = DocType.UNKNOWN;

    @ManyToOne
    @JoinColumn(name = "userid", insertable = false, updatable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag.toLowerCase();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEuropeanaObject() {
        return europeanaObject;
    }

    public void setEuropeanaObject(String europeanaObject) {
        this.europeanaObject = europeanaObject;
    }

    public String getEuropeanaUri() {
        return europeanaUri;
    }

    public void setEuropeanaUri(String europeanaUri) {
        this.europeanaUri = europeanaUri;
    }

    public EuropeanaId getEuropeanaId() {
        return europeanaId;
    }

    public void setEuropeanaId(EuropeanaId europeanaId) {
        this.europeanaId = europeanaId;
        this.europeanaUri = europeanaId.getEuropeanaUri();
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

}