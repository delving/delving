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

package eu.europeana.definitions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static eu.europeana.definitions.annotations.FieldCategory.ESE;

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
    public enum NO_ENUM {
    }

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

    /**
     * If the field is required, then this value is the name of the requirement group.  Typically this is
     * the field name itself, but sometimes two fields can share the same requirement group.
     *
     * @return true if it is
     */

    String requiredGroup() default "";

    /**
     * There are some fields that are added by the Europeana System during the IngestionPhase based on meta-information
     * provided with the DataSet during submission.
     *
     * These fields are therefore unmappable and must be kept separated during ESE validation and only used during ESE+
     * validation.
     *
     * When EDM is adopted as the internal datamodel the same will apply. These fields need to be kept separate during
     * initial import validation and only be validated during the validation of the Archival Information Packages.
     *
     *
     * @return true if it is
     */

    boolean constant() default false; // todo: make it generate the fields on the analysis

    /**
     *  The annotated fields can be valid at different levels in the application. The FieldCategory will be used to
     * create a Data Model Validator to be used at various stages of ingestion.
     *
     * The validator should also be used in the Sip-Creator and could be used in external applications.
     *
     * @return the validation level of a certain field
     */

    FieldCategory category() default ESE;

    /**
     * The converter is the name of the groovy method in ToolCode.groovy which is to be applied to the
     * values of this field when it is normalized.
     *
     * @return the name of the converter method in ToolCode.groovy
     */

    String converter() default "";

    /**
     * Flag whether the converter is producing an array of results rather than just one result.
     * This will affect the generated code.
     *
     * @return true if an array is returned
     */
    
    boolean converterMultipleOutput() default false;

    /**
     * Is this a URL?
     *
     * @return true if it must be
     */

    boolean url() default false;

    /**
     * The regular expression which must match the content of this field
     * @return a regular expression string
     */

    String regularExpression() default ""; // todo: use it in validation

    /**
     * Fetch the enum class to which this field's value must belong
     *
     * @return an enum class
     */

    Class<? extends Enum> enumClass() default NO_ENUM.class; // todo: use in validation

    /**
     * Flag whether the enum is value-mapped, meaning that there will be a map constructed
     * from analyzed source values to the enum.
     *
     * @return true if a map is to be made
     */

    boolean valueMapped() default false;

}
