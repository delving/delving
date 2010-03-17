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

package eu.europeana.sip.reference;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Deprecated
public enum FacetType {
    LOCATION(false, "loc"),
    SUBJECT(false, "sub"),
    LANGUAGE(true, "lang"),
    YEAR(true, "yr"),
    TYPE(true, "type"),
    PROVIDER(true, "prov"),
    COUNTRY(true, "coun"),
    CONTRIBUTOR(false, "contr"),
    USERTAGS(false, "ut"),
    SOCIAL_TAGS(false, "st");

    private boolean searchable;
    // this is used for solr tagName for multivalue facet select
    private String tagName;

    FacetType(boolean searchable, String tagName) {
        this.searchable = searchable;
        this.tagName = tagName;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public String getTagName() {
        return tagName;
    }
}