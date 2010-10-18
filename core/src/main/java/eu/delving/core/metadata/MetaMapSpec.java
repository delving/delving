package eu.delving.core.metadata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A groovy mapping based on a model.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("metadata-mapping")
public class MetaMapSpec {

    @XStreamAlias("record-root")
    public String recordRoot;

    @XStreamAlias("constants")
    public Map<String, String> constants = new HashMap<String, String>();

    @XStreamAlias("field-mappings")
    public Map<String, FieldMapping> fieldMappings = new HashMap<String, FieldMapping>();

    @XStreamAlias("field-mapping")
    public static class FieldMapping {

        @XStreamAlias("value-map")
        public Map<String, String> valueMap;

        @XStreamAlias("groovy-code")
        public List<String> code;


        public void setValue(String key, String value) {
            if (valueMap == null) {
                valueMap = new HashMap<String, String>();
            }
            valueMap.put(key, value);
        }

        public void setCode(String code) {
            if (this.code == null) {
                this.code = new ArrayList<String>();
            }
            this.code.clear();
            this.code.addAll(Arrays.asList(code.split("\n")));
        }
    }

    public String generateCode(MetaModel model) {
        StringBuilder out = new StringBuilder();
        out.append("// Generated code for mapping\n");
        for (Map.Entry<String, String> constantEntry : constants.entrySet()) {
            out.append(String.format("def %s = '%s'\n", constantEntry.getKey(), constantEntry.getValue()));
        }
        out.append("\n");
        for (Map.Entry<String, FieldMapping> fieldMappingEntry : fieldMappings.entrySet()) {
            if (fieldMappingEntry.getValue().valueMap != null) {
                String path = mungePath(fieldMappingEntry.getKey());
                out.append(String.format("def %sMap = [\n", path));
                for (Map.Entry<String, String> entry : fieldMappingEntry.getValue().valueMap.entrySet()) {
                    out.append("   '").append(entry.getKey()).append("':'").append(entry.getValue()).append("',\n");
                }
                out.append("]\n");
                out.append(String.format("def %s = { def v = %sMap[it.toString()]; return v ? v : it }\n\n", path, path));
            }
        }
        out.append("output.record {\n");
        for (MetaNode node : model.getNodes()) {
            generateCode("", node, out, 1);
        }
        out.append("}\n");
        return out.toString();
    }

    private void generateCode(String path, MetaNode node, StringBuilder out, int indent) {
        indented(indent, out).append(node.tag).append(" {\n");
        if (node.nodes != null) {
            for (MetaNode subNode : node.nodes) {
                generateCode(path + "/" + node.tag, subNode, out, indent + 1);
            }
        }
        if (node.fields != null) {
            for (MetaField field : node.fields) {
                generateCode(path + "/" + node.tag, field, out, indent + 1);
            }
        }
        indented(indent, out).append("}\n");
    }

    private void generateCode(String path, MetaField field, StringBuilder out, int indent) {
        String fieldPath = path + "/" + field.getTag();
        FieldMapping fieldMapping = fieldMappings.get(fieldPath);
        if (fieldMapping != null) {
            for (String line : fieldMapping.code) {
                indented(indent, out).append(line).append("\n");
            }
        }
    }

    public static StringBuilder indented(int count, StringBuilder out) {
        while (count-- > 0) {
            out.append("   ");
        }
        return out;
    }

    private String mungePath(String path) {
        return path.replaceAll("/", "__");
    }

    // ==== reading and writing

    public static MetaMapSpec read(InputStream inputStream) {
        return (MetaMapSpec) stream().fromXML(inputStream);
    }

    public static MetaMapSpec read(String string) {
        return (MetaMapSpec) stream().fromXML(string);
    }

    public static String toString(MetaMapSpec spec) {
        return stream().toXML(spec);
    }

    private static XStream stream() {
        XStream stream = new XStream();
        stream.processAnnotations(MetaMapSpec.class);
        return stream;
    }
}
