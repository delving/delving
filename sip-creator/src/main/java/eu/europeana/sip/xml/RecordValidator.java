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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate a record
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordValidator {
    private static Pattern PATTERN = Pattern.compile("<([^>]*)>([^<]*)<[^>]*>");
    private AnnotationProcessor annotationProcessor;
    private Map<String, EuropeanaField> fieldMap = new HashMap<String, EuropeanaField>();
    private Map<FieldCategory, List<String>> categoryFieldMap = new HashMap<FieldCategory, List<String>>();
    private Set<String> unique;

    public RecordValidator(AnnotationProcessor annotationProcessor, boolean checkUniqueness) {
        this.annotationProcessor = annotationProcessor;
        if (checkUniqueness) {
             unique = new HashSet<String>();
        }
        for (FieldCategory category : FieldCategory.values()) {
            categoryFieldMap.put(category, new ArrayList<String>());
        }
        for (EuropeanaField field : annotationProcessor.getSolrFields()) {
            fieldMap.put(field.getPrefixedName(), field);
            categoryFieldMap.get(field.getCategory()).add(field.getPrefixedName());
        }
    }

    public String validate(String recordString) throws RecordValidationException {
        List<Entry> entries = createNonemptyEntryList(recordString);
        Collections.sort(entries);
        eliminateDuplicates(entries);
        List<String> problems = new ArrayList<String>();
        validateAgainstAnnotations(entries, problems);
        if (!problems.isEmpty()) {
            throw new RecordValidationException(problems);
        }
        return toString(entries);
    }

    private void validateAgainstAnnotations(List<Entry> entries, List<String> problems) {
        Map<String, Counter> counterMap = new HashMap<String, Counter>();
        for (Entry entry : entries) {
            EuropeanaField field = fieldMap.get(entry.tag);
            if (field == null) {
                problems.add("Unknown tag: "+entry.tag);
            }
            else {
                Counter counter = counterMap.get(entry.tag);
                if (counter == null) {
                    counter = new Counter();
                    counterMap.put(entry.tag, counter);
                }
                counter.count++;
            }
        }
        for (String requiredName : categoryFieldMap.get(FieldCategory.ESE_REQUIRED)) {
            Counter counter = counterMap.get(requiredName);
            if (counter == null || counter.count == 0) {
                problems.add("Required: "+requiredName);
            }
            else if (counter.count > 1) {
                problems.add("Too many: "+requiredName);
            }
        }
    }

//                switch (field.getCategory()) {
//                    case ESE_OPTIONAL:
//                        break;
//                    case ESE_REQUIRED:
//                        break;
//                    case ESE_PLUS_OPTIONAL:
//                        break;
//                    case ESE_PLUS_REQUIRED:
//                        break;
//                    case COPY_FIELD:
//                    case INDEX_TIME_FIELD:
//                        problems.add("Not allowed: "+entry.tag);
//                        break;
//                }

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