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

import eu.delving.core.metadata.AnalysisTree;
import eu.delving.core.metadata.FieldDefinition;
import eu.delving.core.metadata.FieldMapping;
import eu.delving.core.metadata.MappingModel;
import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.Path;
import eu.delving.core.metadata.RecordMapping;
import eu.delving.core.metadata.SourceVariable;
import eu.delving.core.metadata.Statistics;
import eu.europeana.sip.core.DataSetDetails;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCode;
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
//    private Logger log = Logger.getLogger(getClass());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MetadataModel metadataModel;
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
    private MappingModel mappingModel = new MappingModel();
    private DefaultBoundedRangeModel normalizeProgressModel = new DefaultBoundedRangeModel();
    private DefaultBoundedRangeModel zipProgressModel = new DefaultBoundedRangeModel();
    private DefaultBoundedRangeModel uploadProgressModel = new DefaultBoundedRangeModel();
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private DataSetDetails dataSetDetails;
    private List<UpdateListener> updateListeners = new CopyOnWriteArrayList<UpdateListener>();
    private List<ParseListener> parseListeners = new CopyOnWriteArrayList<ParseListener>();
    private Configuration configuration;
    private String serverUrl;

    public interface UpdateListener {

        void templateApplied();

        void updatedFileSet(FileSet fileSet);

        void updatedDetails(DataSetDetails dataSetDetails);

        void updatedRecordRoot(Path recordRoot, int recordCount);

        void normalizationMessage(boolean complete, String message);
    }

    public interface AnalysisListener {
        void finished(boolean success);

        void analysisProgress(long elementCount);
    }

    public interface ParseListener {
        void updatedRecord(MetadataRecord metadataRecord);
    }

    public SipModel(MetadataModel metadataModel, UserNotifier userNotifier, String serverUrl) {
        this.metadataModel = metadataModel;
        this.userNotifier = userNotifier;
        this.serverUrl = serverUrl;
        analysisTree = AnalysisTree.create("No Document Selected");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
        fieldListModel = new FieldListModel(metadataModel);
        ToolCode toolCode = new ToolCode();
        recordCompileModel = new CompileModel(metadataModel, toolCode);
        recordCompileModel.setRecordValidator(new RecordValidator(metadataModel, false));
        fieldCompileModel = new CompileModel(metadataModel, toolCode);
        parseListeners.add(recordCompileModel);
        parseListeners.add(fieldCompileModel);
        fieldMappingListModel = new FieldMappingListModel();
        mappingModel.addListener(fieldMappingListModel);
        mappingModel.addListener(recordCompileModel);
        mappingModel.addListener(fieldCompileModel);
        fieldCompileModel.addListener(new CompileModel.Listener() {

            @Override
            public void stateChanged(CompileModel.State state) {
                if (state == CompileModel.State.COMMITTED) {
                    executor.execute(new MappingSetter());
                }
            }
        });
        this.configuration = new Configuration();
    }

    public void addUpdateListener(UpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

    public void setServerAccessKey(String key) {
        configuration.setServerAccessKey(key);
    }

    public MetadataModel getMetadataModel() {
        return metadataModel;
    }

    public FileSet getFileSet() {
        return fileSet;
    }

    public MappingModel getMappingModel() {
        return mappingModel;
    }

    public RecordMapping _getRecordMapping() {
        return getMappingModel().getRecordMapping();
    }

    public Path getRecordRoot() {
        return _getRecordMapping().getRecordRoot();
    }

    public void tellUser(String message) {
        userNotifier.tellUser(message);
    }

    public void tellUser(String message, Exception e) {
        userNotifier.tellUser(message, e);
    }

    public String getServerAccessKey() {
        return configuration.getServerAccessKey();
    }

    public FileSet.Recent getRecentFileSets() {
        return configuration.getRecentFileSets();
    }

    public void setFileSet(final FileSet newFileSet) {
        checkSwingThread();
        this.fileSet = newFileSet;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<Statistics> statistics = newFileSet.getStatistics();
                final RecordMapping recordMapping = newFileSet.getMapping();
                final FileSet.Report report = newFileSet.getReport();
                final DataSetDetails dataSetDetails = newFileSet.getDataSetDetails();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setStatisticsList(statistics);
                        setDataSetDetails(dataSetDetails);
                        variableListModel.clear();
                        mappingModel.setRecordMapping(recordMapping);
                        if (getRecordRoot() != null) {
                            setRecordRootInternal(recordMapping.getRecordRoot(), recordMapping.getRecordCount());
                        }
                        createMetadataParser(1);
                        if (recordMapping != null && recordMapping.getRecordRoot() != null) {
                            normalizeProgressModel.setMaximum(recordMapping.getRecordCount());
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

//    public String getMappingTemplate() {
//        return recordCompileModel.getRecordMapping().getCodeForTemplate();
//    }
//
//    public void loadMappingTemplate(File file) {
//        if (recordCompileModel.getRecordMapping().fieldMappings == null) {
//            userNotifier.tellUser("Record must be empty to use a template.");
//        }
//        else {
//            try {
//                BufferedReader in = new BufferedReader(new FileReader(file));
//                StringBuilder out = new StringBuilder();
//                String line;
//                while ((line = in.readLine()) != null) {
//                    out.append(line).append('\n');
//                }
//                in.close();
//                String templateCode = out.toString();
//                RecordMapping recordMapping = recordCompileModel.getRecordMapping();
//                recordMapping.setCode(templateCode, fieldMap);
//                setRecordRootInternal(recordMapping.recordRoot);
//                recordMapping.getConstantFieldModel().clear();
//                createMetadataParser(1);
//                for (UpdateListener updateListener : updateListeners) {
//                    updateListener.templateApplied();
//                }
//            }
//            catch (IOException e) {
//                userNotifier.tellUser("Unable to load template", e);
//            }
//        }
//    }

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

    public String getServerUrl() {
        return serverUrl;
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

    public void normalize(boolean discardInvalid, boolean storeNormalizedFile) {
        checkSwingThread();
        abortNormalize();
        normalizeMessage("Normalizing and validating...");
        normalizer = new Normalizer(
                this,
                discardInvalid,
                storeNormalizedFile,
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
        final Normalizer existingNormalizer = normalizer;
        normalizer = null;
        if (existingNormalizer != null) {
            normalizeProgressModel.setValue(0);
            existingNormalizer.abort();
        }
    }

    public void createUploadZipFile() {
        checkSwingThread();
        String zipFileName = getDataSetDetails().getSpec();
        executor.execute(new ZipUploader(this, zipFileName));
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

    public void setUniqueElement(Path uniqueElement) {
        dataSetDetails.setUniqueElement(uniqueElement.toString());
        executor.execute(new DetailsSetter(dataSetDetails));
        AnalysisTree.setUniqueElement(analysisTreeModel, uniqueElement);
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedDetails(dataSetDetails);
        }
    }

    public void setRecordRoot(Path recordRoot, int recordCount) {
        checkSwingThread();
        setRecordRootInternal(recordRoot, recordCount);
        createMetadataParser(1);
        dataSetDetails.setRecordRoot(recordRoot.toString());
        executor.execute(new DetailsSetter(dataSetDetails));
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedDetails(dataSetDetails);
        }
        getMappingModel().setRecordRoot(recordRoot, recordCount);
        executor.execute(new MappingSetter());
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
        return fieldListModel.getUnmapped(getMappingModel());
    }

    public List<FieldDefinition> getUnmappedFields() {
        List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
        ListModel listModel = getUnmappedFieldListModel();
        for (int walkField = 0; walkField < listModel.getSize(); walkField++) {
            fields.add((FieldDefinition) listModel.getElementAt(walkField));
        }
        return fields;
    }

    public ListModel getVariablesListModel() {
        return variableListModel;
    }

    public List<SourceVariable> getVariables() {
        List<SourceVariable> list = new ArrayList<SourceVariable>();
        for (int walkVar = 0; walkVar < variableListModel.getSize(); walkVar++) {
            list.add((SourceVariable) variableListModel.getElementAt(walkVar));
        }
        return list;
    }

    public ListModel getVariablesListWithCountsModel() {
        return variableListModel.getWithCounts(getMappingModel());
    }

    public void addFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        getMappingModel().setMapping(fieldMapping.getFieldDefinition().path.toString(), fieldMapping);
        executor.execute(new MappingSetter());
    }

    public void removeFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        getMappingModel().setMapping(fieldMapping.getFieldDefinition().path.toString(), null);
        executor.execute(new MappingSetter());
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

    public CompileModel getRecordCompileModel() {
        return recordCompileModel;
    }

    public CompileModel getFieldCompileModel() {
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
                "Completed at %tT on %tY-%tm-%td with %d normalized, and %d discarded",
                report.getNormalizationDate(),
                report.getNormalizationDate(),
                report.getNormalizationDate(),
                report.getNormalizationDate(),
                report.getRecordsNormalized(),
                report.getRecordsDiscarded()
        );
        for (UpdateListener updateListener : updateListeners) {
            updateListener.normalizationMessage(true, message);
        }
    }

    private void setRecordRootInternal(Path recordRoot, int recordCount) {
        checkSwingThread();
        List<AnalysisTree.Node> variables = new ArrayList<AnalysisTree.Node>();
        normalizeProgressModel.setValue(0);
        if (recordRoot != null) {
            AnalysisTree.setRecordRoot(analysisTreeModel, recordRoot);
            analysisTree.getVariables(variables);
            variableListModel.setVariableList(variables);
            normalizeProgressModel.setMaximum(recordCount);
        }
        else {
            variableListModel.clear();
            normalizeProgressModel.setMaximum(100);
        }
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedRecordRoot(recordRoot, recordCount);
        }
    }

    private void setStatisticsList(List<Statistics> statisticsList) {
        checkSwingThread();
        this.statisticsList = statisticsList;
        if (statisticsList != null) {
            analysisTree = AnalysisTree.create(statisticsList);
        }
        else {
            analysisTree = AnalysisTree.create("Analysis not yet performed");
        }
        analysisTreeModel.setRoot(analysisTree.getRoot());
        if (getRecordRoot() != null) {
            AnalysisTree.setRecordRoot(analysisTreeModel, getRecordRoot());
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
        Path recordRoot = getRecordRoot();
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
            Path recordRoot = getRecordRoot();
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
        @Override
        public void run() {
            fileSet.setMapping(_getRecordMapping());
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
