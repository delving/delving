package eu.europeana.sip.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * todo: add class description
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class AutoCompleteDialog extends JDialog {

    private JScrollPane availableElementsWindow = new JScrollPane();

    public AutoCompleteDialog() {
        init();
    }

    private void init() {
        setAlwaysOnTop(true);
        setSize(new Dimension(300, 200));
        setUndecorated(true);
        add(availableElementsWindow);
        setVisible(true);

        addKeyListener(
                new KeyAdapter() { // todo: not working

                    private void handleKeyEvent(KeyEvent keyEvent) {
                        switch (keyEvent.getKeyCode()) {
                            case KeyEvent.VK_DOWN:
                                System.out.printf("Down%n");
                                break;
                            case KeyEvent.VK_UP:
                                System.out.printf("Up%n");
                                break;
                            default:
                                System.out.printf("Dont know %s%n", keyEvent);
                        }
                    }

                    @Override
                    public void keyTyped(KeyEvent e) {
                        handleKeyEvent(e);
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        handleKeyEvent(e);
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        handleKeyEvent(e);
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
        availableElementsWindow.getViewport().setView(new JList(availableElements.toArray()));
    }
}
