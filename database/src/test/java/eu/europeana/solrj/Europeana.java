package eu.europeana.solrj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Europeana {
    String [] copyFields() default {};
}
