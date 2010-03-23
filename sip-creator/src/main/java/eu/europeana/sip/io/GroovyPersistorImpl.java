package eu.europeana.sip.io;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Implementation of GroovyPersistor
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyPersistorImpl implements GroovyPersistor {

    private final Logger LOG = Logger.getLogger(this.getClass().getName());

    private File mappingFile;

    public GroovyPersistorImpl(File mappingFile) {
        this.mappingFile = mappingFile;
        LOG.info(String.format("Mapping file %s%n", mappingFile));
    }

    @Override
    public void save(File file, StringBuffer groovySnippet) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        LOG.debug(String.format("Writing to %s; %s [%d bytes written]%n", file, groovySnippet, groovySnippet.length()));
        fileOutputStream.write(groovySnippet.toString().getBytes(), 0, groovySnippet.length());
        fileOutputStream.close();
    }

    @Override
    public void save(StringBuffer groovySnippet) throws IOException {
        mappingFile = new File(mappingFile.getPath());
        LOG.info("Saving to file " + mappingFile);
        save(mappingFile, groovySnippet);
    }

    @Override
    public String read(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        StringBuffer result = new StringBuffer();
        int count;
        while (-1 != (count = fileInputStream.read())) {
            result.append((char) count);
        }
        LOG.debug(String.format("Reading from %s; %s [%d bytes read]%n", file, result, result.length()));
        fileInputStream.close();
        return result.toString();
    }

    @Override
    public String read() throws IOException {
        return read(mappingFile);
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
        LOG.info(String.format("Updated mapping file to %s%n", mappingFile));
    }

    public String toString() {
        return "GroovyPersistorImpl{" +
                "mappingFile=" + mappingFile +
                '}';
    }
}
