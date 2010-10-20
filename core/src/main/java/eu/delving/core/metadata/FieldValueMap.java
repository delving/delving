/*
 * Copyright 2010 DELVING BV
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A named map from strings to members of a particular set of strings.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldValueMap {
    private String name;
    private List<String> rangeValues;
    private Map<String, String> map = new TreeMap<String, String>();

    public FieldValueMap(String name, List<String> rangeValues) {
        this.name = name;
        this.rangeValues = rangeValues;
    }

    public String getName() {
        return name;
    }

    public List<String> getRangeValues() {
        return rangeValues;
    }

    public void setDomain(Set<String> domain) {
        for (String key : domain) {
            map.put(key, "");
        }
    }

    public void put(String key, String value) {
        if (!value.isEmpty() && !rangeValues.contains(value)) {
            throw new RuntimeException(String.format("Value [%s] not among range values %s", value, rangeValues.toString()));
        }
        map.put(key, value);
    }

    public Set<Map.Entry<String,String>> entrySet() {
        return map.entrySet();
    }

    public String get(String key) {
        return map.get(key);
    }

    public String toString() {
        return name;
    }
}
