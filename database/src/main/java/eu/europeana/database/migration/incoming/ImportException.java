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

package eu.europeana.database.migration.incoming;

/**
 * Something went wrong during importing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ImportException extends Exception {
    private static final long serialVersionUID = 957621372671543988L;
    private int recordNumber = -1;

    public ImportException(String message, int recordNumber) {
        super(message);
        this.recordNumber = recordNumber;
    }

    public ImportException(String message, Throwable cause, int recordNumber) {
        super(message, cause);
        this.recordNumber = recordNumber;
    }

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    @Override
    public String getMessage() {
        if (recordNumber >= 0) {
            return super.getMessage()+" [Record #"+recordNumber+"]";
        }
        else {
            return super.getMessage();
        }
    }

}