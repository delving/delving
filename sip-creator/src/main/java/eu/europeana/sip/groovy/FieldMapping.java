/*
 * Copyright 2007 EDL FOUNDATION
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

package eu.europeana.sip.groovy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map one or more fields to one or more destinatiopn fields using a conversion and the resulting
 * code snippet.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldMapping {
    private static final Pattern FULL_PATTERN = Pattern.compile("^\\[([^]]+)\\] ==> \\[([^}]+)\\]$");
    private List<String> inputVariables = new ArrayList<String>();
    private List<String> outputFields = new ArrayList<String>();
    private List<String> codeLines = new ArrayList<String>();

    public FieldMapping() {
    }

    public FieldMapping(String string) {
        Matcher matcher = FULL_PATTERN.matcher(string);
        if (!matcher.find()) {
            throw new RuntimeException("Unable to interpret field mapping: "+string);
        }
        String from = matcher.group(1);
        String to = matcher.group(2);
        inputVariables.addAll(Arrays.asList(from.split(",")));
        outputFields.addAll(Arrays.asList(to.split(",")));
    }

    public void addFromVariable(String fromVariable) {
        inputVariables.add(fromVariable);
    }

    public void addToField(String toField) {
        outputFields.add(toField);
    }

    public void addCodeLine(String codeLine) {
        codeLines.add(codeLine);
    }

    public List<String> getInputVariables() {
        return inputVariables;
    }

    public List<String> getOutputFields() {
        return outputFields;
    }

    public List<String> getCodeLines() {
        return codeLines;
    }

    public String getCodeForDisplay() {
        StringBuilder out = new StringBuilder();
        int indent = 0;
        for (String codeLine : codeLines) {
            if (codeLine.endsWith("}")) {
                indent--;
            }
            for (int walk = 0; walk<indent; walk++) {
                out.append("   ");
            }
            out.append(codeLine).append('\n');
            if (codeLine.endsWith("{")) {
                indent++;
            }
        }
        return out.toString();
    }

    public String toString() {
        StringBuilder out = new StringBuilder("[");
        Iterator<String> walk = inputVariables.iterator();
        while (walk.hasNext()) {
            out.append(walk.next());
            if (walk.hasNext()) {
                out.append(",");
            }
        }
        out.append("] ==> [");
        walk = outputFields.iterator();
        while (walk.hasNext()) {
            out.append(walk.next());
            if (walk.hasNext()) {
                out.append(",");
            }
        }
        out.append("]");
        return out.toString();
    }

}
