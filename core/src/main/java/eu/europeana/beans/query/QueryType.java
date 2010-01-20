package eu.europeana.beans.query;

/**
 * The enumeration of query types
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum QueryType {
    SIMPLE_QUERY("europeana"),
    ADVANCED_QUERY("standard"),
    MORE_LIKE_THIS_QUERY("moreLikeThis");

    private String appearance;

    QueryType(String appearance) {
        this.appearance = appearance;
    }

    public String toString() {
        return appearance;
    }
}
