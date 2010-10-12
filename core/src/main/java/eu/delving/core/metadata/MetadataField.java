package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;

/**
 * An XStream approach for replacing the annotated beans.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("field")
public class MetadataField {

    private Object readResolve() {
        multivalued = setDefaultTrue(multivalued);
        stored = setDefaultTrue(stored);
        indexed = setDefaultTrue(indexed);
        termVectors = setDefaultTrue(termVectors);
        return this;
    }

    private Boolean setDefaultTrue(Boolean value) {
        return value == null ? true : value;
    }

    /**
     * A prefix for a facet, must be unique
     */

    @XStreamAsAttribute
    String facetPrefix = "";

    /**
     * This field will appear in the brief doc rendering
     */

    @XStreamAsAttribute
    Boolean briefDoc = false;

    /**
     * This field will appear in the full doc rendering
     */

    @XStreamAsAttribute
    Boolean fullDoc = true;

    /**
     * This field is hidden
     */

    @XStreamAsAttribute
    Boolean hidden = false;

    /**
     * Is this the europeana id to use
     */

    @XStreamAsAttribute
    Boolean id = false;

    /**
     * Is this an object to which the record refers?
     */

    @XStreamAsAttribute
    Boolean object = false;

    /**
     * Is this the europeana type?
     */

    @XStreamAsAttribute
    Boolean type = false;

    /**
     * If the field is required, then this value is the name of the requirement group.  Typically this is
     * the field name itself, but sometimes two fields can share the same requirement group.
     */

    @XStreamAsAttribute
    String requiredGroup = "";

    /**
     * There are some fields that are added by the Europeana System during the IngestionPhase based on meta-information
     * provided with the DataSet during submission.
     * <p/>
     * These fields are therefore unmappable and must be kept separated during ESE validation and only used during ESE+
     * validation.
     * <p/>
     * When EDM is adopted as the internal datamodel the same will apply. These fields need to be kept separate during
     * initial import validation and only be validated during the validation of the Archival Information Packages.
     */

    @XStreamAsAttribute
    Boolean constant = false; // todo: make it generate the fields on the analysis

    /**
     * The annotated fields can be valid at different levels in the application. The FieldCategory will be used to
     * create a Data Model Validator to be used at various stages of ingestion.
     * <p/>
     * The validator should also be used in the Sip-Creator and could be used in external applications.
     */

    @XStreamAsAttribute
    String category = "ESE";

    /**
     * The converter is the name of the groovy method in ToolCode.groovy which is to be applied to the
     * values of this field when it is normalized.
     */

    @XStreamAsAttribute
    String converter = "";

    /**
     * Flag whether the converter is producing an array of results rather than just one result.
     * This will affect the generated code.
     */

    @XStreamAsAttribute
    Boolean converterMultipleOutput = false;

    /**
     * Is this a URL?
     */

    @XStreamAsAttribute
    Boolean url = false;

    /**
     * The regular expression which must match the content of this field
     */

    @XStreamAsAttribute
    String regularExpression = ""; // todo: use it in validation

    /**
     * Fetch the enum class to which this field's value must belong
     */

    @XStreamAsAttribute
    String enumClass = ""; // todo: use in validation

    /**
     * Flag whether the enum is value-mapped, meaning that there will be a map constructed
     * from analyzed source values to the enum.
     */

    @XStreamAsAttribute
    Boolean valueMapped = false;

    @XStreamAsAttribute
    String prefix = "";  // overrides Field value

    @XStreamAsAttribute
    String localName = "";       // overrides Field value

    // Solr Schema stuff

    @XStreamAsAttribute
    String fieldType = "text";

    @XStreamAsAttribute
    Boolean multivalued = true; // todo: use this for validation!

    @XStreamAsAttribute
    Boolean stored = true;

    @XStreamAsAttribute
    Boolean indexed = true;

    @XStreamAsAttribute
    Boolean required = false;

    @XStreamAsAttribute
    Boolean compressed = false;

    // advanced (fields should not be displayed when not specified)

    @XStreamAsAttribute
    Boolean termVectors = true;

    @XStreamAsAttribute
    Boolean termPositions = false;

    @XStreamAsAttribute
    Boolean termOffsets = false;

    @XStreamAsAttribute
    Boolean omitNorms = false;

    @XStreamAsAttribute
    String defaultValue = "";

    List<String> toCopyField;

    private void string(String value, String name, StringBuilder out) {
        if (value != null && !value.isEmpty()) {
            out.append(name).append("=").append(value).append('\n');
        }
    }

    private void flagOn(Boolean value, String name, StringBuilder out) {
        if (value != null && value) {
            out.append(name).append("=true").append('\n');
        }
    }

    private void flagOff(Boolean value, String name, StringBuilder out) {
        if (value == null || !value) {
            out.append(name).append("=false").append('\n');
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        string(facetPrefix, "facetPrefix", out);
        string(localName, "localName", out);
        string(fieldType, "fieldType", out);
        flagOn(briefDoc, "briefDoc", out);
        flagOn(hidden, "hidden", out);
        flagOn(id, "id", out);
        flagOn(object, "object", out);
        flagOn(type, "type", out);
        flagOn(constant, "constant", out);
        string(requiredGroup, "requiredGroup", out);
        string(category, "category", out);
        string(converter, "converter", out);
        flagOn(converterMultipleOutput, "converterMultipleOutput", out);
        flagOn(url, "url", out);
        string(regularExpression, "regularExpression", out);
        string(enumClass, "enumClass", out);
        flagOn(valueMapped, "valueMapped", out);
        string(prefix, "prefix", out);
        flagOff(multivalued, "multivalued", out);
        flagOff(stored, "stored", out);
        flagOff(indexed, "indexed", out);
        flagOn(required, "required", out);
        flagOn(compressed, "compressed", out);
        flagOff(termVectors, "termVectors", out);
        flagOn(termOffsets, "termOffsets", out);
        flagOn(omitNorms, "omitNorms", out);
        string(defaultValue, "defaultValue", out);
        if (toCopyField != null) {
            for (String f : toCopyField) {
                out.append("toCopyField=").append(f).append('\n');
            }
        }
        return out.toString();
    }
}
