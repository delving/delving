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
package eu.europeana.sip.groovy;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores and retrieves snippets in a file separated by a delimiter. File structure looks like this
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class RecordMapping {
    private static final String INDENT = "   ";
    private static final String MAPPING_PREFIX = INDENT + "//<<<";
    private static final String MAPPING_SUFFIX = INDENT + "//>>>";
    private static final String RECORD_PREFIX = "output.record { // Mapping Builder";
    private static final String RECORD_SUFFIX = "} // Mapping Builder";
    private List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();

    public RecordMapping(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public RecordMapping(String code) {
        FieldMapping fieldMapping = null;
        for (String line : code.split("\n")) {
            if (line.startsWith(MAPPING_PREFIX)) {
                String mappingSpec = line.substring(MAPPING_PREFIX.length()).trim();
                fieldMapping = new FieldMapping(mappingSpec);
            }
            else if (line.startsWith(MAPPING_SUFFIX)) {
                if (fieldMapping != null) {
                    fieldMappings.add(fieldMapping);
                    fieldMapping = null;
                }
            }
            else {
                if (fieldMapping != null) {
                    if (line.startsWith(INDENT)) {
                        line = line.substring(INDENT.length());
                    }
                    fieldMapping.addCodeLine(line);
                }
            }
        }
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public String getCode() {
        StringBuilder out = new StringBuilder();
        out.append(RECORD_PREFIX).append('\n');
        for (FieldMapping mapping : fieldMappings) {
            out.append(MAPPING_PREFIX).append(mapping.toString()).append('\n');
            for (String codeLine : mapping.getCodeLines()) {
                out.append(INDENT).append(INDENT).append(codeLine).append('\n');
            }
            out.append(MAPPING_SUFFIX).append('\n');
        }
        out.append(RECORD_SUFFIX).append('\n');
        return out.toString();
    }

    public String toString() {
        return getCode();
    }
}
