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

import java.util.ArrayList;
import java.util.List;

/**
 * Generate code snippets for field mappings
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class CodeGenerator {

    public void generateCodeFor(FieldMapping fieldMapping, List<SourceVariable> sourceVariables, String constantValue, boolean dictionaryPreferred) {
        if (sourceVariables.isEmpty()) {
            lineConstant(fieldMapping, constantValue);
        }
        else {
            for (SourceVariable variable : sourceVariables) {
                copyCode(variable.getNode(), fieldMapping, dictionaryPreferred);
            }
        }
    }

    public List<FieldMapping> createObviousMappings(List<FieldDefinition> unmappedFieldDefinitions, List<SourceVariable> variables) {
        List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
        FieldMapping uniqueMapping = createUniqueMapping(unmappedFieldDefinitions, variables);
        if (uniqueMapping != null) {
            fieldMappings.add(uniqueMapping);
        }
        for (FieldDefinition fieldDefinition : unmappedFieldDefinitions) {
            if (fieldDefinition.validation != null && fieldDefinition.validation.factName != null) {
                FieldMapping fieldMapping = createObviousMappingFromFact(fieldDefinition);
                if (fieldMapping != null) {
                    fieldMappings.add(fieldMapping);
                }
            }
            else {
                for (SourceVariable variable : variables) {
                    String variableName = variable.getVariableName();
                    String fieldName = fieldDefinition.getFieldNameString();
                    if (variableName.endsWith(fieldName)) {
                        FieldMapping fieldMapping = createObviousMappingFromVariable(fieldDefinition, variables);
                        if (fieldMapping != null) {
                            fieldMappings.add(fieldMapping);
                        }
                    }
                }
            }
        }
        return fieldMappings;
    }

    // ===================== the rest is private

    private FieldMapping createUniqueMapping(List<FieldDefinition> unmappedFieldDefinitions, List<SourceVariable> variables) {
        for (SourceVariable variable : variables) {
            if (variable.getNode().isUniqueElement()) {
                for (FieldDefinition definition : unmappedFieldDefinitions) {
                    if (definition.validation != null && definition.validation.id) {
                        FieldMapping fieldMapping = new FieldMapping(definition);
                        lineSelectFirst(fieldMapping, variable.getVariableName());
                    }
                }
            }
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private FieldMapping createObviousMappingFromFact(FieldDefinition fieldDefinition) {
        FieldMapping fieldMapping = new FieldMapping(fieldDefinition);
        for (FactDefinition factDefinition : Facts.definitions()) {
            if (factDefinition.name.equals(fieldDefinition.validation.factName)) {
                line(fieldMapping, factDefinition.name);
            }
        }
        return fieldMapping.code == null ? null : fieldMapping;
    }

    private FieldMapping createObviousMappingFromVariable(FieldDefinition fieldDefinition, List<SourceVariable> variables) {
        FieldMapping fieldMapping = new FieldMapping(fieldDefinition);
        for (SourceVariable variable : variables) {
            String variableName = variable.getVariableName();
            String fieldName = fieldDefinition.getFieldNameString();
            if (variableName.endsWith(fieldName)) {
                copyCode(variable.getNode(), fieldMapping, false);
            }
        }
        return fieldMapping.code == null ? null : fieldMapping;
    }

    private void copyCode(AnalysisTree.Node node, FieldMapping fieldMapping, boolean dictionaryPreferred) {
        FieldDefinition fieldDefinition = fieldMapping.getFieldDefinition();
        if (fieldDefinition.validation != null && fieldDefinition.validation.multivalued) {
            each(fieldMapping, node.getVariableName());
            codeLine(node, fieldMapping, "it", dictionaryPreferred);
            endBlock(fieldMapping);
        }
        else {
            codeLine(node, fieldMapping, String.format("%s[0]", node.getVariableName()), dictionaryPreferred);
        }
    }

    private void codeLine(AnalysisTree.Node node, FieldMapping fieldMapping, String variable, boolean dictionaryPreferred) {
        FieldDefinition fieldDefinition = fieldMapping.getFieldDefinition();
        if (fieldDefinition.validation != null && fieldDefinition.validation.converter != null) {
            if (fieldDefinition.validation.converter.multipleOutput) {
                forPartIn(fieldMapping, variable);
                line(fieldMapping, "part");
                endBlock(fieldMapping);
            }
            else {
                lineSelectFirst(fieldMapping, variable);
            }
        }
        else {
            if (Dictionary.isPossible(fieldDefinition, node) && dictionaryPreferred) {
                dictionaryCall(fieldMapping, node, variable);
            }
            else {
                line(fieldMapping, variable);
            }
        }
    }

    private void lineSelectFirst(FieldMapping fieldMapping, String variable) {
        FieldDefinition fieldDefinition = fieldMapping.getFieldDefinition();
        if (fieldDefinition.validation.converter.pattern != null) {
            variable = String.format(fieldDefinition.validation.converter.pattern, variable);
        }
        fieldMapping.addCodeLine(
                String.format(
                        "%s.%s %s[0]",
                        fieldDefinition.getPrefix(),
                        fieldDefinition.getLocalName(),
                        variable
                )
        );
    }

    private void each(FieldMapping fieldMapping, String variable) {
        fieldMapping.addCodeLine(String.format("%s.each {", variable));
    }

    private void forPartIn(FieldMapping fieldMapping, String variable) {
        FieldDefinition fieldDefinition = fieldMapping.getFieldDefinition();
        fieldMapping.addCodeLine(
                String.format(
                        "for (part in %s) {",
                        String.format(
                                fieldDefinition.validation.converter.pattern,
                                variable
                        )
                )
        );
    }

    private void endBlock(FieldMapping fieldMapping) {
        fieldMapping.addCodeLine("}");
    }

    private void lineConstant(FieldMapping fieldMapping, String constantValue) {
        FieldDefinition fieldDefinition = fieldMapping.getFieldDefinition();
        fieldMapping.addCodeLine(String.format(
                "%s.%s '%s'",
                fieldDefinition.getPrefix(),
                fieldDefinition.getLocalName(),
                constantValue
        ));
    }


    private void line(FieldMapping fieldMapping, String variable) {
        FieldDefinition fieldDefinition = fieldMapping.getFieldDefinition();
        fieldMapping.addCodeLine(
                String.format(
                        "%s.%s %s",
                        fieldDefinition.getPrefix(),
                        fieldDefinition.getLocalName(),
                        variable
                )
        );
    }

    private void dictionaryCall(FieldMapping fieldMapping, AnalysisTree.Node node, String variable) {
        FieldDefinition fieldDefinition = fieldMapping.getFieldDefinition();
        fieldMapping.createValueMap(node.getStatistics().getHistogramValues());
        fieldMapping.addCodeLine(
                String.format(
                        "%s.%s %s(%s)",
                        fieldDefinition.getPrefix(),
                        fieldDefinition.getLocalName(),
                        fieldDefinition.getFieldNameString(),
                        variable
                )
        );
    }

}
