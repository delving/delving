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
import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.DataSetDetails;
import eu.europeana.sip.core.FieldMapping;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordMapping;
import eu.europeana.sip.core.RecordRoot;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCodeModel;
import eu.europeana.sip.xml.AnalysisParser;
import eu.europeana.sip.xml.MetadataParser;
import eu.europeana.sip.xml.Normalizer;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
//    private Logger log = Logger.getLogger(getClass());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AnnotationProcessor annotationProcessor;
    private FileSet fileSet;
    private UserNotifier userNotifier;
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
    private DefaultBoundedRangeModel zipProgressModel = new DefaultBoundedRangeModel();
    private DefaultBoundedRangeModel uploadProgressModel = new DefaultBoundedRangeModel();
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private DataSetDetails dataSetDetails;
    private List<UpdateListener> updateListeners = new CopyOnWriteArrayList<UpdateListener>();
    private List<ParseListener> parseListeners = new CopyOnWriteArrayList<ParseListener>();
    private String dataSetControllerUrl;

    public interface UpdateListener {

        void templateApplied();

        void updatedFileSet(FileSet fileSet);

        void updatedDetails(DataSetDetails dataSetDetails);

        void updatedRecordRoot(RecordRoot recordRoot);

        void updatedConstantFieldModel(ConstantFieldModel constantFieldModel);

        void normalizationMessage(boolean complete, String message);
    }

    public interface AnalysisListener {
        void finished(boolean success);

        void analysisProgress(long elementCount);
    }

    public interface ParseListener {
        void updatedRecord(MetadataRecord metadataRecord);
    }

    public SipModel(AnnotationProcessor annotationProcessor, UserNotifier userNotifier, String dataSetControllerUrl) {
        this.annotationProcessor = annotationProcessor;
        this.userNotifier = userNotifier;
        this.dataSetControllerUrl = dataSetControllerUrl;
        analysisTree = AnalysisTree.create("No Document Selected");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
        fieldListModel = new FieldListModel(annotationProcessor);
        ToolCodeModel toolCodeModel = new ToolCodeModel();
        ConstantFieldModel constantFieldModel = new ConstantFieldModel(annotationProcessor, new ConstantFieldModel.Listener() {
            @Override
            public void updatedConstant() {
                recordCompileModel.compileSoon();
            }
        });
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
                final FileSet.Report report = newFileSet.getReport();
                final DataSetDetails dataSetDetails = newFileSet.getDataSetDetails();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setStatisticsList(statistics);
                        variableListModel.clear();
                        RecordMapping recordMapping = recordCompileModel.getRecordMapping();
                        recordMapping.setCode(mapping, europeanaFieldMap);
                        RecordRoot recordRoot = recordMapping.getRecordRoot();
                        setRecordRootInternal(recordRoot);
                        setDataSetDetails(dataSetDetails);
                        setGlobalFieldModelInternal(recordMapping.getConstantFieldModel());
                        fieldCompileModel.getRecordMapping().setConstantFieldModel(recordMapping.getConstantFieldModel());
                        createMetadataParser(1);
                        if (recordRoot != null) {
                            normalizeProgressModel.setMaximum(recordRoot.getRecordCount());
                            if (report != null) {
                                normalizeProgressModel.setValue(report.getRecordsNormalized() + report.getRecordsDiscarded());
                                normalizeMessage(report);
                            }
                            else {
                                normalizeProgressModel.setValue(0);
                                normalizeMessage("Normalization not yet performed.");
                            }
                        }
                        else {
                            normalizeProgressModel.setMaximum(100);
                            normalizeProgressModel.setValue(0);
                        }
                        uploadProgressModel.setMaximum(100);
                        uploadProgressModel.setValue(0);
                        for (UpdateListener updateListener : updateListeners) {
                            updateListener.updatedFileSet(newFileSet);
                            updateListener.updatedDetails(dataSetDetails);
                        }
                    }
                });
            }
        });
    }

    public String getMappingTemplate() {
        return recordCompileModel.getRecordMapping().getCodeForTemplate();
    }

    public void loadMappingTemplate(File file) {
        if (!recordCompileModel.getRecordMapping().isEmpty()) {
            userNotifier.tellUser("Record must be empty to use a template.");
        }
        else {
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    out.append(line).append('\n');
                }
                in.close();
                String templateCode = out.toString();
                RecordMapping recordMapping = recordCompileModel.getRecordMapping();
                recordMapping.setCode(templateCode, europeanaFieldMap);
                setRecordRootInternal(recordMapping.getRecordRoot());
                recordMapping.getConstantFieldModel().clear();
                createMetadataParser(1);
                for (UpdateListener updateListener : updateListeners) {
                    updateListener.templateApplied();
                }
            }
            catch (IOException e) {
                userNotifier.tellUser("Unable to load template", e);
            }
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
                userNotifier.tellUser("Analysis failed", exception);
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

    public DataSetDetails getDataSetDetails() {
        return dataSetDetails;
    }

    public String getDataSetControllerUrl() {
        return dataSetControllerUrl;
    }

    public void setDataSetDetails(DataSetDetails dataSetDetails) {
        this.dataSetDetails = dataSetDetails;
        executor.execute(new DetailsSetter(dataSetDetails));
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedDetails(dataSetDetails);
        }
    }

    public BoundedRangeModel getNormalizeProgress() {
        return normalizeProgressModel;
    }

    public DefaultBoundedRangeModel getUploadProgress() {
        return uploadProgressModel;
    }

    public DefaultBoundedRangeModel getZipProgress() {
        return zipProgressModel;
    }

    public void normalize(final boolean discardInvalid) {
        checkSwingThread();
        abortNormalize();
        normalizeMessage("Normalizing...");
        normalizer = new Normalizer(
                fileSet,
                annotationProcessor,
                new RecordValidator(annotationProcessor, true),
                discardInvalid,
                userNotifier,
                new MetadataParser.Listener() {
                    @Override
                    public void recordsParsed(final int count, final boolean lastRecord) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (lastRecord || count % 100 == 0) {
                                    normalizeProgressModel.setValue(count);
                                }
                            }
                        });
                    }
                },
                new Normalizer.Listener() {
                    @Override
                    public void invalidInput(final MappingException exception) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                createMetadataParser(exception.getMetadataRecord().getRecordNumber());
                            }
                        });
                    }

                    @Override
                    public void invalidOutput(final RecordValidationException exception) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                createMetadataParser(exception.getMetadataRecord().getRecordNumber());
                            }
                        });
                    }

                    @Override
                    public void finished(final boolean success) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    normalizeMessage(fileSet.getReport());
                                }
                                else {
                                    normalizeMessage("Normalization aborted");
                                }
                            }
                        });
                    }
                }
        );
        executor.execute(normalizer);
    }

    public void abortNormalize() {
        checkSwingThread();
        normalizeProgressModel.setValue(0);
        normalizeMessage("Normalization not yet performed.");
        final Normalizer existingNormalizer = normalizer;
        normalizer = null;
        if (existingNormalizer != null) {
            existingNormalizer.abort();
        }
    }

    public void createUploadZipFile() {
        checkSwingThread();
        String zipFileName = getDataSetDetails().getSpec();
        executor.execute(new ZipUploader(dataSetControllerUrl, fileSet, zipFileName, zipProgressModel, uploadProgressModel, userNotifier));
    }

    public TreeModel getAnalysisTreeModel() {
        return analysisTreeModel;
    }

    public void selectNode(AnalysisTree.Node node) {
        checkSwingThread();
        if (node != null && node.getStatistics() != null) {
            List<? extends Statistics.Counter> counters = node.getStatistics().getCounters();
            statisticsTableModel.setCounterList(counters);
        }
    }

    public void setUniqueElement(QName uniqueElement) {
        dataSetDetails.setUniqueElement(uniqueElement.toString());
        executor.execute(new DetailsSetter(dataSetDetails));
        AnalysisTree.setUniqueElement(analysisTreeModel, uniqueElement);
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedDetails(dataSetDetails);
        }
    }

    public void setRecordRoot(RecordRoot recordRoot) {
        checkSwingThread();
        setRecordRootInternal(recordRoot);
        createMetadataParser(1);
        dataSetDetails.setRecordRoot(recordRoot.getRootQName().toString());
        executor.execute(new DetailsSetter(dataSetDetails));
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedDetails(dataSetDetails);
        }
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

    public List<EuropeanaField> getUnmappedFields() {
        List<EuropeanaField> fields = new ArrayList<EuropeanaField>();
        ListModel listModel = getUnmappedFieldListModel();
        for (int walkField = 0; walkField < listModel.getSize(); walkField++) {
            fields.add((EuropeanaField) listModel.getElementAt(walkField));
        }
        return fields;
    }

    public ListModel getVariablesListModel() {
        return variableListModel;
    }

    public List<VariableHolder> getVariables() {
        List<VariableHolder> list = new ArrayList<VariableHolder>();
        for (int walkVar = 0; walkVar < variableListModel.getSize(); walkVar++) {
            list.add((VariableHolder) variableListModel.getElementAt(walkVar));
        }
        return list;
    }

    public ListModel getVariablesListWithCountsModel() {
        return variableListModel.getWithCounts(recordCompileModel.getRecordMapping());
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
        createMetadataParser(1);
    }

    public void nextRecord() {
        checkSwingThread();
        executor.execute(new RecordFetcher(1));
    }

    public CompileModel getRecordMappingModel() {
        return recordCompileModel;
    }

    public CompileModel getFieldMappingModel() {
        return fieldCompileModel;
    }

    // === privates

    private void normalizeMessage(String message) {
        for (UpdateListener updateListener : updateListeners) {
            updateListener.normalizationMessage(false, message);
        }
    }

    private void normalizeMessage(FileSet.Report report) {
        String message = String.format(
                "Normalization completed on %s resulted in %d normalized records and %d discarded.",
                report.getNormalizationDate().toString(),
                report.getRecordsNormalized(),
                report.getRecordsDiscarded()
        );
        for (UpdateListener updateListener : updateListeners) {
            updateListener.normalizationMessage(true, message);
        }
    }

    private void setGlobalFieldModelInternal(ConstantFieldModel constantFieldModel) {
        checkSwingThread();
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedConstantFieldModel(constantFieldModel);
        }
    }

    private void setRecordRootInternal(RecordRoot recordRoot) {
        checkSwingThread();
        List<AnalysisTree.Node> variables = new ArrayList<AnalysisTree.Node>();
        normalizeProgressModel.setValue(0);
        if (recordRoot != null) {
            AnalysisTree.setRecordRoot(analysisTreeModel, recordRoot.getRootQName());
            analysisTree.getVariables(variables);
            variableListModel.setVariableList(variables);
            normalizeProgressModel.setMaximum(recordRoot.getRecordCount());
        }
        else {
            variableListModel.clear();
            normalizeProgressModel.setMaximum(100);
        }
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

    private void createMetadataParser(int recordNumber) {
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
            executor.execute(new RecordFetcher(recordNumber));
        }
    }

    private class RecordFetcher implements Runnable {
        private int recordNumber;

        private RecordFetcher(int recordNumber) {
            this.recordNumber = recordNumber;
        }

        @Override
        public void run() {
            RecordRoot recordRoot = recordCompileModel.getRecordMapping().getRecordRoot();
            if (recordRoot == null) {
                return;
            }
            try {
                if (metadataParser == null) {
                    metadataParser = new MetadataParser(fileSet.getInputStream(), recordRoot, null);
                }
                while (recordNumber-- > 0) {
                    metadataRecord = metadataParser.nextRecord();
                }
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
                userNotifier.tellUser("Unable to fetch the next record", e);
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

    private class DetailsSetter implements Runnable {
        private DataSetDetails details;

        private DetailsSetter(DataSetDetails details) {
            this.details = details;
        }

        @Override
        public void run() {
            fileSet.setDataSetDetails(details);
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
