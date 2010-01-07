package eu.europeana.beans;

import java.util.Set;

/**
 * Interpret the annotations in the beans which define the search model.  This is the injected interface
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public interface AnnotationProcessor {
    Set<? extends EuropeanaField> getFacetFields();
    EuropeanaBean getEuropeanaBean(Class<?> c);
}
