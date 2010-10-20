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

package eu.delving.core.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generate code snippets for field mappings
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class CodeGenerator {

    public FieldMapping createFieldMapping(FieldDefinition fieldDefinition, List<SourceVariable> variables, String constantValue) {
        FieldMapping fieldMapping = new FieldMapping(fieldDefinition);
        if (variables.isEmpty()) {
            fieldMapping.addCodeLine(String.format(
                    "%s.%s '%s'",
                    fieldDefinition.getPrefix(),
                    fieldDefinition.getLocalName(),
                    constantValue
            ));
        }
        else {
            for (SourceVariable holder : variables) {
                generateCopyCode(fieldMapping.getField(), holder.getNode(), fieldMapping);
            }
        }
        return fieldMapping;
    }

    public List<FieldMapping> createObviousMetaFieldMappings(List<FieldDefinition> unmappedFieldDefinitions, List<SourceVariable> variables) {
        List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
        for (FieldDefinition fieldDefinition : unmappedFieldDefinitions) {
            if (fieldDefinition.constant) {
                FieldMapping fieldMapping = createObviousMapping(fieldDefinition, variables);
                if (fieldMapping != null) {
                    fieldMappings.add(fieldMapping);
                }
            }
            else {
                for (SourceVariable variable : variables) {
                    String variableName = variable.getVariableName();
                    String fieldName = fieldDefinition.getFieldNameString();
                    if (variableName.endsWith(fieldName)) {
                        FieldMapping fieldMapping = createObviousMapping(fieldDefinition, variables);
                        if (fieldMapping != null) {
                            fieldMappings.add(fieldMapping);
                        }
                    }
                }
            }
        }
        return fieldMappings;
    }

    private FieldMapping createObviousMapping(FieldDefinition fieldDefinition, List<SourceVariable> variables) {
        FieldMapping fieldMapping = new FieldMapping(fieldDefinition);
        if (fieldDefinition.constant) {
            fieldMapping.addCodeLine(String.format(
                    "%s.%s %s",
                    fieldDefinition.getPrefix(),
                    fieldDefinition.getLocalName(),
                    fieldDefinition.getFieldNameString()
            ));
        }
        else {
            for (SourceVariable variable : variables) {
                String variableName = variable.getVariableName();
                String fieldName = fieldDefinition.getFieldNameString();
                if (variableName.endsWith(fieldName)) {
                    generateCopyCode(fieldDefinition, variable.getNode(), fieldMapping);
                }
            }
        }
        return fieldMapping.isEmpty() ? null : fieldMapping;
    }

    private void generateCopyCode(FieldDefinition fieldDefinition, AnalysisTree.Node node, FieldMapping fieldMapping) {
        if (fieldDefinition.multivalued) {
            fieldMapping.addCodeLine(String.format("%s.each {", node.getVariableName()));
            if (fieldDefinition.converter == null) {
                if (!fieldDefinition.valueMapped) {
                    fieldMapping.addCodeLine(String.format("%s.%s it", fieldDefinition.getPrefix(), fieldDefinition.getLocalName()));
                }
                else {
                    fieldMapping.createValueMap(getActualValues(node), fieldDefinition.options);
                    fieldMapping.addCodeLine(String.format("%s.%s %s(it)", fieldDefinition.getPrefix(), fieldDefinition.getLocalName(), fieldDefinition.getFieldNameString()));
                }
            }
            else {
                if (fieldDefinition.converterMultipleOutput) {
                    fieldMapping.addCodeLine(String.format("for (part in %s(it)) {", fieldDefinition.converter));
                    fieldMapping.addCodeLine(String.format("%s.%s part", fieldDefinition.getPrefix(), fieldDefinition.getLocalName()));
                    fieldMapping.addCodeLine("}");
                }
                else {
                    fieldMapping.addCodeLine(String.format("%s.%s %s(it)[0]", fieldDefinition.getPrefix(), fieldDefinition.getLocalName(), fieldDefinition.converter));
                }
            }
            fieldMapping.addCodeLine("}");
        }
        else {
            if (fieldDefinition.converter == null) {
                if (!fieldDefinition.valueMapped) {
                    fieldMapping.addCodeLine(String.format("%s.%s %s[0]", fieldDefinition.getPrefix(), fieldDefinition.getLocalName(), node.getVariableName()));
                }
                else {
                    fieldMapping.createValueMap(getActualValues(node), fieldDefinition.options);
                    fieldMapping.addCodeLine(String.format("%s.%s %s(%s[0])", fieldDefinition.getPrefix(), fieldDefinition.getLocalName(), node.getVariableName(), fieldDefinition.getFieldNameString()));
                }
            }
            else {
                if (fieldDefinition.converterMultipleOutput) {
                    fieldMapping.addCodeLine(String.format("for (part in %s(%s[0])) {", fieldDefinition.converter, node.getVariableName()));
                    fieldMapping.addCodeLine(String.format("%s.%s part", fieldDefinition.getPrefix(), fieldDefinition.getLocalName()));
                    fieldMapping.addCodeLine("}");
                }
                else {
                    fieldMapping.addCodeLine(String.format("%s.%s %s(%s[0])[0]", fieldDefinition.getPrefix(), fieldDefinition.getLocalName(), fieldDefinition.converter, node.getVariableName()));
                }
            }
        }
    }

    private Set<String> getActualValues(AnalysisTree.Node node) {
        Set<String> values = new TreeSet<String>();
        for (AnalysisTreeStatistics.Counter counter : node.getStatistics().getCounters()) {
            values.add(counter.getValue());
        }
        return values;
    }
}
