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

import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.groovy.FieldMapping;
import eu.europeana.sip.groovy.MappingScriptBinding;
import eu.europeana.sip.groovy.RecordMapping;
import eu.europeana.sip.xml.AnalysisParser;
import eu.europeana.sip.xml.MetadataParser;
import eu.europeana.sip.xml.MetadataRecord;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This model is behind the whole sip creator, as a facade for all the models related to a FileSet
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SipModel {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FileSet fileSet;
    private ExceptionHandler exceptionHandler;
    private List<Statistics> statisticsList;
    private AnalysisTree analysisTree;
    private RecordMapping recordMapping;
    private QName recordRoot;
    private DefaultTreeModel analysisTreeModel;
    private FieldListModel fieldListModel;
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private FieldMappingListModel fieldMappingListModel = new FieldMappingListModel();
    private Document inputDocument = new PlainDocument();
    private Document codeDocument = new PlainDocument();
    private Document outputDocument = new PlainDocument();
    private List<FileSetListener> fileSetListeners = new CopyOnWriteArrayList<FileSetListener>();
    private MetadataParser metadataParser;
    private MetadataRecord metadataRecord;

    public interface FileSetListener {
        void updatedFileSet();
    }

    public interface AnalysisListener {
        void finished(boolean success);

        void analysisProgress(long elementCount);
    }

    public SipModel() {
        analysisTree = AnalysisTree.create("No Document Selected");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void addFileSetListener(FileSetListener fileSetListener) {
        fileSetListeners.add(fileSetListener);
    }

    public void setAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.fieldListModel = new FieldListModel(annotationProcessor);
    }

    public void setFileSet(FileSet fileSet) {
        checkSwingThread();
        this.fileSet = fileSet;
        setStatisticsList(fileSet.getStatistics());
        setRecordRootInternal(fileSet.getRecordRoot());
        setRecordMapping(fileSet.getMapping());
        if (recordRoot != null) {
            createMetadataParser();
        }
        statisticsTableModel.setCounterList(null);
        for (FileSetListener fileSetListener : fileSetListeners) {
            fileSetListener.updatedFileSet();
        }
    }

    public void analyze(final AnalysisListener listener) {
        checkSwingThread();
        fileSet.analyze(new AnalysisParser.Listener() {

            @Override
            public void success(List<Statistics> list) {
                checkUtilityThread();
                setStatisticsList(list);
                listener.finished(true);
            }

            @Override
            public void failure(Exception exception) {
                checkUtilityThread();
                listener.finished(false);
                exceptionHandler.failure(exception);
            }

            @Override
            public void progress(long elementCount) {
                checkUtilityThread();
                listener.analysisProgress(elementCount);
            }
        });
    }

    public void abortAnalyze() {
        checkSwingThread();
        fileSet.abortAnalysis();
    }

    public TreeModel getAnalysisTreeModel() {
        return analysisTreeModel;
    }

    public void selectNode(AnalysisTree.Node node) {
        checkSwingThread();
        if (node.getStatistics() != null) {
            statisticsTableModel.setCounterList(node.getStatistics().getCounters());
        }
        else {
            statisticsTableModel.setCounterList(null);
        }
    }

    public void setRecordRoot(QName recordRoot) {
        checkSwingThread();
        setRecordRootInternal(recordRoot);
        executor.execute(new RecordRootSetter());
    }

    public TableModel getStatisticsTableModel() {
        return statisticsTableModel;
    }

    public long getElementCount() {
        if (statisticsList != null) {
            long total = 0L;
            for (Statistics stats : statisticsList) {
                total += stats.getTotal();
            }
            return total;
        }
        else {
            return 0L;
        }
    }

    public ListModel getUnmappedFieldListModel() {
        return fieldListModel.createUnmapped(fieldMappingListModel);
    }

    public ListModel getVariablesListModel() {
        return variableListModel;
    }

    public ListModel getUnmappedVariablesListModel() {
        return variableListModel.createUnmapped(fieldMappingListModel);
    }

    public void addFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        recordMapping.getFieldMappings().add(fieldMapping);
        String code = recordMapping.getCodeForPersistence();
        setRecordMapping(code);
        executor.execute(new MappingSetter(code));
    }

    public void removeFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        recordMapping.getFieldMappings().remove(fieldMapping);
        String code = recordMapping.getCodeForPersistence();
        setRecordMapping(code);
        executor.execute(new MappingSetter(code));
    }

    public ListModel getFieldMappingListModel() {
        return fieldMappingListModel;
    }

    public void firstRecord() {
        checkSwingThread();
        createMetadataParser();
    }

    public void nextRecord() {
        checkSwingThread();
        executor.execute(new NextRecordFetcher());
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

    private void compileCode() {
        checkSwingThread();
        executor.execute(new CompilationRunner());
    }

    private void setRecordMapping(String recordMappingString) {
        checkSwingThread();
        recordMapping = new RecordMapping(recordMappingString);
        fieldMappingListModel.setList(recordMapping.getFieldMappings());
        DocumentSetter.set(codeDocument, recordMapping.getCodeForDisplay());
        compileCode();
    }

    private void setRecordRootInternal(QName recordRoot) {
        checkSwingThread();
        this.recordRoot = recordRoot;
        List<AnalysisTree.Node> changedNodes = new ArrayList<AnalysisTree.Node>();
        AnalysisTree.setRecordRoot(analysisTreeModel, recordRoot, changedNodes);
        for (AnalysisTree.Node node : changedNodes) {
            analysisTreeModel.nodeChanged(node);
        }
        List<String> variables = new ArrayList<String>();
        if (recordRoot != null) {
            analysisTree.getVariables(variables);
        }
        variableListModel.setVariableList(variables);
        fieldMappingListModel.clear();
    }

    private void setStatisticsList(List<Statistics> statisticsList) {
        checkSwingThread();
        this.statisticsList = statisticsList;
        if (statisticsList != null) {
            analysisTree = AnalysisTree.create(statisticsList, fileSet.getName(), recordRoot);
        }
        else {
            analysisTree = AnalysisTree.create("Analysis not yet performed");
        }
        analysisTreeModel.setRoot(analysisTree.getRoot());
    }

    private void createMetadataParser() {
        checkSwingThread();
        if (metadataParser != null) {
            metadataParser.close();
            metadataParser = null;
        }
        try {
            metadataParser = new MetadataParser(fileSet.getInputStream(), recordRoot);
            metadataRecord = metadataParser.nextRecord();
            SwingUtilities.invokeLater(new DocumentSetter(inputDocument, metadataRecord.toString()));
            compileCode();
        }
        catch (Exception e) {
            exceptionHandler.failure(e);
        }
    }

    private class NextRecordFetcher implements Runnable {
        @Override
        public void run() {
            if (metadataParser != null) {
                try {
                    metadataRecord = metadataParser.nextRecord();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            DocumentSetter.set(inputDocument, metadataRecord.toString());
                            compileCode();
                        }
                    });
                }
                catch (Exception e) {
                    exceptionHandler.failure(e);
                }
            }
        }
    }

    private class RecordRootSetter implements Runnable {
        @Override
        public void run() {
            fileSet.setRecordRoot(recordRoot);
        }
    }

    private class MappingSetter implements Runnable {
        private String mapping;

        private MappingSetter(String mapping) {
            this.mapping = mapping;
        }

        @Override
        public void run() {
            fileSet.setMapping(mapping);
        }
    }

    private static class DocumentSetter implements Runnable {
        private Document document;
        private String content;

        private DocumentSetter(Document document, String content) {
            this.document = document;
            this.content = content;
        }

        @Override
        public void run() {
            setDocumentContents(document, content);
        }

        public static void set(Document document, String content) {
            checkSwingThread();
            setDocumentContents(document, content);
        }

        private static void setDocumentContents(Document document, String content) {
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

    private class CompilationRunner implements Runnable {
        private String code;
        private StringWriter writer = new StringWriter();

        private CompilationRunner() {
            this.code = recordMapping.getCodeForPersistence();
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
                compilationComplete(out.toString());
            }
            catch (Exception e) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                compilationComplete(writer.toString());
            }
        }

        private void compilationComplete(String result) {
            SwingUtilities.invokeLater(new DocumentSetter(outputDocument, result));
        }
    }

    private static void checkSwingThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Swing thread");
        }
    }

    private static void checkUtilityThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected a utility thread, but it was the Swing thread");
        }
    }
}
