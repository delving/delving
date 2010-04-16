package eu.europeana.sip.gui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * GUI for the AutoCompletionImpl.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class AutoCompleteDialog extends JFrame {

    private Listener listener;
    private JTextComponent parent;
    private Point lastCaretPosition;
    private JComboBox jComboBox = new JComboBox();

    interface Listener {

        void itemSelected(Object selectedItem);
    }

    public AutoCompleteDialog(Listener listener, JTextComponent parent) {
        this.listener = listener;
        this.parent = parent;
        add(jComboBox);
        init();
    }

    private void init() {
        jComboBox.setEditable(true);
        jComboBox.getEditor().getEditorComponent().addKeyListener(
                new KeyAdapter() {

                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_ESCAPE: {
                                setVisible(false);
                                parent.requestFocus();
                            }
                        }
                    }
                }
        );
        jComboBox.addItemListener(
                new ItemListener() {

                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (ItemEvent.SELECTED == e.getStateChange()) {
                            listener.itemSelected(e.getItem());
                        }
                    }
                }
        );
        setUndecorated(true);
    }

    public void updateLocation(Point caretLocation, Point editorLocation) {
        if (null == caretLocation) {
            return;
        }
        Point point = new Point(
                (int) caretLocation.getX() + (int) editorLocation.getX(),
                (int) caretLocation.getY() + (int) editorLocation.getY() + 16 // todo: get caret height
        );
        setLocation(point);
    }

    public void updateElements(List<String> availableElements) {
        if (null == availableElements) {
            setVisible(false);
            parent.requestFocus();
            return;
        }
        jComboBox.setModel(new DefaultComboBoxModel(availableElements.toArray()));
        setVisible(true);
        setSize(new Dimension(300, 20));
    }

    public void requestFocus(Point lastCaretPosition) {
        this.lastCaretPosition = lastCaretPosition;
    }
}
