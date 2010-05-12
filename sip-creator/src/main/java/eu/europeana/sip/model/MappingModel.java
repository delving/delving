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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This model is behind the scenario with input data, groovy code, and output record
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MappingModel implements SipModel.ParseListener {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean multipleMappings;
    private RecordMapping recordMapping;
    private MetadataRecord metadataRecord;
    private Document inputDocument = new PlainDocument();
    private Document codeDocument = new PlainDocument();
    private Document outputDocument = new PlainDocument();

    public MappingModel(boolean multipleMappings) {
        this.multipleMappings = multipleMappings;
    }

    public void setRecordMapping(RecordMapping recordMapping) {
        this.recordMapping = recordMapping;
        if (recordMapping == null) {
            clearCode();
        }
        else {
            setCode(recordMapping.getCodeForDisplay(multipleMappings));
        }
        if (!multipleMappings) {
            updateInputDocument(metadataRecord); // because it's filtered
        }
    }

    @Override
    public void updatedRecord(MetadataRecord metadataRecord) {
        this.metadataRecord = metadataRecord;
        if (metadataRecord == null) {
            setDocumentContents(inputDocument, "No input");
        }
        else {
            updateInputDocument(metadataRecord);
            compileCode();
        }
    }

    private void updateInputDocument(MetadataRecord metadataRecord) {
        if (metadataRecord != null) {
            List<MetadataVariable> variables = metadataRecord.getVariables();
            if (!multipleMappings && recordMapping != null) {
                FieldMapping fieldMapping = recordMapping.getFieldMappings().get(0);
                Iterator<MetadataVariable> walk = variables.iterator();
                while (walk.hasNext()) {
                    MetadataVariable variable = walk.next();
                    if (!fieldMapping.getInputVariables().contains(variable.getName())) {
                        walk.remove();
                    }
                }
            }
            StringBuilder out = new StringBuilder();
            for (MetadataVariable variable : variables) {
                out.append(variable.toString()).append('\n');
            }
            setDocumentContents(inputDocument, out.toString());
        }
        else {
            setDocumentContents(inputDocument, "No Input");
        }
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

    private void setCode(String code) {
        setDocumentContents(codeDocument, code);
        compileCode();
    }

    private void clearCode() {
        setDocumentContents(codeDocument, "No Code");
    }

    private void compileCode() {
        checkSwingThread();
        if (metadataRecord != null && recordMapping != null) {
            String code = recordMapping.getCode();
            executor.execute(new CompilationRunner(code, metadataRecord, outputDocument));
        }
        else {
            setDocumentContents(outputDocument, "");
        }
    }

    private static void setDocumentContents(Document document, String content) {
        checkSwingThread();
        int docLength = document.getLength();
        try {
            document.remove(0, docLength);
            document.insertString(0, content, null);
        }
        catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CompilationRunner implements Runnable {
        private MetadataRecord metadataRecord;
        private Document outputDocument;
        private String code;
        private StringWriter writer = new StringWriter();

        private CompilationRunner(String code, MetadataRecord metadataRecord, Document outputDocument) {
            this.code = code;
            this.metadataRecord = metadataRecord;
            this.outputDocument = outputDocument;
        }

        @Override
        public void run() {
            try {
                MappingScriptBinding mappingScriptBinding = new MappingScriptBinding(writer);
                mappingScriptBinding.setRecord(metadataRecord);
                new GroovyShell(mappingScriptBinding).evaluate(code);
                compilationComplete(writer.toString());
            }
            catch (MissingPropertyException e) {
                compilationComplete("Missing Property: " + e.getProperty());
            }
            catch (MultipleCompilationErrorsException e) {
                StringBuilder out = new StringBuilder();
                for (Object o : e.getErrorCollector().getErrors()) {
                    SyntaxErrorMessage message = (SyntaxErrorMessage) o;
                    SyntaxException se = message.getCause();
                    out.append(String.format("Line %d Column %d: %s%n", se.getLine(), se.getStartColumn(), se.getOriginalMessage()));
                }
                compilationComplete(code + "\n" + out);
//                compilationComplete(out.toString());
            }
            catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                compilationComplete(writer.toString());
            }
        }

        private void compilationComplete(final String result) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setDocumentContents(outputDocument, result);
                }
            });
        }
    }

    private static void checkSwingThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Swing thread");
        }
    }
}