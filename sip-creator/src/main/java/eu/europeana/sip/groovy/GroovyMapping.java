package eu.europeana.sip.groovy;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores and retrieves snippets in a file separated by a delimiter. File structure looks like this
 * <p/>
 * MetadataFile.xml.mapping
 * europeana.record {
 * <p/>
 * // === MAPPING(dc_date) ===
 * for($d in input.dc_date) {
 * date $d;
 * }
 * <p/>
 * // === MAPPING(dc_type) ===
 * for($t in input.dc_type) {
 * type $t;
 * }
 * <p/>
 * }
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyMapping {

    private final static Logger LOG = Logger.getLogger(GroovyMapping.class);
    private final static String FILE_HEADER = "// *** Europeana Mapping File ***%noutput.record {%n%n";
    private final static String FILE_FOOTER = "} // *** EOF ***%n";

    private Map<Delimiter, String> snippets = new TreeMap<Delimiter, String>();


    /**
     * Separate the records with this delimiter.
     */
    public static class Delimiter implements Serializable, Comparable<Delimiter> {

        private final static String DELIMITER = "// === MAPPING(%s) ===";
        private String id;

        public Delimiter(String id) {
            this.id = String.format(DELIMITER, id);
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return id.equals(obj);
        }

        @Override
        public int compareTo(Delimiter o) {
            return o.toString().compareTo(id);
        }
    }



    // todo: not working properly; regenerating groovyLoops from scratch based on delimiter
    // todo: also loading entire file to buffer

//    private void load() throws IOException {
//        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
//        byte[] buffer = new byte[(int) randomAccessFile.length()];
//        randomAccessFile.readFully(buffer);
//        Pattern pattern = Pattern.compile("(//\\s===\\sMAPPING\\((.+)\\)\\s===)");
//        Matcher matcher = pattern.matcher(new String(buffer));
//        while (matcher.find()) {
//            snippets.put(new Delimiter(matcher.group(2)), GroovyService.generateGroovyLoop(matcher.group(2)));
//        }
//        LOG.info(String.format("Loaded %d snippets%n", snippets.size()));
//    }

    public String findNode(Delimiter delimiter) throws IOException {
        LOG.info(String.format("Find > %s%n", delimiter));
        return snippets.get(delimiter);
    }

    /**
     * Write the delimiter first, then write the groovy snippet.
     * Replace the snippet if it exists.
     */
    public Delimiter storeNode(Delimiter delimiter, String node) throws IOException {
        if (snippets.containsKey(delimiter)) {
            LOG.info(String.format("Overwriting %s%n", delimiter));
            snippets.remove(delimiter);
        }
        LOG.info(String.format("Storing %s%n", delimiter));
        snippets.put(delimiter, node);
        return delimiter;
    }


    public boolean deleteNode(Delimiter delimiter) throws IOException {
        LOG.info(String.format("Removing %s%n", delimiter));
        return null != snippets.remove(delimiter);
    }

    public StringBuffer createMapping() throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format(FILE_HEADER));
        for (Map.Entry entries : snippets.entrySet()) {
            buffer.append(String.format("\t%s%n%s%n", entries.getKey(), entries.getValue()));
        }
        buffer.append(String.format(FILE_FOOTER));
        return buffer;
    }
}
