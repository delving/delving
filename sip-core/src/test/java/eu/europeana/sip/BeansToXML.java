package eu.europeana.sip;

import eu.delving.core.metadata.ElementDefinition;
import eu.delving.core.metadata.FieldDefinition;
import eu.delving.core.metadata.RecordDefinition;
import eu.europeana.sip.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.sip.definitions.annotations.Europeana;
import eu.europeana.sip.definitions.annotations.EuropeanaField;
import eu.europeana.sip.definitions.beans.abm.AllFieldBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class BeansToXML {

    private static List<String> getEnumValues(Class<?> enumClass) {
        if (enumClass != null) {
            Method getCodeMethod = null;
            try {
                getCodeMethod = enumClass.getMethod("getCode");
            }
            catch (NoSuchMethodException e) {
                // ok, so you don't have such a method, see if i care.
            }
            List<String> enumValues = new ArrayList<String>();
            for (Object e : enumClass.getEnumConstants()) {
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
        else {
            return null;
        }
    }

    private static String string(String value) {
        return value != null && !value.isEmpty() ? value : null;
    }

    private static Boolean bool(Boolean value) {
        return value != null && value ? value : null;
    }

    public static void main(String[] args) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(AllFieldBean.class);
        AnnotationProcessorImpl ap = new AnnotationProcessorImpl();
        ap.setClasses(classes);
        RecordDefinition rd = new RecordDefinition();
        rd.root = new ElementDefinition();
        for (EuropeanaField ef : ap.getAllFields()) {
            FieldDefinition fd = new FieldDefinition();
            fd.prefix = string(ef.solr().prefix());
            fd.localName = string(ef.getLocalName());
            fd.briefDoc = bool(ef.europeana().briefDoc());
            fd.category = string(ef.europeana().category().toString());
            fd.compressed = bool(ef.solr().compressed());
            fd.constant = bool(ef.europeana().constant());
            fd.converter = string(ef.europeana().converter());
            fd.converterMultipleOutput = bool(ef.europeana().converterMultipleOutput());
            fd.defaultValue = string(ef.solr().defaultValue());
            fd.facetPrefix = string(ef.europeana().facetPrefix());
            fd.fieldType = string(ef.solr().fieldType());
            fd.fullDoc = bool(ef.europeana().fullDoc());
            fd.hidden = bool(ef.europeana().hidden());
            fd.id = bool(ef.europeana().id());
            fd.indexed = bool(ef.solr().indexed());
            fd.multivalued = bool(ef.solr().multivalued());
            fd.object = bool(ef.europeana().object());
            fd.omitNorms = bool(ef.solr().omitNorms());
            fd.options = (ef.europeana().enumClass() == Europeana.NO_ENUM.class) ? null : getEnumValues(ef.europeana().enumClass());
            fd.regularExpression = string(ef.europeana().regularExpression());
            fd.required = bool(ef.solr().required());
            fd.requiredGroup = string(ef.europeana().requiredGroup());
            fd.stored = bool(ef.solr().stored());
            fd.termOffsets = bool(ef.solr().termOffsets());
            fd.termPositions = bool(ef.solr().termPositions());
            fd.termVectors = bool(ef.solr().termVectors());
            fd.toCopyField = ef.solr().toCopyField().length == 0 ? null : new ArrayList<String>(Arrays.asList(ef.solr().toCopyField()));
            fd.type = bool(ef.europeana().type());
            fd.url = bool(ef.europeana().url());
            fd.valueMapped = bool(ef.europeana().valueMapped());
            rd.root.fields.add(fd);
        }
        Collections.sort(rd.root.fields, new Comparator<FieldDefinition>() {
            @Override
            public int compare(FieldDefinition a, FieldDefinition b) {
                String aa = a.prefix;
                String bb = b.prefix;
                if (aa == null) {
                    return 1;
                }
                if (bb == null) {
                    return -1;
                }
                int pc = aa.compareTo(bb);
                if (pc != 0) {
                    return pc;
                }
                return a.getLocalName().compareTo(b.getLocalName());
            }
        });
        System.out.println(rd.toString());
    }
}
