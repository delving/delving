package eu.europeana.beans;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Interpret the annotations in the beans which define the search model.  This is the injected interface
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public interface AnnotationProcessor {
    Set<? extends FacetField> getFacetFields();
    Set<Field> getFields(Class<?> c);
}
