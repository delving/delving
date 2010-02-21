package eu.europeana.core.querymodel.query;

/**
 * The enumeration of query types
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum QueryType {
    SIMPLE_QUERY("simple"),
    ADVANCED_QUERY("advanced"),
    MORE_LIKE_THIS_QUERY("moreLikeThis");

    private String appearance;

    QueryType(String appearance) {
        this.appearance = appearance;
    }

    public String toString() {
        return appearance;
    }
}
