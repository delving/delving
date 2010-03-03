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

import eu.europeana.core.querymodel.query.DocType;

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
public class SavedItem implements Serializable {
    private static final long serialVersionUID = -7059004310525816113L;
                         
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = FieldSize.TITLE)
    private String title;

    @Column(length = FieldSize.AUTHOR)
    private String author;

    @Column(length = FieldSize.DOCTYPE)
    @Enumerated(EnumType.STRING)
    private DocType docType = DocType.UNKNOWN;

    @ManyToOne
    @JoinColumn(name = "europeanaid")
    private EuropeanaId europeanaId;

    @Column(length = FieldSize.EUROPEANA_OBJECT)
    private String europeanaObject;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaved;

    @Column(length = FieldSize.LANGUAGE)
    @Enumerated(EnumType.STRING)
    private Language language;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "carouselitemid")
    private CarouselItem carouselItem;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "userid", insertable = false, updatable = false)
    private User user;

    public CarouselItem getCarouselItem() {
        return carouselItem;
    }

    public boolean hasCarouselItem() {
        return carouselItem != null;
    }

    public void setCarouselItem(CarouselItem carouselItem) {
        this.carouselItem = carouselItem;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }

    public EuropeanaId getEuropeanaId() {
        return europeanaId;
    }

    public void setEuropeanaId(EuropeanaId europeanaId) {
        this.europeanaId = europeanaId;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public String getEuropeanaObject() {
        return europeanaObject;
    }

    public void setEuropeanaObject(String europeanaObject) {
        this.europeanaObject = europeanaObject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
