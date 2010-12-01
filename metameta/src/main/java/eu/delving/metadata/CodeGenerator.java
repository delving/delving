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
            for (SourceVariable variable : variables) {
                generateCopyCode(fieldMapping.fieldDefinition, variable.getNode(), fieldMapping);
            }
        }
        return fieldMapping;
    }

    public List<FieldMapping> createObviousMappings(List<FieldDefinition> unmappedFieldDefinitions, List<SourceVariable> variables) {
        // todo: unique element -> id is also obvious
        List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
        for (FieldDefinition fieldDefinition : unmappedFieldDefinitions) {
            if (fieldDefinition.factName != null) {
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

    private FieldMapping createObviousMappingFromFact(FieldDefinition fieldDefinition) {
        FieldMapping fieldMapping = new FieldMapping(fieldDefinition);
        for (FactDefinition factDefinition : Facts.definitions()) {
            if (factDefinition.name.equals(fieldDefinition.factName)) {
                renderLineSimple(fieldDefinition, fieldMapping, factDefinition.name);
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
                generateCopyCode(fieldDefinition, variable.getNode(), fieldMapping);
            }
        }
        return fieldMapping.code == null ? null : fieldMapping;
    }

    private void generateCopyCode(FieldDefinition fieldDefinition, AnalysisTree.Node node, FieldMapping fieldMapping) {
        if (fieldDefinition.multivalued) {
            startEachBlock(fieldMapping, node.getVariableName());
            renderLine(fieldDefinition, node, fieldMapping, "it");
            endBlock(fieldMapping);
        }
        else {
            renderLine(fieldDefinition, node, fieldMapping, String.format("%s[0]", node.getVariableName()));
        }
    }

    private void renderLine(FieldDefinition fieldDefinition, AnalysisTree.Node node, FieldMapping fieldMapping, String variable) {
        if (fieldDefinition.converterPattern != null) {
            if (fieldDefinition.converterMultipleOutput) {
                startForPartInConvert(fieldDefinition, fieldMapping, variable);
                renderLineSimple(fieldDefinition, fieldMapping, "part");
                endBlock(fieldMapping);
            }
            else {
                renderLineSelect(fieldDefinition, fieldMapping, variable);
            }
        }
        else {
            if (fieldDefinition.valueMapped) {
                renderValueMapLine(fieldDefinition, fieldMapping, node, variable);
            }
            else {
                renderLineSimple(fieldDefinition, fieldMapping, variable);
            }
        }
    }

    private void startForPartInConvert(FieldDefinition fieldDefinition, FieldMapping fieldMapping, String variable) {
        fieldMapping.addCodeLine(
                String.format(
                        "for (part in %s) {",
                        String.format(
                                fieldDefinition.converterPattern,
                                variable
                        )
                )
        );
    }

    private void renderLineSelect(FieldDefinition fieldDefinition, FieldMapping fieldMapping, String variable) {
        if (fieldDefinition.converterPattern != null) {
            variable = String.format(fieldDefinition.converterPattern, variable);
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

    private void renderValueMapLine(FieldDefinition fieldDefinition, FieldMapping fieldMapping, AnalysisTree.Node node, String variable) {
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

    private void renderLineSimple(FieldDefinition fieldDefinition, FieldMapping fieldMapping, String variable) {
        fieldMapping.addCodeLine(
                String.format(
                        "%s.%s %s",
                        fieldDefinition.getPrefix(),
                        fieldDefinition.getLocalName(),
                        variable
                )
        );
    }

    private void startEachBlock(FieldMapping fieldMapping, String variable) {
        fieldMapping.addCodeLine(String.format("%s.each {", variable));
    }

    private void endBlock(FieldMapping fieldMapping) {
        fieldMapping.addCodeLine("}");
    }
}
