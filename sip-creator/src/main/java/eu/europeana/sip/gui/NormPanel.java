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

import eu.europeana.sip.model.SipModel;
import eu.europeana.sip.xml.Normalizer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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

public class NormPanel extends JPanel {
    private static final Logger LOG = Logger.getLogger(NormPanel.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SipModel sipModel;
    private JButton normalizeButton = new JButton("Normalize");
    private JCheckBox debugLevel = new JCheckBox("Debug Mode", false);
    private JButton abort = new JButton("Abort");
    private Normalizer normalizer;

    public NormPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.99;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        add(createInputPanel(), gbc);
        gbc.gridx++;
        add(createCodePanel(), gbc);
        gbc.gridx++;
        add(createOutputPanel(), gbc);
        gbc.weighty = 0.01;
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy++;
        add(createNormalizePanel(), gbc);
        wireUp();
    }

    private JPanel createInputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Input Record"));
        JTextArea area = new JTextArea(sipModel.getInputDocument());
        p.add(scroll(area), BorderLayout.CENTER);
        p.add(createInputButtons(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel createInputButtons() {
        JButton rewind = new JButton("Rewind");
        rewind.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.firstRecord();
            }
        });
        JButton next = new JButton("Next");
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.nextRecord();
            }
        });
        JPanel p = new JPanel(new GridLayout(1, 0, 5, 5));
        p.add(rewind);
        p.add(next);
        return p;
    }

    private JPanel createCodePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        JTextArea area = new JTextArea(sipModel.getCodeDocument());
        p.add(scroll(area));
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Record"));
        JTextArea area = new JTextArea(sipModel.getOutputDocument());
        p.add(scroll(area));
        return p;
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(300, 800));
        return scroll;
    }

    private JPanel createNormalizePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.add(normalizeButton, BorderLayout.WEST);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setString("Progress?");
        progressBar.setBorderPainted(true);
        p.add(progressBar);
        return p;
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
//                normalize();
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

//    private JPanel createWest() {
//        JPanel p = new JPanel(new GridLayout(0, 1));
//        p.add(progressLabel);
//        p.add(memoryLabel);
//        p.add(normalizeButton);
//        abort.setEnabled(false);
//        p.add(abort);
//        debugLevel.setHorizontalAlignment(JCheckBox.CENTER);
//        p.add(debugLevel);
//        return p;
//    }
//
//    private Runnable finalAct = new Runnable() {
//        @Override
//        public void run() {
//            normalizeButton.setEnabled(true);
//            abort.setEnabled(false);
////            list.setEnabled(true);
////            normalizer = null;
//        }
//    };

//    private void normalize() {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                normalizeButton.setEnabled(false);
//            }
//        });
//        normalizer = new Normalizer(fileSet);
//        executor.execute(normalizer);
//    }
}