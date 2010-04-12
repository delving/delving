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

import eu.europeana.sip.gui.AnalyzerPanel;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import javax.xml.namespace.QName;
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
public class GroovyService implements AnalyzerPanel.RecordChangeListener {

    private final Logger LOG = Logger.getLogger(this.getClass().getName());
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private File groovyFile;
    private Listener listener;
    private BindingSource bindingSource;
    private QName recordRoot;

    @Override
    public void recordRootChanged(File file, QName recordRoot) {
        this.recordRoot = recordRoot;
    }

    @Override
    public void save(File file, QName recordRoot) {
        // todo: implement
    }

    @Override
    public QName load(File file) {
        return null;  // todo: implement
    }

    public interface Listener {
        void loadComplete(String groovySnippet);

        void compilationResult(String result);
    }

    public interface BindingSource {

        public static final String INPUT = "input";
        public static final String OUTPUT = "output";
        public static final String DC = "dc";
        public static final String DCTERMS = "dcterms";
        public static final String EUROPEANA = "europeana";

        public Binding createBinding(Writer writer);
    }

    public GroovyService(BindingSource bindingSource, Listener listener) {
        this.bindingSource = bindingSource;
        this.listener = listener;
    }

    public void setGroovyFile(File groovyFile) {
        this.groovyFile = groovyFile;
        if (this.groovyFile.exists()) {
            threadPool.execute(new Loader());
        }
        else {
            LOG.info("Mapping file did not exist " + groovyFile.getAbsolutePath());
        }
    }

    public void setGroovyCode(String groovyCode) {
        threadPool.execute(new ComplilationRunner(groovyCode));
    }

    private class Persistor implements Runnable {

        private String groovySnippet;

        public Persistor(String groovySnippet) {
            this.groovySnippet = groovySnippet;
        }

        @Override
        public void run() {
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(groovyFile);
                LOG.debug(String.format("Writing to %s; %s [%d bytes written]%n", groovyFile, groovySnippet, groovySnippet.length()));
                fileOutputStream.write(groovySnippet.getBytes(), 0, groovySnippet.length());
                fileOutputStream.close();
            }
            catch (IOException e) {
                LOG.error("Error persisting snippet", e);
            }
        }
    }

    private class Loader implements Runnable {
        @Override
        public void run() {
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(groovyFile);
                StringBuffer result = new StringBuffer();
                int count;
                while (-1 != (count = fileInputStream.read())) {
                    result.append((char) count);
                }
                LOG.debug(String.format("Reading from %s; %s [%d bytes read]%n", groovyFile, result, result.length()));
                fileInputStream.close();
                if (listener != null) {
                    listener.loadComplete(result.toString());
                }
            }
            catch (IOException e) {
                LOG.error("Error reading snippet", e);
            }
        }
    }

    private class ComplilationRunner implements Runnable {
        private StringWriter writer = new StringWriter();
        private String groovySnippet;

        public ComplilationRunner(String groovySnippet) {
            this.groovySnippet = groovySnippet;
        }

        @Override
        public void run() {
            try {
                Binding binding = bindingSource.createBinding(writer);
                if (null == binding.getVariable(BindingSource.INPUT)) {
                    LOG.error(String.format("Input is returning null on delimiter '%s'", recordRoot));

                    return;
                }
                new GroovyShell(binding).evaluate(groovySnippet);
                sendResult(writer.toString());
                threadPool.execute(new Persistor(groovySnippet));
            }
            catch (MissingPropertyException e) {
                sendResult("Missing Property: " + e.getProperty());
            }
            catch (MultipleCompilationErrorsException e) {
                StringBuilder out = new StringBuilder();
                for (Object o : e.getErrorCollector().getErrors()) {
                    SyntaxErrorMessage message = (SyntaxErrorMessage) o;
                    SyntaxException se = message.getCause();
                    out.append(String.format("Line %d Column %d: %s\n", se.getLine(), se.getStartColumn(), se.getOriginalMessage()));
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
            if (listener != null) {
                listener.compilationResult(result);
            }
        }
    }

    public String toString() {
        return "GroovyEngineImpl{" +
                "groovyFile=" + groovyFile +
                '}';
    }
}
