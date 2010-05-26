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

import eu.europeana.definitions.annotations.AnnotationProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
    }

    public String validate(String recordString) {
        List<Entry> entries = createNonemptyEntryList(recordString);
        Collections.sort(entries);
        eliminateDuplicates(entries);
        validateAgainstAnnotations(entries);
        return toString(entries);
    }

    private void validateAgainstAnnotations(List<Entry> entries) {
        // todo: use the annotations to validate the entries
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

    public class ValidationException extends java.lang.Exception {
        
    }
}