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

import eu.europeana.sip.xml.AnalysisParser;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog which shows the load progress.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class ProgressDialog extends JDialog implements AnalysisParser.Listener {

    private final JLabel caption = new JLabel();
    private boolean running = true;

    public ProgressDialog(Frame parent, String title) {
        super(parent, title);
        this.setLocationRelativeTo(parent);
        configure();
    }

    private void configure() {
        caption.setText("Loading ...");
        JButton jButton = new JButton("Cancel");
        jButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        running = false;
                    }
                }
        );
        this.setLayout(new GridLayout(2, 0));
        this.add(caption);
        this.add(jButton);
        this.pack();
    }

    public void setMessage(String message) {
        caption.setText(message);
    }

    @Override
    public boolean running() {
        return running;
    }

    @Override
    public void finished() {
        this.dispose();
    }

    @Override
    public void updateProgressValue(String progressValue) {
        setMessage(String.format("Processed %s elements", progressValue));
    }
}
