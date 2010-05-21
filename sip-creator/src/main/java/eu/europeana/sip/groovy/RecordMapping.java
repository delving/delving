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

import eu.europeana.definitions.annotations.EuropeanaField;
import eu.europeana.sip.model.GlobalFieldModel;
import eu.europeana.sip.model.RecordRoot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores and retrieves snippets in a file separated by a delimiter. File structure looks like this
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class RecordMapping implements Iterable<FieldMapping> {
    private static final String HEADER = "// SIP-Creator Mapping file";
    private static final String MAPPING_PREFIX = "//<<<";
    private static final String MAPPING_SUFFIX = "//>>>";
    private static final String RECORD_PREFIX = "output.record {";
    private static final String RECORD_SUFFIX = "}";
    private boolean singleFieldMapping;
    private RecordRoot recordRoot;
    private GlobalFieldModel globalFieldModel;
    private List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public interface Listener {
        void mappingAdded(FieldMapping fieldMapping);

        void mappingRemoved(FieldMapping fieldMapping);

        void mappingsRefreshed(RecordMapping recordMapping);
    }

    public RecordMapping() {
    }

    public FieldMapping getOnlyFieldMapping() {
        if (fieldMappings.size() != 1) {
            return null;
        }
        else {
            return fieldMappings.get(0);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void clear() {
        fieldMappings.clear();
        fireRefresh();
    }

    public RecordRoot getRecordRoot() {
        return recordRoot;
    }

    public void setGlobalFieldModel(GlobalFieldModel globalFieldModel) {
        this.globalFieldModel = globalFieldModel;
    }

    public GlobalFieldModel getGlobalFieldModel() {
        return globalFieldModel;
    }

    public void setRecordRoot(RecordRoot recordRoot) {
        this.recordRoot = recordRoot;
    }

    public void setFieldMapping(FieldMapping fieldMapping) {
        singleFieldMapping = true;
        fieldMappings.clear();
        fieldMappings.add(fieldMapping);
        fireRefresh();
    }

    public boolean isSingleFieldMapping() {
        return singleFieldMapping;
    }

    public void setCode(String code, Map<String, EuropeanaField> fieldMap) {
        fieldMappings.clear();
        recordRoot = null;
        if (!singleFieldMapping) {
            globalFieldModel = new GlobalFieldModel();
        }
        FieldMapping fieldMapping = null;
        for (String line : code.split("\n")) {
            RecordRoot root = RecordRoot.fromLine(line);
            if (root != null) {
                this.recordRoot = root;
                continue;
            }
            if (!singleFieldMapping && globalFieldModel.fromLine(line)) {
                continue;
            }
            if (line.startsWith(MAPPING_PREFIX)) {
                String europeanaFieldName = line.substring(MAPPING_PREFIX.length()).trim();
                EuropeanaField europeanaField = fieldMap.get(europeanaFieldName);
                if (europeanaField == null) {
                    throw new RuntimeException("Cannot find "+europeanaFieldName); // todo: better response
                }
                fieldMapping = new FieldMapping(europeanaField);
            }
            else if (line.startsWith(MAPPING_SUFFIX)) {
                if (fieldMapping != null) {
                    fieldMappings.add(fieldMapping);
                    fieldMapping = null;
                }
            }
            else {
                if (fieldMapping != null) {
                    fieldMapping.getCodeLines().add(line.trim());
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

    public String getCodeForCompile() {
        return getCode(true, false, false);
    }

    public static String getCodeForCompile(String code) {
        StringBuilder out = new StringBuilder();
        out.append(RECORD_PREFIX).append('\n');
        out.append(code);
        out.append(RECORD_SUFFIX).append('\n');
        return out.toString();
    }

    public String getCodeForPersistence() {
        return getCode(true, false, true);
    }

    public String getCodeForDisplay() {
        return getCode(!singleFieldMapping, true, false);
    }

    private String getCode(boolean wrappedInRecord, boolean indented, boolean delimited) {
        StringBuilder out = new StringBuilder();
        if (delimited) {
            out.append(HEADER).append('\n').append('\n');
            if (recordRoot != null) {
                out.append(recordRoot.toString()).append('\n').append('\n');
            }
            out.append(globalFieldModel.toString()).append('\n');
        }
        int indent = 0;
        if (wrappedInRecord) {
            out.append(RECORD_PREFIX).append('\n');
            indent++;
        }
        for (FieldMapping mapping : fieldMappings) {
            if (delimited) {
                out.append('\n').append(MAPPING_PREFIX).append(mapping.toString()).append('\n');
            }
            for (String codeLine : mapping.getCodeLines()) {
                if (codeLine.endsWith("}")) {
                    indent--;
                }
                if (indented) {
                    for (int walk = 0; walk < indent; walk++) {
                        out.append("   ");
                    }
                }
                out.append(codeLine).append('\n');
                if (codeLine.endsWith("{")) {
                    indent++;
                }
            }
            if (delimited) {
                out.append(MAPPING_SUFFIX).append('\n');
            }
        }
        if (wrappedInRecord) {
            out.append(RECORD_SUFFIX).append('\n');
        }
        return out.toString();
    }

    public String toString() {
        return getCodeForDisplay();
    }

    private void fireRefresh() {
        for (Listener listener : listeners) {
            listener.mappingsRefreshed(this);
        }
    }

}
