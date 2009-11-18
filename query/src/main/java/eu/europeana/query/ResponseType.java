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

package eu.europeana.query;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public enum ResponseType {
    SINGLE_FULL_DOC(5, false),
    FACETS_ONLY(0, true),
    SMALL_BRIEF_DOC_WINDOW(12, true),
    LARGE_BRIEF_DOC_WINDOW(20, false),
    DOC_ID_WINDOW(3, false);

    private int rows;
    private boolean facets;

    ResponseType(int rows, boolean facets) {
        this.rows = rows;
        this.facets = facets;
    }

    public int getRows() {
        return rows;
    }

    public boolean isFacets() {
        return facets;
    }
}