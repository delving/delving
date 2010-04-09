package eu.europeana.sip.gui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
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
    private JTextComponent targetTextPanel;

    public AutoCompleteDialog(JEditorPane targetTextPanel) {
        this.targetTextPanel = targetTextPanel;
        init();
    }

    private void init() {
        setAlwaysOnTop(true);
        setSize(new Dimension(300, 200));
        setUndecorated(true);
        add(availableElementsWindow);
        setVisible(true);
    }

    public void updateLocation(Point caretLocation, Point editorLocation) {
        if (!isVisible()) {
            setVisible(true);
        }
        Point point = new Point(
                (int) caretLocation.getX() + (int) editorLocation.getX(),
                (int) caretLocation.getY() + (int) editorLocation.getY() + 16 // todo: get caret height
        );
        setLocation(point);
    }

    public void updateElements(java.util.List<String> availableElements) {
        final JList jList = new JList(availableElements.toArray());
        jList.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        selectItem(e);
                    }
                }
        );
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
        availableElementsWindow.getViewport().setView(jList);
    }

    private void selectItem(InputEvent inputEvent) {
        targetTextPanel.setText(targetTextPanel.getText() + ((JList) inputEvent.getSource()).getSelectedValue());
        setVisible(false);
    }
}
