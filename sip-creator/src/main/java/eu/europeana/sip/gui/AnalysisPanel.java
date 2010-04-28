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

import eu.europeana.sip.model.AnalysisTree;
import eu.europeana.sip.model.QNameNode;
import eu.europeana.sip.model.RecordRoot;
import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class AnalysisPanel extends JPanel {
    private static final Dimension PREFERRED_SIZE = new Dimension(300, 700);
    private static final String ELEMENTS_PROCESSED = "%d Elements Processed";
    private static final String RECORDS = "%d Records";
    private JButton selectRecordRootButton = new JButton("Select Record Root");
    private JLabel recordCountLabel = new JLabel(String.format(RECORDS, 0L), JLabel.CENTER);
    private JButton analyzeButton = new JButton("Perform Analysis");
    private JLabel elementCountLabel = new JLabel(String.format(ELEMENTS_PROCESSED, 0L), JLabel.CENTER);
    private JButton abortButton = new JButton("Abort");
    private JTree statisticsJTree;
    private SipModel sipModel;

    public AnalysisPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.99;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        gbc.gridheight = 2;
        add(createTreePanel(), gbc);
        gbc.gridheight = 1;
        gbc.gridx++;
        add(createStatisticsPanel(), gbc);
        gbc.gridx++;
        add(createVariablesPanel(), gbc);
        gbc.gridy++;
        gbc.weighty = 0.01;
        gbc.gridwidth = 2;
        gbc.gridx = 1;
        add(createProgress(), gbc);
        wireUp();
    }

    private JPanel createTreePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setPreferredSize(PREFERRED_SIZE);
        p.setBorder(BorderFactory.createTitledBorder("Document Structure"));
        statisticsJTree = new JTree(sipModel.getAnalysisTreeModel());
        statisticsJTree.getModel().addTreeModelListener(new Expander());
        statisticsJTree.setCellRenderer(new AnalysisTreeCellRenderer());
        statisticsJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane scroll = new JScrollPane(statisticsJTree);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        p.add(scroll, BorderLayout.CENTER);
        p.add(createTreeSouth(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createTreeSouth() {
        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        selectRecordRootButton.setEnabled(false);
        p.add(selectRecordRootButton);
        p.add(recordCountLabel);
        return p;
    }

    private JPanel createStatisticsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setPreferredSize(PREFERRED_SIZE);
        p.setBorder(BorderFactory.createTitledBorder("Statistics"));
        JPanel tablePanel = new JPanel(new BorderLayout());
        JTable statsTable = new JTable(sipModel.getStatisticsTableModel(), createStatsColumnModel());
        statsTable.getTableHeader().setReorderingAllowed(false);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePanel.add(statsTable.getTableHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(statsTable);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tablePanel.add(scroll, BorderLayout.CENTER);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private DefaultTableColumnModel createStatsColumnModel() {
        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        columnModel.addColumn(new TableColumn(0));
        columnModel.getColumn(0).setHeaderValue("Percent");
        columnModel.getColumn(0).setMaxWidth(80);
        columnModel.addColumn(new TableColumn(1));
        columnModel.getColumn(1).setHeaderValue("Count");
        columnModel.getColumn(1).setMaxWidth(80);
        columnModel.addColumn(new TableColumn(2));
        columnModel.getColumn(2).setHeaderValue("Value");
        return columnModel;
    }

    private JPanel createVariablesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(PREFERRED_SIZE);
        p.setBorder(BorderFactory.createTitledBorder("Variables"));
        JList list = new JList(sipModel.getVariablesListModel());
        JScrollPane scroll = new JScrollPane(list);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel createProgress() {
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeButton.setEnabled(false);
                abortButton.setEnabled(true);
                sipModel.analyze(new SipModel.AnalysisListener() {

                    @Override
                    public void finished(boolean success) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                analyzeButton.setEnabled(true);
                                setElementsProcessed(sipModel.getElementCount());
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
        });
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.abortAnalyze();
                analyzeButton.setEnabled(true);
            }
        });
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Analysis Process"));
        p.add(analyzeButton, BorderLayout.WEST);
        p.add(elementCountLabel, BorderLayout.CENTER);
        p.add(abortButton, BorderLayout.EAST);
        return p;
    }

    private void setElementsProcessed(long count) {
        elementCountLabel.setText(String.format(ELEMENTS_PROCESSED, count));
    }

    private void wireUp() {
        sipModel.addFileSetListener(new SipModel.FileSetListener() {
            @Override
            public void updatedFileSet() {
                setElementsProcessed(sipModel.getElementCount());
                analyzeButton.setEnabled(true);
                abortButton.setEnabled(true);
            }
        });
        statisticsJTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TreePath path = event.getPath();
                AnalysisTree.Node node = (AnalysisTree.Node) path.getLastPathComponent();
                sipModel.selectNode(node);
                selectRecordRootButton.setEnabled(node.getStatistics() == null);
            }
        });
        selectRecordRootButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = statisticsJTree.getSelectionPath();
                QNameNode node = (QNameNode) path.getLastPathComponent();
                RecordRoot recordRoot = new RecordRoot(node.getQName(), 1000000); // todo: get the real number!
                sipModel.setRecordRoot(recordRoot);
            }
        });
    }

    private class AnalysisTreeCellRenderer extends DefaultTreeCellRenderer {
        private Font normalFont, thickFont;

        @Override
        public Component getTreeCellRendererComponent(JTree jTree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(jTree, value, selected, expanded, leaf, row, hasFocus);
            AnalysisTree.Node node = (AnalysisTree.Node) value;
            label.setFont(node.getStatistics() != null ? getThickFont() : getNormalFont());
            if (node.isRecordRoot()) {
                label.setForeground(Color.RED);
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
            timer.start();
        }

        private void expandEmptyNodes(AnalysisTree.Node node) {
            if (node.getStatistics() == null) {
                TreePath path = node.getTreePath();
                statisticsJTree.expandPath(path);
            }
            for (AnalysisTree.Node childNode : node.getChildNodes()) {
                expandEmptyNodes(childNode);
            }
        }
    }
}
