package eu.europeana.beans;

import java.util.Set;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public interface EuropeanaBean {
    int rows();
    boolean facets();
    Set<EuropeanaField> getFields();
    String [] getFieldStrings();
}
