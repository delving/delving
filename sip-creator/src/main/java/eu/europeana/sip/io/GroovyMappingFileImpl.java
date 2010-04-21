package eu.europeana.sip.io;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Implementation of GroovyMappingFile // todo: retrieve from FileSet
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyMappingFileImpl implements GroovyMappingFile {

    private final static Logger LOG = Logger.getLogger(GroovyMappingFileImpl.class);
    private final static String TEMP_EXTENSION = ".temp.xml";
    private final static String NEXT_NODE_IDENTIFIER = "// ===";
    private final static String FILE_HEADER = "// *** Europeana Mapping File ***%neuropeana.record {%n%n";
    private final static String FILE_FOOTER = "%n} // *** EOF ***";

    private final File file;

    private FileOutputStream fileOutputStream;
    private LineNumberReader lineNumberReader;

    public GroovyMappingFileImpl(File file) {
        this.file = file;
    }

    @Override
    public String findNode(Delimiter delimiter) throws IOException {
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

    /**
     * Write the delimiter first, then write the groovy snippet.
     * Replace the snippet if it exists.
     */
    @Override
    public Delimiter storeNode(Delimiter delimiter, String node) throws IOException {
        if (!file.exists()) {
            createFile(file);
        }
        if (null != findNode(delimiter)) {
            deleteNode(delimiter);
            LOG.info(String.format("Snippet for delimiter %s already exists, removing it", delimiter));
        }
        if (null == fileOutputStream) {
            fileOutputStream = new FileOutputStream(file, true);
        }
        fileOutputStream.write(String.format("\t%s%n", delimiter).getBytes());
        fileOutputStream.write(String.format("%s", node).getBytes());
        fileOutputStream.close();
        fileOutputStream = null;
        return delimiter;
    }

    private void createFile(File file) throws IOException {
        file = new File(file.getName());
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(String.format(FILE_HEADER).getBytes());
        fileOutputStream.close();
        fileOutputStream = null;
    }

    @Override
    public boolean deleteNode(Delimiter delimiter) throws IOException {
        if (null == findNode(delimiter)) {
            return false;
        }
        File tempFile = new File(file.getName() + TEMP_EXTENSION);
        fileOutputStream = new FileOutputStream(tempFile);
        String line;
        lineNumberReader = new LineNumberReader(new FileReader(file));
        while (null != (line = lineNumberReader.readLine())) {
            if (line.contains(delimiter.toString())) {
                while (null != (line = lineNumberReader.readLine())) {
                    if (line.contains(NEXT_NODE_IDENTIFIER)) {
                        break;
                    }
                }
            }
            if (line != null) {
                fileOutputStream.write(String.format("%s%n", line).getBytes());
            }
        }
        fileOutputStream.close();
        fileOutputStream = null;
        return file.delete() && tempFile.renameTo(file);
    }
}
