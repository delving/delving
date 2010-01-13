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

import java.util.ArrayList;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 9, 2009: 4:24:55 PM
 */

@Deprecated
public enum RecordFieldChoice {
    FULL_DOC(getFullDocFields()),
    BRIEF_DOC(getBriefDocFields()),
    DOC_ID(getDocId());

    private ArrayList<String> recordFields;

    private RecordFieldChoice(ArrayList<String> recordFields) {
        this.recordFields = recordFields;
    }

    public ArrayList<String> getRecordFields() {
        return recordFields;
    }

    private static ArrayList<String> getFullDocFields() {
        ArrayList<String> fullDocFields = new ArrayList<String>();
        // iterate over all Recordfields
        for (RecordField recordField : RecordField.values()) {

            if (!recordField.isHiddenField() && recordField.getFacetType() == null) {
                fullDocFields.add(recordField.toFieldNameString());
            }
        }
        // iterate over the FacetTypes that are searchable
        for (FacetType facetType : FacetType.values()) {
            if (facetType.isSearchable()) {
                fullDocFields.add(facetType.toString());
            }
        }
        return fullDocFields;
    }

    private static ArrayList<String> getBriefDocFields() {
        ArrayList<String> briefDocFields = new ArrayList<String>();
        // briefdoc fields
        briefDocFields.add(RecordField.EUROPEANA_URI.toFieldNameString());
        briefDocFields.add(RecordField.EUROPEANA_OBJECT.toFieldNameString());
        briefDocFields.add(RecordField.DC_TITLE.toFieldNameString());
        briefDocFields.add(RecordField.DC_CREATOR.toFieldNameString());
        briefDocFields.add(RecordField.DC_CONTRIBUTOR.toFieldNameString());
        briefDocFields.add(RecordField.DC_DESCRIPTION.toFieldNameString());
        briefDocFields.add(RecordField.DCTERMS_ALTERNATIVE.toFieldNameString());
        // briefdoc fields from facets
        for (FacetType facetType : FacetType.values()) {
            if (facetType.isSearchable()) {
                briefDocFields.add(facetType.toString());
            }
        }
        return briefDocFields;
    }

    private static ArrayList<String> getDocId() {
        ArrayList<String> docId = new ArrayList<String>();
        docId.add(RecordField.EUROPEANA_URI.toFieldNameString());
        return docId;
    }
}
