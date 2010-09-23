package eu.europeana.sip.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A field entry of a record is a tag with a value.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldEntry implements Comparable<FieldEntry> {
    private static Pattern PATTERN = Pattern.compile("<([^>]*)>([^<]*)<[^>]*>");
    private String tag;
    private String value;

    /**
     * Create a list of field entries from a record in string form.  The result will be unique
     * and
     *
     * @param recordString the string containing the whole record, lines separated by \n
     * @return a list of unique nonempty entries
     */

    public static List<FieldEntry> createList(String recordString) {
        List<FieldEntry> fieldEntries = new ArrayList<FieldEntry>();
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
                    fieldEntries.add(new FieldEntry(tag, value));
                }
            }
            else {
                throw new RuntimeException(String.format("Line [%s] does not look like a record field", line));
            }
        }
        Collections.sort(fieldEntries);
        Iterator<FieldEntry> entryWalk = fieldEntries.iterator();
        FieldEntry previousFieldEntry = null;
        while (entryWalk.hasNext()) {
            if (previousFieldEntry == null) {
                previousFieldEntry = entryWalk.next();
            }
            else {
                FieldEntry next = entryWalk.next();
                if (previousFieldEntry.equals(next)) {
                    entryWalk.remove();
                }
                else {
                    previousFieldEntry = next;
                }
            }
        }
        return fieldEntries;
    }

    public static String toString(List<FieldEntry> fieldEntries, boolean wrappedInRecord) {
        StringBuilder out = new StringBuilder();
        if (wrappedInRecord) {
            out.append("<record>\n");
        }
        for (FieldEntry fieldEntry : fieldEntries) {
            if (wrappedInRecord) {
                out.append("   ");
            }
            out.append(fieldEntry).append('\n');
        }
        if (wrappedInRecord) {
            out.append("</record>\n");
        }
        return out.toString();
    }

    public FieldEntry(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(FieldEntry fieldEntry) {
        return tag.compareTo(fieldEntry.tag);
    }

    public String toString() {
        return "<" + tag + ">" + value + "</" + tag + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldEntry fieldEntry = (FieldEntry) o;
        return !(tag != null ? !tag.equals(fieldEntry.tag) : fieldEntry.tag != null) && !(value != null ? !value.equals(fieldEntry.value) : fieldEntry.value != null);
    }


}
