package eu.europeana.solrj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * todo: javadoc
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Solr {

   String namespace() default "";  // overrides Field value
   String name() default "";       // overrides Field value

   // Solr Schema stuff
    String fieldType() default "text";
    boolean multivalued() default true;
    boolean stored() default true;
    boolean indexed() default true;
    boolean required() default false;
    boolean compressed() default false;

    // advanced (fields should not be displayed when not specified)
    boolean termVectors() default true;
    boolean termPositions() default false;
    boolean termOffsets() default false;
    boolean omitNorms() default false;
    String defaultValue() default "";

    String [] toCopyField() default {};
}