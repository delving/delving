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

import eu.delving.metadata.FieldStatistics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * Show statistics in an html panel, with special tricks for separately threading the html generation
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FieldStatisticsPanel extends JPanel {
    private JEditorPane statisticsView = new JEditorPane();

    public FieldStatisticsPanel() {
        super(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Statistics"));
        statisticsView.setEditable(false);
        statisticsView.setContentType("text/html");
        add(scroll(statisticsView), BorderLayout.CENTER);
    }

    public void setStatistics(final FieldStatistics fieldStatistics) {
        if (fieldStatistics == null) {
            statisticsView.setText("<html><h3>No Statistics</h3>");
        }
        else {
            statisticsView.setText("<html><h3>Loading...</h3>");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    statisticsView.setText(fieldStatistics.toHtml());
                    statisticsView.setCaretPosition(0);
                }
            });
        }
    }

    private JScrollPane scroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return scroll;
    }
}
