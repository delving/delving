package eu.europeana.sip.gui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
        if (!isVisible()) {
            setVisible(true);
        }
        jComboBox.setModel(new DefaultComboBoxModel(availableElements.toArray()));
        jComboBox.setVisible(true);
        jComboBox.setPopupVisible(true);
        jComboBox.addItemListener(
                new ItemListener() {

                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        selectItem(e);
                    }
                }
        );
    }

    public void requestFocus(Point lastCaretPosition) {
        this.lastCaretPosition = lastCaretPosition;
        jComboBox.setPopupVisible(true);
    }

    private void selectItem(ItemEvent inputEvent) {
        listener.itemSelected(inputEvent.getItem());
    }
}
