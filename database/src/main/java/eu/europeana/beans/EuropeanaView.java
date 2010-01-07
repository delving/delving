package eu.europeana.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation at bean class level
 *
 * @author Gerald de Jong geralddejong@gmail.com
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EuropeanaView {
    int rows();
    boolean facets();
}