package eu.europeana.sip.gui;

import eu.europeana.sip.xml.AnalysisParser;

import javax.swing.*;
import java.awt.*;
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
        this.setVisible(true);
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
