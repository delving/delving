/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Entity
public class EuropeanaCollection implements Serializable {
    private static final long serialVersionUID = -498740782430114939L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = 256, unique = true)
    @Index(name = "collectionnameindex")
    private String name;

    @Lob
    private String description;

    @Column(length = 60)
    private String fileUserName;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date collectionLastModified;

    @Column(length = 256)
    private String fileName;

    @Column(length = 25)
    @Enumerated(EnumType.STRING)
    private ImportFileState fileState = ImportFileState.NONEXISTENT;

    @Column(length = 25)
    @Enumerated(EnumType.STRING)
    private CollectionState collectionState = CollectionState.EMPTY;

    @Column
    private Integer totalRecords;

    @Column
    private Integer totalObjects;

    @Column
    private Integer totalOrphans;

    @Lob
    private String importError;

    public EuropeanaCollection(Long id) {
        this.id = id;
    }

    public EuropeanaCollection() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileUserName() {
        return fileUserName;
    }

    public void setFileUserName(String fileUserName) {
        this.fileUserName = fileUserName;
    }

    public Date getCollectionLastModified() {
        return collectionLastModified;
    }

    public void setCollectionLastModified(Date collectionLastModified) {
        this.collectionLastModified = collectionLastModified;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ImportFileState getFileState() {
        return fileState;
    }

    public void setFileState(ImportFileState fileState) {
        this.fileState = fileState;
    }

    public CollectionState getCollectionState() {
        return collectionState;
    }

    public void setCollectionState(CollectionState collectionState) {
        this.collectionState = collectionState;
    }

    public Integer getTotalRecords() {
        if (totalRecords == null) totalRecords = 0;
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getTotalObjects() {
        if (totalObjects == null) totalObjects = 0;
        return totalObjects;
    }

    public void setTotalObjects(Integer totalObjects) {
        this.totalObjects = totalObjects;
    }

    public Integer getTotalOrphans() {
        if (totalOrphans == null) totalOrphans = 0;
        return totalOrphans;
    }

    public void setTotalOrphans(Integer totalOrphans) {
        this.totalOrphans = totalOrphans;
    }

    public String getImportError() {
        return importError;
    }

    public void setImportError(String importError) {
        this.importError = importError;
    }

    @Override
    public String toString() {
        return "EuropeanaCollection{" + name + "}";
    }
}