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

package eu.delving.core.storage.annotation;

import eu.delving.domain.Language;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */

public class Annotation implements Serializable {

	private static final long serialVersionUID = 1704555721067084065L;

    private Long id;

    private Long predecessorId;

    private AnnotationType type;

//    private EuropeanaId europeanaId;

    private Language language;

    private String content;

//    @ManyToOne
//    @JoinColumn(name = "userid")
//    private User user;

	private List<Annotation> children;

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

//    public EuropeanaId getEuropeanaId() {
//        return europeanaId;
//    }
//
//    public void setEuropeanaId(EuropeanaId europeanaId) {
//        this.europeanaId = europeanaId;
//    }

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

//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }

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