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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores and retrieves snippets in a file separated by a delimiter. File structure looks like this
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class RecordMapping implements Iterable<FieldMapping> {
    private static final String MAPPING_PREFIX = "//<<<";
    private static final String MAPPING_SUFFIX = "//>>>";
    private static final String RECORD_PREFIX = "output.record {";
    private static final String RECORD_SUFFIX = "}";
    private List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public interface Listener {
        void mappingAdded(FieldMapping fieldMapping);
        void mappingRemoved(FieldMapping fieldMapping);
        void mappingsRefreshed(RecordMapping recordMapping);
    }

    public RecordMapping() {
    }

    public boolean hasFieldMapping() {
        return !fieldMappings.isEmpty();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void clear() {
        fieldMappings.clear();
        fireRefresh();
    }

    public void setFieldMapping(FieldMapping fieldMapping) {
        fieldMappings.clear();
        fieldMappings.add(fieldMapping);
        fireRefresh();
    }

    public void setCode(String code) {
        fieldMappings.clear();
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
        fireRefresh();
    }

    @Override
    public Iterator<FieldMapping> iterator() {
        return fieldMappings.iterator();
    }

    public void add(FieldMapping fieldMapping) {
        fieldMappings.add(fieldMapping);
        for (Listener listener : listeners) {
            listener.mappingAdded(fieldMapping);
        }
    }

    public void remove(FieldMapping fieldMapping) {
        fieldMappings.remove(fieldMapping);
        for (Listener listener : listeners) {
            listener.mappingRemoved(fieldMapping);
        }
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

    private void fireRefresh() {
        for (Listener listener : listeners) {
            listener.mappingsRefreshed(this);
        }
    }

}
