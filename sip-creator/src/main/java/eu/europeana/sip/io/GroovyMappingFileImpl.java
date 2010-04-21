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

    private Map<Delimiter, String> snippets = new TreeMap<Delimiter, String>();

    public GroovyMappingFileImpl(File file) {
        this.file = file;
        if (file.exists()) {
            try {
                load();
            }
            catch (IOException e) {
                e.printStackTrace();  // todo: handle catch
            }
        }
    }

    private void load() throws IOException { // todo: broken, skips delimiters
        LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
        String line;
        String delimiter;
        StringBuffer snippet = new StringBuffer();
        while (null != (line = lineNumberReader.readLine())) {
            if (line.contains(NEXT_NODE_IDENTIFIER)) {
                LOG.info(String.format("Found ID %s%n", line));
                //snippet.append(String.format("%s%n", line));
                delimiter = line;
                while (null != (line = lineNumberReader.readLine())) {
                    if (line.contains(NEXT_NODE_IDENTIFIER) || line.contains("EOF")) {
                        LOG.info(String.format("Next node or EOF found, break%n"));
                        break;
                    }
                    LOG.info(String.format("Appending to snippet %s%n", snippet));
                    LOG.info(String.format("Line : %s%n", line));
                    snippet.append(String.format("%s%n", line));
                }
                snippets.put(new Delimiter(delimiter), snippet.toString());
            }
        }
        lineNumberReader.close();
        LOG.info(String.format("Loaded %d snippets%n", snippets.size()));
    }

    @Override
    public String findNode(Delimiter delimiter) throws IOException {
        LOG.info(String.format("Find > %s%n", delimiter));
        return snippets.get(delimiter);
    }

    /**
     * Write the delimiter first, then write the groovy snippet.
     * Replace the snippet if it exists.
     */
    @Override
    public Delimiter storeNode(Delimiter delimiter, String node) throws IOException {
        if (snippets.containsKey(delimiter)) {
            LOG.info(String.format("Overwriting %s%n", delimiter));
            snippets.remove(delimiter);
        }
        LOG.info(String.format("Storing %s%n", delimiter));
        snippets.put(delimiter, node);
        return delimiter;
    }


    @Override
    public boolean deleteNode(Delimiter delimiter) throws IOException {
        LOG.info(String.format("Removing %s%n", delimiter));
        return null != snippets.remove(delimiter);
    }

    @Override
    public void persist() throws IOException {
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(String.format(FILE_HEADER).getBytes());
        for (Map.Entry entries : snippets.entrySet()) {
            fileOutputStream.write(String.format("\t%s%n%s%n", entries.getKey(), entries.getValue()).getBytes());
        }
        fileOutputStream.write(String.format(FILE_FOOTER).getBytes());
        fileOutputStream.close();
        LOG.info(String.format("Saved successfully to %s%n", file.getAbsoluteFile()));
    }
}
