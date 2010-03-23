package eu.europeana.sip.io;

import groovy.lang.GroovyShell;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Implementation of GroovyService
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyService {

    private final Logger LOG = Logger.getLogger(this.getClass().getName());

    private File mappingFile;

    public interface LoadListener {
        void loadComplete(String groovySnippet);
    }

    public interface CompileListener {
        void compilationResult(String result);
    }

    public GroovyService(File mappingFile) {
        this.mappingFile = mappingFile;
        LOG.debug(String.format("Mapping file %s%n", mappingFile));
    }

    private synchronized void save(File file, String groovySnippet) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        LOG.debug(String.format("Writing to %s; %s [%d bytes written]%n", file, groovySnippet, groovySnippet.length()));
        fileOutputStream.write(groovySnippet.getBytes(), 0, groovySnippet.length());
        fileOutputStream.close();
    }

    private synchronized void save(String groovySnippet) throws IOException {
        mappingFile = new File(mappingFile.getPath());
        LOG.debug("Saving to file " + mappingFile);
        save(mappingFile, groovySnippet);
    }

    private synchronized String read(File file) throws IOException {
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

    private synchronized String read() throws IOException {
        return read(mappingFile);
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
        LOG.debug(String.format("Updated mapping file to %s%n", mappingFile));
    }

    public class Persist implements Runnable {

        private String snippet;

        public Persist(String snippet) {
            this.snippet = snippet;
        }

        @Override
        public void run() {
            try {
                save(snippet);
            }
            catch (IOException e) {
                LOG.error("Error saving file", e);
            }
        }
    }

    public class Load implements Runnable {

        private LoadListener loadListener;

        public Load(LoadListener loadListener) {
            this.loadListener = loadListener;
        }

        @Override
        public void run() {
            try {
                String snippet = read(mappingFile);
                loadListener.loadComplete(snippet);
            }
            catch (IOException e) {
                LOG.error("Error reading file", e);
            }
        }
    }

    public class Compile implements Runnable {

        private CompileListener compileListener;
        private String groovySnippet;

        public Compile(String groovySnippet, CompileListener compileListener) {
            this.groovySnippet = groovySnippet;
            this.compileListener = compileListener;
        }

        @Override
        public void run() {
            compileListener.compilationResult((String) new GroovyShell().evaluate(groovySnippet));
        }
    }

    public String toString() {
        return "GroovyEngineImpl{" +
                "mappingFile=" + mappingFile +
                '}';
    }
}
