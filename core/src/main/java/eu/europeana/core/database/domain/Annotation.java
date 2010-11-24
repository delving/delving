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

import eu.delving.domain.Language;
import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */

@Entity
public class Annotation implements Serializable {

	private static final long serialVersionUID = 1704555721067084065L;

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @Column
    private Long predecessorId;

    @Column(length = FieldSize.IDENTIFIER)
    @Enumerated(EnumType.STRING)
    private AnnotationType type;

    @ManyToOne
    @JoinColumn(name = "europeanaid", nullable = false)
    @Index(name = "annotation_europeanaid_index")
    private EuropeanaId europeanaId;

    @Column(length = FieldSize.LANGUAGE)
    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(columnDefinition = "text")
    private String content;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;

    @OneToMany
    @JoinColumn(name="predecessorId")
	private List<Annotation> children;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaved;

    public Annotation() {
    	children = new ArrayList<Annotation>();
    }
    
    public Long getId() {
        return id;
    }

    public Long getPredecessorId() {
        return predecessorId;
    }

    public void setPredecessorId(Long predecessorId) {
        this.predecessorId = predecessorId;
    }

    public AnnotationType getType() {
        return type;
    }

    public void setType(AnnotationType type) {
        this.type = type;
    }

    public EuropeanaId getEuropeanaId() {
        return europeanaId;
    }

    public void setEuropeanaId(EuropeanaId europeanaId) {
        this.europeanaId = europeanaId;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }

	public List<Annotation> getChildren() {
		return children;
	}

	public void setChildren(List<Annotation> children) {
		this.children = children;
	}

}