package eu.europeana.sip.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

/**
 * Showing the progress XML processing and provides the user a cancel button
 * to abort the task.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class ProgressDialog extends JDialog {
    private final DecimalFormat format = new DecimalFormat("#########");
    private final JLabel label;
    private final JButton button = new JButton("Abort");
    private final Listener listener;
    private final String caption;

    public interface Listener {

        public void abort();

        public JFrame getFrame();
    }

    public ProgressDialog(String title, String caption, Listener listener) {
        super(listener.getFrame(), title, false);
        this.listener = listener;
        this.caption = caption;
        this.label = new JLabel(caption);
        getContentPane().add(createContent());
        pack();
        setLocationRelativeTo(listener.getFrame());
    }

    private Component createContent() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        label.setFont(new Font("Serif", Font.BOLD, 16));
        p.add(label, BorderLayout.CENTER);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.abort();
            }
        });
        p.add(button, BorderLayout.SOUTH);
        return p;
    }

    public void setProgress(long recordNumber) {
        label.setText(String.format("%s %sk", caption, format.format(recordNumber / 1000)));
    }

    public void setProgress(long recordNumber, long bytes) {
        label.setText(String.format("%s %d records %dKB", caption, recordNumber, bytes / 1024));
    }
}
