package eu.europeana.sip.gui;

import eu.europeana.core.querymodel.annotation.AnnotationProcessor;
import eu.europeana.core.querymodel.annotation.AnnotationProcessorImpl;
import eu.europeana.core.querymodel.beans.AllFieldBean;
import eu.europeana.sip.mapping.MappingTree;
import eu.europeana.sip.mapping.Statistics;
import eu.europeana.sip.xml.FileHandler;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
    private static final int COUNTER_LIST_SIZE = 100;
    private JTree statisticsJTree = new JTree(MappingTree.create("No Document Loaded").createTreeModel());
    private MappingPanel mappingPanel;
    private JLabel title = new JLabel("Document Structure", JLabel.CENTER);
    private FileMenu fileMenu;

    public SipCreator(Class<?> beanClass) {
        super("Europeana Ingestion SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mappingPanel = new MappingPanel(createAnnotationProcessor(beanClass));
        setJMenuBar(createMenuBar());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(createAnalysisPanel());
        split.setRightComponent(mappingPanel);
        getContentPane().add(split, BorderLayout.CENTER);
//        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setSize(1200, 800);
    }

    private Component createAnalysisPanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createEmptyBorder(15,15,15,0));
        title.setFont(new Font("Serif", Font.BOLD, 12));
        p.add(title, BorderLayout.NORTH);
        statisticsJTree.setCellRenderer(new CellRenderer());
        statisticsJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        statisticsJTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TreePath path = event.getPath();
                MappingTree.Node node = (MappingTree.Node) path.getLastPathComponent();
                if (node.getStatistics() != null) {
                    mappingPanel.setNode(node);
                }
                else {
                    mappingPanel.setNode(null);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(statisticsJTree);
        p.add(scroll, BorderLayout.CENTER);
        scroll.setPreferredSize(new Dimension(400, 800));
        return p;
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(File file) {
                fileMenu.setEnabled(false);
                analyze(file);
            }
        });
        bar.add(fileMenu);
        return bar;
    }

    private void setMappingTree(MappingTree mappingTree) {
        TreeModel treeModel = mappingTree.createTreeModel();
        statisticsJTree.setModel(treeModel);
        expandEmptyNodes((MappingTree.Node)treeModel.getRoot());
    }

    private void expandEmptyNodes(MappingTree.Node node) {
        if (node.getStatistics() == null) {
            TreePath path = node.getTreePath();
            statisticsJTree.expandPath(path);
        }
        for (MappingTree.Node childNode : node.getChildNodes()) {
            expandEmptyNodes(childNode);
        }
    }

    private AnnotationProcessor createAnnotationProcessor(Class<?> beanClass) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(beanClass);
        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
        annotationProcessor.setClasses(classes);
        return annotationProcessor;
    }

    private void analyze(final File file) {
        File statisticsFile = createStatisticsFile(file);
        if (statisticsFile.exists()) {
            FileHandler.loadStatistics(statisticsFile, new FileHandler.LoadListener() {
                @Override
                public void success(List<Statistics> list) {
                    title.setText("Document Structure of \""+file.getName()+"\"");
                    setMappingTree(MappingTree.create(list, file.getName()));
                }

                @Override
                public void failure(Exception exception) {
                    title.setText("Document Structure");
                    // TODO: implement!
                }

                @Override
                public void finished() {
                    fileMenu.setEnabled(true);
                }
            });
        }
        else {
            FileHandler.compileStatistics(file, createStatisticsFile(file), COUNTER_LIST_SIZE, new FileHandler.LoadListener() {
                @Override
                public void success(List<Statistics> list) {
                    title.setText("Document Structure of \""+file.getName()+"\"");
                    setMappingTree(MappingTree.create(list, file.getName()));
                }

                @Override
                public void failure(Exception exception) {
                    title.setText("Document Structure");
                    // TODO: implement!
                }

                @Override
                public void finished() {
                    fileMenu.setEnabled(true);
                }
            });
        }
    }

    private File createStatisticsFile(File file) {
        return new File(file.getParentFile(), file.getName() + ".statistics");
    }

    private File createMappingFile(File file) { // todo: writing and reading of this
        return new File(file.getParentFile(), file.getName() + ".mapping");
    }

    private class CellRenderer extends DefaultTreeCellRenderer {
        private Font normalFont, thickFont;

        @Override
        public Component getTreeCellRendererComponent(JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3) {
            MappingTree.Node node = (MappingTree.Node) o;
            JLabel label = (JLabel) super.getTreeCellRendererComponent(jTree, o, b, b1, b2, i, b3);
            label.setFont(node.getStatistics() != null ? getThickFont() : getNormalFont());
            return label;
        }

        private Font getNormalFont() {
            if (normalFont == null) {
                normalFont = super.getFont();
            }
            return normalFont;
        }

        private Font getThickFont() {
            if (thickFont == null) {
                thickFont = new Font(getNormalFont().getFontName(), Font.BOLD, getNormalFont().getSize());
            }
            return thickFont;
        }
    }

    public static void main(String[] args) {
        SipCreator sipCreator = new SipCreator(AllFieldBean.class);
        sipCreator.setVisible(true);
    }
}