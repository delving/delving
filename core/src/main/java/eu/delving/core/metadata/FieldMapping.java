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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map one or more fields to one or more destinatiopn fields using a conversion and the resulting
 * code snippet.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldMapping implements Iterable<String> {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("input(\\.\\w+)+");
    private static final String [] TO_REMOVE = {
            ".each",
            ".split"
    };
    private FieldDefinition fieldDefinition;
    private FieldValueMap valueMap;
    private List<String> codeLines = new ArrayList<String>();
    private List<String> variables;

    public FieldMapping(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    public FieldMapping(FieldDefinition fieldDefinition, FieldValueMap valueMap) {
        this.fieldDefinition = fieldDefinition;
        this.valueMap = valueMap;
    }

    public boolean codeLooksLike(String code) {
        Iterator<String> walk = codeLines.iterator();
        for (String line : code.split("\n")) {
            line = line.trim();
            if (!line.isEmpty()) {
                if (!walk.hasNext()) {
                    return false;
                }
                String codeLine = walk.next();
                if (!codeLine.equals(line)) {
                    return false;
                }
            }
        }
        return !walk.hasNext();
    }

    public void setCode(String code) {
        codeLines.clear();
        variables = null;
        if (code != null) {
            for (String line : code.split("\n")) {
                line = line.trim();
                if (!line.isEmpty()) {
                    codeLines.add(line);
                }
            }
        }
    }

    public FieldDefinition getField() {
        return fieldDefinition;
    }

    public List<String> getVariables() {
        if (variables == null) {
            variables = new ArrayList<String>();
            for (String line : codeLines) {
                Matcher matcher = VARIABLE_PATTERN.matcher(line);
                while (matcher.find()) {
                    String var = matcher.group(0);
                    for (String toRemove : TO_REMOVE) {
                        if (var.endsWith(toRemove)) {
                            var = var.substring(0, var.length() - toRemove.length());
                            break;        
                        }
                    }
                    variables.add(var);
                }
            }
        }
        return variables;
    }


    public void createValueMap(Set<String> domainValues, Set<String> rangeValues) {
        this.valueMap = new FieldValueMap(fieldDefinition.getFieldNameString(), rangeValues);
        this.valueMap.setDomain(domainValues);
    }

    public FieldValueMap getValueMap() {
        return valueMap;
    }

    public void addCodeLine(String line) {
        codeLines.add(line);
        variables = null;
    }

    @Override
    public Iterator<String> iterator() {
        return codeLines.iterator();
    }

    public boolean isEmpty() {
        return codeLines.isEmpty();
    }

    public String getDescription() {
        String fieldName = fieldDefinition == null ? "?" : fieldDefinition.getFieldNameString();
        if (getVariables().isEmpty()) {
            return fieldName;
        }
        else {
            return fieldName + " from " + getVariables();
        }
    }

    public String toString() {
        return fieldDefinition == null ? "?" : fieldDefinition.getFieldNameString();
    }
}
