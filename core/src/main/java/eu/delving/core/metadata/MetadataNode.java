package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("node")
public class MetadataNode {

    @XStreamAsAttribute
    private String tag;

    @XStreamImplicit
    private List<MetadataField> fields;

    @XStreamImplicit
    private List<MetadataNode> nodes;

    public String getTag() {
        return tag;
    }

    public List<MetadataField> getFields() {
        return fields;
    }

    public List<MetadataNode> getNodes() {
        return nodes;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(tag).append(" {\n");
        if (fields != null) {
            for (MetadataField field : fields) {
                for (String line : field.toString().split("\n")) {
                    out.append("   ").append(line).append('\n');
                }
            }
        }
        if (nodes != null) {
            for (MetadataNode node : nodes) {
                for (String line : node.toString().split("\n")) {
                    out.append("   ").append(line).append('\n');
                }
            }
        }
        out.append("}\n");
        return out.toString();
    }
}
