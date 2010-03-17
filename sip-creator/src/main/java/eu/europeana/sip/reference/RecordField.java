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

@Deprecated
public enum RecordField {
    EUROPEANA_URI("europeana","uri"),
    EUROPEANA_TYPE("europeana","type", FacetType.TYPE),
    EUROPEANA_LANGUAGE("europeana","language", FacetType.LANGUAGE),
    EUROPEANA_YEAR("europeana","year", FacetType.YEAR),
    EUROPEANA_IS_SHOWN_AT("europeana","isShownAt"),
    EUROPEANA_IS_SHOWN_BY("europeana","isShownBy"),
    EUROPEANA_USER_TAG("europeana","userTag"),
    EUROPEANA_UNSTORED("europeana","unstored", true),
    EUROPEANA_OBJECT("europeana","object"),
    EUROPEANA_HAS_OBJECT("europeana","hasObject"),
    EUROPEANA_COUNTRY("europeana","country", FacetType.COUNTRY),
    EUROPEANA_SOURCE("europeana","source"),
    EUROPEANA_PROVIDER("europeana","provider", FacetType.PROVIDER),
    EUROPEANA_PERSON("europeana", "person", true),
    EUROPEANA_LOCATION("europeana", "location", true),
    EUROPEANA_SUBJECT("europeana", "subject", true),
    EUROPEANA_EDITORS_PICK("europeana", "editorsPick", true),
    EUROPEANA_COLLECTION_NAME("europeana","collectionName"),


    // here the dcterms namespaces starts
    DCTERMS_ALTERNATIVE("dcterms","alternative"),
    DCTERMS_CONFORMS_TO("dcterms","conformsTo"),
    DCTERMS_CREATED("dcterms","created"),
    DCTERMS_EXTENT("dcterms","extent"),
    DCTERMS_HAS_FORMAT("dcterms","hasFormat"),
    DCTERMS_HAS_PART("dcterms","hasPart"),
    DCTERMS_HAS_VERSION("dcterms","hasVersion"),
    DCTERMS_IS_FORMAT_OF("dcterms","isFormatOf"),
    DCTERMS_IS_PART_OF("dcterms","isPartOf"),
    DCTERMS_IS_REFERENCED_BY("dcterms","isReferencedBy"),
    DCTERMS_IS_REPLACED_BY("dcterms","isReplacedBy"),
    DCTERMS_IS_REQUIRED_BY("dcterms","isRequiredBy"),
    DCTERMS_ISSUED("dcterms","issued"),
    DCTERMS_IS_VERSION_OF("dcterms","isVersionOf"),
    DCTERMS_MEDIUM("dcterms","medium"),
    DCTERMS_PROVENANCE("dcterms","provenance"),
    DCTERMS_REFERENCES("dcterms","references"),
    DCTERMS_REPLACES("dcterms","replaces"),
    DCTERMS_REQUIRES("dcterms","requires"),
    DCTERMS_SPATIAL("dcterms","spatial"),
    DCTERMS_TABLE_OF_CONTENTS("dcterms","tableOfContents"),
    DCTERMS_TEMPORAL("dcterms","temporal"),

    // here the dc namespace starts
    DC_CONTRIBUTOR("dc","contributor"),
    DC_COVERAGE("dc","coverage"),
    DC_CREATOR("dc","creator"),
    DC_DATE("dc","date"),
    DC_DESCRIPTION("dc","description"),
    DC_FORMAT("dc","format"),
    DC_IDENTIFIER("dc","identifier"),
    DC_LANGUAGE("dc","language"),
    DC_PUBLISHER("dc","publisher"),
    DC_RELATION("dc","relation"),
    DC_RIGHTS("dc","rights"),
    DC_SOURCE("dc","source"),
    DC_SUBJECT("dc","subject"),
    DC_TITLE("dc","title"),
    DC_TYPE("dc","type");

    private String prefix;
    private String localName;
    private FacetType facetType;
    private boolean hiddenField;

    RecordField(String prefix, String localName) {
        this.prefix = prefix;
        this.localName = localName;
    }

    RecordField(String prefix, String localName, FacetType facetType) {
        this.prefix = prefix;
        this.localName = localName;
        this.facetType = facetType;
    }

    RecordField(String prefix, String localName, boolean hiddenField) {
        this.prefix = prefix;
        this.localName = localName;
        this.hiddenField = hiddenField;
    }

    public String getLocalName() {
        return localName;
    }

    public String getPrefix() {
        return prefix;
    }

    public FacetType getFacetType() {
        return facetType;
    }

    public boolean isHiddenField() {
        return hiddenField;
    }

    public String toFieldNameString() {
        return prefix+"_"+localName;
    }

    public String toString() {
        return prefix+":"+localName;
    }
}
