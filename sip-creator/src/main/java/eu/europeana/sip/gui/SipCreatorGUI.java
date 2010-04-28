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

import eu.europeana.core.querymodel.beans.AllFieldBean;
import eu.europeana.core.querymodel.beans.BriefBean;
import eu.europeana.core.querymodel.beans.FullBean;
import eu.europeana.core.querymodel.beans.IdBean;
import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.sip.model.ExceptionHandler;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.SipModel;
import org.apache.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
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
    private Logger log = Logger.getLogger(getClass());
    private SipModel sipModel = new SipModel();

    public SipCreatorGUI() {
        super("Europeana Ingestion SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sipModel.setAnnotationProcessor(createAnnotationProcessor());
        sipModel.setExceptionHandler(new PopupExceptionHandler());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Analyzer", new AnalysisPanel(sipModel));
        tabs.addTab("Mapping", new MappingPanel(sipModel));
        tabs.addTab("Normalizer", new NormPanel(sipModel));
        getContentPane().add(tabs);
        setJMenuBar(createMenuBar());
//        setSize(1200, 800);
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        FileMenu fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public boolean select(FileSet fileSet) {
                if (!fileSet.isValid()) {
                    return false;
                }
                else {
                    fileSet.setExceptionHandler(new PopupExceptionHandler());
                    sipModel.setFileSet(fileSet);
                    return true;
                }
            }
        });
        bar.add(fileMenu);
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

    private class PopupExceptionHandler implements ExceptionHandler {
        @Override
        public void failure(final Exception exception) {
            if (SwingUtilities.isEventDispatchThread()) {
                reportException(exception);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reportException(exception);
                    }
                });
            }
            log.warn("Problem", exception);
        }

        private void reportException(Exception exception) {
            JOptionPane.showMessageDialog(SipCreatorGUI.this, exception.toString()); // todo: improve
        }
    }

    public static void main(String[] args) {
        SipCreatorGUI sipCreatorGUI = new SipCreatorGUI();
        sipCreatorGUI.setVisible(true);
    }
}