package eu.europeana.sip.model;

/**
 * Hold a variable for later use
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class VariableHolder implements Comparable<VariableHolder> {
    private AnalysisTree.Node node;
    private String variableName;
    private int mappingCount;

    public VariableHolder(AnalysisTree.Node node) {
        this.node = node;
        this.variableName = node.getVariableName();
    }

    public void checkIfMapped(String variableName) {
        if (this.variableName.equals(variableName)) {
            mappingCount++;
        }
    }

    public AnalysisTree.Node getNode() {
        return node;
    }

    public String getVariableName() {
        return variableName;
    }

    public String toString() {
        StringBuilder out = new StringBuilder(variableName);
        switch (mappingCount) {
            case 0:
                break;
            case 1:
                out.append(" (mapped once)");
                break;
            case 2:
                out.append(" (mapped twice)");
                break;
            default:
                out.append(" (mapped ").append(mappingCount).append(" times)");
                break;
        }
        return out.toString();
    }

    @Override
    public int compareTo(VariableHolder o) {
        if (mappingCount > o.mappingCount) {
            return 11;
        }
        else if (mappingCount < o.mappingCount) {
            return -1;
        }
        else {
            return node.compareTo(o.node);
        }
    }
}
