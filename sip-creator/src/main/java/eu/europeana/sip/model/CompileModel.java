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

import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.FieldMapping;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordMapping;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCodeModel;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This model is behind the scenario with input data, groovy code, and output record
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class CompileModel implements SipModel.ParseListener, RecordMapping.Listener {
    public final static int COMPILE_DELAY = 500;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private boolean multipleMappings;
    private RecordMapping recordMapping;
    private MetadataRecord metadataRecord;
    private Document inputDocument = new PlainDocument();
    private Document codeDocument = new PlainDocument();
    private Document outputDocument = new PlainDocument();
    private CompileTimer compileTimer = new CompileTimer();
    private ToolCodeModel toolCodeModel;
    private RecordValidator recordValidator;
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

    public CompileModel(ToolCodeModel toolCodeModel, ConstantFieldModel constantFieldModel, RecordValidator recordValidator) {
        this.multipleMappings = true;
        this.recordMapping = new RecordMapping(false, constantFieldModel);
        this.recordMapping.addListener(this);
        this.toolCodeModel = toolCodeModel;
        this.recordValidator = recordValidator;
    }

    public CompileModel(ToolCodeModel toolCodeModel, ConstantFieldModel constantFieldModel) {
        this.multipleMappings = false;
        this.recordMapping = new RecordMapping(true, constantFieldModel);
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
                notifyStateChange(State.EDITED);
            }
            else {
                editedCode = null;
                notifyStateChange(State.PRISTINE);
            }
        }
        compileSoon();
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

    @Override
    public void valueMapChanged() {
        editedCode = recordMapping.getCodeForDisplay();
        compileSoon();
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
        notifyStateChange(State.PRISTINE);
        if (!multipleMappings) {
            updateInputDocument(metadataRecord);
        }
        editedCode = null;
        compileSoon();
    }

    private void updateInputDocument(MetadataRecord metadataRecord) {
        if (metadataRecord != null) {
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, metadataRecord.toString()));
        }
        else {
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, "No Input"));
        }
    }

    private class CompilationRunner implements Runnable {

        @Override
        public void run() {
            if (metadataRecord == null) {
                return;
            }
            String mappingCode = editedCode == null ? recordMapping.getCodeForCompile() : RecordMapping.getCodeForCompile(editedCode);
            StringBuilder valueMapCode = new StringBuilder();
            for (FieldMapping fieldMapping : recordMapping) {
                if (fieldMapping.getValueMap() != null) {
                    valueMapCode.append(fieldMapping.getValueMap());
                }
            }
            MappingRunner mappingRunner = new MappingRunner(toolCodeModel.getCode() + valueMapCode + mappingCode, recordMapping.getConstantFieldModel());
            try {
                String output = mappingRunner.runMapping(metadataRecord);
                if (multipleMappings) {
                    String validated = recordValidator.validate(metadataRecord, output);
                    compilationComplete(validated);
                }
                else {
                    compilationComplete(output);
                    if (editedCode == null) {
                        notifyStateChange(State.PRISTINE);
                    }
                    else {
                        FieldMapping fieldMapping = recordMapping.getOnlyFieldMapping();
                        if (fieldMapping != null) {
                            fieldMapping.setCode(editedCode);
                            notifyStateChange(State.COMMITTED);
                            editedCode = null;
                            notifyStateChange(State.PRISTINE);
                        }
                        else {
                            notifyStateChange(State.EDITED);
                        }
                    }
                }
            }
            catch (MappingException e) {
                compilationComplete(e.getMessage());
                notifyStateChange(State.ERROR);
            }
            catch (RecordValidationException e) {
                compilationComplete(e.getMessage());
                notifyStateChange(State.ERROR);
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

    private void notifyStateChange(State state) {
        for (Listener listener : listeners) {
            listener.stateChanged(state);
        }
    }

    private static void checkSwingThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Swing thread");
        }
    }
}