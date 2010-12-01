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

package eu.europeana.sip.gui;

import eu.delving.metadata.MetadataModel;
import eu.delving.metadata.MetadataModelImpl;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetResponse;
import eu.delving.sip.FileStore;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.FileStoreImpl;
import eu.delving.sip.FileType;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.model.FileUploader;
import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.model.UserNotifier;
import org.apache.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private static final String LOCAL_SETS = "Local Data Sets";
    private static final String LOCAL_AND_REMOTE_SETS = "Local and Remote Data Sets";
    private static final Dimension SIZE = new Dimension(1024-60, 768-60);
    private static final int MARGIN = 15;
    private Logger log = Logger.getLogger(getClass());
    private SipModel sipModel;
    private JLabel titleLabel = new JLabel(LOCAL_SETS, JLabel.CENTER);
    private MappingFrame mappingFrame;
    private AnalysisFactsFrame analysisFactsFrame;
    private RepositoryConnection repositoryConnection;
    private DataSetListModel dataSetListModel = new DataSetListModel();
    private JList dataSetList = new JList(dataSetListModel);
    private List<JButton> controlButtons = new ArrayList<JButton>();

    public SipCreatorGUI(File fileStoreDirectory, String serverUrl) throws FileStoreException {
        super("Delving SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MetadataModel metadataModel = loadMetadataModel();
        FileStore fileStore = new FileStoreImpl(fileStoreDirectory, metadataModel);
        this.sipModel = new SipModel(fileStore, metadataModel, new PopupExceptionHandler(), serverUrl);
        this.repositoryConnection = new RepositoryConnection(sipModel, new RepositoryConnection.Listener() {

            @Override
            public void setInfo(DataSetInfo dataSetInfo) {
                dataSetListModel.setDataSetInfo(dataSetInfo);
            }

            @Override
            public void setList(List<DataSetInfo> list) {
                for (DataSetInfo info : list) {
                    dataSetListModel.setDataSetInfo(info);
                }
            }
        });
        this.mappingFrame = new MappingFrame(sipModel);
        this.analysisFactsFrame = new AnalysisFactsFrame(sipModel);
        setJMenuBar(createMenuBar());
        JPanel main = new JPanel(new BorderLayout(MARGIN, MARGIN));
        main.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(MARGIN, MARGIN, 0, MARGIN),
                        BorderFactory.createRaisedBevelBorder()
                )
        );
        titleLabel.setBackground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setFont(new Font("Sans", Font.BOLD, 24));
        JLabel northRight = new JLabel(new ImageIcon(getClass().getResource("/delving-logo-name.jpg")));
        north.add(titleLabel, BorderLayout.CENTER);
        north.add(northRight, BorderLayout.EAST);
        getContentPane().add(north, BorderLayout.NORTH);
        getContentPane().add(main, BorderLayout.CENTER);
        main.add(createList(), BorderLayout.CENTER);
        main.add(createControl(), BorderLayout.SOUTH);
        setSize(SIZE);
        setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - SIZE.width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - SIZE.height) / 2);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (FileStore.DataSetStore dataSetStore : sipModel.getFileStore().getDataSetStores().values()) {
                    dataSetListModel.setDataSetStore(dataSetStore);
                }
            }
        });
        for (JButton button : controlButtons) {
            button.setEnabled(false);
        }
    }

    private JComponent createList() {
        dataSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataSetList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) return;
                boolean enable = getSelectedDataSetStore() != null;
                for (JButton button : controlButtons) {
                    button.setEnabled(enable);
                }
            }
        });
        dataSetList.setCellRenderer(new DataSetListModel.Cell());
        dataSetList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dataSetList.setBackground(getBackground());
        JScrollPane scroll = new JScrollPane(dataSetList);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scroll;
    }

    private JComponent createControl() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Control"));

        JButton analysis = new JButton("Analysis Facts");
        analysis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileStore.DataSetStore store = getSelectedDataSetStore();
                if (store != null) {
                    sipModel.setDataSetStore(store);
                    analysisFactsFrame.reveal();
                }
            }
        });
        p.add(analysis);
        controlButtons.add(analysis);

        for (final String metadataPrefix : sipModel.getMetadataModel().getPrefixes()) {
            JButton button = new JButton(String.format("Edit '%s' Mapping", metadataPrefix));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    FileStore.DataSetStore store = getSelectedDataSetStore();
                    if (store != null) {
                        sipModel.setDataSetStore(store);
                        sipModel.setMetadataPrefix(metadataPrefix);
                        mappingFrame.reveal(metadataPrefix);
                    }
                }
            });
            p.add(button);
            controlButtons.add(button);
        }

        JButton uploadButton = new JButton(new UploadAction());
        p.add(uploadButton);
        controlButtons.add(uploadButton);
        return p;
    }

    private FileStore.DataSetStore getSelectedDataSetStore() {
        int selected = dataSetList.getSelectedIndex();
        return selected >= 0 ? dataSetListModel.getEntry(selected).getDataSetStore() : null;
    }

    private class UploadAction extends AbstractAction {

        private UploadAction() {
            super("Upload to Repository");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            final FileStore.DataSetStore store = getSelectedDataSetStore();
            if (store != null) {
                try {
                    if (!store.getFacts().isValid()) {
                        sipModel.tellUser("Sorry, but the Facts are not yet valid");
                        return;
                    }
                }
                catch (FileStoreException e) {
                    sipModel.tellUser("Unable to read Facts for this data set");
                    return;
                }
                sipModel.setDataSetStore(store);
                ProgressMonitor progressMonitor = new ProgressMonitor(SwingUtilities.getRoot(SipCreatorGUI.this), "Uploading", "Uploading files for " + store.getSpec(), 0, 100);
                final ProgressListener progressListener = new ProgressListener.Adapter(progressMonitor, this);
                sipModel.uploadFile(FileType.FACTS, store.getFactsFile(), progressListener, new FileUploader.Receiver() {
                    @Override
                    public void acceptResponse(DataSetResponse dataSetResponse) {
                        switch (dataSetResponse) {
                            case THANK_YOU:
                            case GOT_IT_ALREADY:
                                sipModel.uploadFile(FileType.SOURCE, store.getSourceFile(), progressListener, new FileUploader.Receiver() {
                                    @Override
                                    public void acceptResponse(DataSetResponse dataSetResponse) {
                                        switch (dataSetResponse) {
                                            case THANK_YOU:
                                            case GOT_IT_ALREADY:
                                                Collection<File> mappingFiles = store.getMappingFiles();
                                                if (mappingFiles.size() > 1) {
                                                    throw new RuntimeException("Not yet ready for multiple mappings");
                                                }
                                                File mappingFile = mappingFiles.iterator().next();
                                                sipModel.uploadFile(FileType.MAPPING, mappingFile, progressListener, new FileUploader.Receiver() {
                                                    @Override
                                                    public void acceptResponse(DataSetResponse dataSetResponse) {
                                                        // todo: done
                                                    }
                                                });
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.add(new ImportMenu(this, sipModel, new Runnable() {
            @Override
            public void run() {
                for (FileStore.DataSetStore dataSetStore : sipModel.getFileStore().getDataSetStores().values()) {
                    dataSetListModel.setDataSetStore(dataSetStore);
                }
            }
        }));
        JMenu repository = new JMenu("Repository");
        repository.add(new AccessKeyAction());
        JCheckBoxMenuItem connect = new JCheckBoxMenuItem("Connect");
        connect.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                boolean enabled = itemEvent.getStateChange() == ItemEvent.SELECTED;
                repositoryConnection.enablePolling(enabled);
                if (!enabled) {
                    dataSetListModel.clear();
                    for (FileStore.DataSetStore dataSetStore : sipModel.getFileStore().getDataSetStores().values()) {
                        dataSetListModel.setDataSetStore(dataSetStore);
                    }
                }
                titleLabel.setText(enabled ? LOCAL_AND_REMOTE_SETS : LOCAL_SETS);
            }
        });
        repository.add(connect);
        bar.add(repository);
        return bar;
    }

    private MetadataModel loadMetadataModel() {
        try {
            MetadataModelImpl metadataModel = new MetadataModelImpl();
            metadataModel.setRecordDefinitionResources(Arrays.asList("/abm-record-definition.xml"));
            metadataModel.setDefaultPrefix("abm");
            return metadataModel;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private class PopupExceptionHandler implements UserNotifier {

        @Override
        public void tellUser(final String message, final Exception exception) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String html = exception != null ?
                            String.format("<html><h3>%s</h3><p>%s</p></html>", message, exception.getMessage()) :
                            String.format("<html><h3>%s</h3></html>", message);
                    if (exception instanceof RecordValidationException) {
                        RecordValidationException rve = (RecordValidationException) exception;
                        StringBuilder problemHtml = new StringBuilder(String.format("<html><h3>%s</h3><ul>", message));
                        for (String problem : rve.getProblems()) {
                            problemHtml.append(String.format("<li>%s</li>", problem));
                        }
                        problemHtml.append("</ul></html>");
                        html = problemHtml.toString();
                    }
                    JOptionPane.showMessageDialog(null, html);
                }
            });
            if (exception != null) {
                log.warn(message, exception);
            }
            else {
                log.warn(message);
            }
        }

        @Override
        public void tellUser(String message) {
            tellUser(message, null);
        }
    }

    private class AccessKeyAction extends AbstractAction {

        public AccessKeyAction() {
            super("Access Key");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String accessKey = JOptionPane.showInputDialog(SipCreatorGUI.this, "Server Access Key", sipModel.getServerAccessKey());
            if (accessKey != null && !accessKey.isEmpty()) {
                sipModel.setServerAccessKey(accessKey);
            }
        }
    }

    private class AnalysisFactsFrame extends JDialog {
        private SipModel sipModel;

        private AnalysisFactsFrame(SipModel sipModel) throws HeadlessException {
            super(SipCreatorGUI.this, "Analysis Facts", true);
            this.sipModel = sipModel;
            getContentPane().add(new AnalysisFactsPanel(sipModel));
            getContentPane().add(createFinishedPanel(this), BorderLayout.SOUTH);
//            setSize(Toolkit.getDefaultToolkit().getScreenSize());
            setSize(SIZE);
            setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - SIZE.width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - SIZE.height) / 2);
        }

        public void reveal() {
            setTitle(String.format("Analysis Facts of '%s'", sipModel.getDataSetStore().getSpec()));
            setVisible(true);
        }
    }

    private class MappingFrame extends JDialog {
        private SipModel sipModel;

        private MappingFrame(SipModel sipModel) throws HeadlessException {
            super(SipCreatorGUI.this, "Mapping", true);
            this.sipModel = sipModel;
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Mapping", new MappingPanel(sipModel));
            tabs.addTab("Refinement", new RefinementPanel(sipModel));
            tabs.addTab("Normalization", new NormalizationPanel(sipModel));
            getContentPane().add(tabs, BorderLayout.CENTER);
            getContentPane().add(createFinishedPanel(this), BorderLayout.SOUTH);
            setSize(SIZE);
            setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - SIZE.width) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - SIZE.height) / 2);
//            setSize(Toolkit.getDefaultToolkit().getScreenSize());
//        MappingTemplateMenu mappingTemplateMenu = new MappingTemplateMenu(this, sipModel);
//        bar.add(mappingTemplateMenu);
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

    public static void main(final String[] args) throws ClassNotFoundException {
//        if (args.length != 1) {
//            throw new RuntimeException("SipCreatorGUI gets two parameters <server-url>");
//        }
        final String serverUrl = args.length > 0 ? args[0] : null;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SipCreatorGUI sipCreatorGUI = new SipCreatorGUI(new File(System.getProperty("user.home"), "/sip-creator-file-store"), serverUrl);
                    sipCreatorGUI.setVisible(true);
                }
                catch (FileStoreException e) {
                    JOptionPane.showMessageDialog(null, "Unable to create the file store");
                    e.printStackTrace();
                }
            }
        });
    }
}