package eu.europeana.sip.gui;

import eu.europeana.sip.io.GroovyService;
import org.apache.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * The GroovyEditor for creating live Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GroovyEditor extends JPanel implements GroovyService.Listener {

    public final static int VALIDATION_DELAY = 500;
    private final static Logger LOG = Logger.getLogger(GroovyEditor.class.getName());

    private JTextArea codeArea = new JTextArea();
    private JTextArea outputArea = new JTextArea();
    private CompileTimer compileTimer;
    private GroovyService groovyService;

    public GroovyEditor(GroovyService.BindingSource bindingSource) {
        super(new BorderLayout());
        codeArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Code"));
        outputArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Output"));
        this.groovyService = new GroovyService(bindingSource, this);
        this.compileTimer = new CompileTimer();
        codeArea.getDocument().addDocumentListener(compileTimer);
        add(createSplitPane());
    }

    public void setGroovyFile(File groovyFile) {
        groovyService.setGroovyFile(groovyFile);
    }

    private JComponent createSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane scroll;
        split.setTopComponent(scroll = new JScrollPane(codeArea));
        scroll.setPreferredSize(new Dimension(1200, 400));
        split.setBottomComponent(scroll = new JScrollPane(outputArea));
        scroll.setPreferredSize(new Dimension(1200, 400));
        split.setDividerLocation(0.5);
        return split;
    }

    public void triggerExecution() {
        compileTimer.timer.restart();
    }

    @Override
    public void loadComplete(final String groovyCode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                codeArea.setText(groovyCode);
            }
        });
    }

    @Override
    public void compilationResult(final String result) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                outputArea.setText(result);
            }
        });
    }

    private class CompileTimer implements ActionListener, DocumentListener {

        private Timer timer = new Timer(VALIDATION_DELAY, this);

        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            groovyService.setGroovyCode(codeArea.getText());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

    }

    @Override
    public String toString() {
        return "GroovyEditor";
    }
}
