package eu.europeana.sip.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog which shows the load progress.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class ProgressDialog extends JDialog {

    private final JLabel caption = new JLabel();

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
                        // TODO: cancel loading action
                        System.out.printf("Cancel pressed%n");
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

    interface Listener {
        public void cancel();
    }

}
