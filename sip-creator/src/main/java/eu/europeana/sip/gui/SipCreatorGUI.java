package eu.europeana.sip.gui;

import eu.europeana.core.querymodel.annotation.AnnotationProcessor;
import eu.europeana.core.querymodel.annotation.AnnotationProcessorImpl;
import eu.europeana.core.querymodel.beans.AllFieldBean;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private FileMenu fileMenu;
    private AnalyzerPanel analyzerPanel;

    public SipCreatorGUI(Class<?> beanClass) {
        super("Europeana Ingestion SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        analyzerPanel = new AnalyzerPanel(createAnnotationProcessor(beanClass));
        analyzerPanel.setProgressDialog(new ProgressDialog(this, "Analyzing file"));
        tabs.addTab("Analyzer", analyzerPanel);
        tabs.addTab("Normalizer", new NormalizerPanel(new File("."), new File(".")));
        getContentPane().add(tabs);
        setJMenuBar(createMenuBar());
        analyzerPanel.setFileMenuEnablement(fileMenu.getEnablement());
        setSize(1200, 800);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(File file) {
                analyzerPanel.analyze(file);
            }
        });
        bar.add(fileMenu);
        return bar;
    }

    private AnnotationProcessor createAnnotationProcessor(Class<?> beanClass) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(beanClass);
        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
        annotationProcessor.setClasses(classes);
        return annotationProcessor;
    }

    public static void main(String[] args) {
        SipCreatorGUI sipCreatorGUI = new SipCreatorGUI(AllFieldBean.class);
        sipCreatorGUI.setVisible(true);
    }
}