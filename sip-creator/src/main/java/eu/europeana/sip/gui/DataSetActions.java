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
import eu.delving.sip.FileStoreException;
import eu.delving.sip.FileType;
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
import java.util.Collection;
import java.util.List;

/**
 * All the actions that can be launched when a data set is selected
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetActions {
    private static final Dimension SIZE = new Dimension(1024 - 60, 768 - 60);
    private JFrame frame;
    private AnalysisFactsDialog analysisFactsDialog;
    private MappingDialog mappingDialog;
    private SipModel sipModel;
    private DataSetClient dataSetClient;
    private DataSetListModel.Entry entry;
    private List<DataSetAction> actions = new ArrayList<DataSetAction>();

    public DataSetActions(JFrame frame, SipModel sipModel, DataSetClient dataSetClient) {
        this.frame = frame;
        this.sipModel = sipModel;
        this.dataSetClient = dataSetClient;
        this.analysisFactsDialog = new AnalysisFactsDialog(sipModel);
        this.mappingDialog = new MappingDialog(sipModel);
        actions.add(createAnalyzeFactsAction());
        for (String metadataPrefix : sipModel.getMetadataModel().getPrefixes()) {
            actions.add(createEditMappingAction(metadataPrefix));
        }
        actions.add(createUploadAction());
        for (DataSetCommand command : DataSetCommand.values()) {
            actions.add(createCommandAction(command));
        }
    }

    public List<Action> getActions() {
        return new ArrayList<Action>(actions);
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

    private DataSetAction createAnalyzeFactsAction() {
        return new DataSetAction("Analysis & Facts") {
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

    private DataSetAction createUploadAction() {
        return new DataSetAction("Upload to Repository") {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final FileStore.DataSetStore store = entry.getDataSetStore();
                try {
                    if (!store.getFacts().isValid()) {
                        sipModel.getUserNotifier().tellUser("Sorry, but the Facts are not yet valid");
                        return;
                    }
                }
                catch (FileStoreException e) {
                    sipModel.getUserNotifier().tellUser("Unable to read Facts for this data set");
                    return;
                }
                sipModel.setDataSetStore(entry.getDataSetStore());
                ProgressMonitor progressMonitor = new ProgressMonitor(frame, "Uploading", "Uploading files for " + store.getSpec(), 0, 100);
                final ProgressListener progressListener = new ProgressListener.Adapter(progressMonitor, this);
                dataSetClient.uploadFile(FileType.FACTS, store.getFactsFile(), progressListener, new Runnable() {
                    @Override
                    public void run() {
                        dataSetClient.uploadFile(FileType.SOURCE, store.getSourceFile(), progressListener, new Runnable() {
                            @Override
                            public void run() {
                                Collection<File> mappingFiles = store.getMappingFiles();
                                if (!mappingFiles.isEmpty()) {
                                    if (mappingFiles.size() > 1) {
                                        throw new RuntimeException("Not yet ready for multiple mappings");
                                    }
                                    File mappingFile = mappingFiles.iterator().next();
                                    dataSetClient.uploadFile(FileType.MAPPING, mappingFile, progressListener, new Runnable() {
                                        @Override
                                        public void run() {
                                            // todo: implement
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }

            @Override
            boolean isEnabled(DataSetListModel.Entry entry) {
                return entry.getDataSetStore() != null;
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
                frame.setVisible(false);
            }
        });
        panel.add(hide);
        return panel;
    }

}
