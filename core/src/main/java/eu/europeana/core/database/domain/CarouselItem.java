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

import eu.europeana.core.querymodel.query.BriefDoc;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.definitions.domain.Language;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Entity
public class CarouselItem implements Serializable {
    private static final long serialVersionUID = -9202570753387517106L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @Column(length = FieldSize.EUROPEANA_URI, unique = true)
    private String europeanaUri;

    @Column(length = FieldSize.TITLE)
    private String title;

    @Column(length = FieldSize.THUMBNAIL)
    private String thumbnail;

    @Column(length = FieldSize.CREATOR)
    private String creator;

    @Column(length = FieldSize.YEAR)
    private String year;

    @Column(length = FieldSize.THUMBNAIL)
    private String provider;

    @Column(length = FieldSize.LANGUAGE)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(length = FieldSize.DOCTYPE)
    @Enumerated(EnumType.STRING)
    private DocType type;

    @ManyToOne
    @JoinColumn(name = "europeanaid")
    private EuropeanaId europeanaId;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, mappedBy = "carouselItem")
    private SavedItem savedItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEuropeanaUri() {
        return europeanaUri;
    }

    public void setEuropeanaUri(String europeanaUri) {
        this.europeanaUri = europeanaUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Language getLanguage() {
        if (language == null) {
            language = Language.EN;
        }
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public DocType getType() {
        return type;
    }

    public void setType(DocType type) {
        this.type = type;
    }

    public EuropeanaId getEuropeanaId() {
        return europeanaId;
    }

    public void setEuropeanaId(EuropeanaId europeanaId) {
        this.europeanaId = europeanaId;
    }

    public BriefDoc getDoc() {
        return new BriefDoc() {
            private int index;
            private String fullDocUrl;
            private int score;
            private String debugQuery;

            @Override
            public void setIndex(int index) {
                this.index = index;
            }

            @Override
            public void setFullDocUrl(String fullDocUrl) {
                this.fullDocUrl = fullDocUrl;
            }

            @Override
            public void setScore(int score) {
                this.score = score;
            }

            @Override
            public void setDebugQuery(String debugQuery) {
                this.debugQuery = debugQuery;
            }

            @Override
            public int getIndex() {
                return index;
            }

            @Override
            public String getFullDocUrl() {
                return fullDocUrl;
            }

            @Override
            public String getId() {
                return europeanaUri;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getThumbnail() {
                return thumbnail;
            }

            @Override
            public String getCreator() {
                return creator;
            }

            @Override
            public String getYear() {
                return year;
            }

            @Override
            public String getProvider() {
                return provider;
            }

            @Override
            public String getLanguage() {
                if (language == null) {
                    language = Language.EN;
                }
                return language.getCode();
            }

            @Override
            public DocType getType() {
                return type;
            }

            @Override
            public int getScore() {
                return 0;
            }

            @Override
            public String getDebugQuery() {
                return null;
            }

        };
    }

    public SavedItem getSavedItem() {
        return savedItem;
    }

    public void setSavedItem(SavedItem savedItem) {
        this.savedItem = savedItem;
    }

    @Override
    public String toString() {
        return "CarouselItem{" +
                "europeanaUri='" + europeanaUri + '\'' +
                ", title='" + title + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", creator='" + creator + '\'' +
                '}';
    }
}
