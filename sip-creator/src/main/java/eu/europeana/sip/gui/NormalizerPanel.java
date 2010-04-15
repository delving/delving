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

import eu.europeana.sip.io.FileSet;
import eu.europeana.sip.io.GroovyService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormalizerPanel extends JPanel {
    private static final long MEGABYTE = 1024L * 1024L;
    private static final long KILOBYTE = 1024L;
    private static String newLine = (null != System.getProperty("line.separator")) ? System.getProperty("line.separator") : "\n";

    private JButton normalize = new JButton("Normalize");
    private JCheckBox debugLevel = new JCheckBox("Debug Mode", false);
    private JLabel progressLabel = new JLabel("Make your choice", JLabel.CENTER);
    private JLabel memoryLabel = new JLabel("Memory", JLabel.CENTER);
    private JButton abort = new JButton("Abort");
    private LogPanel logPanel = new LogPanel();
    private static final Logger LOG = Logger.getLogger(NormalizerPanel.class);
    private static long bytesWritten;
    private static long nodesWritten;

    private ProgressDialog progressDialog;

    private NormalizerPanel instance;

    private GroovyService groovyService = new GroovyService(new GroovyService.Listener() {

        @Override
        public void setMapping(String mapping) {

        }

        @Override
        public void setResult(String result) {
            try {
                groovyService.getFileSet().getOutputStream().write((result + newLine).getBytes(), 0, result.getBytes().length); // todo: exceptions are also written
                bytesWritten += result.getBytes().length;
                nodesWritten++;
                memoryLabel.setText(String.format("Free %sMB/Available %sMB", Runtime.getRuntime().freeMemory() / MEGABYTE, (Runtime.getRuntime().maxMemory() / MEGABYTE)));
                Logger.getRootLogger().info(String.format("Written so far [%dKB] ; [%d nodes]%n", bytesWritten / KILOBYTE, nodesWritten));
                progressDialog.setProgress(nodesWritten, bytesWritten);
            }
            catch (IOException e) {
                LOG.error("Error writing to outputStream", e);
            }
        }
    });

    public NormalizerPanel() {
        super(new BorderLayout());
        this.instance = this;
//        Logger.getRootLogger().addAppender(logPanel.createAppender(Normalizer.LOG_LAYOUT));
        add(createWest(), BorderLayout.WEST);
        add(logPanel, BorderLayout.CENTER);
        wireUp();
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
        normalize.addActionListener(

                new ActionListener() {

                    GroovyService.Normalizer normalizer;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            progressDialog = new ProgressDialog("Normalize", "Normalized",
                                    new ProgressDialog.Listener() {

                                        @Override
                                        public void abort() {
                                            progressDialog.setVisible(false);
                                            normalizer.abort();
                                        }

                                        @Override
                                        public JFrame getFrame() {
                                            return (JFrame) SwingUtilities.getWindowAncestor(instance);
                                        }
                                    }
                            );
                            progressDialog.setVisible(true);
                            normalizer = groovyService.normalize();
                            abort.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (normalizer != null) {
                                        normalizer.abort();
                                    }
                                }
                            });
                        }
                        catch (FileNotFoundException exception) {
                            Logger.getRootLogger().error("Error writing to file", exception);
                        }
                    }
                }
        );
    }

//    private JPanel createaWest() {
//        list.setCellRenderer(new CellRenderer());
//        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        JPanel p = new JPanel(new BorderLayout());
//        p.add(new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
//        p.add(createSouthWest(), BorderLayout.SOUTH);
//        return p;
//    }

    private JPanel createWest() {
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

//    private static class ProfileListModel extends AbstractListModel {
//        private static final long serialVersionUID = 939393939;
//        private List<File> list;
//
//        public ProfileListModel(File sourceRoot) {
//            list = Normalizer.getSourcesWithProfiles(sourceRoot);
//        }
//
//        @Override
//        public int getSize() {
//            return list.size();
//        }
//
//        @Override
//        public Object getElementAt(int index) {
//            return list.get(index);
//        }
//    }
//
//    private static class CellRenderer extends DefaultListCellRenderer {
//        @Override
//        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//            File file = (File) value;
//            return super.getListCellRendererComponent(list, file.getName(), index, isSelected, cellHasFocus);
//        }
//    }

    private Runnable finalAct = new Runnable() {
        @Override
        public void run() {
            normalize.setEnabled(true);
            abort.setEnabled(false);
//            list.setEnabled(true);
            logPanel.flush();
//            normalizer = null;
        }     
    };

    public void setFileSet(FileSet fileSet) {
        groovyService.setFileSet(fileSet);
    }
}