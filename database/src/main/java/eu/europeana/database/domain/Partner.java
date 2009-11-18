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
@XStreamAlias("Partner")
public class Partner implements Serializable {
    private static final long serialVersionUID = 1031789766698810309L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    @XStreamOmitField
    private Long id;

    @Column(nullable = false, length = 255)
    @XStreamAlias("name")
    private String name;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    @XStreamAlias("sector")
    private PartnerSector sector;

    @Column(length = 255)
    @XStreamAlias("url")
    private String url;

    public Partner() {
    }

    public Partner(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartnerSector getSector() {
        return sector;
    }

    public void setSector(PartnerSector sector) {
        this.sector = sector;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}