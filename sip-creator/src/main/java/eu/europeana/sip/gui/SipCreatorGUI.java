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

import eu.europeana.sip.io.FileSet;

import javax.swing.*;
import java.awt.*;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private AnalyzerPanel analyzerPanel = new AnalyzerPanel();
    private NormalizerPanel normalizerPanel = new NormalizerPanel();

    public SipCreatorGUI() {
        super("Europeana Ingestion SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Analyzer", analyzerPanel);
        tabs.addTab("Normalizer", normalizerPanel);
        getContentPane().add(tabs);
        setJMenuBar(createMenuBar());
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        FileMenu fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(FileSet fileSet) {
                analyzerPanel.setFileSet(fileSet);
                normalizerPanel.setFileSet(fileSet);
            }
        });
        analyzerPanel.setFileMenuEnablement(fileMenu.getEnablement());
        bar.add(fileMenu);
        return bar;
    }

//    private AnnotationProcessor createAnnotationProcessor(Class<?> beanClass) {
//        List<Class<?>> classes = new ArrayList<Class<?>>();
//        classes.add(beanClass);
//        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
//        annotationProcessor.setClasses(classes);
//        return annotationProcessor;
//    }
//

    public static void main(String[] args) {
        SipCreatorGUI sipCreatorGUI = new SipCreatorGUI();
        sipCreatorGUI.setVisible(true);
    }
}