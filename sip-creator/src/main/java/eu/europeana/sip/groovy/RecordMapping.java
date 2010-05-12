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
    private static final String MAPPING_PREFIX = "//<<<";
    private static final String MAPPING_SUFFIX = "//>>>";
    private static final String RECORD_PREFIX = "output.record {";
    private static final String RECORD_SUFFIX = "}";
    private List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();

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
                    fieldMapping.addCodeLine(line.trim());
                }
            }
        }
    }

    public RecordMapping(FieldMapping fieldMapping) {
        fieldMappings.add(fieldMapping);
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
                out.append(codeLine).append('\n');
            }
            out.append(MAPPING_SUFFIX).append('\n');
        }
        out.append(RECORD_SUFFIX).append('\n');
        return out.toString();
    }

    public String getCodeForDisplay(boolean wrappedInRecord) {
        StringBuilder out = new StringBuilder();
        int indent;
        if (wrappedInRecord) {
            out.append(RECORD_PREFIX).append('\n');
            indent = 1;
        }
        else {
            indent = 0;
        }
        for (FieldMapping mapping : fieldMappings) {
            for (String codeLine : mapping.getCodeLines()) {
                if (codeLine.endsWith("}")) {
                    indent--;
                }
                for (int walk = 0; walk < indent; walk++) {
                    out.append("   ");
                }
                out.append(codeLine).append('\n');
                if (codeLine.endsWith("{")) {
                    indent++;
                }
            }
        }
        if (wrappedInRecord) {
            out.append(RECORD_SUFFIX).append('\n');
        }
        return out.toString();
    }

    public String toString() {
        return getCodeForDisplay(true);
    }

}
