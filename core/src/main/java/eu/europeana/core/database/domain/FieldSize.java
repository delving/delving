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

/**
 * This class gathers the constant field sizes, used in both entity annotations
 * and in code that fills the field values (for limiting size).
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldSize {
    public static final int EUROPEANA_URI = 256;
    public static final int EUROPEANA_OBJECT = 256;
    public static final int TITLE = 120;
    public static final int AUTHOR = 80;
    public static final int DOCTYPE = 10;
    public static final int LANGUAGE = 3;
    public static final int TAG = 60;
    public static final int QUERY = 200;
    public static final int QUERY_STRING = 200;
    public static final int THUMBNAIL = 256;
    public static final int CREATOR = 256;
    public static final int YEAR = 25;
    public static final int USER_NAME = 60;
    public static final int COLLECTION_NAME = 256;
    public static final int FILE_NAME = 256;
    public static final int FILE_USER_NAME = 60;
    public static final int COLLECTION_STATE_ENUM = 25;
    public static final int PROPOSED_SEARCH_TERM = 64;
    public static final int SEARCH_QUERY_PARAMETERS = 256;
    public static final int PERSONAL_FIELD = 100;
    public static final int PASSWORD = 64;
    public static final int LANGUAGE_LIST = 30;
    public static final int IDENTIFIER = 30;
}
