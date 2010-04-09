package eu.europeana.sip.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * GUI for the AutoCompletionImpl
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class AutoCompleteDialog extends JDialog {

    private JScrollPane availableElementsWindow = new JScrollPane();
    private Listener listener;
    private JList jList = new JList();

    @Override
    public void requestFocus() {
        jList.requestFocus();
        if (-1 == jList.getSelectedIndex()) {
            jList.setSelectedIndex(0);
        }
    }

    interface Listener {
        void itemSelected(Object selectedItem);
    }

    public AutoCompleteDialog(Listener listener) {
        this.listener = listener;
        init();
    }

    private void init() {
        setAlwaysOnTop(true);
        setSize(new Dimension(300, 200));
        setUndecorated(true);
        add(availableElementsWindow);
        setVisible(true);
        jList.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                            selectItem(e);
                        }
                    }
                }
        );
    }

    public void updateLocation(Point caretLocation, Point editorLocation) {
        Point point = new Point(
                (int) caretLocation.getX() + (int) editorLocation.getX(),
                (int) caretLocation.getY() + (int) editorLocation.getY() + 16 // todo: get caret height
        );
        setLocation(point);
    }

    public void updateElements(java.util.List<String> availableElements) {
        assert null != availableElements;
        if (!isVisible()) {
            setVisible(true);
        }
        jList.setListData(availableElements.toArray());
        jList.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        selectItem(e);
                    }
                }
        );
        availableElementsWindow.getViewport().setView(jList);
    }

    private void selectItem(InputEvent inputEvent) {
        setVisible(false);
        listener.itemSelected(((JList) inputEvent.getSource()).getSelectedValue());
    }
}
