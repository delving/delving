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
 * This is the annotation which describes the europeana aspects of a
 * field in one of the beans being used.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Europeana {

    /**
     * only copy field cannot be used to add metadata directly during indexing
     * @return true if this is a copy field
     */

    boolean copyField() default false;

    /**
     * Is this field one of the facets?
     *
     * @return true if it is to be a facet
     */

    boolean facet() default false;

    /**
     * A prefix for a facet, must be unique
     *
     * @return a string prefix
     */

    String facetPrefix() default "";

    /**
     * This field will appear in the brief doc rendering
     *
     * @return true if it will
     */

    boolean briefDoc() default false;

    /**
     * This field will appear in the full doc rendering
     *
     * @return true if it will
     */

    boolean fullDoc() default true;

    /**
     * This field is hidden
     *
     * @return true if it should not appear
     */

    boolean hidden() default false;

    /**
     * Is this the europeana id to use
     *
     * @return true if it is
     */

    boolean id() default false;

    /**
     * Is this an object to which the record refers?
     *
     * @return true if it is
     */

    boolean object() default false;

    /**
     * Is this the europeana type?
     *
     * @return true if it is
     */

    boolean type() default false;
}
