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

package eu.europeana.sip.core;

import eu.delving.core.metadata.FieldDefinition;
import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.Path;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Validate a record
 * <p/>
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordValidator {
    private Map<String, FieldDefinition> fieldMap;
    private Map<String, String> constantMap;
    private Set<String> unique;

    public RecordValidator(MetadataModel metadataModel, boolean checkUniqueness) {
        if (metadataModel.getRecordDefinition().root.elements != null) {
            throw new RuntimeException("Hierarchical model not yet supported");
        }
        if (checkUniqueness) {
            unique = new HashSet<String>();
            constantMap = new HashMap<String, String>();
        }
        fieldMap = metadataModel.getRecordDefinition().getMappableFields();
    }

    public void validate(MetadataRecord metadataRecord, List<FieldEntry> fieldEntries) throws RecordValidationException {
        List<String> problems = new ArrayList<String>();
        validateAgainstAnnotations(fieldEntries, problems);
        if (!problems.isEmpty()) {
            throw new RecordValidationException(metadataRecord, problems);
        }
    }

    private void validateAgainstAnnotations(List<FieldEntry> fieldEntries, List<String> problems) {
        Map<Path, Counter> counterMap = new HashMap<Path, Counter>();
        for (FieldEntry fieldEntry : fieldEntries) {
            FieldDefinition field = fieldMap.get(fieldEntry.getTag());
            if (field == null) {
                problems.add(String.format("Unknown XML element [%s]", fieldEntry.getTag()));
            }
            else {
                Counter counter = counterMap.get(fieldEntry.getPath());
                if (counter == null) {
                    counter = new Counter();
                    counterMap.put(fieldEntry.getPath(), counter);
                }
                counter.count++;
                if (field.options != null && !field.valueMapped && !field.options.contains(fieldEntry.getValue())) {
                    StringBuilder enumString = new StringBuilder();
                    Iterator<String> walk = field.options.iterator();
                    while (walk.hasNext()) {
                        enumString.append(walk.next());
                        if (walk.hasNext()) {
                            enumString.append(',');
                        }
                    }
                    problems.add(String.format("Value for [%s] was [%s] which does not belong to [%s]", fieldEntry.getTag(), fieldEntry.getValue(), enumString.toString()));
                }
                if (field.constant && constantMap != null) {
                    String value = constantMap.get(fieldEntry.getTag());
                    if (value == null) {
                        constantMap.put(fieldEntry.getTag(), fieldEntry.getValue());
                    }
                    else if (!value.equals(fieldEntry.getValue())) {
                        problems.add(String.format("Value for [%s] should be constant but it had multiple values [%s] and [%s]", fieldEntry.getTag(), fieldEntry.getValue(), value));
                    }
                }
                String regex = field.regularExpression;
                if (regex != null) {
                    if (!fieldEntry.getValue().matches(regex)) {
                        problems.add(String.format("Value for [%s] was [%s] which does not match regular expression [%s]", fieldEntry.getTag(), fieldEntry.getValue(), regex));
                    }
                }
                if (field.url) {
                    try {
                        new URL(fieldEntry.getValue());
                    }
                    catch (MalformedURLException e) {
                        problems.add(String.format("URL value for [%s] was [%s] which is malformed", fieldEntry.getTag(), fieldEntry.getValue()));
                    }
                }
                if (field.id && unique != null) {
                    if (unique.contains(fieldEntry.getValue())) {
                        problems.add(String.format("Identifier [%s] must be unique but the value [%s] appears more than once", fieldEntry.getTag(), fieldEntry.getValue()));
                    }
                    unique.add(fieldEntry.getValue());
                }
            }
        }
        Map<String, Boolean> present = new TreeMap<String, Boolean>();
        for (FieldDefinition field : fieldMap.values()) {
            if (field.requiredGroup != null) {
                present.put(field.requiredGroup, false);
            }
        }
        for (FieldEntry fieldEntry : fieldEntries) {
            FieldDefinition field = fieldMap.get(fieldEntry.getTag());
            if (field != null && field.requiredGroup != null) {
                present.put(field.requiredGroup, true);
            }
        }
        for (Map.Entry<String, Boolean> entry : present.entrySet()) {
            if (!entry.getValue()) {
                problems.add(String.format("Required field violation for [%s]", entry.getKey()));
            }
        }
        for (FieldDefinition field : fieldMap.values()) {
            Counter counter = counterMap.get(field.path);
            if (counter != null && !field.multivalued && counter.count > 1) {
                problems.add(String.format("Single-valued field [%s] had %d values", field.tag, counter.count));
            }
        }
    }

    private static class Counter {
        int count;
    }
}