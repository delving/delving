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

import eu.delving.metadata.Path;
import eu.delving.metadata.Statistics;
import eu.delving.sip.FileStore;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.model.SipModel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class NormalizationPanel extends JPanel {
    private SipModel sipModel;
    private JCheckBox discardInvalidBox = new JCheckBox("Discard Invalid Records");
    private JCheckBox storeNormalizedBox = new JCheckBox("Store Normalized XML");
    private JLabel normalizeMessageLabel = new JLabel("?", JLabel.CENTER);

    public NormalizationPanel(SipModel sipModel) {
        super(new BorderLayout(5, 5));
        this.sipModel = sipModel;
        JPanel center = new JPanel(new GridLayout(1, 0, 5, 5));
        center.add(new RecordPanel(sipModel, sipModel.getRecordCompileModel()));
        center.add(createCodePanel());
        center.add(createOutputPanel());
        add(center, BorderLayout.CENTER);
        add(createNormalizePanel(), BorderLayout.SOUTH);
        wireUp();
    }

    private JPanel createCodePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Groovy Code"));
        JTextArea area = new JTextArea(sipModel.getRecordCompileModel().getCodeDocument());
        area.setEditable(false);
        p.add(scroll(area));
        return p;
    }

    private JPanel createOutputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Output Record"));
        JTextArea area = new JTextArea(sipModel.getRecordCompileModel().getOutputDocument());
        area.setEditable(false);
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
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(normalizeMessageLabel, BorderLayout.CENTER);
        p.add(createNormalizeEast(), BorderLayout.EAST);
        return p;
    }

    private JPanel createNormalizeEast() {
        JPanel p = new JPanel(new GridLayout(1, 0, 5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Control"));
        p.add(discardInvalidBox);
        p.add(storeNormalizedBox);
        p.add(new JButton(normalizeAction));
        return p;
    }

    private void wireUp() {
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedDataSetStore(FileStore.DataSetStore store) {
            }

            @Override
            public void updatedStatistics(Statistics statistics) {
            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
                normalizeMessageLabel.setText(message);
            }
        });
    }

    private Action normalizeAction = new AbstractAction("Normalize") {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ProgressMonitor progressMonitor = new ProgressMonitor(SwingUtilities.getRoot(NormalizationPanel.this), "Normalizing", "Normalizing  " + sipModel.getDataSetStore().getSpec(), 0, 100);
            sipModel.normalize(discardInvalidBox.isSelected(), storeNormalizedBox.isSelected(), new ProgressListener.Adapter(progressMonitor, this));
        }
    };
}