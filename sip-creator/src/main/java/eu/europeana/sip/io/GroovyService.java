package eu.europeana.sip.io;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of GroovyService
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyService {

    private final Logger LOG = Logger.getLogger(this.getClass().getName());
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

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

    public void compile(String groovySnippet, Binding binding, CompileListener compileListener) throws Exception {
        threadPool.execute(new GroovyCompiler(groovySnippet, binding, compileListener));
    }

    public void save(File file, String groovySnippet) throws IOException {
        threadPool.execute(new Persistor(groovySnippet, file));
    }

    public void read(File file, LoadListener loadListener) throws IOException {
        threadPool.execute(new Loader(file, loadListener));
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
        LOG.debug(String.format("Updated mapping file to %s%n", mappingFile));
    }

    private class Persistor implements Runnable {

        private String groovySnippet;
        private File file;

        public Persistor(String groovySnippet, File file) {
            this.groovySnippet = groovySnippet;
            this.file = file;
        }

        @Override
        public void run() {
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file);
                LOG.debug(String.format("Writing to %s; %s [%d bytes written]%n", file, groovySnippet, groovySnippet.length()));
                fileOutputStream.write(groovySnippet.getBytes(), 0, groovySnippet.length());
                fileOutputStream.close();
            }
            catch (IOException e) {
                LOG.error("Error persisting snippet", e);
            }
        }
    }

    private class Loader implements Runnable {

        private File file;
        private LoadListener loadListener;

        private Loader(File file, LoadListener loadListener) {
            this.file = file;
            this.loadListener = loadListener;
        }

        @Override
        public void run() {
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(file);
                StringBuffer result = new StringBuffer();
                int count;
                while (-1 != (count = fileInputStream.read())) {
                    result.append((char) count);
                }
                LOG.debug(String.format("Reading from %s; %s [%d bytes read]%n", file, result, result.length()));
                fileInputStream.close();
                loadListener.loadComplete(result.toString());
            }
            catch (IOException e) {
                LOG.error("Error reading snippet", e);
            }
        }
    }

    private class GroovyCompiler implements Runnable {

        private CompileListener compileListener;
        private String groovySnippet;
        private Binding binding;

        public GroovyCompiler(String groovySnippet, Binding binding, CompileListener compileListener) {
            this.groovySnippet = groovySnippet;
            this.binding = binding;
            this.compileListener = compileListener;
        }

        @Override
        public void run() {

            String result;
            try {
                result = (String) new GroovyShell(binding).evaluate(groovySnippet);
                compileListener.compilationResult(result);
            }
            catch (Exception e) {
                LOG.error("Error compiling snippet", e);
            }
        }
    }

    public String toString() {
        return "GroovyEngineImpl{" +
                "mappingFile=" + mappingFile +
                '}';
    }
}
