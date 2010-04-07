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

import eu.europeana.sip.mapping.MappingTree;
import eu.europeana.sip.mapping.Statistics;
import eu.europeana.sip.xml.FileHandler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(15, 15, 15, 15);
    private JLabel title = new JLabel("Document Structure", JLabel.CENTER);
    private JTree statisticsJTree = new JTree(MappingTree.create("No Document Loaded").createTreeModel());
    private JLabel statsTitle = new JLabel("Statistics", JLabel.CENTER);
    private JTable statsTable;
    private DefaultTableColumnModel statisticsTableColumnModel;
    private FileMenu.Enablement fileMenuEnablement;
    private ProgressDialog progressDialog;

    private GroovyEditor groovyEditor = new GroovyEditor();
    private JButton next = new JButton("Next");
    private File analyzedFile;
    private static final String DEFAULT_RECORD = "record";

    public interface RecordChangeListener {

        void recordRootChanged(File file, QName recordRoot);
    }

    public AnalyzerPanel() {
        super(new BorderLayout());
        createStatsTable();
        this.statsTitle.setFont(new Font("Serif", Font.BOLD, 20));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(createAnalysisPanel());
        split.setRightComponent(createMappingPanel());
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        add(split, BorderLayout.CENTER);
    }

    private Component createMappingPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(groovyEditor, BorderLayout.CENTER);
        p.add(createNextButton(), BorderLayout.NORTH);
        p.setPreferredSize(new Dimension(500, 800));
        return p;
    }

    private JComponent createNextButton() {
        next.setEnabled(false);
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                groovyEditor.triggerExecution();
            }
        });
        return next;
    }

    private Component createAnalysisPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setTopComponent(createDocumentTreePanel());
        split.setBottomComponent(createStatisticsListPanel());
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        return split;
    }

    private Component createDocumentTreePanel() {
        final AnalysisTreeCellRenderer analysisTreeCellRenderer = new AnalysisTreeCellRenderer();
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 0));
        title.setFont(new Font("Serif", Font.BOLD, 22));
        p.add(title, BorderLayout.NORTH);
        statisticsJTree.setCellRenderer(analysisTreeCellRenderer);
        statisticsJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        statisticsJTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                TreePath path = event.getPath();
                MappingTree.Node node = (MappingTree.Node) path.getLastPathComponent();
                setStatistics(node.getStatistics());
            }
        });
        statisticsJTree.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            final TreePath path = statisticsJTree.getPathForLocation(e.getX(), e.getY());
                            statisticsJTree.setSelectionPath(path);
                            JPopupMenu jPopupMenu = new JPopupMenu();
                            JMenuItem jMenuItem = new JMenuItem("Set as delimiter");
                            jMenuItem.addActionListener(
                                    new ActionListener() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            QName recordRoot = new QName(path.getPath()[path.getPath().length - 1].toString());
                                            groovyEditor.recordRootChanged(analyzedFile, recordRoot);
                                            analysisTreeCellRenderer.setSelectedPath(path.getPath()[path.getPath().length - 1].toString());
                                        }
                                    }
                            );
                            jPopupMenu.add(jMenuItem);
                            jPopupMenu.show(statisticsJTree, e.getX(), e.getY());
                        }
                    }
                }
        );
        JScrollPane scroll = new JScrollPane(statisticsJTree);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private Component createStatisticsListPanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(statsTable.getTableHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(statsTable);
        tablePanel.add(scroll, BorderLayout.CENTER);
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(EMPTY_BORDER);
        p.add(statsTitle, BorderLayout.NORTH);
        p.add(tablePanel, BorderLayout.CENTER);
        return p;
    }

    private void createStatsTable() {
        statisticsTableColumnModel = new DefaultTableColumnModel();
        statisticsTableColumnModel.addColumn(new TableColumn(0, 70));
        statisticsTableColumnModel.getColumn(0).setHeaderValue("Percent");
        statisticsTableColumnModel.getColumn(0).setResizable(false);
        statisticsTableColumnModel.addColumn(new TableColumn(1, 90));
        statisticsTableColumnModel.getColumn(1).setHeaderValue("Count");
        statisticsTableColumnModel.getColumn(1).setResizable(false);
        statisticsTableColumnModel.addColumn(new TableColumn(2));
        statisticsTableColumnModel.getColumn(2).setHeaderValue("Value");
        statisticsTableColumnModel.getColumn(2).setPreferredWidth(300);
        statsTable = new JTable(new StatisticsCounterTableModel(null), statisticsTableColumnModel);
        statsTable.getTableHeader().setReorderingAllowed(false);
        statsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void setStatistics(Statistics statistics) {
        if (statistics == null) {
            statsTitle.setText("Statistics");
            statsTable.setModel(new StatisticsCounterTableModel(null));
            statsTable.setColumnModel(statisticsTableColumnModel);
        }
        else {
            statsTitle.setText("Statistics for \"" + statistics.getPath().getLastNodeString() + "\"");
            statsTable.setModel(new StatisticsCounterTableModel(statistics.getCounters()));
            statsTable.setColumnModel(statisticsTableColumnModel);
        }
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
        groovyEditor.recordRootChanged(analyzedFile, new QName(DEFAULT_RECORD));
    }

    public void analyze(final File file) {
        this.analyzedFile = file;
        loadStarted();
        FileHandler.Listener listener = new FileHandler.Listener() {
            @Override
            public void success(List<Statistics> list) {
                setMappingTree(MappingTree.create(list, file.getName()));
                File mappingFile = createMappingFile(file);
                groovyEditor.setGroovyFile(mappingFile);
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
        File statisticsFile = createStatisticsFile(file);
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

    private File createMappingFile(File file) {
        return new File(file.getParentFile(), file.getName() + ".mapping");

    }
}
