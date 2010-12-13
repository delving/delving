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
import eu.europeana.sip.core.GroovyCodeResource;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.model.UserNotifier;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
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
    private static final Dimension SIZE = new Dimension(1024 - 60, 768 - 60);
    private static final int MARGIN = 15;
    private Logger log = Logger.getLogger(getClass());
    private SipModel sipModel;
    private JLabel titleLabel = new JLabel(LOCAL_SETS, JLabel.CENTER);
    private DataSetClient dataSetClient;
    private JCheckBoxMenuItem connectedBox = new JCheckBoxMenuItem("Connect");
    private DataSetListModel dataSetListModel = new DataSetListModel();
    private JList dataSetList = new JList(dataSetListModel);
    private DataSetActions dataSetActions;

    public SipCreatorGUI() throws FileStoreException {
        super("Delving SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MetadataModel metadataModel = loadMetadataModel();
        File fileStoreDirectory = getFileStoreDirectory();
        FileStore fileStore = new FileStoreImpl(fileStoreDirectory, metadataModel);
        GroovyCodeResource groovyCodeResource = new GroovyCodeResource();
        this.sipModel = new SipModel(fileStore, metadataModel, groovyCodeResource, new PopupExceptionHandler());
        this.dataSetClient = new DataSetClient(sipModel, new DataSetClient.Listener() {

            @Override
            public void setInfo(DataSetInfo dataSetInfo) {
                dataSetListModel.setDataSetInfo(dataSetInfo);
                dataSetActions.setDataSetInfo(dataSetInfo);
            }

            @Override
            public void setList(List<DataSetInfo> list) {
                if (list != null) {
                    for (DataSetInfo dataSetInfo : list) {
                        dataSetListModel.setDataSetInfo(dataSetInfo);
                        dataSetActions.setDataSetInfo(dataSetInfo);
                    }
                }
            }

            @Override
            public void disconnected() {
                connectedBox.setSelected(false);
            }
        });
        dataSetActions = new DataSetActions(this, sipModel, dataSetClient);
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
        main.add(createEast(), BorderLayout.EAST);
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
        dataSetActions.setEntry(null);
    }

    private File getFileStoreDirectory() throws FileStoreException {
        File fileStoreDirectory = new File(System.getProperty("user.home"), "/sip-creator-file-store");
        if (fileStoreDirectory.isFile()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileStoreDirectory));
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        fileStoreDirectory = new File(line);
                        log.info(String.format("Using %s as file store directory", fileStoreDirectory.getAbsolutePath()));
                        break;
                    }
                }
                br.close();
            }
            catch (IOException e) {
                throw new FileStoreException("Unable to read the file " + fileStoreDirectory.getAbsolutePath());
            }
        }
        return fileStoreDirectory;
    }

    private JPanel createEast() {
        JPanel local = new JPanel(new GridLayout(0, 1, 5, 5));
        local.setBorder(BorderFactory.createTitledBorder("Local Actions"));
        for (Action action : dataSetActions.getLocalActions()) {
            local.add(new JButton(action));
        }
        JPanel remote = new JPanel(new GridLayout(0, 1, 5, 5));
        remote.setBorder(BorderFactory.createTitledBorder("Remote Actions"));
        for (Action action : dataSetActions.getRemoteActions()) {
            remote.add(new JButton(action));
        }
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.add(local);
        p.add(remote);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JComponent createList() {
        dataSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataSetList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) return;
                int selected = dataSetList.getSelectedIndex();
                DataSetListModel.Entry selectedEntry = selected >= 0 ? dataSetListModel.getEntry(selected) : null;
                dataSetActions.setEntry(selectedEntry);
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
        bar.add(createRepositoryMenu());
        bar.add(createLocalActionMenu());
        bar.add(createRemoteActionMenu());
        return bar;
    }

    private JMenu createLocalActionMenu() {
        JMenu actions = new JMenu("Local Actions");
        for (Action action : dataSetActions.getLocalActions()) {
            actions.add(action);
        }
        return actions;
    }

    private JMenu createRemoteActionMenu() {
        JMenu actions = new JMenu("Remote Actions");
        for (Action action : dataSetActions.getRemoteActions()) {
            actions.add(action);
        }
        return actions;
    }

    private JMenu createRepositoryMenu() {
        JMenu repository = new JMenu("Repository");
        repository.add(new ServerHostPortAction());
        repository.add(new AccessKeyAction());
        connectedBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                boolean enabled = itemEvent.getStateChange() == ItemEvent.SELECTED;
                dataSetClient.setListFetchingEnabled(enabled);
                if (!enabled) {
                    dataSetListModel.clear();
                    for (FileStore.DataSetStore dataSetStore : sipModel.getFileStore().getDataSetStores().values()) {
                        dataSetListModel.setDataSetStore(dataSetStore);
                    }
                }
                titleLabel.setText(enabled ? LOCAL_AND_REMOTE_SETS : LOCAL_SETS);
                dataSetList.clearSelection();
            }
        });
        repository.add(connectedBox);
        return repository;
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

    private class ServerHostPortAction extends AbstractAction {

        public ServerHostPortAction() {
            super("Server Network Address");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String serverHostPort = JOptionPane.showInputDialog(SipCreatorGUI.this, "Server network address host:port (eg. delving.eu:8080).", sipModel.getServerHostPort());
            if (serverHostPort != null && !serverHostPort.isEmpty()) {
                sipModel.setServerHostPort(serverHostPort);
            }
        }
    }

    private class AccessKeyAction extends AbstractAction {

        public AccessKeyAction() {
            super("Access Key");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JPasswordField passwordField = new JPasswordField(sipModel.getAccessKey());
            Object[] msg = {"Server Access Key", passwordField};
            int result = JOptionPane.showConfirmDialog(SipCreatorGUI.this, msg, "Permission", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                sipModel.setServerAccessKey(new String(passwordField.getPassword()));
            }
        }
    }

    public static void main(final String[] args) throws ClassNotFoundException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SipCreatorGUI sipCreatorGUI = new SipCreatorGUI();
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