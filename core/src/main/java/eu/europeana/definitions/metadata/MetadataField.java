package eu.europeana.definitions.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.Arrays;

/**
 * An XStream approach for replacing the annotated beans.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("metadata-field")
public class MetadataField {

    /**
     * A prefix for a facet, must be unique
     */

    @XStreamAsAttribute
    String facetPrefix = "";

    /**
     * This field will appear in the brief doc rendering
     */

    @XStreamAsAttribute
    boolean briefDoc = false;

    /**
     * This field will appear in the full doc rendering
     */

    @XStreamAsAttribute
    boolean fullDoc = true;

    /**
     * This field is hidden
     */

    @XStreamAsAttribute
    boolean hidden = false;

    /**
     * Is this the europeana id to use
     */

    @XStreamAsAttribute
    boolean id = false;

    /**
     * Is this an object to which the record refers?
     */

    @XStreamAsAttribute
    boolean object = false;

    /**
     * Is this the europeana type?
     */

    @XStreamAsAttribute
    boolean type = false;

    /**
     * If the field is required, then this value is the name of the requirement group.  Typically this is
     * the field name itself, but sometimes two fields can share the same requirement group.
     */

    @XStreamAsAttribute
    String requiredGroup = "";

    /**
     * There are some fields that are added by the Europeana System during the IngestionPhase based on meta-information
     * provided with the DataSet during submission.
     *
     * These fields are therefore unmappable and must be kept separated during ESE validation and only used during ESE+
     * validation.
     *
     * When EDM is adopted as the internal datamodel the same will apply. These fields need to be kept separate during
     * initial import validation and only be validated during the validation of the Archival Information Packages.
     */

    @XStreamAsAttribute
    boolean constant = false; // todo: make it generate the fields on the analysis

    /**
     *  The annotated fields can be valid at different levels in the application. The FieldCategory will be used to
     * create a Data Model Validator to be used at various stages of ingestion.
     *
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
    boolean converterMultipleOutput = false;

    /**
     * Is this a URL?
     */

    @XStreamAsAttribute
    boolean url = false;

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
    boolean valueMapped = false;

    @XStreamAsAttribute
    String prefix = "";  // overrides Field value

    @XStreamAsAttribute
    String localName = "";       // overrides Field value

    // Solr Schema stuff

    @XStreamAsAttribute
    String fieldType = "text";

    @XStreamAsAttribute
    boolean multivalued = true; // todo: use this for validation!

    @XStreamAsAttribute
    boolean stored = true;

    @XStreamAsAttribute
    boolean indexed = true;

    @XStreamAsAttribute
    boolean required = false;

    @XStreamAsAttribute
    boolean compressed = false;

    // advanced (fields should not be displayed when not specified)

    @XStreamAsAttribute
    boolean termVectors = true;

    @XStreamAsAttribute
    boolean termPositions = false;

    @XStreamAsAttribute
    boolean termOffsets = false;

    @XStreamAsAttribute
    boolean omitNorms = false;

    @XStreamAsAttribute
    String defaultValue = "";

    String[] toCopyField = {};


    @Override
    public String toString() {
        return "MetadataField{" +
                "facetPrefix='" + facetPrefix + '\'' +
                ", briefDoc=" + briefDoc +
                ", fullDoc=" + fullDoc +
                ", hidden=" + hidden +
                ", id=" + id +
                ", object=" + object +
                ", type=" + type +
                ", requiredGroup='" + requiredGroup + '\'' +
                ", constant=" + constant +
                ", category='" + category + '\'' +
                ", converter='" + converter + '\'' +
                ", converterMultipleOutput=" + converterMultipleOutput +
                ", url=" + url +
                ", regularExpression='" + regularExpression + '\'' +
                ", enumClass='" + enumClass + '\'' +
                ", valueMapped=" + valueMapped +
                ", prefix='" + prefix + '\'' +
                ", localName='" + localName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", multivalued=" + multivalued +
                ", stored=" + stored +
                ", indexed=" + indexed +
                ", required=" + required +
                ", compressed=" + compressed +
                ", termVectors=" + termVectors +
                ", termPositions=" + termPositions +
                ", termOffsets=" + termOffsets +
                ", omitNorms=" + omitNorms +
                ", defaultValue='" + defaultValue + '\'' +
                ", toCopyField=" + (toCopyField == null ? null : Arrays.asList(toCopyField)) +
                '}';
    }
}
