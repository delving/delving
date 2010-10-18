package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;
import java.util.Map;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("node")
public class MetaNode {

    @XStreamAsAttribute
    public String tag;

    @XStreamImplicit
    public List<MetaField> fields;

    @XStreamImplicit
    public List<MetaNode> nodes;

    public MetaField getField(String path) {
        int slash = path.indexOf("/");
        if (slash < 0) {
            if (fields != null) {
                for (MetaField field : fields) {
                    if (path.equals(field.getTag())) {
                        return field;
                    }
                }
            }
        }
        else {
            if (nodes != null) {
                String tag = path.substring(0, slash);
                for (MetaNode node : nodes) {
                    if (tag.equals(node.tag)) {
                        return node.getField(path.substring(slash+1));
                    }
                }
            }
        }
        return null;
    }

    public void getConstantFields(String path, Map<String, MetaField> map) {
        if (fields != null) {
            for (MetaField field : fields) {
                if (field.constant) {
                    map.put(String.format("%s/%s", path, field.getTag()), field);
                }
            }
        }
        if (nodes != null) {
            for (MetaNode node : nodes) {
                node.getConstantFields(String.format("%s/%s", path, node.tag), map);
            }
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(tag).append(" {\n");
        if (fields != null) {
            for (MetaField field : fields) {
                MetaModel.indent(field.toString(), out);
            }
        }
        if (nodes != null) {
            for (MetaNode node : nodes) {
                MetaModel.indent(node.toString(), out);
            }
        }
        out.append("}\n");
        return out.toString();
    }
}
