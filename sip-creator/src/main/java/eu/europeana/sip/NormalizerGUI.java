package eu.europeana.sip;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormalizerGUI extends JFrame {
    private static final long MEGABYTE = 1024L*1024L;
    private JButton normalize = new JButton("Normalize");
    private JCheckBox debugLevel = new JCheckBox("Debug Mode", false);
    private JLabel progressLabel = new JLabel("Make your choice", JLabel.CENTER);
    private JLabel memoryLabel = new JLabel("Memory", JLabel.CENTER);
    private JButton abort = new JButton("Abort");
    private LogPanel logPanel = new LogPanel();
    private JList list;
    private File destinationRoot;
    private Normalizer normalizer;

    private NormalizerGUI(File sourceRoot, File destinationRoot) {
        super("Europeana Normalizer");
        this.destinationRoot = destinationRoot;
        list = new JList(new ProfileListModel(sourceRoot));
        Logger.getRootLogger().addAppender(logPanel.createAppender(Normalizer.LOG_LAYOUT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(createWest(), BorderLayout.WEST);
        getContentPane().add(logPanel, BorderLayout.CENTER);
        wireUp();
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }

    private void wireUp() {
        debugLevel.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                switch (e.getStateChange()) {
                    case ItemEvent.DESELECTED:
                        Logger.getRootLogger().setLevel(Level.INFO);
                        break;
                    case ItemEvent.SELECTED:
                        Logger.getRootLogger().setLevel(Level.DEBUG);
                        break;
                }
            }
        });
        normalize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                File file = (File) list.getSelectedValue();
                if (file != null) {
                    try {
                        normalize.setEnabled(false);
                        list.setEnabled(false);
                        abort.setEnabled(true);
                        normalizer = new Normalizer(file, destinationRoot, false);
                        normalizer.setFinalAct(finalAct);
                        normalizer.setProgress(new Normalizer.Progress() {
                            @Override
                            public void message(final String message) {
                                try {
                                    SwingUtilities.invokeAndWait(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressLabel.setText(message);
                                            memoryLabel.setText(
                                                    (Runtime.getRuntime().freeMemory()/MEGABYTE)+"/"+
                                                            (Runtime.getRuntime().maxMemory()/MEGABYTE));
                                        }
                                    });
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void recordsProcessed(final int count) {
                                message("Records Processed: " + count);
                            }
                        });
                        Thread thread = new Thread(normalizer);
                        thread.setName(normalizer.toString());
                        thread.start();
                    }
                    catch (Exception e) {
                        Logger.getRootLogger().fatal("Unable to parse profile.xml!", e);
                        finalAct.run();
                        e.printStackTrace();
                    }
                }
            }
        });
        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (normalizer != null) {
                    normalizer.abort();
                }
            }
        });
    }

    private JPanel createWest() {
        list.setCellRenderer(new CellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        p.add(createSouthWest(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createSouthWest() {
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(progressLabel);
        p.add(memoryLabel);
        p.add(normalize);
        abort.setEnabled(false);
        p.add(abort);
        debugLevel.setHorizontalAlignment(JCheckBox.CENTER);
        p.add(debugLevel);
        return p;
    }

    private static class ProfileListModel extends AbstractListModel {
        private static final long serialVersionUID = 939393939;
        private List<File> list;

        public ProfileListModel(File sourceRoot) {
            list = Normalizer.getSourcesWithProfiles(sourceRoot);
        }

        @Override
        public int getSize() {
            return list.size();
        }

        @Override
        public Object getElementAt(int index) {
            return list.get(index);
        }
    }

    private static class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            File file = (File) value;
            return super.getListCellRendererComponent(list, file.getName(), index, isSelected, cellHasFocus);
        }
    }

    private Runnable finalAct = new Runnable() {
        @Override
        public void run() {
            normalize.setEnabled(true);
            abort.setEnabled(false);
            list.setEnabled(true);
            logPanel.flush();
            normalizer = null;
        }
    };

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.INFO);
        String fromDir = ".";
        if (args.length > 0) {
            fromDir = args[0];
        }
        String toDir = ".";
        if (args.length > 1) {
            toDir = args[1];
        }
        final File sourceRoot = new File(fromDir);
        final File destinationRoot = new File(toDir);
        NormalizerGUI normalizerGUI = new NormalizerGUI(sourceRoot, destinationRoot);
        normalizerGUI.setVisible(true);
    }
}