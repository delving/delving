package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("metadata-model")
public class MetadataModel {

    @XStreamImplicit
    private List<MetadataNode> nodes;

    public List<MetadataNode> getNodes() {
        return nodes;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("metadataModel\n");
        for (MetadataNode node : nodes) {
            for (String line : node.toString().split("\n")) {
                out.append("   ").append(line).append('\n');
            }
        }
        return out.toString();
    }
}
