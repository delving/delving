/*
 * Copyright 2010 DELVING BV
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

package eu.europeana.sip.gui;

import eu.delving.sip.DataSetCommand;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetState;
import eu.delving.sip.FileStore;
import eu.delving.sip.FileType;
import eu.delving.sip.Hasher;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.model.SipModel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * All the actions that can be launched when a data set is selected
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetActions {
    private static final Dimension SIZE = new Dimension(1024 - 60, 768 - 60);
    private JFrame frame;
    private RecordStatisticsDialog recordStatisticsDialog;
    private AnalysisFactsDialog analysisFactsDialog;
    private MappingDialog mappingDialog;
    private SipModel sipModel;
    private DataSetClient dataSetClient;
    private DataSetListModel.Entry entry;
    private List<DataSetAction> localActions = new ArrayList<DataSetAction>();
    private List<DataSetAction> remoteActions = new ArrayList<DataSetAction>();
    private List<DataSetAction> actions = new ArrayList<DataSetAction>();

    public DataSetActions(JFrame frame, SipModel sipModel, DataSetClient dataSetClient) {
        this.frame = frame;
        this.sipModel = sipModel;
        this.dataSetClient = dataSetClient;
        this.recordStatisticsDialog = new RecordStatisticsDialog(sipModel);
        this.analysisFactsDialog = new AnalysisFactsDialog(sipModel);
        this.mappingDialog = new MappingDialog(sipModel);
        createLocalActions(sipModel);
        createRemoteActions();
        actions.addAll(localActions);
        actions.addAll(remoteActions);
    }

    private void createRemoteActions() {
        remoteActions.add(createUploadFactsAction());
        remoteActions.add(createUploadSourceAction());
        for (String prefix : sipModel.getMetadataModel().getPrefixes()) {
            remoteActions.add(createUploadMappingAction(prefix));
        }
        for (DataSetCommand command : DataSetCommand.values()) {
            remoteActions.add(createCommandAction(command));
        }
    }

    private void createLocalActions(SipModel sipModel) {
        localActions.add(createAnalyzeFactsAction());
        for (String metadataPrefix : sipModel.getMetadataModel().getPrefixes()) {
            localActions.add(createEditMappingAction(metadataPrefix));
        }
        localActions.add(createRecordStatisticsAction());
    }

    public List<Action> getLocalActions() {
        return new ArrayList<Action>(localActions);
    }

    public List<Action> getRemoteActions() {
        return new ArrayList<Action>(remoteActions);
    }

    public void setEntry(DataSetListModel.Entry entry) {
        this.entry = entry;
        for (DataSetAction dataSetAction : actions) {
            dataSetAction.setEntry(entry);
        }
    }

    public void setDataSetInfo(DataSetInfo dataSetInfo) {
        if (entry != null && entry.getDataSetInfo() != null && entry.getDataSetInfo().spec.equals(dataSetInfo.spec)) {
            for (DataSetAction dataSetAction : actions) {
                dataSetAction.setEntry(entry);
            }
        }
    }

    abstract class DataSetAction extends AbstractAction {

        protected DataSetAction(String s) {
            super(s);
        }

        void setEntry(DataSetListModel.Entry entry) {
            if (entry == null) {
                setEnabled(false);
            }
            else {
                setEnabled(isEnabled(entry));
            }
        }

        abstract boolean isEnabled(DataSetListModel.Entry entry);
    }

    private DataSetAction createRecordStatisticsAction() {
        return new DataSetAction("Gather Record Statistics") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sipModel.setDataSetStore(entry.getDataSetStore());
                recordStatisticsDialog.reveal();
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                return entry.getDataSetStore() != null &&
                        entry.getDataSetStore().getFacts().isValid();
            }
        };
    }

    private DataSetAction createAnalyzeFactsAction() {
        return new DataSetAction("Edit Analysis & Facts") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sipModel.setDataSetStore(entry.getDataSetStore());
                analysisFactsDialog.reveal();
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                return entry.getDataSetStore() != null;
            }
        };
    }

    private DataSetAction createEditMappingAction(final String metadataPrefix) {
        return new DataSetAction(String.format("Edit '%s' Mapping", metadataPrefix)) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sipModel.setDataSetStore(entry.getDataSetStore());
                sipModel.setMetadataPrefix(metadataPrefix);
                mappingDialog.reveal(metadataPrefix);
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                return entry.getDataSetStore() != null;
            }
        };
    }

    private boolean canUpload(File file, DataSetListModel.Entry entry) {
        if (entry.getDataSetStore() == null || entry.getDataSetInfo() == null || file == null) {
            return false;
        }
        String hash = Hasher.getHash(file.getName());
        return !entry.getDataSetInfo().hasHash(hash);
    }

    private DataSetAction createUploadFactsAction() {
        return new DataSetAction("Upload Facts") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileStore.DataSetStore store = entry.getDataSetStore();
                ProgressMonitor progressMonitor = new ProgressMonitor(frame, "Uploading", String.format("Uploading facts for %s", store.getSpec()), 0, 100);
                final ProgressListener progressListener = new ProgressListener.Adapter(progressMonitor) {
                    @Override
                    public void swingFinished(boolean success) {
                        setEnabled(!success);
                    }
                };
                dataSetClient.uploadFile(FileType.FACTS, store.getFactsFile(), progressListener);
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                FileStore.DataSetStore store = entry.getDataSetStore();
                return !(store == null || !store.getFacts().isValid()) && canUpload(entry.getDataSetStore().getFactsFile(), entry);
            }

        };
    }

    private DataSetAction createUploadSourceAction() {
        return new DataSetAction("Upload Source") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final FileStore.DataSetStore store = entry.getDataSetStore();
                ProgressMonitor progressMonitor = new ProgressMonitor(frame, "Uploading", String.format("Uploading source for %s", store.getSpec()), 0, 100);
                final ProgressListener progressListener = new ProgressListener.Adapter(progressMonitor) {
                    @Override
                    public void swingFinished(boolean success) {
                        setEnabled(!success);
                    }
                };
                dataSetClient.uploadFile(FileType.SOURCE, store.getSourceFile(), progressListener);
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                return canUpload(entry.getDataSetStore().getSourceFile(), entry);
            }

        };
    }

    private DataSetAction createUploadMappingAction(final String prefix) {
        return new DataSetAction(String.format("Upload %s Mapping", prefix)) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final FileStore.DataSetStore store = entry.getDataSetStore();
                ProgressMonitor progressMonitor = new ProgressMonitor(frame, "Uploading", String.format("Uploading %s mapping for %s ", prefix, store.getSpec()), 0, 100);
                final ProgressListener progressListener = new ProgressListener.Adapter(progressMonitor) {
                    @Override
                    public void swingFinished(boolean success) {
                        setEnabled(!success);
                    }
                };
                dataSetClient.uploadFile(FileType.MAPPING, store.getMappingFile(prefix), progressListener);
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                return canUpload(entry.getDataSetStore().getMappingFile(prefix), entry);
            }

        };
    }

    private DataSetAction createCommandAction(final DataSetCommand command) {
        return new DataSetAction(getCommandName(command)) {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dataSetClient.sendCommand(entry.getDataSetInfo().spec, command);
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                DataSetInfo info = entry.getDataSetInfo();
                if (info == null) {
                    return false;
                }
                else switch (DataSetState.valueOf(info.state)) {
                    case EMPTY:
                        return false;
                    case UPLOADED:
                        switch (command) {
                            case INDEX:
                                return true;
                            default:
                                return false;
                        }
                    case QUEUED:
                    case INDEXING:
                        switch (command) {
                            case DISABLE:
                                return true;
                            default:
                                return false;
                        }
                    case ENABLED:
                        switch (command) {
                            case DISABLE:
                            case REINDEX:
                                return true;
                            default:
                                return false;
                        }
                    case DISABLED:
                        switch (command) {
                            case INDEX:
                                return true;
                            default:
                                return false;
                        }
                    case ERROR:
                        switch (command) {
                            case DISABLE:
                                return true;
                            default:
                                return false;
                        }
                    default:
                        throw new RuntimeException();
                }
            }
        };
    }

    private String getCommandName(DataSetCommand command) {
        String name;
        switch (command) {
            case INDEX:
                name = "Index";
                break;
            case DISABLE:
                name = "Disable";
                break;
            case REINDEX:
                name = "Re-index";
                break;
            default:
                throw new RuntimeException();
        }
        return name;
    }

    private class RecordStatisticsDialog extends JDialog {
        private SipModel sipModel;

        private RecordStatisticsDialog(SipModel sipModel) throws HeadlessException {
            super(frame, "Analysis & Facts", true);
            this.sipModel = sipModel;
            getContentPane().add(new RecordStatisticsPanel(sipModel));
            getContentPane().add(createFinishedPanel(this), BorderLayout.SOUTH);
            setSize(SIZE);
            setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - SIZE.width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - SIZE.height) / 2);
        }

        public void reveal() {
            setTitle(String.format("Record Statistics for '%s'", sipModel.getDataSetStore().getSpec()));
            setVisible(true);
        }
    }

    private class AnalysisFactsDialog extends JDialog {
        private SipModel sipModel;

        private AnalysisFactsDialog(SipModel sipModel) throws HeadlessException {
            super(frame, "Analysis & Facts", true);
            this.sipModel = sipModel;
            getContentPane().add(new AnalysisFactsPanel(sipModel));
            getContentPane().add(createFinishedPanel(this), BorderLayout.SOUTH);
            setSize(SIZE);
            setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - SIZE.width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - SIZE.height) / 2);
        }

        public void reveal() {
            setTitle(String.format("Analysis & Facts for '%s'", sipModel.getDataSetStore().getSpec()));
            setVisible(true);
        }
    }

    private class MappingDialog extends JDialog {
        private SipModel sipModel;

        private MappingDialog(SipModel sipModel) throws HeadlessException {
            super(frame, "Mapping", true);
            this.sipModel = sipModel;
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Mapping", new MappingPanel(sipModel));
            tabs.addTab("Refinement", new RefinementPanel(this, sipModel));
            tabs.addTab("Normalization", new NormalizationPanel(sipModel));
            getContentPane().add(tabs, BorderLayout.CENTER);
            getContentPane().add(createFinishedPanel(this), BorderLayout.SOUTH);
            setSize(SIZE);
            setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - SIZE.width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - SIZE.height) / 2);
            setJMenuBar(createMappingMenuBar());
        }

        private JMenuBar createMappingMenuBar() {
            JMenuBar bar = new JMenuBar();
            MappingTemplateMenu mappingTemplateMenu = new MappingTemplateMenu(this, sipModel);
            bar.add(mappingTemplateMenu);
            return bar;
        }

        public void reveal(String prefix) {
            setTitle(String.format("Mapping '%s' of '%s'", prefix, sipModel.getDataSetStore().getSpec()));
            setVisible(true);
        }
    }

    private JPanel createFinishedPanel(final JDialog frame) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton hide = new JButton("Finished");
        hide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setEntry(entry);
                frame.setVisible(false);
            }
        });
        panel.add(hide);
        return panel;
    }

}
