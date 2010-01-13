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

@Entity
@XStreamAlias("Translation")
public class Translation implements Serializable {
    private static final long serialVersionUID = 4627185521023758385L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @XStreamOmitField
    private Long id;

    @Column(length = 3)
    @Enumerated(EnumType.STRING)
    @Index(name = "translationlanguageindex")
    @XStreamAlias("language")
    private Language language;

    @Lob
    @XStreamAlias("value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "messagekeyid", insertable = false, updatable = false)
    @XStreamAlias("messageKey")
    private MessageKey messageKey;

    public Translation() {
    }

    public Translation(MessageKey messageKey, Language language, String value) {
        this.language = language;
        this.value = value;
        this.messageKey = messageKey;
    }

    public Long getId() {
        return id;
    }

    public Language getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public void setValue(String value) {
        this.value = value;
    }
}