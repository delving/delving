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
import eu.europeana.sip.xml.Normalizer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormalizerPanel extends JPanel {
    private static final Logger LOG = Logger.getLogger(NormalizerPanel.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private JButton normalizeButton = new JButton("Normalize");
    private JCheckBox debugLevel = new JCheckBox("Debug Mode", false);
    private JLabel progressLabel = new JLabel("Make your choice", JLabel.CENTER);
    private JLabel memoryLabel = new JLabel("Memory", JLabel.CENTER);
    private JButton abort = new JButton("Abort");
    private LogPanel logPanel = new LogPanel();
    private FileSet fileSet;
    private Normalizer normalizer;

    public NormalizerPanel() {
        super(new BorderLayout());
//        Logger.getRootLogger().addAppender(logPanel.createAppender(Normalizer.LOG_LAYOUT));
        add(createWest(), BorderLayout.WEST);
        add(logPanel, BorderLayout.CENTER);
        wireUp();
    }

    public void setFileSet(FileSet fileSet) {
        this.fileSet = fileSet;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                normalizeButton.setEnabled(true);
            }
        });
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
        normalizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                normalize();
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
        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(progressLabel);
        p.add(memoryLabel);
        p.add(normalizeButton);
        abort.setEnabled(false);
        p.add(abort);
        debugLevel.setHorizontalAlignment(JCheckBox.CENTER);
        p.add(debugLevel);
        return p;
    }

    private Runnable finalAct = new Runnable() {
        @Override
        public void run() {
            normalizeButton.setEnabled(true);
            abort.setEnabled(false);
//            list.setEnabled(true);
            logPanel.flush();
//            normalizer = null;
        }
    };

    private void normalize() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                normalizeButton.setEnabled(false);
            }
        });
        normalizer = new Normalizer(fileSet);
        executor.execute(normalizer);
    }
}