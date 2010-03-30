package eu.europeana.sip.gui;

import eu.europeana.sip.io.GroovyService;
import jsyntaxpane.DefaultSyntaxKit;
import org.apache.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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


    private JEditorPane codeArea = new JEditorPane();
    private JTextArea outputArea = new JTextArea();
    private CompileTimer compileTimer;
    private GroovyService groovyService;

    public GroovyEditor(GroovyService.BindingSource bindingSource) {
        super(new BorderLayout());
        DefaultSyntaxKit.initKit();
        add(createSplitPane());
        outputArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Output"));
        this.groovyService = new GroovyService(bindingSource, this);
        this.compileTimer = new CompileTimer();
        this.codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                compileTimer.triggerSoon();
            }
        });
        codeArea.setContentType("text/groovy");
    }

    public void setGroovyFile(File groovyFile) {
        groovyService.setGroovyFile(groovyFile);
    }

    private JComponent createSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane scroll;
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Code"));
        top.add(scroll = new JScrollPane(codeArea));
        split.setTopComponent(top);
        scroll.setPreferredSize(new Dimension(1200, 400));
        scroll.doLayout();
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
                compileTimer.triggerSoon();
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

    private class CompileTimer implements ActionListener {

        private Timer timer = new Timer(VALIDATION_DELAY, this);

        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            groovyService.setGroovyCode(codeArea.getText());
        }

        public void triggerSoon() {
            timer.restart();
        }

    }
    @Override
    public String toString() {
        return "GroovyEditor";
    }
}
