/*
 * Copyright 2010 DELVING BV
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.delving.metadata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Defines the root of a hierarchical model
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("record-definition")
public class RecordDefinition {

    @XStreamAsAttribute
    public String prefix;

    public List<ConstantInputDefinition> constants;

    public List<NamespaceDefinition> namespaces;

    public ElementDefinition root;

    void initialize() throws MetadataException {
        if (constants == null) {
            throw new MetadataException("A record definition must have constants defined");
        }
        root.setPaths(new Path());
        for (ConstantInputDefinition constantInputDefinition : constants) {
            if (constantInputDefinition.fieldPath == null) continue;
            FieldDefinition fieldDefinition = getFieldDefinition(new Path(constantInputDefinition.fieldPath));
            if (fieldDefinition == null) {
                throw new MetadataException(String.format("Constant input %s has no corresponding path", constantInputDefinition.name));
            }
            if (!fieldDefinition.constant) {
                throw new MetadataException(String.format("Constant input %s has path %s which is not to a constant", constantInputDefinition.name, constantInputDefinition.fieldPath));
            }
            constantInputDefinition.fieldDefinition = fieldDefinition;
        }
        List<FieldDefinition> constantFields = new ArrayList<FieldDefinition>();
        root.getConstantFields(constantFields);
        for (FieldDefinition fieldDefinition : constantFields) {
            boolean found = false;
            for (ConstantInputDefinition constantInputDefinition : constants) {
                if (constantInputDefinition.fieldDefinition == fieldDefinition) {
                    found = true;
                }
            }
            if (!found) {
                throw new MetadataException(String.format("Constant field %s has no corresponding constant input", fieldDefinition.path));
            }
        }
    }

    public List<FieldDefinition> getMappableFields() {
        List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
        root.getMappableFields(fields);
        return fields;
    }

    public FieldDefinition getFieldDefinition(Path path) {
        return root.getFieldDefinition(path);
    }

    public List<String> getFieldNameList() {
        List<String> fieldNames = new ArrayList<String>();
        root.getFieldNames(fieldNames);
        return fieldNames;
    }

    public Map<String, String> getFacetMap() {
        Map<String, String> facetMap = new TreeMap<String, String>();
        root.getFacetMap(facetMap);
        return facetMap;
    }

    public String[] getFacetFieldStrings() {
        List<String> facetFieldStrings = new ArrayList<String>();
        root.getFacetFieldStrings(facetFieldStrings);
        return facetFieldStrings.toArray(new String[facetFieldStrings.size()]);
    }

    public String[] getFieldStrings() {
        List<String> fieldStrings = new ArrayList<String>();
        root.getFieldStrings(fieldStrings);
        return fieldStrings.toArray(new String[fieldStrings.size()]);
    }

    public String toString() {
        return toString(this);
    }

    // handy static methods

    public static RecordDefinition read(InputStream in) throws MetadataException {
        RecordDefinition recordDefinition = (RecordDefinition) stream().fromXML(in);
        recordDefinition.initialize();
        return recordDefinition;
    }

    public static String toString(RecordDefinition recordDefinition) {
        return stream().toXML(recordDefinition);
    }

    private static XStream stream() {
        XStream stream = new XStream();
        stream.processAnnotations(new Class[]{
                RecordDefinition.class,
                ElementDefinition.class,
                FieldDefinition.class
        });
        return stream;
    }

}
