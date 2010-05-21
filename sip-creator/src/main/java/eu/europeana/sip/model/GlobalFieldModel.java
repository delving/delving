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

package eu.europeana.sip.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * Hold a collection of global fields that can be used here and there.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GlobalFieldModel {
    private static final String PREFIX = "// GlobalField ";
    private Map<GlobalField, String> map = new TreeMap<GlobalField,String>();

    public static GlobalFieldModel fromMapping(String mapping) {
        GlobalFieldModel model = new GlobalFieldModel();
        for (String line : mapping.split("\n")) {
            model.fromLine(line);
        }
        return model;
    }

    public boolean fromLine(String line) {
        if (line.startsWith(PREFIX)) {
            line = line.substring(PREFIX.length());
            int space = line.indexOf(" ");
            if (space > 0) {
                String globalFieldString = line.substring(0, space);
                String value = line.substring(space).trim();
                set(GlobalField.valueOf(globalFieldString), value);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void set(GlobalField globalField, String value) {
        map.put(globalField, value);
    }

    public String get(GlobalField globalField) {
        String value = map.get(globalField);
        if (value != null) {
            return value;
        }
        else {
            return "";
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<GlobalField,String> entry : map.entrySet()) {
            out.append(PREFIX).append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
        }
        return out.toString();
    }
}
