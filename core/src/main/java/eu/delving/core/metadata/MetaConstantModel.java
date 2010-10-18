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

package eu.delving.core.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Hold a collection of global fields that can be used here and there.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaConstantModel {
    private List<FieldSpec> fields = new ArrayList<FieldSpec>();
    private Map<String, String> map = new TreeMap<String, String>();
    private Listener listener;

    public interface Listener {
        void updatedConstant();
    }

    public MetaConstantModel(MetaModel metaModel, Listener listener) {
        for (Map.Entry<String, MetaField> entry : metaModel.getConstantFields().entrySet()) {
            FieldSpec fieldSpec = new FieldSpec(entry.getKey());
            fieldSpec.setEnumValues(entry.getValue().getEnumValues());
            fields.add(fieldSpec);
        }
        this.listener = listener;
    }

    public List<FieldSpec> getFields() {
        return fields;
    }

    public void clear() {
        map.clear();
        fireUpdate();
    }

    public void set(String field, String value) {
        String oldValue = map.get(field);
        if (oldValue == null || !oldValue.equals(value)) {
            if (value.isEmpty()) {
                map.remove(field);
            }
            else {
                map.put(field, value);
            }
            fireUpdate();
        }
    }

    private void fireUpdate() {
        if (listener != null) {
            listener.updatedConstant();
        }
    }

    public String get(String fieldName) {
        String value = map.get(fieldName);
        if (value != null) {
            return value;
        }
        else {
            return "";
        }
    }

    public String toString() {
        return "MetaConstantModel";
    }

    public class FieldSpec {
        private String path;
        private Set<String> enumValues;

        public FieldSpec(String path) {
            this.path = path;
        }

        public void setEnumValues(Set<String> enumValues) {
            this.enumValues = enumValues;
        }

        public String getPath() {
            return path;
        }

        public Set<String> getEnumValues() {
            return enumValues;
        }

        public String toString() {
            return path;
        }
    }
}
