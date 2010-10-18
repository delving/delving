package eu.delving.core.metadata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An XStream approach for replacing the annotated beans.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("field")
public class MetaField {
    public String getTag() {
        return localName; // todo: prefix?
    }

    public String getFieldNameString() {
        if (getPrefix().isEmpty()) {
            return localName;
        }
        else {
            return getPrefix() + '_' + localName;
        }
    }

    public String getPrefix() {
        if (!facetPrefix.isEmpty()) {
            return facetPrefix;
        }
        else {
            return prefix;
        }
    }

    @XStreamAsAttribute String facetPrefix = "";
    @XStreamAsAttribute Boolean briefDoc = false;
    @XStreamAsAttribute Boolean fullDoc = true;
    @XStreamAsAttribute Boolean hidden = false;
    @XStreamAsAttribute Boolean id = false;
    @XStreamAsAttribute Boolean object = false;
    @XStreamAsAttribute Boolean type = false;
    @XStreamAsAttribute String requiredGroup = "";
    @XStreamAsAttribute Boolean constant = false; // todo: make it generate the fields on the analysis
    @XStreamAsAttribute String category = "ESE";
    @XStreamAsAttribute String converter = "";
    @XStreamAsAttribute Boolean converterMultipleOutput = false;
    @XStreamAsAttribute Boolean url = false;
    @XStreamAsAttribute String regularExpression = ""; // todo: use it in validation
    @XStreamAsAttribute String enumClass = ""; // todo: use in validation
    @XStreamAsAttribute Boolean valueMapped = false;
    @XStreamAsAttribute String prefix = "";  // overrides Field value
    @XStreamAsAttribute String localName = "";       // overrides Field value
    @XStreamAsAttribute String fieldType = "text";
    @XStreamAsAttribute Boolean multivalued = true; // todo: use this for validation!
    @XStreamAsAttribute Boolean stored = true;
    @XStreamAsAttribute Boolean indexed = true;
    @XStreamAsAttribute Boolean required = false;
    @XStreamAsAttribute Boolean compressed = false;
    @XStreamAsAttribute Boolean termVectors = true;
    @XStreamAsAttribute Boolean termPositions = false;
    @XStreamAsAttribute Boolean termOffsets = false;
    @XStreamAsAttribute Boolean omitNorms = false;
    @XStreamAsAttribute String defaultValue = "";
    List<String> toCopyField;

    public Set<String> getEnumValues() {
        if (enumClass != null) {
            try {
                Class<?> classObject = Class.forName(enumClass);
                Method getCodeMethod = null;
                try {
                    getCodeMethod = classObject.getMethod("getCode");
                }
                catch (NoSuchMethodException e) {
                    // ok, so you don't have such a method, see if i care.
                }
                Set<String> enumValues = new TreeSet<String>();
                for (Object e : classObject.getEnumConstants()) {
                    if (getCodeMethod != null) {
                        try {
                            enumValues.add((String) getCodeMethod.invoke(e));
                        }
                        catch (Exception ex) {
                            throw new RuntimeException("Exception while executing getCode() on " + enumClass);
                        }
                    }
                    else {
                        enumValues.add(e.toString());
                    }
                }
                return enumValues;
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not get enum values from " + enumClass);
            }
        }
        else {
            return null;
        }
    }

    private void string(String value, String name, StringBuilder out) {
        if (value != null && !value.isEmpty()) {
            out.append("   ").append(name).append(" = '").append(value).append("',\n");
        }
    }

    private void flagOn(Boolean value, String name, StringBuilder out) {
        if (value != null && value) {
            out.append("   ").append(name).append(" = true,").append('\n');
        }
    }

    private void flagOff(Boolean value, String name, StringBuilder out) {
        if (value == null || !value) {
            out.append("   ").append(name).append(" = false,").append('\n');
        }
    }

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

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(facetPrefix).append('.').append(localName).append(" (\n");
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
                out.append("    ").append("toCopyField=").append(f).append('\n');
            }
        }
        out.append(")\n");
        return out.toString();
    }

}
