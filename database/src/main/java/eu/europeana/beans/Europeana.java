package eu.europeana.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong geralddejong@gmail.com
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Europeana {

    /**
     * only copy field cannot be used to add metadata directly during indexing
     * @return
     */
    boolean copyField() default false;

    // Facets
    boolean facet() default false;
    String facetName() default "";       // overrides Field name
    String facetPrefix() default "";     // must be unique
    // String presentationOrder() default "last";

    // Presentation
    boolean briefDoc() default false;
    boolean fullDoc() default true;
    boolean hidden() default false;
}
