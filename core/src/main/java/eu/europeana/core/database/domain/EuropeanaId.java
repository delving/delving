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

import org.hibernate.annotations.Index;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Entity
public class EuropeanaId implements Serializable {
    private static final long serialVersionUID = 7542358284490036076L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collectionid", nullable = false)
    private EuropeanaCollection collection;

    @Column
    private Integer timesViewed;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastViewed;

    @Column(nullable = false)
    @Index(name = "orphan_index")
    private Boolean orphan = false;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @Index(name = "lastmodified_index")
    private Date lastModified;

    @Column(length = FieldSize.EUROPEANA_URI, unique = true)
    @Index(name = "europeanauri_index")
    private String europeanaUri;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "europeanaid")
    private List<SocialTag> socialTags;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "europeanaId")
    @JoinColumn(name = "europeanaid")
    private List<CarouselItem> carouselItems;

    @Column(nullable = true)
    private Float boostFactor;

    public EuropeanaId() {
        this.orphan = false;
    }

    public EuropeanaId(EuropeanaCollection collection) {
        this.collection = collection;
    }

    public Long getId() {
        return id;
    }

    public EuropeanaCollection getCollection() {
        return collection;
    }

    public String getEuropeanaUri() {
        return europeanaUri;
    }

    public void setEuropeanaUri(String europeanaUri) {
        this.europeanaUri = europeanaUri;
    }

    public Integer getTimesViewed() {
        if (timesViewed == null) {
            timesViewed = 0;
        }
        return timesViewed;
    }

    public void setTimesViewed(Integer timesViewed) {
        this.timesViewed = timesViewed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Date lastViewed) {
        this.lastViewed = lastViewed;
    }

    public boolean isOrphan() {
        return orphan == null ? false : orphan;
    }

    public void setOrphan(Boolean orphan) {
        this.orphan = orphan;
    }

    public List<SocialTag> getSocialTags() {
        if (socialTags == null) {
            socialTags = new ArrayList<SocialTag>();
        }
        return socialTags;
    }

    public void setSocialTags(List<SocialTag> socialTags) {
        this.socialTags = socialTags;
    }

    public List<CarouselItem> getCarouselItems() {
        if (carouselItems == null) {
            carouselItems = new ArrayList<CarouselItem>();
        }
        return carouselItems;
    }

    public void setCarouselItems(List<CarouselItem> carouselItems) {
        this.carouselItems = carouselItems;
    }

//    @Transactional
//    public boolean hasCarouselItem() {
//        boolean foundCarousel = false;
//        if (carouselItems != null && carouselItems.size() != 0) {
//            foundCarousel = true;
//        }
//        return foundCarousel;
//    }
//
//    public void setCarouselItems(List<CarouselItem> carouselItems) {
//        this.carouselItems = carouselItems;
//    }

    public Float getBoostFactor() {
        return boostFactor;
    }

    public void setBoostFactor(Float boostFactor) {
        this.boostFactor = boostFactor;
    }

    public String toString() {
        return "EuropeanaId(" + europeanaUri + ")";
    }
}