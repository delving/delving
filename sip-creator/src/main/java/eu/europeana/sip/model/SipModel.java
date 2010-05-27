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
import eu.europeana.definitions.annotations.EuropeanaField;
import eu.europeana.sip.groovy.FieldMapping;
import eu.europeana.sip.groovy.RecordMapping;
import eu.europeana.sip.xml.AnalysisParser;
import eu.europeana.sip.xml.MetadataParser;
import eu.europeana.sip.xml.MetadataRecord;
import eu.europeana.sip.xml.Normalizer;
import eu.europeana.sip.xml.RecordValidator;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    private AnnotationProcessor annotationProcessor;
    private FileSet fileSet;
    private ExceptionHandler exceptionHandler;
    private List<Statistics> statisticsList;
    private AnalysisParser analysisParser;
    private Normalizer normalizer;
    private AnalysisTree analysisTree;
    private DefaultTreeModel analysisTreeModel;
    private FieldListModel fieldListModel;
    private CompileModel recordCompileModel;
    private CompileModel fieldCompileModel;
    private MetadataParser metadataParser;
    private MetadataRecord metadataRecord;
    private FieldMappingListModel fieldMappingListModel;
    private Map<String, EuropeanaField> europeanaFieldMap = new TreeMap<String, EuropeanaField>();
    private DefaultBoundedRangeModel normalizeProgressModel = new DefaultBoundedRangeModel();
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private List<UpdateListener> updateListeners = new CopyOnWriteArrayList<UpdateListener>();
    private List<ParseListener> parseListeners = new CopyOnWriteArrayList<ParseListener>();

    public interface UpdateListener {
        void updatedFileSet(FileSet fileSet);

        void updatedRecordRoot(RecordRoot recordRoot);

        void updatedConstantFieldModel(ConstantFieldModel constantFieldModel);
    }

    public interface AnalysisListener {
        void finished(boolean success);

        void analysisProgress(long elementCount);
    }

    public interface ParseListener {
        void updatedRecord(MetadataRecord metadataRecord);
    }

    public SipModel(AnnotationProcessor annotationProcessor, ExceptionHandler exceptionHandler) {
        this.annotationProcessor = annotationProcessor;
        this.exceptionHandler = exceptionHandler;
        analysisTree = AnalysisTree.create("No Document Selected");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
        fieldListModel = new FieldListModel(annotationProcessor);
        ConstantFieldModel constantFieldModel = new ConstantFieldModel(annotationProcessor, new ConstantFieldModel.Listener() {
            @Override
            public void updatedConstant() {
                recordCompileModel.compileSoon();
            }
        });
        ToolCodeModel toolCodeModel = new ToolCodeModel();
        recordCompileModel = new CompileModel(toolCodeModel, constantFieldModel, new RecordValidator(annotationProcessor, false));
        fieldCompileModel = new CompileModel(toolCodeModel, constantFieldModel);
        parseListeners.add(recordCompileModel);
        parseListeners.add(fieldCompileModel);
        fieldMappingListModel = new FieldMappingListModel(recordCompileModel.getRecordMapping());
        for (EuropeanaField field : annotationProcessor.getMappableFields()) {
            europeanaFieldMap.put(field.getFieldNameString(), field);
        }
        fieldCompileModel.addListener(new CompileModel.Listener() {

            @Override
            public void stateChanged(CompileModel.State state) {
                if (state == CompileModel.State.COMMITTED) {
                    String code = recordCompileModel.getRecordMapping().getCodeForPersistence();
                    executor.execute(new MappingSetter(code));
                }
            }
        });
    }

    public void addUpdateListener(UpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

    public void setFileSet(final FileSet newFileSet) {
        checkSwingThread();
        this.fileSet = newFileSet;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Statistics> statistics = newFileSet.getStatistics();
                final String mapping = newFileSet.getMapping();
                final boolean outputFilePresent = newFileSet.hasOutputFile();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setStatisticsList(statistics);
                        RecordMapping recordMapping = recordCompileModel.getRecordMapping();
                        recordMapping.setCode(mapping, europeanaFieldMap);
                        RecordRoot recordRoot = recordMapping.getRecordRoot();
                        setRecordRootInternal(recordRoot);
                        setGlobalFieldModelInternal(recordMapping.getConstantFieldModel());
                        fieldCompileModel.getRecordMapping().setConstantFieldModel(recordMapping.getConstantFieldModel());
                        createMetadataParser();
                        if (recordRoot != null) {
                            normalizeProgressModel.setMaximum(recordRoot.getRecordCount());
                            normalizeProgressModel.setValue(outputFilePresent ? recordRoot.getRecordCount() : 0);
                        }
                        else {
                            normalizeProgressModel.setMaximum(100);
                            normalizeProgressModel.setValue(0);
                        }
                        for (UpdateListener updateListener : updateListeners) {
                            updateListener.updatedFileSet(newFileSet);
                        }
                    }
                });
            }
        });
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
        normalizer = new Normalizer(
                fileSet,
                annotationProcessor,
                new RecordValidator(annotationProcessor, true),
                exceptionHandler,
                new MetadataParser.Listener() {
                    @Override
                    public void recordsParsed(final int count) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                normalizeProgressModel.setValue(count);
                            }
                        });
                    }
                }
        );
        executor.execute(normalizer);
    }

    public void abortNormalize() {
        checkSwingThread();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (normalizer != null) {
                    normalizer.abort();
                    normalizer = null;
                }
                if (fileSet.hasOutputFile()) {
                    fileSet.removeOutputFile();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            normalizeProgressModel.setValue(0);
                        }
                    });
                }
            }
        });
    }

    public TreeModel getAnalysisTreeModel() {
        return analysisTreeModel;
    }

    public void selectNode(AnalysisTree.Node node) {
        checkSwingThread();
        if (node != null && node.getStatistics() != null) {
            List<? extends Statistics.Counter> counters = node.getStatistics().getCounters();
            if (!counters.isEmpty()) {
                statisticsTableModel.setCounterList(counters);
            }
            else {
                statisticsTableModel.setCounterList(node.getStatistics().getTotalAsCounter());
            }
        }
    }

    public void setRecordRoot(RecordRoot recordRoot) {
        checkSwingThread();
        setRecordRootInternal(recordRoot);
        createMetadataParser();
        recordCompileModel.getRecordMapping().setRecordRoot(recordRoot);
        String code = recordCompileModel.getRecordMapping().getCodeForPersistence();
        executor.execute(new MappingSetter(code));
    }

    public void setGlobalField(String fieldName, String value) {
        recordCompileModel.getRecordMapping().getConstantFieldModel().set(fieldName, value);
        String code = recordCompileModel.getRecordMapping().getCodeForPersistence();
        executor.execute(new MappingSetter(code));
    }

    public ConstantFieldModel getGlobalFieldModel() {
        return recordCompileModel.getRecordMapping().getConstantFieldModel();
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
        return fieldListModel.getUnmapped(recordCompileModel.getRecordMapping());
    }

    public ListModel getVariablesListModel() {
        return variableListModel;
    }

    public void addFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        recordCompileModel.getRecordMapping().add(fieldMapping);
        String code = recordCompileModel.getRecordMapping().getCodeForPersistence();
        executor.execute(new MappingSetter(code));
    }

    public void removeFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        recordCompileModel.getRecordMapping().remove(fieldMapping);
        String code = recordCompileModel.getRecordMapping().getCodeForPersistence();
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

    public CompileModel getRecordMappingModel() {
        return recordCompileModel;
    }

    public CompileModel getFieldMappingModel() {
        return fieldCompileModel;
    }

    // === privates

    private void setGlobalFieldModelInternal(ConstantFieldModel constantFieldModel) {
        checkSwingThread();
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedConstantFieldModel(constantFieldModel);
        }
    }

    private void setRecordRootInternal(RecordRoot recordRoot) {
        checkSwingThread();
        List<AnalysisTree.Node> variables = new ArrayList<AnalysisTree.Node>();
        if (recordRoot != null) {
            AnalysisTree.setRecordRoot(analysisTreeModel, recordRoot.getRootQName());
            analysisTree.getVariables(variables);
            variableListModel.setVariableList(variables);
        }
        else {
            variableListModel.clear();
        }
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
        RecordRoot recordRoot = recordCompileModel.getRecordMapping().getRecordRoot();
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
        RecordRoot recordRoot = recordCompileModel.getRecordMapping().getRecordRoot();
        if (recordRoot != null) {
            executor.execute(new NextRecordFetcher());
        }
    }

    private class NextRecordFetcher implements Runnable {
        @Override
        public void run() {
            RecordRoot recordRoot = recordCompileModel.getRecordMapping().getRecordRoot();
            if (recordRoot == null) {
                return;
            }
            try {
                if (metadataParser == null) {
                    metadataParser = new MetadataParser(fileSet.getInputStream(), recordRoot, new MetadataParser.Listener() {
                        @Override
                        public void recordsParsed(int count) {
                            // todo: show this in the GUI associated with play/rewind?
                        }
                    });
                }
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

    private static void checkWorkerThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Worker thread");
        }
    }

    private static void checkSwingThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Expected Swing thread");
        }
    }
}
