package eu.europeana.sip.gui;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * The GroovyEditor for creating live Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditor extends JTextArea {

    public final static int VALIDATION_DELAY = 500;

    private Listener listener;
    private Map<String, Object> map = new TreeMap<String, Object>();
    private Binding binding = new Binding(map);

    private Timer timer =
            new Timer(VALIDATION_DELAY,
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (validateGroovyCode(binding, getText())) {
                                listener.update(true);
                            }
                            else {
                                listener.update(false);
                            }

                            timer.stop();
                        }
                    }
            );

    public interface Listener {
        void update(boolean result);
    }

    public GroovyEditor(Listener listener) {
        init();
        this.listener = listener;
    }

    public GroovyEditor(Listener listener, Binding binding) {
        this(listener);
        this.binding = binding;
    }

    private void createDefaultBinding() {
        StringWriter out = new StringWriter();
        MarkupBuilder builder = new MarkupBuilder(out);
        NamespaceBuilder xmlns = new NamespaceBuilder(builder);
        map.put("builder", builder);
        map.put("dc", xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
        map.put("dcterms", xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
        map.put("europeaa", xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
    }

    private void init() {
        createDefaultBinding();
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

    public boolean validateGroovyCode(Binding binding, String code) {
        try {
            new GroovyShell(binding).evaluate(code);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    @Override
    public String toString() {
        return "GroovyEditor{" +
                ", binding=" + binding +
                ", timer=" + timer +
                '}';
    }
}
