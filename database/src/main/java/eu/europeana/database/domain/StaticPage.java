/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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
 * @since Mar 16, 2009: 3:49:12 PM
 */

@Entity
@XStreamAlias("StaticPage")
public class StaticPage implements Serializable {

    private static final long serialVersionUID = -4668564632823349132L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @XStreamOmitField
    private Long id;

    @Column(length = 26)
    @Enumerated(EnumType.STRING)
    @XStreamAlias("pageType")
    private StaticPageType pageType;

    @Column(length=3)
    @Enumerated(EnumType.STRING)
    @XStreamAlias("language")
    private Language language;

    @Lob
    @XStreamAlias("content")
    private String content;

    public StaticPage() {
    }

    public StaticPage(Long id, StaticPageType pageType, Language language, String content) {
        this.id = id;
        this.pageType = pageType;
        this.language = language;
        this.content = content;
    }

    public StaticPage(StaticPageType pageType, Language language) {
        this.pageType = pageType;
        this.language = language;
        this.content = "<h1>No translation available</h1>";
    }

    public Long getId() {
        return id;
    }

    public StaticPageType getPageType() {
        return pageType;
    }

    public Language getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
