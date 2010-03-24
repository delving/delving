package eu.europeana.sip.io;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
    private BindingSource bindingSource;

    public interface LoadListener {
        void loadComplete(String groovySnippet);
    }

    public interface CompileListener {
        void compilationResult(String result);
    }

    public interface BindingSource {
        Binding createBinding(Writer writer);
    }

    public GroovyService(File mappingFile, BindingSource bindingSource) {
        this.mappingFile = mappingFile;
        this.bindingSource = bindingSource;
        LOG.debug(String.format("Mapping file %s%n", mappingFile));
    }

    public void compile(String groovySnippet, CompileListener compileListener) throws Exception {
        threadPool.execute(new GroovyCompiler(groovySnippet, compileListener));
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
        private StringWriter writer = new StringWriter();
        private CompileListener compileListener;
        private String groovySnippet;

        public GroovyCompiler(String groovySnippet, CompileListener compileListener) {
            this.groovySnippet = groovySnippet;
            this.compileListener = compileListener;
        }

        @Override
        public void run() {
            try {
                Binding binding = bindingSource.createBinding(writer);
                new GroovyShell(binding).evaluate(groovySnippet);
                compileListener.compilationResult(writer.toString());
            }
            catch (MissingPropertyException e) {
                compileListener.compilationResult("Missing Property: "+e.getProperty());
            }
            catch (MultipleCompilationErrorsException e) {
                StringBuilder out = new StringBuilder();
                for (Object o : e.getErrorCollector().getErrors()) {
                    SyntaxErrorMessage message = (SyntaxErrorMessage)o;
                    SyntaxException se = message.getCause();
                    out.append(String.format("Line %d Column %d: %s\n", se.getLine(), se.getStartColumn(), se.getOriginalMessage()));
                }
                compileListener.compilationResult(out.toString());
            }
            catch (Exception e) {
                LOG.error("Uncaught exception", e);
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                compileListener.compilationResult(writer.toString());
            }
        }
    }

    public String toString() {
        return "GroovyEngineImpl{" +
                "mappingFile=" + mappingFile +
                '}';
    }
}
