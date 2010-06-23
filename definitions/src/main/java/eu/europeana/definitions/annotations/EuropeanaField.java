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

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reveal information from the annotated bean field
 *
 * @author Gerald de Jong geralddejong@gmail.com
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class EuropeanaField {
    private java.lang.reflect.Field field;
    private Europeana europeanaAnnotation;
    private Solr solrAnnotation;
    private String fieldNameString;
    private Set<String> enumValues;

    public EuropeanaField(java.lang.reflect.Field field) {
        this.field = field;
        europeanaAnnotation = field.getAnnotation(Europeana.class);
        solrAnnotation = field.getAnnotation(Solr.class);
        if (europeanaAnnotation == null) {
            throw new IllegalStateException("Field must have @Europeana annotation: " + field.getDeclaringClass().getName() + "." + field.getName());
        }
        if (solrAnnotation == null) {
            throw new IllegalStateException("Field must have solrj @Solr annotation: " + field.getDeclaringClass().getName() + "." + field.getName());
        }
        if (europeanaAnnotation.enumClass() != Europeana.NO_ENUM.class) {
            collectEnumValues();
        }
    }

    private void collectEnumValues() {
        Method getCodeMethod = null;
        try {
            getCodeMethod = europeanaAnnotation.enumClass().getMethod("getCode");
        }
        catch (NoSuchMethodException e) {
            // ok, so you don't have such a method, see if i care.
        }
        enumValues = new TreeSet<String>();
        for (Enum e : europeanaAnnotation.enumClass().getEnumConstants()) {
            if (getCodeMethod != null) {
                try {
                    enumValues.add((String)getCodeMethod.invoke(e));
                }
                catch (Exception ex) {
                    throw new RuntimeException("Exception while executing getCode() on the enumeration "+europeanaAnnotation.enumClass().getName());
                }
            }
            else {
                enumValues.add(e.toString());
            }
        }
    }

    public String getPrefix() {
        if (!europeanaAnnotation.facetPrefix().isEmpty()) {
            return europeanaAnnotation.facetPrefix();
        }
        else {
            return solrAnnotation.prefix();
        }
    }

    public String getXmlName() {
        return getPrefix() + ":" + getLocalName();
    }

    public String getLocalName() {
        String name = solrAnnotation.localName();
        if (name.isEmpty()) {
            name = field.getName();
        }
        return name;
    }

    public String getFieldNameString() {
        if (fieldNameString == null) {
            if (getPrefix().isEmpty()) {
                fieldNameString = getLocalName();
            }
            else {
                fieldNameString = getPrefix() + '_' + getLocalName();
            }
        }
        return fieldNameString;
    }

    public String getFacetName() {
        if (!europeanaAnnotation.facetPrefix().isEmpty()) {
            if (!solrAnnotation.localName().isEmpty()) {
                return solrAnnotation.localName().toUpperCase();
            }
            else {
                return field.getName().toUpperCase();
            }
        }
        else {
            return field.getName();
        }
    }

    public Europeana europeana() {
        return europeanaAnnotation;
    }

    public Solr solr() {
        return solrAnnotation;
    }

    public Set<String> getEnumValues() {
        return enumValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EuropeanaField that = (EuropeanaField) o;
        return !(field != null ? !field.equals(that.field) : that.field != null);
    }

    @Override
    public int hashCode() {
        return field != null ? field.hashCode() : 0;
    }

    public String toString() {
        return field.getName();
    }
}
