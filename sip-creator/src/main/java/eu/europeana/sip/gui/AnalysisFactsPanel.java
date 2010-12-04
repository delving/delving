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

import eu.delving.metadata.AnalysisTree;
import eu.delving.metadata.AnalysisTreeNode;
import eu.delving.metadata.Path;
import eu.delving.metadata.Statistics;
import eu.delving.sip.FileStore;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class AnalysisFactsPanel extends JPanel {
    private static final String RERUN = "Rerun the Analysis";
    private static final String ELEMENTS_PROCESSED = "%d Elements Processed";
    private JButton selectRecordRootButton = new JButton("Select Record Root >> ");
    private JButton selectUniqueElementButton = new JButton("Select Unique Element >> ");
    private JButton reanalyzeButton = new JButton(RERUN);
    private JEditorPane statisticsView = new JEditorPane();
    private JTree statisticsJTree;
    private SipModel sipModel;

    public AnalysisFactsPanel(SipModel sipModel) {
        super(new BorderLayout(5, 5));
        this.sipModel = sipModel;
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel left = new JPanel(new GridLayout(0, 1, 5, 5));
        left.add(createTreePanel());
        left.add(createStatisticsPanel());
        JPanel center = new JPanel(new GridLayout(1, 0, 5, 5));
        center.add(left);
        center.add(new FactPanel(sipModel.getFactModel()));
        add(center, BorderLayout.CENTER);
        wireUp();
    }

    private JPanel createTreePanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Document Structure"));
        statisticsJTree = new JTree(sipModel.getAnalysisTreeModel());
        statisticsJTree.getModel().addTreeModelListener(new Expander());
        statisticsJTree.setCellRenderer(new AnalysisTreeCellRenderer());
        statisticsJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        statisticsJTree.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                if (reanalyzeButton.isEnabled()) {
                    if (sipModel.getAnalysisTreeModel().getChildCount(sipModel.getAnalysisTreeModel().getRoot()) == 0) {
                        reanalyzeButton.setEnabled(false);
                        performAnalysis();
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
            }
        });
        p.add(scroll(statisticsJTree), BorderLayout.CENTER);
        JPanel south = new JPanel(new GridLayout(0, 1));
        south.add(reanalyzeButton);
        south.add(createSelectButtonPanel());
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createSelectButtonPanel() {
        JPanel bp = new JPanel(new GridLayout(1, 0, 5, 5));
        selectRecordRootButton.setEnabled(false);
        bp.add(selectRecordRootButton);
        selectUniqueElementButton.setEnabled(false);
        bp.add(selectUniqueElementButton);
        return bp;
    }

    private JPanel createStatisticsPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statisticsView.setContentType("text/html");
        p.add(scroll(statisticsView), BorderLayout.CENTER);
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 500));
        return scroll;
    }

    private void setElementsProcessed(long count) {
        reanalyzeButton.setText(String.format(ELEMENTS_PROCESSED, count));
    }

    private void wireUp() {
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedDataSetStore(FileStore.DataSetStore dataSetStore) {
                reanalyzeButton.setText(RERUN);
            }

            @Override
            public void updatedStatistics(final Statistics statistics) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (statistics == null) {
                            statisticsView.setText("<html><h3>No Statistics</h3>");
                        }
                        else {
                            statisticsView.setText(statistics.toHtml());
                        }
                        statisticsView.setCaretPosition(0);
                    }
                });
            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
            }
        });
        statisticsJTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TreePath path = event.getPath();
                if (statisticsJTree.getSelectionModel().isPathSelected(path)) {
                    final AnalysisTree.Node node = (AnalysisTree.Node) path.getLastPathComponent();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            selectRecordRootButton.setEnabled(node.couldBeRecordRoot());
                            selectUniqueElementButton.setEnabled(!node.couldBeRecordRoot());
                            sipModel.setStatistics(node.getStatistics());
                        }
                    });
                }
                else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            selectRecordRootButton.setEnabled(false);
                            selectUniqueElementButton.setEnabled(false);
                            sipModel.setStatistics(null);
                        }
                    });
                }
            }
        });
        selectRecordRootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = statisticsJTree.getSelectionPath();
                AnalysisTreeNode node = (AnalysisTreeNode) path.getLastPathComponent();
                Path recordRoot = node.getPath();
                if (recordRoot != null) {
                    sipModel.setRecordRoot(recordRoot, node.getStatistics().getTotal());
                }
            }
        });
        selectUniqueElementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = statisticsJTree.getSelectionPath();
                AnalysisTreeNode node = (AnalysisTreeNode) path.getLastPathComponent();
                sipModel.setUniqueElement(node.getPath());
            }
        });
        reanalyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                reanalyzeButton.setEnabled(false);
                performAnalysis();
            }
        });
    }

    private void performAnalysis() {
        sipModel.analyze(new SipModel.AnalysisListener() {

            @Override
            public void finished(boolean success) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setElementsProcessed(sipModel.getElementCount());
                        reanalyzeButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void analysisProgress(final long elementCount) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setElementsProcessed(elementCount);
                    }
                });
            }
        });
    }

    private class AnalysisTreeCellRenderer extends DefaultTreeCellRenderer {
        private Font normalFont, thickFont;

        @Override
        public Component getTreeCellRendererComponent(JTree jTree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(jTree, value, selected, expanded, leaf, row, hasFocus);
            AnalysisTree.Node node = (AnalysisTree.Node) value;
            label.setFont(getNormalFont());
            if (node.isRecordRoot()) {
                label.setFont(getThickFont());
                label.setText(String.format("%s : Record Root", node));
            }
            if (node.isUniqueElement()) {
                label.setFont(getThickFont());
                label.setText(String.format("%s : Unique Element", node));
            }
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

    private class Expander implements TreeModelListener {

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    expandEmptyNodes((AnalysisTree.Node) statisticsJTree.getModel().getRoot());
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        private void expandEmptyNodes(AnalysisTree.Node node) {
            if (node.couldBeRecordRoot()) {
                TreePath path = node.getTreePath();
                statisticsJTree.expandPath(path);
            }
            for (AnalysisTree.Node childNode : node.getChildNodes()) {
                expandEmptyNodes(childNode);
            }
        }
    }
}
