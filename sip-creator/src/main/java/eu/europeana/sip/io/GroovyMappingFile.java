package eu.europeana.sip.io;

import java.io.IOException;
import java.io.Serializable;

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
public interface GroovyMappingFile {

    /**
     * Separate the records with this delimiter.
     */
    class Delimiter implements Serializable {

        private final static String DELIMITER = "// === MAPPING(%s) ===";
        private String id;

        public Delimiter(String id) {
            this.id = String.format(DELIMITER, id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    /**
     * Finds the node with the given delimiter
     *
     * @param delimiter search for the record with this delimiter
     * @return the found node
     * @throws java.io.IOException error while reading from file
     */
    String findNode(Delimiter delimiter) throws IOException;

    /**
     * Stores the node and generates a delimiter which will be returned
     *
     * @param delimiter the delimiter
     * @param node      the node that will be persisted
     * @return the generated delimiter
     * @throws java.io.IOException error while writing to file
     */
    Delimiter storeNode(Delimiter delimiter, String node) throws IOException;

    /**
     * Delete a node from the file
     *
     * @param delimiter look for the node with this delimiter
     * @return deleted?
     * @throws IOException error while deleting from file
     */
    boolean deleteNode(Delimiter delimiter) throws IOException;
}
