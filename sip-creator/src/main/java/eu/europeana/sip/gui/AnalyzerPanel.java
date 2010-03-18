package eu.europeana.sip.gui;

import eu.europeana.core.querymodel.annotation.AnnotationProcessor;
import eu.europeana.sip.mapping.MappingTree;
import eu.europeana.sip.mapping.Statistics;
import eu.europeana.sip.xml.FileHandler;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
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
import java.util.List;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class AnalyzerPanel extends JPanel {
    private static final int COUNTER_LIST_SIZE = 100;
    private JTree statisticsJTree = new JTree(MappingTree.create("No Document Loaded").createTreeModel());
    private MappingPanel mappingPanel;
    private JLabel title = new JLabel("Document Structure", JLabel.CENTER);
    private FileMenu.Enablement fileMenuEnablement;
    private ProgressDialog progressDialog;

    public AnalyzerPanel(AnnotationProcessor annotationProcessor) {
        super(new BorderLayout());
        this.mappingPanel = new MappingPanel(annotationProcessor);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(createAnalysisPanel());
        split.setRightComponent(mappingPanel);
        split.setDividerLocation(0.4);
        split.setSize(1280, 800);
        add(split, BorderLayout.CENTER);
    }

    private Component createAnalysisPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 0));
        title.setFont(new Font("Serif", Font.BOLD, 22));
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
                } else {
                    mappingPanel.setNode(null);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(statisticsJTree);
        p.add(scroll, BorderLayout.CENTER);
        scroll.setPreferredSize(new Dimension(400, 800));
        return p;
    }

    private void setMappingTree(MappingTree mappingTree) {
        TreeModel treeModel = mappingTree.createTreeModel();
        statisticsJTree.setModel(treeModel);
        expandEmptyNodes((MappingTree.Node) treeModel.getRoot());
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

    public void setFileMenuEnablement(FileMenu.Enablement fileMenuEnablement) {
        this.fileMenuEnablement = fileMenuEnablement;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    private void loadStarted() {
        fileMenuEnablement.enable(false);
        progressDialog.setVisible(true);
    }

    private void loadFinished() {
        fileMenuEnablement.enable(true);
        progressDialog.dispose();
    }

    public void analyze(final File file) {
        File statisticsFile = createStatisticsFile(file);
        loadStarted();
        FileHandler.Listener listener = new FileHandler.Listener() {
            @Override
            public void success(List<Statistics> list) {
                setMappingTree(MappingTree.create(list, file.getName()));
                loadFinished();
            }

            @Override
            public void failure(Exception exception) {
                // todo: use JOptionPane or something else to show failure to user
                loadFinished();
            }

            @Override
            public void finished() {
                loadFinished();
            }
        };

        if (statisticsFile.exists()) {
            FileHandler.loadStatistics(statisticsFile, listener, progressDialog);
        }
        else {
            FileHandler.compileStatistics(file, createStatisticsFile(file), COUNTER_LIST_SIZE, listener, progressDialog);
        }
    }

    private File createStatisticsFile(File file) {
        return new File(file.getParentFile(), file.getName() + ".statistics");
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
}
