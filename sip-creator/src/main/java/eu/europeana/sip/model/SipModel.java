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

import eu.delving.metadata.AnalysisTree;
import eu.delving.metadata.FieldDefinition;
import eu.delving.metadata.FieldMapping;
import eu.delving.metadata.MappingModel;
import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.Path;
import eu.delving.metadata.RecordMapping;
import eu.delving.metadata.SourceDetails;
import eu.delving.metadata.SourceVariable;
import eu.delving.metadata.Statistics;
import eu.delving.sip.AppConfig;
import eu.delving.sip.FileStore;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.core.MappingException;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.core.RecordValidator;
import eu.europeana.sip.core.ToolCodeResource;
import eu.europeana.sip.xml.AnalysisParser;
import eu.europeana.sip.xml.MetadataParser;
import eu.europeana.sip.xml.Normalizer;
import org.apache.log4j.Logger;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This model is behind the whole sip creator, as a facade for all the models related to a data set
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class SipModel {
    private Logger log = Logger.getLogger(getClass());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FileStore fileStore;
    private MetadataModel metadataModel;
    private AppConfig appConfig;
    private FileStore.DataSetStore dataSetStore;
    private SourceDetails sourceDetails;
    private UserNotifier userNotifier;
    private List<Statistics> statisticsList;
    private AnalysisParser analysisParser;
    private AnalysisTree analysisTree;
    private DefaultTreeModel analysisTreeModel;
    private FieldListModel fieldListModel;
    private CompileModel recordCompileModel;
    private CompileModel fieldCompileModel;
    private MetadataParser metadataParser;
    private MetadataRecord metadataRecord;
    private ConstantFieldModel constantFieldModel = new ConstantFieldModel();
    private FieldMappingListModel fieldMappingListModel;
    private MappingModel mappingModel = new MappingModel();
    private MappingSaveTimer mappingSaveTimer = new MappingSaveTimer();
    private VariableListModel variableListModel = new VariableListModel();
    private StatisticsTableModel statisticsTableModel = new StatisticsTableModel();
    private List<UpdateListener> updateListeners = new CopyOnWriteArrayList<UpdateListener>();
    private List<ParseListener> parseListeners = new CopyOnWriteArrayList<ParseListener>();
    private String serverUrl;

    public interface UpdateListener {

        void templateApplied();

        void updatedDataSetStore(FileStore.DataSetStore dataSetStore);

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

    public SipModel(FileStore fileStore, MetadataModel metadataModel, UserNotifier userNotifier, String serverUrl) throws FileStoreException {
        this.fileStore = fileStore;
        this.appConfig = fileStore.getAppConfig();
        this.metadataModel = metadataModel;
        this.userNotifier = userNotifier;
        this.serverUrl = serverUrl;
        analysisTree = AnalysisTree.create("Select a Data Set from the File menu");
        analysisTreeModel = new DefaultTreeModel(analysisTree.getRoot());
        fieldListModel = new FieldListModel(metadataModel);
        ToolCodeResource toolCodeResource = new ToolCodeResource();
        recordCompileModel = new CompileModel(CompileModel.Type.RECORD, metadataModel, toolCodeResource);
        recordCompileModel.setRecordValidator(new RecordValidator(metadataModel, false));
        fieldCompileModel = new CompileModel(CompileModel.Type.FIELD, metadataModel, toolCodeResource);
        parseListeners.add(recordCompileModel);
        parseListeners.add(fieldCompileModel);
        fieldMappingListModel = new FieldMappingListModel();
        constantFieldModel.setRecordDefinition(metadataModel.getRecordDefinition());
        constantFieldModel.addListener(new ConstantFieldModelAdapter());
        mappingModel.addListener(fieldMappingListModel);
        mappingModel.addListener(recordCompileModel);
        mappingModel.addListener(fieldCompileModel);
        mappingModel.addListener(mappingSaveTimer);
        fieldCompileModel.addListener(new CompileModel.Listener() {
            @Override
            public void stateChanged(CompileModel.State state) {
                if (state == CompileModel.State.COMMITTED) {
                    mappingSaveTimer.mappingChanged(null);
                }
            }
        });
    }

    public void addUpdateListener(UpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

    public FileStore getFileStore() {
        return fileStore;
    }

    public void createDataSetStore(final FileStore.DataSetStore dataSetStore, final File file, final ProgressListener progressListener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dataSetStore.importFile(file, progressListener);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setDataSetStore(dataSetStore);
                        }
                    });
                }
                catch (FileStoreException e) {
                    userNotifier.tellUser("Couldn't create Data Set from " + file.getAbsolutePath(), e);
                }
            }
        });
    }

    public ConstantFieldModel getConstantFieldModel() {
        return constantFieldModel;
    }

    public String getServerAccessKey() {
        return appConfig.getAccessKey();
    }

    public void setServerAccessKey(String key) {
        appConfig.setAccessKey(key);
        executor.execute(new AppConfigSetter());
    }

    public String getRecentDirectory() {
        return appConfig.getRecentDirectory();
    }

    public void setRecentDirectory(File directory) {
        if (!directory.isDirectory()) {
            directory = directory.getParentFile();
        }
        appConfig.setRecentDirectory(directory.getAbsolutePath());
        executor.execute(new AppConfigSetter());
    }

    public FileStore.DataSetStore getDataSetStore() {
        return dataSetStore;
    }

    public MetadataModel getMetadataModel() {
        return metadataModel;
    }

    public MappingModel getMappingModel() {
        return mappingModel;
    }

    public RecordMapping getRecordMapping() {
        return getMappingModel().getRecordMapping();
    }

    public void tellUser(String message) {
        userNotifier.tellUser(message);
    }

    public void tellUser(String message, Exception e) {
        userNotifier.tellUser(message, e);
    }

    public void setDataSetStore(final FileStore.DataSetStore dataSetStore) {
        checkSwingThread();
        this.dataSetStore = dataSetStore;
        if (dataSetStore != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final List<Statistics> statistics = dataSetStore.getStatistics();
                        final RecordMapping recordMapping = dataSetStore.getRecordMapping(metadataModel.getRecordDefinition());
                        final SourceDetails sourceDetails = dataSetStore.getSourceDetails();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                SipModel.this.sourceDetails = sourceDetails;
                                constantFieldModel.clear();
                                constantFieldModel.setSourceDetails(sourceDetails);
                                constantFieldModel.setRecordMapping(recordMapping);
                                setStatisticsList(statistics);
                                variableListModel.clear();
                                mappingModel.setRecordMapping(recordMapping);
                                if (getRecordRoot() != null) {
                                    setRecordRootInternal(new Path(sourceDetails.get(SourceDetails.RECORD_PATH)), Integer.parseInt(sourceDetails.get(SourceDetails.RECORD_COUNT)));
                                }
                                AnalysisTree.setUniqueElement(analysisTreeModel, getUniqueElement());
                                createMetadataParser(1);
                                if (recordMapping != null) {
                                    if (recordMapping.getNormalizeTime() == 0) {
                                        normalizeMessage(false, "Normalization not yet performed.");
                                    }
                                    else {
                                        normalizeMessage(recordMapping);
                                    }
                                }
                            }
                        });
                    }
                    catch (FileStoreException e) {
                        tellUser("Unable to select Data Set " + dataSetStore, e);
                    }
                }
            });
        }
        for (UpdateListener updateListener : updateListeners) {
            updateListener.updatedDataSetStore(this.dataSetStore);
        }
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
        this.analysisParser = new AnalysisParser(dataSetStore, new AnalysisParser.Listener() {

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

    public String getServerUrl() {
        return serverUrl;
    }

    public SourceDetails getSourceDetails() {
        return sourceDetails;
    }

    public void normalize(boolean discardInvalid, boolean storeNormalizedFile, ProgressListener progressListener) {
        checkSwingThread();
        File normalizedFile = null;
        if (storeNormalizedFile) {
            // todo: file chooser
        }
        normalizeMessage(false, "Normalizing and validating...");
        executor.execute(new Normalizer(
                this,
                getRecordRoot(),
                getRecordCount(),
                discardInvalid,
                normalizedFile,
                progressListener,
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
                                    normalizeMessage(getRecordMapping());
                                }
                                else {
                                    normalizeMessage(false, "Normalization aborted");
                                }
                            }
                        });
                    }
                }
        ));
    }

    public void uploadFile(File file, ProgressListener progressListener) {
        checkSwingThread();
        executor.execute(new FileUploader(file, serverUrl, getServerAccessKey(), userNotifier, progressListener));
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

    public Path getUniqueElement() {
        if (sourceDetails == null || sourceDetails.get(SourceDetails.UNIQUE_ELEMENT_PATH).isEmpty()) {
            return null;
        }
        return new Path(getSourceDetails().get(SourceDetails.UNIQUE_ELEMENT_PATH));
    }

    public void setUniqueElement(Path uniqueElement) {
        sourceDetails.set(SourceDetails.UNIQUE_ELEMENT_PATH, uniqueElement.toString());
        constantFieldModel.setSourceDetails(sourceDetails);
        executor.execute(new SourceDetailsSetter(sourceDetails));
        AnalysisTree.setUniqueElement(analysisTreeModel, uniqueElement);
    }

    public Path getRecordRoot() {
        if (sourceDetails == null || sourceDetails.get(SourceDetails.RECORD_PATH).isEmpty()) {
            return null;
        }
        return new Path(getSourceDetails().get(SourceDetails.RECORD_PATH));
    }

    public int getRecordCount() {
        if (sourceDetails == null || sourceDetails.get(SourceDetails.RECORD_COUNT).isEmpty()) {
            return 0;
        }
        return Integer.parseInt(getSourceDetails().get(SourceDetails.RECORD_COUNT));
    }

    public void setRecordRoot(Path recordRoot, int recordCount) {
        checkSwingThread();
        setRecordRootInternal(recordRoot, recordCount);
        createMetadataParser(1);
        sourceDetails.set(SourceDetails.RECORD_PATH, recordRoot.toString());
        sourceDetails.set(SourceDetails.RECORD_COUNT, String.valueOf(recordCount));
        constantFieldModel.setSourceDetails(sourceDetails);
        executor.execute(new SourceDetailsSetter(sourceDetails));
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
    }

    public void removeFieldMapping(FieldMapping fieldMapping) {
        checkSwingThread();
        getMappingModel().setMapping(fieldMapping.getFieldDefinition().path.toString(), null);
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

    private void normalizeMessage(boolean complete, String message) {
        for (UpdateListener updateListener : updateListeners) {
            updateListener.normalizationMessage(complete, message);
        }
    }

    private void normalizeMessage(RecordMapping recordMapping) {
        Date date = new Date(recordMapping.getNormalizeTime());
        String message = String.format(
                "Completed at %tT on %tY-%tm-%td with %d normalized, and %d discarded",
                date, date, date, date,
                recordMapping.getRecordsNormalized(),
                recordMapping.getRecordsDiscarded()
        );
        normalizeMessage(true, message);
    }

    private void setRecordRootInternal(Path recordRoot, int recordCount) {
        checkSwingThread();
        List<AnalysisTree.Node> variables = new ArrayList<AnalysisTree.Node>();
        if (recordRoot != null) {
            AnalysisTree.setRecordRoot(analysisTreeModel, recordRoot);
            analysisTree.getVariables(variables);
            variableListModel.setVariableList(variables);
        }
        else {
            variableListModel.clear();
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
                    metadataParser = new MetadataParser(dataSetStore.createXmlInputStream(), recordRoot, getRecordCount(), null);
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
                metadataParser = null;
            }
        }
    }

    private class MappingSaveTimer implements MappingModel.Listener, ActionListener, Runnable {
        private Timer timer = new Timer(200, this);

        private MappingSaveTimer() {
            timer.setRepeats(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            executor.execute(this);
        }

        @Override
        public void run() {
            try {
                dataSetStore.setRecordMapping(getRecordMapping());
                log.info("Mapping saved!");
            }
            catch (FileStoreException e) {
                userNotifier.tellUser("Unable to save mapping", e);
            }
        }

        @Override
        public void mappingChanged(RecordMapping recordMapping) {
            log.info("Mapping changed");
            timer.restart();
        }
    }

    private class AppConfigSetter implements Runnable {
        @Override
        public void run() {
            try {
                fileStore.setAppConfig(appConfig);
            }
            catch (FileStoreException e) {
                userNotifier.tellUser("Unable to save application configuration", e);
            }
        }
    }

    private class ConstantFieldModelAdapter implements ConstantFieldModel.Listener {
        @Override
        public void updatedDefinitions(ConstantFieldModel constantFieldModel) {
            // do nothing
        }

        @Override
        public void updatedConstant(ConstantFieldModel constantFieldModel, boolean interactive) {
            if (interactive) {
                if (constantFieldModel.fillRecordMapping(getRecordMapping())) {
                    mappingSaveTimer.mappingChanged(getRecordMapping());
                }
                if (constantFieldModel.fillSourceDetails(sourceDetails)) {
                    executor.execute(new SourceDetailsSetter(sourceDetails));
                }
            }
        }
    }

    private class SourceDetailsSetter implements Runnable {
        private SourceDetails details;

        private SourceDetailsSetter(SourceDetails details) {
            this.details = details;
        }

        @Override
        public void run() {
            try {
                dataSetStore.setSourceDetails(details);
            }
            catch (FileStoreException e) {
                userNotifier.tellUser("Unable to save source details", e);
            }
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
