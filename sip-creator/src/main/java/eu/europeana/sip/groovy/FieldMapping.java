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

import eu.europeana.definitions.annotations.EuropeanaField;

import java.util.ArrayList;
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
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("input(\\.\\w+)+");
    private EuropeanaField europeanaField;
    private List<String> codeLines = new ArrayList<String>();

    public FieldMapping(EuropeanaField europeanaField) {
        this.europeanaField = europeanaField;
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
        if (code != null) {
            for (String line : code.split("\n")) {
                line = line.trim();
                if (!line.isEmpty()) {
                    codeLines.add(line);
                }
            }
        }
    }

    public EuropeanaField getEuropeanaField() {
        return europeanaField;
    }

    public List<String> getVariables() {
        List<String> vars = new ArrayList<String>();
        for (String line : codeLines) {
            Matcher matcher = VARIABLE_PATTERN.matcher(line);
            while (matcher.find()) {
                String var = matcher.group(0);
                vars.add(var);
            }
        }
        return vars;
    }

    public List<String> getCodeLines() {
        return codeLines;
    }

    public String toString() {
        if (europeanaField != null) {
            return europeanaField.getFieldNameString();
        }
        else {
            return "?";
        }
    }

}
