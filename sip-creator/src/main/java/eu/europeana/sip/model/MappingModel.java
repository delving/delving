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

package eu.europeana.sip.model;

import eu.europeana.sip.groovy.FieldMapping;
import eu.europeana.sip.groovy.MappingScriptBinding;
import eu.europeana.sip.groovy.RecordMapping;
import eu.europeana.sip.xml.MetadataRecord;
import eu.europeana.sip.xml.MetadataVariable;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This model is behind the scenario with input data, groovy code, and output record
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MappingModel implements SipModel.ParseListener, RecordMapping.Listener {
    public final static int COMPILE_DELAY = 500;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private boolean multipleMappings;
    private RecordMapping recordMapping = new RecordMapping();
    private MetadataRecord metadataRecord;
    private Document inputDocument = new PlainDocument();
    private Document codeDocument = new PlainDocument();
    private Document outputDocument = new PlainDocument();
    private CompileTimer compileTimer = new CompileTimer();
    private ToolCodeModel toolCodeModel;
    private State state = State.UNCOMPILED;
    private String editedCode;

    public enum State {
        UNCOMPILED,
        PRISTINE,
        EDITED,
        ERROR,
        COMMITTED
    }

    public interface Listener {
        void stateChanged(State state);
    }

    public MappingModel(boolean multipleMappings, ToolCodeModel toolCodeModel) {
        this.multipleMappings = multipleMappings;
        this.recordMapping.addListener(this);
        this.toolCodeModel = toolCodeModel;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void refreshCode() {
        String code = recordMapping.getCodeForDisplay();
        SwingUtilities.invokeLater(new DocumentSetter(codeDocument, code));
        compileSoon();
    }

    public void compileSoon() {
        compileTimer.triggerSoon();
    }

    public void setCode(String code) {
        if (multipleMappings) {
            throw new RuntimeException();
        }
        FieldMapping fieldMapping = recordMapping.getOnlyFieldMapping();
        if (fieldMapping != null) {
            if (!fieldMapping.codeLooksLike(code)) {
                editedCode = code;
            }
            else {
                editedCode = null;
                changeState(State.PRISTINE);
            }
        }
        compileSoon();
    }

    public void commitCode() {
        if (multipleMappings) {
            throw new RuntimeException();
        }
        switch (state) {
            case UNCOMPILED:
            case PRISTINE:
                break;
            case EDITED:
                if (editedCode != null) {
                    FieldMapping fieldMapping = recordMapping.getOnlyFieldMapping();
                    if (fieldMapping != null) {
                        recordMapping.iterator().next().setCode(editedCode);
                        changeState(State.COMMITTED);
                        compileSoon();
                    }
                }
                break;
            case ERROR:
                String code = recordMapping.getCodeForDisplay();
                SwingUtilities.invokeLater(new DocumentSetter(codeDocument, code));
                break;
        }
        editedCode = null;
    }

    public RecordMapping getRecordMapping() {
        return recordMapping;
    }

    @Override
    public void updatedRecord(MetadataRecord metadataRecord) {
        this.metadataRecord = metadataRecord;
        if (metadataRecord == null) {
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, "No input"));
        }
        else {
            updateInputDocument(metadataRecord);
            compileSoon();
        }
    }

    @Override
    public void mappingAdded(FieldMapping fieldMapping) {
        mappingChanged();
    }

    @Override
    public void mappingRemoved(FieldMapping fieldMapping) {
        mappingChanged();
    }

    @Override
    public void mappingsRefreshed(RecordMapping recordMapping) {
        mappingChanged();
    }

    public Document getInputDocument() {
        return inputDocument;
    }

    public Document getCodeDocument() {
        return codeDocument;
    }

    public Document getOutputDocument() {
        return outputDocument;
    }

    // === privates

    private void mappingChanged() {
        String code = recordMapping.getCodeForDisplay();
        SwingUtilities.invokeLater(new DocumentSetter(codeDocument, code));
        changeState(State.PRISTINE);
        if (!multipleMappings) {
            updateInputDocument(metadataRecord);
        }
        editedCode = null;
        compileSoon();
    }

    private void updateInputDocument(MetadataRecord metadataRecord) {
        if (metadataRecord != null) {
            StringBuilder out = new StringBuilder();
            for (MetadataVariable variable : metadataRecord.getVariables()) {
                out.append(variable.toString()).append('\n');
            }
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, out.toString()));
        }
        else {
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, "No Input"));
        }
    }

    private class CompilationRunner implements Runnable {
        private StringWriter writer = new StringWriter();

        @Override
        public void run() {
            try {
                String code = editedCode == null ? recordMapping.getCodeForCompile() : RecordMapping.getCodeForCompile(editedCode);
                MappingScriptBinding mappingScriptBinding = new MappingScriptBinding(writer);
                mappingScriptBinding.setRecord(metadataRecord);
                new GroovyShell(mappingScriptBinding).evaluate(toolCodeModel.getToolCode() + code);
                compilationComplete(writer.toString());
                changeState(editedCode == null ? State.PRISTINE : State.EDITED);
            }
            catch (MissingPropertyException e) {
                compilationComplete("Missing Property: " + e.getProperty());
                changeState(State.ERROR);
            }
            catch (MultipleCompilationErrorsException e) {
                StringBuilder out = new StringBuilder();
                for (Object o : e.getErrorCollector().getErrors()) {
                    SyntaxErrorMessage message = (SyntaxErrorMessage) o;
                    SyntaxException se = message.getCause();
                    // todo: line numbers will not match
                    out.append(String.format("Line %d: %s%n", se.getLine(), se.getOriginalMessage()));
                }
                compilationComplete(out.toString());
                changeState(State.ERROR);
            }
            catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                compilationComplete(writer.toString());
                changeState(State.ERROR);
            }
        }

        private void compilationComplete(final String result) {
            SwingUtilities.invokeLater(new DocumentSetter(outputDocument, result));
        }
    }

    private class DocumentSetter implements Runnable {

        private Document document;
        private String content;

        private DocumentSetter(Document document, String content) {
            this.document = document;
            this.content = content;
        }

        @Override
        public void run() {
            int docLength = document.getLength();
            try {
                document.remove(0, docLength);
                document.insertString(0, content, null);
            }
            catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class CompileTimer implements ActionListener {
        private Timer timer = new Timer(COMPILE_DELAY, this);

        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            executor.execute(new CompilationRunner());
        }

        public void triggerSoon() {
            timer.restart();
        }
    }

    private void changeState(State state) {
        this.state = state;
        for (Listener listener : listeners) {
            listener.stateChanged(this.state);
        }
    }

    private static void checkSwingThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Swing thread");
        }
    }
}