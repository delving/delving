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

package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * A domain object to correspond with the EuropeanaCollection
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class EuropeanaCollectionX implements IsSerializable {
    private Long id;
    private String name;
    private String description;
    private String fileName;
    private String fileUserName;
    private Date collectionLastModified;
    private ImportFileX.State fileState = ImportFileX.State.NONEXISTENT;
    private CollectionStateX collectionState = CollectionStateX.EMPTY;
    private int totalRecords;
    private String importError;

    public EuropeanaCollectionX() {
    }

    public EuropeanaCollectionX(String name) {
        this.name = name;
    }

    public EuropeanaCollectionX(Long id, String name, String description, String fileName, Date collectionLastModified, String fileUserName, ImportFileX.State fileState, CollectionStateX collectionState, int totalRecords, String importError) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fileName = fileName;
        this.collectionLastModified = collectionLastModified;
        this.fileUserName = fileUserName;
        this.fileState = fileState;
        this.collectionState = collectionState;
        this.totalRecords = totalRecords;
        this.importError = importError;
    }

    public EuropeanaCollectionX(Long id, String name, String description, String fileName, Date collectionLastModified, String fileUserName, String fileState, String collectionState, int totalRecords, String importError) {
        this(
                id,
                name,
                description,
                fileName,
                collectionLastModified,
                fileUserName,
                ImportFileX.State.valueOf(fileState),
                CollectionStateX.valueOf(collectionState),
                totalRecords,
                importError
        );
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public ImportFileX.State getFileState() {
        return fileState;
    }

    public void setFileState(ImportFileX.State fileState) {
        this.fileState = fileState;
    }

    public CollectionStateX getCollectionState() {
        return collectionState;
    }

    public void setCollectionState(CollectionStateX collectionState) {
        this.collectionState = collectionState;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public String getImportError() {
        return importError;
    }

    public String toString() {
        return "Collection("+name+")";
    }
}
