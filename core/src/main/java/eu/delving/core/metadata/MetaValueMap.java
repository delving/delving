package eu.delving.core.metadata;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A named map from strings to members of a particular set of strings.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaValueMap {
    private String name;
    private Set<String> rangeValues;
    private Map<String, String> map = new TreeMap<String, String>();

    public MetaValueMap(String name, Set<String> rangeValues) {
        this.name = name;
        this.rangeValues = rangeValues;
    }

    public String getName() {
        return name;
    }

    public Set<String> getRangeValues() {
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
