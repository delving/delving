package eu.delving.core.metadata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("metadata-model")
public class MetaModel {

    @XStreamAsAttribute
    public String name;

    @XStreamImplicit
    public List<MetaNode> nodes;

    public Map<String, MetaField> getConstantFields() {
        Map<String, MetaField> map = new TreeMap<String, MetaField>();
        for (MetaNode node : nodes) {
            node.getConstantFields("", map);
        }
        return map;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(name).append(" {\n");
        for (MetaNode node : nodes) {
            indent(node.toString(), out);
        }
        out.append("}");
        return out.toString();
    }

    // handy static methods

    public static void indent(String s, StringBuilder out) {
        for (String line : s.split("\n")) {
            out.append("   ").append(line).append('\n');
        }
    }

    public static MetaModel read(InputStream in) {
        XStream stream = new XStream();
        stream.processAnnotations(new Class[]{
                MetaModel.class,
                MetaNode.class,
                MetaField.class
        });
        return (MetaModel) stream.fromXML(in);
    }
}
