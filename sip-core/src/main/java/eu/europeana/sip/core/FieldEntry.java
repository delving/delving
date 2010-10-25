package eu.europeana.sip.core;

import eu.delving.core.metadata.Path;

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
    private Path path;
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
                String tagString = matcher.group(1);
                String value = matcher.group(2).trim();
                if (!value.isEmpty()) {
                    fieldEntries.add(new FieldEntry(new Path(tagString), value));
                }
            }
            else {
                // todo: note that this will fail if the record is hierarchical
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

    public FieldEntry(Path path, String value) {
        this.path = path;
        this.value = value;
    }

    public Path getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public String getTag() {
        if (path.size() != 1) {
            throw new RuntimeException("Hierarchical model not yet supported");
        }
        return path.getTag(0).toString();
    }

    @Override
    public int compareTo(FieldEntry fieldEntry) {
        return path.compareTo(fieldEntry.path);
    }

    public String toString() {
        return "<" + getTag() + ">" + value + "</" + getTag() + ">";
    }
}
