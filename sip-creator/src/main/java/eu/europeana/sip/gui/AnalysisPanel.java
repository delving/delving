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
import eu.europeana.sip.model.SipModel;
import org.apache.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.namespace.QName;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A Graphical interface for analysis
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class AnalysisPanel extends JPanel {
    private Logger log = Logger.getLogger(getClass());

    private static final Dimension PREFERRED_SIZE = new Dimension(400, 700);
    private JButton analyzeButton = new JButton("Perform Analysis");
    private JLabel recordCountLabel = new JLabel("???????? Records Processed");
    private JButton abortButton = new JButton("Abort");

    private JTree statisticsJTree = new JTree(AnalysisTree.create("No Document Loaded").createTreeModel());

    private JTable statsTable;
    private FileMenu.Enablement fileMenuEnablement;
    private boolean abort = false;

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
        add(createTreePanel(), gbc);
        gbc.gridx++;
        add(createStatsSelectPanel(), gbc);
        gbc.gridx++;
        add(createVariablesPanel(), gbc);
        gbc.weighty = 0.01;
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy++;
        add(createProgress(), gbc);
        wireUpTree();
    }

    public void setFileMenuEnablement(FileMenu.Enablement fileMenuEnablement) {
        this.fileMenuEnablement = fileMenuEnablement;
    }

//    public void setFileSet(final FileSet fileSet) {
//        try {
//            final QName recordRoot = fileSet.getRecordRoot();
//            List<Statistics> statistics = fileSet.getStatistics();
//            if (statistics == null) {
//                abort = false;
//                fileMenuEnablement.enable(false);
//                fileSet.analyze(new FileSet.AnalysisListener() {
//                    @Override
//                    public void success(List<Statistics> statistics) {
//                        setMappingTree(AnalysisTree.create(statistics, fileSet.getName(), recordRoot));
//                        fileMenuEnablement.enable(true);
//                    }
//
//                    @Override
//                    public void failure(Exception exception) {
//                        JOptionPane.showMessageDialog(AnalysisPanel.this, "Error analyzing file : '" + exception.getMessage() + "'");
//                        fileMenuEnablement.enable(true);
//                    }
//
//                    @Override
//                    public void progress(final long recordNumber) {
//                        SwingUtilities.invokeLater(new Runnable() {
//                            @Override
//                            public void run() {
////                                    progressDialog.setProgress(recordNumber);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public boolean abort() {
//                        return abort;
//                    }
//
//                });
//            }
//            else {
//                setMappingTree(AnalysisTree.create(statistics, fileSet.getName(), recordRoot));
//            }
//        }
//        catch (IOException e) {
//            JOptionPane.showMessageDialog(null, "Error analyzing file : '" + e.getMessage() + "'");
//        }
//    }

    private JPanel createTreePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Document Structure"));
        statisticsJTree.setCellRenderer(new AnalysisTreeCellRenderer());
        statisticsJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane scroll = new JScrollPane(statisticsJTree);
        scroll.setPreferredSize(PREFERRED_SIZE);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatsSelectPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Statistics"));
        JPanel tablePanel = new JPanel(new BorderLayout());
        statsTable = new JTable(sipModel.getStatisticsTableModel(), createStatsColumnModel());
        statsTable.getTableHeader().setReorderingAllowed(false);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePanel.add(statsTable.getTableHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(statsTable);
        scroll.setPreferredSize(PREFERRED_SIZE);
        tablePanel.add(scroll, BorderLayout.CENTER);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private DefaultTableColumnModel createStatsColumnModel() {
        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        columnModel.addColumn(new TableColumn(0));
        columnModel.getColumn(0).setHeaderValue("Percent");
        columnModel.addColumn(new TableColumn(1));
        columnModel.getColumn(1).setHeaderValue("Count");
        columnModel.addColumn(new TableColumn(2));
        columnModel.getColumn(2).setHeaderValue("Value");
        return columnModel;
    }

    private JPanel createVariablesPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Variables"));
        JList list = new JList(sipModel.getVariablesListModel());
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(PREFERRED_SIZE);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel createProgress() {
        JPanel p = new JPanel();
        p.add(analyzeButton);
        p.add(recordCountLabel);
        p.add(abortButton);
        return p;
    }

    private void wireUpTree() {
        statisticsJTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TreePath path = event.getPath();
                AnalysisTree.Node node = (AnalysisTree.Node) path.getLastPathComponent();
                sipModel.selectNode(node);
            }
        });
        statisticsJTree.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            final TreePath path = statisticsJTree.getPathForLocation(e.getX(), e.getY());
                            statisticsJTree.setSelectionPath(path);
                            JPopupMenu delimiterPopup = new JPopupMenu();
                            JMenuItem delimiterMenuItem = new JMenuItem("Set as delimiter");
                            delimiterMenuItem.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    AnalysisTree.Node node = (AnalysisTree.Node) path.getLastPathComponent();
                                    QName recordRoot = node.getQName();
                                    DefaultTreeModel tm = (DefaultTreeModel) statisticsJTree.getModel();
                                    int count = AnalysisTree.setRecordRoot(tm, recordRoot);
                                    if (count != 1) {
                                        JOptionPane.showConfirmDialog(AnalysisPanel.this, "Expected one record root, got " + count);
                                    }
                                    else {
                                        tm.reload(node);
                                    }
                                }
                            });
                            delimiterPopup.add(delimiterMenuItem);
                            delimiterPopup.show(statisticsJTree, e.getX(), e.getY());
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }
                }
        );
    }

    private void setMappingTree(AnalysisTree analysisTree) {
        TreeModel treeModel = analysisTree.createTreeModel();
        statisticsJTree.setModel(treeModel);
        expandEmptyNodes((AnalysisTree.Node) treeModel.getRoot());
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
}
