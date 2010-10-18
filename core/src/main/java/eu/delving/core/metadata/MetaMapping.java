package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A groovy mapping based on a model.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("metadata-mapping")
public class MetaMapping {
    private static final String HEADER = "// SIP-Creator Hierarchical Mapping file";
    private static final String RECORD_ROOT_PREFIX = "// ## RecordRoot ";
    private static final String CONSTANT_PREFIX = "// ConstantField ";
    private static final String VALUE_MAP_PREFIX = "/* ValueMap */ def ";
    private static final String VALUE_MAP_RANGE_PREFIX = "// ";
    private static final Pattern VALUE_MAP_ENTRY_PATTERN = Pattern.compile("'([^']*)':'([^']*)',");
    private static final String VALUE_MAP_SUFFIX = "]";
    private static final String MAPPING_PREFIX = "//<<<";
    private static final String MAPPING_SUFFIX = "//>>>";
    private static final String RECORD_PREFIX = "output.record {";
    private static final String RECORD_SUFFIX = "}";

    private Logger log = Logger.getLogger(getClass());
    private MetaModel metaModel;
    private MetaPath recordRootPath;
    private Map<String, String> constantMap = new TreeMap<String, String>();
    private Map<String, MetaValueMap> valueMaps = new TreeMap<String, MetaValueMap>();
    private Map<String, MetaFieldMapping> fieldMappings = new TreeMap<String, MetaFieldMapping>();

    public MetaMapping(MetaModel metaModel) {
        this.metaModel = metaModel;
    }

    public boolean isEmpty() {
        return fieldMappings.isEmpty();
    }

    public void clear() {
        fieldMappings.clear();
        fireChangeEvent();
    }

    public void setRecordRootPath(MetaPath metaPath) {
        this.recordRootPath = metaPath;
        fireChangeEvent();
    }

    public MetaPath getRecordRootPath() {
        return recordRootPath;
    }

    public void setConstant(String path, String value) {
        if (value == null || value.isEmpty()) {
            constantMap.remove(path);
        }
        else {
            constantMap.put(path, value);
        }
        fireChangeEvent();
    }

    public String getConstant(String path) {
        String value = constantMap.get(path);
        return value == null ? "" : value;
    }

    public void setCode(String code) {
        fieldMappings.clear();
        constantMap.clear();
        valueMaps.clear();
        recordRootPath = null;
        MetaValueMap valueMap = null;
        MetaFieldMapping fieldMapping = null;
        for (String line : code.split("\n")) {
            if (line.startsWith(RECORD_ROOT_PREFIX)) {
                setRecordRootFrom(line);
            }
            else if (line.startsWith(CONSTANT_PREFIX)) {
                setConstantFrom(line);
            }
            else if (line.startsWith(VALUE_MAP_PREFIX)) {
                valueMap = startValueMapFrom(line);
            }
            else if (line.startsWith(MAPPING_PREFIX)) {
                fieldMapping = startFieldMappingFrom(line);
            }
            else if (line.startsWith(MAPPING_SUFFIX)) {
                if (fieldMapping != null) {
                    fieldMappings.put(fieldMapping.getMetadataField().getTag(), fieldMapping);
                    fieldMapping = null;
                }
            }
            else if (VALUE_MAP_SUFFIX.equals(line)) {
                if (valueMap != null) {
                    this.valueMaps.put(valueMap.getName(), valueMap);
                    valueMap = null;
                }
            }
            else {
                if (fieldMapping != null) {
                    fieldMapping.addCodeLine(line.trim());
                }
                if (valueMap != null) {
                    Matcher matcher = VALUE_MAP_ENTRY_PATTERN.matcher(line);
                    if (!matcher.matches()) {
                        throw new RuntimeException(String.format("Line [%s] does not match entry pattern", line));
                    }
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    valueMap.put(key, value);
                }
            }
        }
        fireChangeEvent();
    }

    public void add(MetaFieldMapping fieldMapping) {
        fieldMappings.put(fieldMapping.getMetadataField().getTag(), fieldMapping);
        fireChangeEvent();
    }

    public void remove(MetaFieldMapping fieldMapping) {
        fieldMappings.remove(fieldMapping);
        fireChangeEvent();
    }

    public String generateCode() {
        StringBuilder out = new StringBuilder();
        if (recordRootPath != null) {
            out.append(RECORD_ROOT_PREFIX).append(recordRootPath.toString());
        }
        for (Map.Entry<String, String> entry : constantMap.entrySet()) {
            out.append(CONSTANT_PREFIX).append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
        }
        for (MetaValueMap valueMap : valueMaps.values()) {
            out.append(VALUE_MAP_PREFIX).append(valueMap.getName()).append("Map = [ // ");
            for (String rangeValue : valueMap.getRangeValues()) {
                out.append(rangeValue).append(',');
            }
            out.append('\n');
            for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                out.append("'").append(entry.getKey()).append("':'").append(entry.getValue()).append("',\n");
            }
            out.append("]\n");
            out.append(String.format("def %s = { def v = %sMap[it.toString()]; return v ? v : it }\n", valueMap.getName(), valueMap.getName()));
        }
        out.append(RECORD_PREFIX).append('\n');
        metaModel.generateCode(this, out);
        out.append(RECORD_SUFFIX).append('\n');
        return out.toString();
    }

    public String toString() {
        return "MetaMapping";
    }

    // private

    private MetaFieldMapping startFieldMappingFrom(String line) {
        MetaFieldMapping fieldMapping;
        String path = line.substring(MAPPING_PREFIX.length()).trim();
        MetaField metaField = metaModel.getField(path); // todo: a PATH!
        if (metaField != null) {
            MetaValueMap existingMap = valueMaps.get(metaField.getFieldNameString());
            if (existingMap != null) {
                fieldMapping = new MetaFieldMapping(metaField, existingMap);
            }
            else {
                fieldMapping = new MetaFieldMapping(metaField);
            }
        }
        else {
            log.warn("Discarding unrecognized field " + path);
            fieldMapping = null;
        }
        return fieldMapping;
    }

    private MetaValueMap startValueMapFrom(String line) {
        MetaValueMap valueMap;
        String def = line.substring(VALUE_MAP_PREFIX.length());
        int eq = def.indexOf("Map =");
        if (eq < 0) throw new RuntimeException("No 'Map =' found");
        String name = def.substring(0, eq).trim();
        int range = def.indexOf(VALUE_MAP_RANGE_PREFIX);
        if (range < 0) throw new RuntimeException("No range values");
        String rangeString = def.substring(range + VALUE_MAP_RANGE_PREFIX.length());
        Set<String> rangeValues = new TreeSet<String>();
        rangeValues.addAll(Arrays.asList(rangeString.split(",")));
        valueMap = new MetaValueMap(name, rangeValues);
        return valueMap;
    }

    private void setConstantFrom(String line) {
        line = line.substring(CONSTANT_PREFIX.length());
        int space = line.indexOf(" ");
        if (space > 0) {
            String fieldName = line.substring(0, space);
            String value = line.substring(space).trim();
            constantMap.put(fieldName, value);
        }
    }

    private void setRecordRootFrom(String line) {
        String recordRootString = line.substring(RECORD_ROOT_PREFIX.length());
        recordRootPath = new MetaPath(recordRootString);
    }

    // observable

    public interface Listener {
        void mappingChanged(MetaMapping metaMapping);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    private void fireChangeEvent() {
        for (Listener listener : listeners) {
            listener.mappingChanged(this);
        }
    }
}
