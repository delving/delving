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
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@XStreamAlias("MessageKey")
public class MessageKey implements Serializable {
    private static final long serialVersionUID = -5640721284594257416L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @XStreamOmitField
    private Long id;

    @Column(length = 64)
    @Index(name = "messagekeyindex")
    @XStreamAlias("key")
    private String key;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "messagekeyid", nullable = false)
    private List<Translation> translations;

    public MessageKey() {
    }

    public MessageKey(String key) {
        this.key = key;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public List<Translation> getTranslations() {
        if (translations == null) {
            translations = new ArrayList<Translation>();
        }
        return translations;
    }

    public Translation getTranslation(Language language) {
        for (Translation translation : getTranslations()) {
            if (translation.getLanguage() == language) {
                return translation;
            }
        }
        return null;
    }

    public Translation setTranslation(Language language, String value) {
        Translation translation = getTranslation(language);
        if (translation != null) {
            translation.setValue(value);
            return translation;
        }
        else {
            translation = new Translation(this, language, value);
            getTranslations().add(translation);
            return translation;
        }
    }
}