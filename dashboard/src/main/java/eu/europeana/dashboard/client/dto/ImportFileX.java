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

package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * How the incoming files are represented in the GUI
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ImportFileX implements IsSerializable {
    private static final String XML_SUFFIX = ".xml";
    private static final String GZIP_XML_SUFFIX = ".xml.gz";
    private String fileName;
    private ImportFileX.State state;
    private Date lastModified;

    public ImportFileX(String fileName, State state, Date lastModified) {
        this.fileName = fileName;
        this.state = state;
        this.lastModified = lastModified;
    }

    public ImportFileX(String fileName, State state) {
        this.fileName = fileName;
        this.state = state;
        this.lastModified = new Date();
    }

    public ImportFileX() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ImportFileX.State getState() {
        return state;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isXml() {
        return fileName.endsWith(XML_SUFFIX);
    }

    public boolean isGzipXml() {
        return fileName.endsWith(GZIP_XML_SUFFIX);
    }

    public String deriveCollectionName() {
        if (isXml()) {
            return fileName.substring(0,fileName.length()-XML_SUFFIX.length());
        }
        else if (isGzipXml()) {
            return fileName.substring(0,fileName.length()-GZIP_XML_SUFFIX.length());
        }
        else {
            return fileName;
        }
    }

    public String toString() {
        return fileName + "(" + state + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ImportFileX)) return false;
        ImportFileX otherFile = (ImportFileX) other;
        return otherFile.fileName.equals(fileName);
    }

    public static boolean isCorrectSuffix(String name) {
        return name.endsWith(XML_SUFFIX) || name.endsWith(GZIP_XML_SUFFIX);
    }

    /**
     * This should be a perfect replica of eu.europeana.core.database.domain.ImportFileState
     * todo: unit test
     */

    public enum State implements IsSerializable{
        NONEXISTENT(FilterChoice.NOT_STARTED),
        UPLOADING(FilterChoice.NOT_STARTED),
        UPLOADED(FilterChoice.IN_PROGRESS),
        VALIDATING(FilterChoice.IN_PROGRESS),
        VALIDATED(FilterChoice.IN_PROGRESS),
        IMPORTING(FilterChoice.IN_PROGRESS),
        IMPORTED(FilterChoice.COMPLETED),
        ERROR(FilterChoice.ERROR);

        private FilterChoice filterChoice;

        State(FilterChoice filterChoice) {
            this.filterChoice = filterChoice;
        }

        @SuppressWarnings({"GwtInconsistentSerializableClass"})
        State() {
        }

        public FilterChoice getFilterChoice() {
            return filterChoice;
        }
    }

}
