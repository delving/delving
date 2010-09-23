package eu.europeana.sip.model;

import eu.europeana.definitions.annotations.EuropeanaField;
import eu.europeana.sip.core.FieldMapping;

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

    public FieldMapping createFieldMapping(EuropeanaField field, List<VariableHolder> variables, String constantValue) {
        FieldMapping fieldMapping = new FieldMapping(field);
        if (variables.isEmpty()) {
            fieldMapping.addCodeLine(String.format(
                    "%s.%s '%s'",
                    field.getPrefix(),
                    field.getLocalName(),
                    constantValue
            ));
        }
        else {
            for (VariableHolder holder : variables) {
                generateCopyCode(fieldMapping.getEuropeanaField(), holder.getNode(), fieldMapping);
            }
        }
        return fieldMapping;
    }

    public List<FieldMapping> createObviousFieldMappings(List<EuropeanaField> unmappedFields, List<VariableHolder> variables) {
        List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
        for (EuropeanaField field : unmappedFields) {
            if (field.europeana().constant()) {
                FieldMapping fieldMapping = createObviousMapping(field, variables);
                if (fieldMapping != null) {
                    fieldMappings.add(fieldMapping);
                }
            }
            else {
                for (VariableHolder variableHolder : variables) {
                    String variableName = variableHolder.getVariableName();
                    String fieldName = field.getFieldNameString();
                    if (variableName.endsWith(fieldName)) {
                        FieldMapping fieldMapping = createObviousMapping(field, variables);
                        if (fieldMapping != null) {
                            fieldMappings.add(fieldMapping);
                        }
                    }
                }
            }
        }
        return fieldMappings;
    }

    private FieldMapping createObviousMapping(EuropeanaField field, List<VariableHolder> variables) {
        FieldMapping fieldMapping = new FieldMapping(field);
        if (field.europeana().constant()) {
            fieldMapping.addCodeLine(String.format(
                    "%s.%s %s",
                    field.getPrefix(),
                    field.getLocalName(),
                    field.getFieldNameString()
            ));
        }
        else {
            for (VariableHolder variableHolder : variables) {
                String variableName = variableHolder.getVariableName();
                String fieldName = field.getFieldNameString();
                if (variableName.endsWith(fieldName)) {
                    generateCopyCode(field, variableHolder.getNode(), fieldMapping);
                }
            }
        }
        return fieldMapping.isEmpty() ? null : fieldMapping;
    }

    private void generateCopyCode(EuropeanaField field, AnalysisTree.Node node, FieldMapping fieldMapping) {
        if (field.solr().multivalued()) {
            fieldMapping.addCodeLine(String.format("%s.each {", node.getVariableName()));
            if (field.europeana().converter().isEmpty()) {
                if (!field.europeana().valueMapped()) {
                    fieldMapping.addCodeLine(String.format("%s.%s it", field.getPrefix(), field.getLocalName()));
                }
                else {
                    fieldMapping.createValueMap(getActualValues(node), field.getEnumValues());
                    fieldMapping.addCodeLine(String.format("%s.%s %s(it)", field.getPrefix(), field.getLocalName(), field.getFieldNameString()));
                }
            }
            else {
                if (field.europeana().converterMultipleOutput()) {
                    fieldMapping.addCodeLine(String.format("for (part in %s(it)) {", field.europeana().converter()));
                    fieldMapping.addCodeLine(String.format("%s.%s part", field.getPrefix(), field.getLocalName()));
                    fieldMapping.addCodeLine("}");
                }
                else {
                    fieldMapping.addCodeLine(String.format("%s.%s %s(it)[0]", field.getPrefix(), field.getLocalName(), field.europeana().converter()));
                }
            }
            fieldMapping.addCodeLine("}");
        }
        else {
            if (field.europeana().converter().isEmpty()) {
                if (!field.europeana().valueMapped()) {
                    fieldMapping.addCodeLine(String.format("%s.%s %s[0]", field.getPrefix(), field.getLocalName(), node.getVariableName()));
                }
                else {
                    fieldMapping.createValueMap(getActualValues(node), field.getEnumValues());
                    fieldMapping.addCodeLine(String.format("%s.%s %s(%s[0])", field.getPrefix(), field.getLocalName(), node.getVariableName(), field.getFieldNameString()));
                }
            }
            else {
                if (field.europeana().converterMultipleOutput()) {
                    fieldMapping.addCodeLine(String.format("for (part in %s(%s[0])) {", field.europeana().converter(), node.getVariableName()));
                    fieldMapping.addCodeLine(String.format("%s.%s part", field.getPrefix(), field.getLocalName()));
                    fieldMapping.addCodeLine("}");
                }
                else {
                    fieldMapping.addCodeLine(String.format("%s.%s %s(%s[0])[0]", field.getPrefix(), field.getLocalName(), field.europeana().converter(), node.getVariableName()));
                }
            }
        }
    }

    private Set<String> getActualValues(AnalysisTree.Node node) {
        Set<String> values = new TreeSet<String>();
        for (Statistics.Counter counter : node.getStatistics().getCounters()) {
            values.add(counter.getValue());
        }
        return values;
    }
}
