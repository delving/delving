/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.io;

import eu.europeana.sip.xml.GroovyNode;
import eu.europeana.sip.xml.NormalizationParser;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import javax.xml.namespace.QName;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This service handles compiling and saving the groovy mapping snippets, the record root QName
 * and also fetches records from the input file for testing the groovy.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GroovyService {

    private final Logger LOG = Logger.getLogger(this.getClass().getName());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private NormalizationParser normalizationParser;
    private FileSet fileSet;
    private GroovyNode record;
    private Listener listener;
    private String mapping;
    private CompilationRunner compilationRunner = new CompilationRunner();

    public interface CompilationListener {
        void nodeAvailable(GroovyNode groovyNode);
    }

    public interface Listener {
        void setMapping(String mapping);

        void setResult(String result);
    }

    public GroovyService(Listener listener) {
        this.listener = listener;
    }

    public void setFileSet(FileSet fileSet) {
        this.fileSet = fileSet;
        normalizationParser = null;
        record = null;
        LOG.info("set file set to " + fileSet);
        executor.execute(new FileSetLoader());
    }

    public FileSet getFileSet() {
        return fileSet;
    }

    public void compile(GroovyNode groovyNode) {
        this.record = groovyNode;
        compilationRunner.setMapping(mapping);
        compilationRunner.nodeAvailable(groovyNode);
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
        compilationRunner.setMapping(mapping);
        executor.execute(compilationRunner);
    }

    public void setRecordRoot(QName recordRoot) {
        executor.execute(new RecordRootSaver(recordRoot));
    }

    public void nextRecord() {
        executor.execute(new NextRecordFetcher());
    }

    public Normalizer normalize() throws FileNotFoundException {
        Normalizer normalizer = new Normalizer();
        executor.execute(normalizer);
        return normalizer;
    }

    private class NextRecordFetcher implements Runnable {

        @Override
        public void run() {
            try {
                record = normalizationParser.nextRecord();
                setMapping(fileSet.getMapping());
            }
            catch (Exception e) {
                listener.setResult(e.toString()); // todo
            }
        }
    }

    public class Normalizer implements Runnable {

        private boolean running = true;
        private boolean nodesAvailable = true;
        private int nodeCount;

        {
            normalizationParser.setListener(
                    new NormalizationParser.Listener() {

                        @Override
                        public void finished(boolean success) {
                            nodesAvailable = false;
                            LOG.info(String.format("Fetched %d records", nodeCount));  // todo: compare this with written nodes
                        }
                    }

            );
        }

        public void abort() {
            running = false;
        }

        @Override
        public void run() {
            try {
                while (nodesAvailable && running) {
                    GroovyNode node = normalizationParser.nextRecord();
                    if (null != node) {
                        compile(node);
                    }
                    nodeCount++;
                }
            }
            catch (Exception e) {
                listener.setResult(e.toString()); // todo
            }
        }
    }

    private class RecordRootSaver implements Runnable {
        private QName recordRoot;

        private RecordRootSaver(QName recordRoot) {
            this.recordRoot = recordRoot;
        }

        @Override
        public void run() {
            try {
                fileSet.setRecordRoot(recordRoot);
                setMapping(fileSet.getMapping());
            }
            catch (IOException e) {
                LOG.error("Error persisting snippet", e);
            }
        }
    }

    private class FileSetLoader implements Runnable {
        @Override
        public void run() {
            try {
                QName recordRoot = fileSet.getRecordRoot();
                LOG.info("got record root " + recordRoot);
                if (normalizationParser != null) {
                    normalizationParser.close();
                }
                normalizationParser = new NormalizationParser(fileSet.getInputStream(), recordRoot);
                record = normalizationParser.nextRecord();
                LOG.info("set up normalization parser, got first record " + record);
                String mapping = fileSet.getMapping();
                LOG.info("got mapping");
                listener.setMapping(mapping);
                LOG.info("setting mapping so it compiles");
                setMapping(mapping);
                LOG.info("file set loaded");
            }
            catch (Exception e) {
                LOG.error("Error loading file set", e);
                listener.setMapping("");
                listener.setResult(e.toString());
            }
        }
    }

    private class CompilationRunner implements Runnable, CompilationListener {

        private static final String INPUT = "input";
        private static final String OUTPUT = "output";
        private static final String DC = "dc";
        private static final String DCTERMS = "dcterms";
        private static final String EUROPEANA = "europeana";

        private StringWriter writer = new StringWriter();
        private Binding binding = createBinding();
        private String mapping;

        public void setMapping(String mapping) {
            this.mapping = mapping;
            run(); // todo: fix
            executor.execute(new MappingSaver(mapping));
        }

        @Override
        public void run() {
            if (null == mapping) {
                throw new NullPointerException("Mapping can't be null");
            }
            try {
                Binding binding = createBinding();
                new GroovyShell(binding).evaluate(mapping);
                sendResult(writer.toString());
                writer.getBuffer().setLength(0);
            }
            catch (MissingPropertyException e) {
                sendResult("Missing Property: " + e.getProperty());
            }
            catch (MultipleCompilationErrorsException e) {
                StringBuilder out = new StringBuilder();
                for (Object o : e.getErrorCollector().getErrors()) {
                    SyntaxErrorMessage message = (SyntaxErrorMessage) o;
                    SyntaxException se = message.getCause();
                    out.append(String.format("Line %d Column %d: %s%n", se.getLine(), se.getStartColumn(), se.getOriginalMessage()));
                }
                sendResult(out.toString());
            }
            catch (Exception e) {
                LOG.error("Uncaught exception", e);
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                sendResult(writer.toString());
            }
        }

        private void sendResult(String result) {
            listener.setResult(result);
        }

        @Override
        public void nodeAvailable(GroovyNode groovyNode) {
            binding.setVariable(INPUT, groovyNode);
            run(); // todo: fix
        }

        public Binding createBinding() {
            MarkupBuilder builder = new MarkupBuilder(writer);
            NamespaceBuilder xmlns = new NamespaceBuilder(builder);
            Binding binding = new Binding();
            binding.setVariable(INPUT, record);
            binding.setVariable(OUTPUT, builder);
            binding.setVariable(DC, xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
            binding.setVariable(DCTERMS, xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
            binding.setVariable(EUROPEANA, xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
            return binding;
        }
    }

    private class MappingSaver implements Runnable {
        private String mapping;

        public MappingSaver(String mapping) {
            this.mapping = mapping;
        }

        @Override
        public void run() {
            try {
                fileSet.setMapping(mapping);
            }
            catch (IOException e) {
                LOG.error("Error persisting snippet", e);
            }
        }

    }

    public String toString() {
        return "GroovyService";
    }
}
