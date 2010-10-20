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

import eu.europeana.sip.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.sip.definitions.beans.AllFieldBean;
import eu.europeana.sip.definitions.beans.BriefBean;
import eu.europeana.sip.definitions.beans.FullBean;
import eu.europeana.sip.definitions.beans.IdBean;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.model.UserNotifier;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private SipModel sipModel;

    public SipCreatorGUI(String serverUrl) {
        super("SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.sipModel = new SipModel(createAnnotationProcessor(), new PopupExceptionHandler(), serverUrl);
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
            public boolean select(FileSet fileSet) {
                if (!fileSet.isValid()) {
                    return false;
                }
                else {
                    fileSet.setExceptionHandler(new PopupExceptionHandler());
                    sipModel.setFileSet(fileSet);
                    setTitle(String.format("SIP Creator - %s", fileSet.getAbsolutePath()));
                    return true;
                }
            }
        });
        bar.add(fileMenu);
        MappingTemplateMenu mappingTemplateMenu = new MappingTemplateMenu(this, sipModel);
        bar.add(mappingTemplateMenu);
        return bar;
    }

    private AnnotationProcessor createAnnotationProcessor() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(IdBean.class);
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
        annotationProcessor.setClasses(list);
        return annotationProcessor;
    }

    private class PopupExceptionHandler implements UserNotifier {

        @Override
        public void tellUser(final String message, final Exception exception) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SipCreatorGUI.this, message);
                }
            });
//            if (exception != null) {
//                log.warn(message, exception);
//            }
//            else {
//                log.warn(message);
//            }
        }

        @Override
        public void tellUser(String message) {
            tellUser(message, null);
        }
    }

    public static void main(final String[] args) {
        final String serverUrl = args.length > 0 ? args[0] : null;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                SipCreatorGUI sipCreatorGUI = new SipCreatorGUI(serverUrl);
                sipCreatorGUI.setVisible(true);
            }
        });
    }
}