package eu.europeana.sip;

import com.thoughtworks.xstream.annotations.*;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import eu.europeana.query.RecordField;
import eu.europeana.sip.converters.Converter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Just for reading config information
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("profile")
public class Profile {

    @XStreamAsAttribute
    public String name;

    @XStreamAsAttribute
    public boolean duplicatesAllowed;

    @XStreamAsAttribute
    public boolean renderDuplicates;

    @XStreamAsAttribute
    public boolean discardWithoutObject;

    public List<Source> sources;

    @XStreamAlias("type-mappings")
    public List<TypeMapping> typeMappings;

    @XStreamAlias("language-mappings")
    public List<LanguageMapping> languageMappings;

    @XStreamAlias("source")
    public static class Source {
        @XStreamAsAttribute
        public String file;

        @XStreamAsAttribute
        public String recordSeparator;

        @XStreamAsAttribute
        public int recordSeparatorDepth;

        @XStreamAsAttribute
        public String collectionId;

        @XStreamAlias("field-mappings")
        public List<FieldMapping> fieldMappings;

        @XStreamAlias("additions")
        public List<RecordAddition> additions;

    }

    @XStreamAlias("type-mapping")
    public static class TypeMapping {
        @XStreamAsAttribute
        String type;

        @XStreamImplicit
        List<String> from;
    }

    @XStreamAlias("language-mapping")
    public static class LanguageMapping {
        @XStreamAsAttribute
        String code;

        @XStreamImplicit
        List<String> from;
    }

    @XStreamAlias("field-mapping")
    public static class FieldMapping implements Comparable<FieldMapping> {

        public FieldMapping() {
        }

        public FieldMapping(String from) {
            this.from = from;
            this.mapTo = new ArrayList<MapTo>();
        }

        @XStreamAsAttribute
        public String from;

        @XStreamAsAttribute
        String acceptField;

        @XStreamAsAttribute
        String discardRecord;

        @XStreamAsAttribute
        boolean chooseFirst;

        @XStreamAsAttribute
        boolean chooseLast;

        @XStreamImplicit
        public List<MapTo> mapTo;

        @Override
        public int compareTo(FieldMapping o) {
            return from.compareTo(o.from);
        }
    }

    @XStreamAlias("to")
    public static class MapTo {

        public MapTo() {
        }

        public MapTo(RecordField key) {
            this.key = key;
        }

        @XStreamAsAttribute
        @XStreamConverter(EnumConverter.class)
        RecordField key;

        @XStreamAsAttribute
        String converter;

        @XStreamAsAttribute
        int order;

        @XStreamAsAttribute
        String concatenateSuffix;

        @XStreamOmitField
        Converter converterInstance;

        Converter getConverter() {
            if (converter != null && converterInstance == null) {
                try {
                    String [] constructParts = parseConverterString(converter);
                    Class<?> clazz = Class.forName("eu.europeana.sip.converters."+constructParts[0]);
                    if (!Converter.class.isAssignableFrom(clazz)) {
                        throw new RuntimeException("Class '"+clazz.getName()+"' must implement SolrField.Converter");
                    }
                    String [] params = new String[constructParts.length-1];
                    Class<?> [] paramTypes = new Class<?>[params.length];
                    for (int walk=0; walk<params.length; walk++) {
                        paramTypes[walk] = String.class;
                        String trimmed = constructParts[walk+1].trim();
                        if (trimmed.length() == 0) {
                            params[walk] = trimmed;
                        }
                        else {
                            params[walk] = constructParts[walk+1];
                        }
                    }
                    Constructor constructor = clazz.getConstructor(paramTypes);
                    converterInstance = (Converter) constructor.newInstance((Object[])params);
                }
                catch (Exception e) {
                    throw new IllegalArgumentException("Problem loading converter",e);
                }
            }
            return converterInstance;
        }

        public String toString() {
            return key.toString();
        }
    }

    @XStreamAlias("addition")
    public static class RecordAddition {
        @XStreamAsAttribute
        @XStreamConverter(EnumConverter.class)
        RecordField key;

        @XStreamAsAttribute
        String value;

        @XStreamAsAttribute
        boolean ifMissing;
    }

    private static String[] parseConverterString(String converter) {
        if (converter.indexOf('|') >= 0) {
            return parsePipeString(converter);
        }
        else if (converter.indexOf("(") > 0) {
            return parseMethodCallString(converter);
        }
        else {
            return new String[] {converter};
        }
    }

    private static String[] parsePipeString(String converter) {
        List<String> parts = new ArrayList<String>();
        int pipe;
        do {
            pipe = converter.indexOf("|");
            if (pipe >= 0) {
                parts.add(converter.substring(0,pipe));
                converter = converter.substring(pipe+1);
            }
            else {
                parts.add(converter);
            }
        }
        while (pipe >= 0);
        String [] result = new String[parts.size()];
        return parts.toArray(result);
    }

    private static String[] parseMethodCallString(String converter) {
        List<String> parts = new ArrayList<String>();
        int pipe;
        do {
            pipe = converter.indexOf("|");
            if (pipe >= 0) {
                parts.add(converter.substring(0,pipe));
                converter = converter.substring(pipe+1);
            }
            else {
                parts.add(converter);
            }
        }
        while (pipe >= 0);
        String [] result = new String[parts.size()];
        return parts.toArray(result);
    }
}
