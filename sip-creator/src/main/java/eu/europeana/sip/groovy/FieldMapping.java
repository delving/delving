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
    private static final Pattern REGEX = Pattern.compile("^([^{]+)\\{([^}]+)\\}\\{([^}]+)\\}$");
    private Conversion conversion;
    private List<String> fromVariables = new ArrayList<String>();
    private List<String> toFields = new ArrayList<String>();
    private List<String> codeLines = new ArrayList<String>();

    public FieldMapping(String string) {
        Matcher matcher = REGEX.matcher(string);
        if (!matcher.find()) {
            throw new RuntimeException("No match for ["+string+"]");
        }
        conversion = Conversion.valueOf(matcher.group(1));
        String from = matcher.group(2);
        String to = matcher.group(3);
        fromVariables.addAll(Arrays.asList(from.split(",")));
        toFields.addAll(Arrays.asList(to.split(",")));
    }

    public FieldMapping(Conversion conversion) {
        this.conversion = conversion;
    }

    public void addFromVariable(String fromVariable) {
        fromVariables.add(fromVariable);
    }

    public void addToField(String toField) {
        toFields.add(toField);
    }

    public void addCodeLine(String codeLine) {
        codeLines.add(codeLine);
    }

    public Conversion getConversion() {
        return conversion;
    }

    public List<String> getFromVariables() {
        return fromVariables;
    }

    public List<String> getToFields() {
        return toFields;
    }

    public List<String> getCodeLines() {
        return codeLines;
    }

    public String toString() {
        StringBuilder out = new StringBuilder(conversion+"{");
        Iterator<String> walk = fromVariables.iterator();
        while (walk.hasNext()) {
            out.append(walk.next());
            if (walk.hasNext()) {
                out.append(",");
            }
        }
        out.append("}{");
        walk = toFields.iterator();
        while (walk.hasNext()) {
            out.append(walk.next());
            if (walk.hasNext()) {
                out.append(",");
            }
        }
        out.append("}");
        return out.toString();
    }
}
