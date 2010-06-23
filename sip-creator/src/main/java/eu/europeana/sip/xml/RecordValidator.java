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

package eu.europeana.sip.xml;

import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.definitions.annotations.EuropeanaField;
import eu.europeana.definitions.annotations.FieldCategory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate a record
 * <p/>
 * todo: move this class to the definitions module
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordValidator {
    private static Pattern PATTERN = Pattern.compile("<([^>]*)>([^<]*)<[^>]*>");
    private Map<String, EuropeanaField> fieldMap = new HashMap<String, EuropeanaField>();
    private Map<String, String> constantMap;
    private Set<String> unique;

    public RecordValidator(AnnotationProcessor annotationProcessor, boolean checkUniqueness) {
        if (checkUniqueness) {
            unique = new HashSet<String>();
            constantMap = new HashMap<String, String>();
        }
        for (EuropeanaField field : annotationProcessor.getAllFields()) {
            if (field.europeana().category() != FieldCategory.INDEX_TIME_ADDITION) {
                fieldMap.put(field.getXmlName(), field);
            }
        }
    }

    public String validate(MetadataRecord metadataRecord, String recordString) throws RecordValidationException {
        List<Entry> entries = createNonemptyEntryList(recordString);
        Collections.sort(entries);
        eliminateDuplicates(entries);
        List<String> problems = new ArrayList<String>();
        validateAgainstAnnotations(entries, problems);
        if (!problems.isEmpty()) {
            throw new RecordValidationException(metadataRecord, problems);
        }
        return toString(entries);
    }

    private void validateAgainstAnnotations(List<Entry> entries, List<String> problems) {
        Map<String, Counter> counterMap = new HashMap<String, Counter>();
        for (Entry entry : entries) {
            EuropeanaField field = fieldMap.get(entry.tag);
            if (field == null) {
                problems.add(String.format("Unknown XML element [%s]", entry.tag));
            }
            else {
                Counter counter = counterMap.get(entry.tag);
                if (counter == null) {
                    counter = new Counter();
                    counterMap.put(entry.tag, counter);
                }
                counter.count++;
                Set<String> enumValues = field.getEnumValues();
                if (enumValues != null && !enumValues.contains(entry.value)) {
                    StringBuilder enumString = new StringBuilder();
                    Iterator<String> walk = enumValues.iterator();
                    while (walk.hasNext()) {
                        enumString.append(walk.next());
                        if (walk.hasNext()) {
                            enumString.append(',');
                        }
                    }
                    problems.add(String.format("Value for [%s] was [%s] which does not belong to [%s]", entry.tag, entry.value, enumString.toString()));
                }
                if (field.europeana().constant() && constantMap != null) {
                    String value = constantMap.get(entry.tag);
                    if (value == null) {
                        constantMap.put(entry.tag, entry.value);
                    }
                    else if (!value.equals(entry.value)) {
                        problems.add(String.format("Value for [%s] should be constant but it had multiple values [%s] and [%s]", entry.tag, entry.value, value));
                    }
                }
                String regex = field.europeana().regularExpression();
                if (!regex.isEmpty()) {
                    if (!entry.value.matches(regex)) {
                        problems.add(String.format("Value for [%s] was [%s] which does not match regular expression [%s]", entry.tag, entry.value, regex));
                    }
                }
                if (field.europeana().url()) {
                    try {
                        new URL(entry.value);
                    }
                    catch (MalformedURLException e) {
                        problems.add(String.format("URL value for [%s] was [%s] which is malformed", entry.tag, entry.value));
                    }
                }
                if (field.europeana().id() && unique != null) {
                    if (unique.contains(entry.value)) {
                        problems.add(String.format("Identifier [%s] must be unique but the value [%s] appears more than once", entry.tag, entry.value));
                    }
                    unique.add(entry.value);
                }
            }
        }
        Map<String, Boolean> present = new TreeMap<String, Boolean>();
        for (EuropeanaField field : fieldMap.values()) {
            if (!field.europeana().requiredGroup().isEmpty()) {
                present.put(field.europeana().requiredGroup(), false);
            }
        }
        for (Entry entry : entries) {
            EuropeanaField field = fieldMap.get(entry.tag);
            if (field != null && !field.europeana().requiredGroup().isEmpty()) {
                present.put(field.europeana().requiredGroup(), true);
            }
        }
        for (Map.Entry<String,Boolean> entry : present.entrySet()) {
            if (!entry.getValue()) {
                problems.add(String.format("Required field violation for [%s]", entry.getKey()));
            }
        }
        for (EuropeanaField field : fieldMap.values()) {
            Counter counter = counterMap.get(field.getXmlName());
            if (counter != null && !field.solr().multivalued() && counter.count > 1) {
                problems.add(String.format("Single-valued field [%s] had %d values", field.getXmlName(), counter.count));
            }
        }
    }

    private String toString(List<Entry> entries) {
        StringBuilder out = new StringBuilder();
        out.append("<record>\n");
        for (Entry entry : entries) {
            out.append("   ").append(entry).append('\n');
        }
        out.append("</record>\n");
        return out.toString();
    }

    private void eliminateDuplicates(List<Entry> entries) {
        Iterator<Entry> entryWalk = entries.iterator();
        Entry previousEntry = null;
        while (entryWalk.hasNext()) {
            if (previousEntry == null) {
                previousEntry = entryWalk.next();
            }
            else {
                Entry next = entryWalk.next();
                if (previousEntry.equals(next)) {
                    entryWalk.remove();
                }
                else {
                    previousEntry = next;
                }
            }
        }
    }

    private List<Entry> createNonemptyEntryList(String recordString) {
        List<Entry> entries = new ArrayList<Entry>();
        for (String line : recordString.split("\n")) {
            line = line.trim();
            if ("<record>".equals(line) || "</record>".equals(line)) {
                continue;
            }
            if (line.endsWith("/>")) { // empty tag
                continue;
            }
            Matcher matcher = PATTERN.matcher(line);
            if (matcher.matches()) {
                String tag = matcher.group(1);
                String value = matcher.group(2).trim();
                if (!value.isEmpty()) {
                    entries.add(new Entry(tag, value));
                }
            }
            else {
                throw new RuntimeException("Mismatch!");
            }
        }
        return entries;
    }

    private static class Counter {
        int count;
    }

    private static class Entry implements Comparable<Entry> {
        private String tag;
        private String value;

        private Entry(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        @Override
        public int compareTo(Entry entry) {
            return tag.compareTo(entry.tag);
        }

        public String toString() {
            return "<" + tag + ">" + value + "</" + tag + ">";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return !(tag != null ? !tag.equals(entry.tag) : entry.tag != null) && !(value != null ? !value.equals(entry.value) : entry.value != null);
        }
    }
}