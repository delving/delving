/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.model;

/**
 * An enumeration of the global fields that will be used
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum GlobalField {
    COLLECTION_ID("Collection ID", "collectionId"),
    PROVIDER_NAME("Provider Name", "providerName"),
    PROVIDER_ABBREVIATION("Provider Abbreviation", "providerAbbreviation"),
    COLLECTION_NAME("Collection Name", "collectionName"),
    LANGUAGE("Language", "language"),
    COUNTRY("Country", "country"),
    TYPE("Type", "type");

    private String prompt;
    private String variableName;

    GlobalField(String prompt, String variableName) {
        this.prompt = prompt;
        this.variableName = variableName;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getVariableName() {
        return variableName;
    }
}