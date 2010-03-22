package eu.europeana.sip.gui;

import eu.europeana.sip.io.GroovyPersistor;
import eu.europeana.sip.io.GroovyPersistorImpl;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The GroovyEditor for creating live Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditor extends JTextArea {

    public final static int VALIDATION_DELAY = 500;

    private static final String DEFAULT_FILE_NAME = "File.mapping";
    private GroovyPersistor groovyPersistor;
    private Listener listener;
    private BindingSource bindingSource;
    private Timer timer =
            new Timer(VALIDATION_DELAY,
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            listener.update(executeGroovyCode());
                            timer.stop();
                        }
                    }
            );

    public interface Listener {
        void update(String result);
    }

    public interface BindingSource {
        Binding createBinding(Writer writer);
    }

    public GroovyEditor(Listener listener) {
        init();
        this.listener = listener;
    }

    public GroovyEditor(Listener listener, BindingSource bindingSource) {
        this(listener);
        this.bindingSource = bindingSource;
    }

    public void triggerExecution() {
        timer.restart();
    }

    private void init() {
        // todo: retrieve working directory
        File mappingFile = new File(DEFAULT_FILE_NAME);
        groovyPersistor = new GroovyPersistorImpl(mappingFile);
        this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.getDocument().addDocumentListener(
                new DocumentListener() {
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
        );
    }

    private String executeGroovyCode() {
        try {
            StringWriter writer = new StringWriter();
            GroovyShell shell = new GroovyShell(bindingSource.createBinding(writer));
            shell.evaluate(getText());
            return writer.toString();
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    @Override
    public String toString() {
        return "GroovyEditor";
    }
}
