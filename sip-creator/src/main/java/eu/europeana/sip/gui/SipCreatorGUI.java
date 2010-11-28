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
import eu.delving.sip.FileStore;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.FileStoreImpl;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.model.UserNotifier;
import org.apache.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private static final Dimension SIZE = new Dimension(800, 600);
    private static final int MARGIN = 15;
    private Logger log = Logger.getLogger(getClass());
    private SipModel sipModel;
    private MappingFrame mappingFrame;
    private RepositoryConnection repositoryConnection;
    private DataSetListModel dataSetListModel = new DataSetListModel();
    private JList dataSetList = new JList(dataSetListModel);
    private List<JButton> mappingButtons = new ArrayList<JButton>();

    public SipCreatorGUI(File fileStoreDirectory, String serverUrl) throws FileStoreException {
        super("Delving SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FileStore fileStore = new FileStoreImpl(fileStoreDirectory);
        this.sipModel = new SipModel(fileStore, loadMetadataModel(), new PopupExceptionHandler(), serverUrl);
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
        JLabel northCenter = new JLabel("Data Sets", JLabel.CENTER);
        northCenter.setBackground(Color.WHITE);
        northCenter.setOpaque(true);
        northCenter.setFont(new Font("Sans", Font.BOLD, 24));
        JLabel northRight = new JLabel(new ImageIcon(getClass().getResource("/delving-logo-name.jpg")));
        north.add(northCenter, BorderLayout.CENTER);
        north.add(northRight, BorderLayout.EAST);
        getContentPane().add(north, BorderLayout.NORTH);
        getContentPane().add(main, BorderLayout.CENTER);
        main.add(createList(), BorderLayout.CENTER);
        main.add(createControl(), BorderLayout.SOUTH);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(SIZE);
        setLocation((screen.width - SIZE.width) / 2, (screen.height - SIZE.height) / 2);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (FileStore.DataSetStore dataSetStore : sipModel.getFileStore().getDataSetStores().values()) {
                    dataSetListModel.setDataSetStore(dataSetStore);
                }
            }
        });
        for (JButton button : mappingButtons) {
            button.setEnabled(false);
        }
    }

    private JComponent createList() {
        dataSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataSetList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                DataSetListModel.Entry entry = (DataSetListModel.Entry) dataSetList.getSelectedValue();
                boolean enable = entry != null && entry.getDataSetStore() != null;
                for (JButton button : mappingButtons) {
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
        for (final String prefix : sipModel.getMetadataModel().getPrefixes()) {
            JButton button = new JButton(String.format("Map to '%s'", prefix));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    int selected = dataSetList.getSelectedIndex();
                    if (selected >= 0) {
                        DataSetListModel.Entry entry = dataSetListModel.getEntry(selected);
                        sipModel.setDataSetStore(entry.getDataSetStore());
                        mappingFrame.show(prefix);
                    }
                }
            });
            p.add(button);
            mappingButtons.add(button);
        }
        return p;
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
                repositoryConnection.enablePolling(itemEvent.getStateChange() == ItemEvent.SELECTED);
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
                    JOptionPane.showMessageDialog(SipCreatorGUI.this, html);
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

    private class MappingFrame extends JFrame {
        private SipModel sipModel;

        private MappingFrame(SipModel sipModel) throws HeadlessException {
            super("Mapping");
            this.sipModel = sipModel;
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Analysis", new AnalysisPanel(sipModel));
            tabs.addTab("Mapping", new MappingPanel(sipModel));
            tabs.addTab("Refinement", new RefinementPanel(sipModel));
            tabs.addTab("Normalization", new NormalizationPanel(sipModel));
            getContentPane().add(tabs, BorderLayout.CENTER);
            setSize(Toolkit.getDefaultToolkit().getScreenSize());
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    SipCreatorGUI.this.setVisible(true);
                }
            });
//        MappingTemplateMenu mappingTemplateMenu = new MappingTemplateMenu(this, sipModel);
//        bar.add(mappingTemplateMenu);
        }

        public void show(String prefix) {
            setTitle(String.format("Mapping '%s' of '%s'", prefix, sipModel.getDataSetStore().getSpec()));
            setVisible(true);
        }
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