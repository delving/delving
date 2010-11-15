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

import eu.delving.core.metadata.MetadataModel;
import eu.delving.core.metadata.MetadataModelImpl;
import eu.delving.sip.FileStore;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.FileStoreImpl;
import eu.europeana.sip.core.RecordValidationException;
import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.model.UserNotifier;
import org.apache.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private Logger log = Logger.getLogger(getClass());
    private SipModel sipModel;

    public SipCreatorGUI(String fileStoreDirectory, String serverUrl) throws FileStoreException {
        super("SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FileStore fileStore = new FileStoreImpl(new File(fileStoreDirectory));
        this.sipModel = new SipModel(fileStore, loadMetadataModel(), new PopupExceptionHandler(), serverUrl);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Analysis", new AnalysisPanel(sipModel));
        tabs.addTab("Mapping", new MappingPanel(sipModel));
        tabs.addTab("Refinement", new RefinementPanel(sipModel));
        tabs.addTab("Normalization", new NormalizationPanel(sipModel));
        if (serverUrl != null) {
            tabs.addTab("Repository", new DataSetPanel(sipModel));
        }
        getContentPane().add(tabs, BorderLayout.CENTER);
        setJMenuBar(createMenuBar());
//        setSize(1024, 768);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        FileMenu fileMenu = new FileMenu(this, sipModel, new FileMenu.SelectListener() {
            @Override
            public boolean select(File file) {
                if (!file.exists()) {
                    return false;
                }
                else {
                    String spec = JOptionPane.showInputDialog(SipCreatorGUI.this, "You must enter the Dataset spec which identifies it. This cannot be changed later.");
                    spec = spec.trim();
                    if (spec.isEmpty()) {
                        return false;
                    }
                    else {
                        int answer = JOptionPane.showConfirmDialog(SipCreatorGUI.this, String.format("Are you sure you wish to import %s as Data set %s", file.getAbsolutePath(), spec));
                        if (answer == JOptionPane.YES_OPTION) {
                            sipModel.createDataSetStore(spec, file);
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                }
            }
        });
        bar.add(fileMenu);
        MappingTemplateMenu mappingTemplateMenu = new MappingTemplateMenu(this, sipModel);
        bar.add(mappingTemplateMenu);
        return bar;
    }

    private MetadataModel loadMetadataModel() {
        try {
            MetadataModelImpl metadataModel = new MetadataModelImpl();
            metadataModel.setRecordDefinitionResource("/abm-record-definition.xml");
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
                    String html = String.format("<html><h3>%s</h3><p>%s</p></html>", message, exception.getMessage());
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

    public static void main(final String[] args) throws ClassNotFoundException {
//        if (args.length != 1) {
//            throw new RuntimeException("SipCreatorGUI gets two parameters <server-url>");
//        }
        final String serverUrl = args.length > 0 ? args[0] : null;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SipCreatorGUI sipCreatorGUI = new SipCreatorGUI("sip-creator-file-store", serverUrl);
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