package org.oclc.oai.harvester.app;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * A Graphical User Interface for the harvester
 *
 * @author Gerald de Jong <geralddejong@gmai.com>
 */

public class HarvesterGUI extends JFrame {
    private Harvester.Control control;
    private Action startSelectedAction = new StartSelectedAction();
    private Action startAllAction = new StartAllAction();
    private Action abortAction = new AbortAction();
    private HarvestTask selectedTask;
    private WriterAppender appender = new Log4JAppender();
    private HarvestTableModel tableModel;
    private JTabbedPane logTab = new JTabbedPane();
    private JTextArea globalLog = new JTextArea();
    private JTextArea localLog = new JTextArea();
    private File outputDirectory;
    private int logCount;

    public HarvesterGUI(HarvestConfig config, Harvester.Control control) {
        super("Harvester");
        this.control = control;
        this.outputDirectory = config.outputDirectory;
        Logger.getRootLogger().addAppender(appender);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = getContentPane();
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                createTopPanel(config.harvests),
                createLogPanel()
        );
        split.setDividerLocation(0.5);
        split.setBorder(BorderFactory.createTitledBorder("Harvest Tasks"));
        cp.add(split, BorderLayout.CENTER);
        pack();
    }

    private JComponent createLogPanel() {
        globalLog.setEditable(false);
        JPanel p = new JPanel(new BorderLayout());
        JScrollPane globalPane = new JScrollPane(
                globalLog,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        logTab.addTab("Global Log", globalPane);
        JScrollPane localPane = new JScrollPane(
                localLog,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        logTab.addTab("Local Log", localPane);
        p.add(logTab);
        p.setPreferredSize(new Dimension(800, 300));
        return p;
    }

    private Component createTopPanel(List<HarvestConfig.Harvest> harvests) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(createTable(harvests), BorderLayout.CENTER);
        p.add(createControlPanel(), BorderLayout.EAST);
        return p;
    }

    private Component createControlPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(new JButton(startSelectedAction), gbc);
        gbc.gridy++;
        p.add(new JButton(startAllAction), gbc);
        gbc.gridy++;
        p.add(new JButton(abortAction), gbc);
        return p;
    }

    private Component createTable(List<HarvestConfig.Harvest> harvests) {
        tableModel = new HarvestTableModel(harvests);
        final JTable table = new JTable(tableModel, HarvestTask.getTableColumnModel(tableModel.getCellRenderer()));
        table.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                int row = table.getSelectedRow();
                selectedTask = ((HarvestTableModel) table.getModel()).getRow(row);
                setEnablement();
            }
        });
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                TableColumnModel columnModel = table.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
                int column = table.convertColumnIndexToModel(viewColumn);
                if (mouseEvent.getClickCount() == 1 && column != -1) {
                    ((HarvestTableModel) table.getModel()).sortOn(column);
                }
            }
        });
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                int row = table.getSelectedRow();
                selectedTask = ((HarvestTableModel) table.getModel()).getRow(row);
                if (selectedTask != null) {
                    fetchLocalLog();
                }
                setEnablement();
            }
        });
        JScrollPane pane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setPreferredSize(new Dimension(800, 300));
        return pane;
    }

    private void fetchLocalLog() {
        // todo: when the log is very very large, this fails with OutOfMemoryError
        final File logFile = new File(outputDirectory, selectedTask.getOutput() + ".log");
        logTab.setTitleAt(1, "Local log: " + logFile.getName());
        if (logFile.exists()) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    final StringBuilder logText = new StringBuilder();
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(logFile));
                        String line;
                        while ((line = in.readLine()) != null) {
                            logText.append(line);
                            logText.append("\n");
                        }
                    }
                    catch (IOException e) {
                        logText.append("Cannot load: ").append(logFile.getAbsolutePath());
                        logText.append(e.toString());
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            localLog.setText(logText.toString());
                        }
                    });
                }
            });
            thread.start();
        }
        else {
            localLog.setText("No log found: " + logFile.getAbsolutePath());
        }
    }

    private void setEnablement() {
        startSelectedAction.setEnabled(selectedTask != null && HarvestTask.isStartable(selectedTask.getStatus()));
        abortAction.setEnabled(selectedTask != null && !HarvestTask.isStartable(selectedTask.getStatus()));
    }

    private class StartSelectedAction extends AbstractAction {
        private StartSelectedAction() {
            super("Start Selected");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent event) {
            if (selectedTask == null) return;
            control.add(selectedTask);
            setEnablement();
        }
    }

    private class StartAllAction extends AbstractAction {
        private StartAllAction() {
            super("Start All");
        }

        public void actionPerformed(ActionEvent event) {
            int answer = JOptionPane.showConfirmDialog(
                    HarvesterGUI.this,
                    "Are you sure you want to start all?",
                    "Start All",
                    JOptionPane.YES_NO_OPTION
            );
            System.out.println("answer "+answer);
            if (answer == JOptionPane.YES_OPTION) {
                for (HarvestTask task : tableModel.getTasks()) {
                    if (HarvestTask.isStartable(task.getStatus())) {
                        control.add(task);
                    }
                }
                setEnablement();
            }
        }
    }

    private class AbortAction extends AbstractAction {
        private AbortAction() {
            super("Abort Selected");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent event) {
            if (selectedTask == null) return;
            selectedTask.abort();
            setEnablement();
        }
    }

    private class Log4JAppender extends WriterAppender {
        private Log4JAppender() {
            this.layout = new PatternLayout("%d{HH:mm:ss} %-5p %m%n");
        }

        public void append(LoggingEvent loggingEvent) {
            SwingUtilities.invokeLater(new LogMessageAppender(this.layout.format(loggingEvent)));
        }
    }

    private class LogMessageAppender implements Runnable {
        private String message;

        LogMessageAppender(String theMessage) {
            message = theMessage;
        }

        public void run() {
            if (logCount++ >= 1000) {
                String text = globalLog.getText();
                int split = text.length() / 2;
                split = text.indexOf('\n', split);
                text = text.substring(split);
                globalLog.setText(text);
                logCount = 0;
            }
            globalLog.append(message);
//            globalLog.append("\n");
            globalLog.setCaretPosition(globalLog.getText().length());
        }
    }
}
