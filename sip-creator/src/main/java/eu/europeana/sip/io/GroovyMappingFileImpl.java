package eu.europeana.sip.io;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of GroovyMappingFile // todo: retrieve from FileSet
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyMappingFileImpl implements GroovyMappingFile {

    private final static Logger LOG = Logger.getLogger(GroovyMappingFileImpl.class);
    private final static String NEXT_NODE_IDENTIFIER = "// ===";
    private final static String FILE_HEADER = "// *** Europeana Mapping File ***%neuropeana.record {%n%n";
    private final static String FILE_FOOTER = "} // *** EOF ***%n";

    private final File file;

    private LineNumberReader lineNumberReader;
    private Map<Delimiter, String> snippets = new TreeMap<Delimiter, String>();

    public GroovyMappingFileImpl(File file) {
        this.file = file;
    }

    private String load(Delimiter delimiter) throws IOException {
        StringBuffer node = new StringBuffer();
        lineNumberReader = new LineNumberReader(new FileReader(file));
        String line;
        while (null != (line = lineNumberReader.readLine())) {
            if (line.contains(delimiter.toString())) {
                node.append(String.format("%s%n", line));
                while (null != (line = lineNumberReader.readLine())) {
                    if (line.contains(NEXT_NODE_IDENTIFIER)) {
                        lineNumberReader.close();
                        return node.toString();
                    }
                    node.append(String.format("%s%n", line));
                }
                return node.toString();
            }
        }
        lineNumberReader.close();
        return line;
    }

    @Override
    public String findNode(Delimiter delimiter) throws IOException {
        System.out.printf("Find > %s%n", delimiter);
        return snippets.get(delimiter);
    }

    /**
     * Write the delimiter first, then write the groovy snippet.
     * Replace the snippet if it exists.
     */
    @Override
    public Delimiter storeNode(Delimiter delimiter, String node) throws IOException {
        if (snippets.containsKey(delimiter)) {
            snippets.remove(delimiter);
        }
        snippets.put(delimiter, node);
        System.out.printf("Stored in snippets<> %d%n", snippets.size());
        for (Map.Entry entry : snippets.entrySet()) {
            System.out.printf("%s:%s%n", entry.getKey(), entry.getValue());
        }
        return delimiter;
    }


    @Override
    public boolean deleteNode(Delimiter delimiter) throws IOException {
        System.out.printf("Removing %s%n", delimiter);
        return null != snippets.remove(delimiter);
    }

    @Override
    public void persist() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(String.format(FILE_HEADER).getBytes());
        for (Map.Entry entries : snippets.entrySet()) {
            fileOutputStream.write(String.format("\t%s%n%s%n", entries.getKey(), entries.getValue()).getBytes());
        }
        fileOutputStream.write(String.format(FILE_FOOTER).getBytes());
        fileOutputStream.close();
    }
}
