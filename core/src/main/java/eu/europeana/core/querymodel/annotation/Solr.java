/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.querymodel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation reveals all of the information that is needed in order
 * to build a Solr field out of a bean field.
 *
 * todo: more javadoc
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Gerald de Jong <geralddejong@gmail.com>
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

    String[] toCopyField() default {};
}