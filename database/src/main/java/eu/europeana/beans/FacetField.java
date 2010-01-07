package eu.europeana.beans;

/**
 * The facet field as it is needed for building queries
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public interface FacetField {
    String getPrefix();
    String getName();
    String getFieldNameString();
}
