package eu.europeana.sip.gui;

import eu.europeana.core.querymodel.annotation.AnnotationProcessor;
import eu.europeana.core.querymodel.annotation.AnnotationProcessorImpl;
import eu.europeana.core.querymodel.beans.AllFieldBean;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan.demirel@kb.nl>
 */

public class SipCreator extends JFrame {
    private FileMenu fileMenu;

    public SipCreator(Class<?> beanClass) {
        super("Europeana Ingestion SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(createMenuBar());
        JTabbedPane tabs = new JTabbedPane();

        // todo: tabs
        // tab for analyzer
        //    (pass in createAnnotationProcessor with this beanClass)
        // tab for normalizer
        getContentPane().add(tabs);
        setSize(1200, 800);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(File file) {
                fileMenu.setEnabled(false);
//                todo: trigger analyze(file);
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
        SipCreator sipCreator = new SipCreator(AllFieldBean.class);
        sipCreator.setVisible(true);
    }
}