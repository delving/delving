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
import eu.delving.metadata.Path;
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
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private Logger log = Logger.getLogger(getClass());
    private SipModel sipModel;

    public SipCreatorGUI(File fileStoreDirectory, String serverUrl) throws FileStoreException {
        super("SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FileStore fileStore = new FileStoreImpl(fileStoreDirectory);
        this.sipModel = new SipModel(fileStore, loadMetadataModel(), new PopupExceptionHandler(), serverUrl);
        sipModel.addUpdateListener(new SipModel.UpdateListener() {

            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedDataSetStore(FileStore.DataSetStore dataSetStore) {
                if (dataSetStore == null) {
                    SipCreatorGUI.this.setTitle("SIP Creator");
                }
                else {
                    SipCreatorGUI.this.setTitle(String.format("SIP Creator - Data Set %s", dataSetStore.getSpec()));
                }
            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
            }
        });
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
        bar.add(new ImportMenu(this, sipModel, new ImportMenu.SelectListener() {
            @Override
            public boolean selectInputFile(File file) {
                if (!file.exists()) {
                    return false;
                }
                else {
                    String spec = JOptionPane.showInputDialog(
                            SipCreatorGUI.this,
                            String.format(
                                    "<html>You have selected the following file for importing:<br><br>" +
                                            "<pre>      <strong>%s</strong></pre><br>" +
                                            "To complete the import you must enter a Data Set Spec name which will serve<br>" +
                                            "to identify it in the future. For consistency this cannot be changed later, so choose<br>" +
                                            "carefully.",
                                    file.getAbsolutePath()
                            ),
                            "Select and Enter Data Set Spec",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (spec == null || spec.trim().isEmpty()) {
                        return false;
                    }
                    int doImport = JOptionPane.showConfirmDialog(
                            SipCreatorGUI.this,
                            String.format(
                                    "<html>Are you sure you wish to import this file<br><br>" +
                                            "<pre>     <strong>%s</strong></pre><br>" +
                                            "as a new Data set by the name of<br><br>" +
                                            "<pre>     <strong>%s</strong></pre>",
                                    file.getAbsolutePath(),
                                    spec
                            ),
                            "Verify your choice",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (doImport == JOptionPane.YES_OPTION) {
                        ProgressMonitor progressMonitor = new ProgressMonitor(SipCreatorGUI.this, "Importing", "Storing data for " + spec, 0, 100);
                        sipModel.createDataSetStore(spec, file, progressMonitor, null);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
        }));
        bar.add(new DataSetMenu(sipModel, new DataSetMenu.SelectListener() {
            @Override
            public void selectDataSet(String spec) {
                sipModel.setDataSetStore(spec);
            }
        }));
        MappingTemplateMenu mappingTemplateMenu = new MappingTemplateMenu(this, sipModel);
        bar.add(mappingTemplateMenu);
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