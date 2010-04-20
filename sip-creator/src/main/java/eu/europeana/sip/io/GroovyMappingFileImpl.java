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

    private FileOutputStream fileOutputStream;
    private LineNumberReader lineNumberReader;
    private final File file;

    public GroovyMappingFileImpl(File file) {
        this.file = file;
    }

    /**
     * Search for the delimiter, and grab the snippet
     * todo: refactor
     */
    @Override
    public String findNode(Delimiter delimiter) throws IOException {
        StringBuffer node = new StringBuffer();
        lineNumberReader = new LineNumberReader(new FileReader(file));
        String line;
        while (null != (line = lineNumberReader.readLine())) {
            if (line.contains(delimiter.toString())) {
                node.append(line).append(String.format("%n"));
                lineNumberReader.mark((int) file.length());
                while (null != (line = lineNumberReader.readLine())) {
                    if (line.contains("// ===")) {
                        lineNumberReader.close();
                        return node.toString();
                    }
                    node.append(line).append(String.format("%n"));
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
        if (null == fileOutputStream) {
            fileOutputStream = new FileOutputStream(file);
        }
        if (null != findNode(delimiter)) {
            deleteNode(delimiter);
        }
        fileOutputStream.write(delimiter.getBytes(), 0, delimiter.getBytes().length);
        fileOutputStream.write(String.format("%n").getBytes());
        fileOutputStream.write(node.getBytes(), 0, node.getBytes().length);
        return delimiter;
    }

    /**
     * todo: refactor
     */
    @Override
    public boolean deleteNode(Delimiter delimiter) throws IOException {
        if (null == findNode(delimiter)) {
            return false;
        }
        File tempFile = new File(file.getName() + TEMP_EXTENSION);
        if (tempFile.exists()) {
            tempFile.delete();
            LOG.info(String.format("%s deleted", tempFile.getAbsoluteFile()));
        }
        fileOutputStream = new FileOutputStream(tempFile);
        String line;
        lineNumberReader = new LineNumberReader(new FileReader(file));
        while (null != (line = lineNumberReader.readLine())) {
            if (line.contains(delimiter.toString())) {
                LOG.info(String.format("DELETE %s", line));
                lineNumberReader.mark((int) file.length());
                while (null != (line = lineNumberReader.readLine())) {
                    if (line.contains("// ===")) {
                        break;
                    }
                    LOG.info(String.format("DELETE %s", line));
                }
            }
            if (line != null) {
                fileOutputStream.write(line.getBytes(), 0, line.length());
            }
            fileOutputStream.write(String.format("%n").getBytes());
        }
        fileOutputStream.close();
        file.delete();
        tempFile.renameTo(file);
        return true;
    }

    @Override
    public void close() throws IOException {
        fileOutputStream.close();
    }
}
