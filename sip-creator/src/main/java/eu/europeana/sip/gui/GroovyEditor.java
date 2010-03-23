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
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The GroovyEditor for creating live Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditor extends JTextArea {

    public final static int VALIDATION_DELAY = 500;

    private File mappingFile;
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
        this.listener = listener;
        init();
    }

    public GroovyEditor(Listener listener, File mappingFile) {
        this.listener = listener;
        this.mappingFile = mappingFile;
        init();
    }

    public GroovyEditor(Listener listener, BindingSource bindingSource, File mappingFile) {
        this(listener, mappingFile);
        this.bindingSource = bindingSource;
        this.mappingFile = mappingFile;
    }

    public void triggerExecution() {
        timer.restart();
    }

    private void init() {
        if (null == mappingFile) {
            mappingFile = new File("Groovy.mapping");
        }
        groovyPersistor = new GroovyPersistorImpl(mappingFile);
        if (mappingFile.exists()) {
            try {
                this.setText(groovyPersistor.read(mappingFile));
            }
            catch (IOException e) {
                e.printStackTrace();  // todo: handle catch
            }
        }
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
            groovyPersistor.save(new StringBuffer(getText()));
            return writer.toString();
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
        ((GroovyPersistorImpl) groovyPersistor).setMappingFile(mappingFile);
    }

    @Override
    public String toString() {
        return "GroovyEditor";
    }
}
