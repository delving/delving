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

package eu.europeana.database.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Mar 9, 2009: 3:57:00 PM
 */
@Entity
@XStreamAlias("Contributor")
public class Contributor implements Serializable {
    private static final long serialVersionUID = 8476127088566577884L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    @XStreamOmitField
    private Long id;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    @XStreamAlias("country")
    private Country country;

    @Column(unique = true, nullable = false, length = 12)
    @XStreamAlias("providerId")
    private String providerId;

    @Column(nullable = false, length = 256)
    @XStreamAlias("originalName")
    private String originalName;

    @Column(length = 256)
    @XStreamAlias("englishName")
    private String englishName;

    @Column(length = 10)
    @XStreamAlias("acronym")
    private String acronym;

    @Column(length = 5)
    @XStreamAlias("numberOfPartners")
    private String numberOfPartners;

    @Column(length = 256)
    @XStreamAlias("url")
    private String url;

    public Contributor() {
    }

    public Contributor(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getNumberOfPartners() {
        return numberOfPartners;
    }

    public void setNumberOfPartners(String numberOfPartners) {
        this.numberOfPartners = numberOfPartners;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
