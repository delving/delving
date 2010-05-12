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
import eu.europeana.sip.xml.Normalizer;
import groovy.lang.GroovyShell;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
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
    private AnalysisParser analysisParser;
    private Normalizer normalizer;
    private AnalysisTree analysisTree;
    private RecordMapping recordMapping;
    private RecordRoot recordRoot;
    private DefaultTreeModel analysisTreeModel;
    private FieldListModel fieldListModel;
    private DefaultBoundedRangeModel normalizeProgressModel = new DefaultBoundedRangeModel();
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private FieldMappingListModel fieldMappingListModel = new FieldMappingListModel();
    private List<UpdateListener> updateListeners = new CopyOnWriteArrayList<UpdateListener>();
    private List<ParseListener> parseListeners = new CopyOnWriteArrayList<ParseListener>();
    private MetadataParser metadataParser;
    private MetadataRecord metadataRecord;
    private MappingModel recordMappingModel, fieldMappingModel;

    public interface UpdateListener {
        void updatedFileSet(FileSet fileSet);

        void updatedRecordRoot(RecordRoot recordRoot);
    }

    public interface AnalysisListener {
        void finished(boolean success);

        void analysisProgress(long elementCount);
    }

    public interface ParseListener {
        void updatedRecord(MetadataRecord metadataRecord);
    }

    public SipModel() {
        analysisTree = AnalysisTree.create("No Document Selected");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
        recordMappingModel = new MappingModel(true);
        parseListeners.add(recordMappingModel);
        fieldMappingModel = new MappingModel(false);
        parseListeners.add(fieldMappingModel);
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void addUpdateListener(UpdateListener updateListener) {
        updateListeners.add(updateListener);
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
        createMetadataParser();
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedFileSet(fileSet);
        }
    }

    public void analyze(final AnalysisListener listener) {
        checkSwingThread();
        abortAnalyze();
        this.analysisParser = new AnalysisParser(fileSet, new AnalysisParser.Listener() {

            @Override
            public void success(final List<Statistics> list) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setStatisticsList(list);
                    }
                });
                listener.finished(true);
            }

            @Override
            public void failure(Exception exception) {
                listener.finished(false);
                exceptionHandler.failure(exception);
            }

            @Override
            public void progress(long elementCount) {
                listener.analysisProgress(elementCount);
            }
        });
        executor.execute(analysisParser);
    }

    public void abortAnalyze() {
        checkSwingThread();
        if (analysisParser != null) {
            analysisParser.abort();
            analysisParser = null;
        }
    }

    public BoundedRangeModel getNormalizeProgress() {
        return normalizeProgressModel;
    }

    public void normalize() {
        checkSwingThread();
        abortNormalize();
        normalizeProgressModel.setMaximum(recordRoot.getRecordCount());
        normalizer = new Normalizer(fileSet, new MetadataParser.Listener() {
            @Override
            public void recordsParsed(final int count) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        normalizeProgressModel.setValue(count);
                    }
                });
            }
        });
        executor.execute(normalizer);
    }

    public void abortNormalize() {
        checkSwingThread();
        if (normalizer != null) {
            normalizer.abort();
            normalizer = null;
        }
    }

    public TreeModel getAnalysisTreeModel() {
        return analysisTreeModel;
    }

    public void selectNode(AnalysisTree.Node node) {
        checkSwingThread();
        if (node != null && node.getStatistics() != null) {
            statisticsTableModel.setCounterList(node.getStatistics().getCounters());
        }
        else {
            statisticsTableModel.setCounterList(null);
        }
    }

    public void setRecordRoot(RecordRoot recordRoot) {
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
        String code = recordMapping.getCode();
        setRecordMapping(code);
        executor.execute(new MappingSetter(code));
    }

    public void removeFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        recordMapping.getFieldMappings().remove(fieldMapping);
        String code = recordMapping.getCode();
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

    public MappingModel getRecordMappingModel() {
        return recordMappingModel;
    }

    public MappingModel getFieldMappingModel() {
        return fieldMappingModel;
    }

    // === privates

    private void setRecordMapping(String recordMappingString) {
        checkSwingThread();
        recordMapping = new RecordMapping(recordMappingString);
        fieldMappingListModel.setList(recordMapping.getFieldMappings());
        recordMappingModel.setRecordMapping(recordMapping);
    }

    private void setRecordRootInternal(RecordRoot recordRoot) {
        checkSwingThread();
        this.recordRoot = recordRoot;
        List<AnalysisTree.Node> variables = new ArrayList<AnalysisTree.Node>();
        if (recordRoot != null) {
            AnalysisTree.setRecordRoot(analysisTreeModel, recordRoot.getRootQName());
            analysisTree.getVariables(variables);
            variableListModel.setVariableList(variables);
        }
        else {
            variableListModel.clear();
        }
        createMetadataParser();
        normalizeProgressModel.setValue(0);
        normalizeProgressModel.setMaximum(100);
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedRecordRoot(recordRoot);
        }
    }

    private void setStatisticsList(List<Statistics> statisticsList) {
        checkSwingThread();
        this.statisticsList = statisticsList;
        if (statisticsList != null) {
            analysisTree = AnalysisTree.create(statisticsList, fileSet.getName());
        }
        else {
            analysisTree = AnalysisTree.create("Analysis not yet performed");
        }
        analysisTreeModel.setRoot(analysisTree.getRoot());
        if (recordRoot != null) {
            AnalysisTree.setRecordRoot(analysisTreeModel, recordRoot.getRootQName());
        }
        statisticsTableModel.setCounterList(null);
    }

    private void createMetadataParser() {
        checkSwingThread();
        if (metadataParser != null) {
            metadataParser.close();
            metadataParser = null;
            for (ParseListener parseListener : parseListeners) {
                parseListener.updatedRecord(null);
            }
        }
        if (recordRoot != null) {
            executor.execute(new FirstRecordFetcher());
        }
    }

    private class FirstRecordFetcher implements Runnable {
        @Override
        public void run() {
            try {
                metadataParser = new MetadataParser(fileSet.getInputStream(), recordRoot, new MetadataParser.Listener() {
                    @Override
                    public void recordsParsed(int count) {
                        // todo: show this in the GUI associated with play/rewind?
                    }
                });
                metadataRecord = metadataParser.nextRecord();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (ParseListener parseListener : parseListeners) {
                            parseListener.updatedRecord(metadataRecord);
                        }
                    }
                });
            }
            catch (Exception e) {
                exceptionHandler.failure(e);
            }
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
                            for (ParseListener parseListener : parseListeners) {
                                parseListener.updatedRecord(metadataRecord);
                            }
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
                compilationComplete(out.toString());
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
