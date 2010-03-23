package eu.europeana.sip.gui;

import eu.europeana.sip.io.GroovyService;
import org.apache.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * The GroovyEditor for creating live Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditor extends JTextArea {

    public final static int VALIDATION_DELAY = 500;
    private final static Logger LOG = Logger.getLogger(GroovyEditor.class.getName());

    private File mappingFile;
    private GroovyService groovyService;
    private Listener listener;
    private GroovyService.BindingSource bindingSource;

    private Timer timer =
            new Timer(VALIDATION_DELAY,
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            executeGroovyCode();
                            timer.stop();
                        }
                    }
            );

    private GroovyService.LoadListener loadListener =
            new GroovyService.LoadListener() {
                @Override
                public void loadComplete(String groovySnippet) {
                    setText(groovySnippet);
                }
            };

    public interface Listener {
        void update(String result);
    }

    public GroovyEditor(Listener listener) {
        this.listener = listener;
        init();
    }

    public GroovyEditor(Listener listener, GroovyService.BindingSource bindingSource, File mappingFile) {
        this.listener = listener;
        this.bindingSource = bindingSource;
        this.mappingFile = mappingFile;
        init();
    }

    public void triggerExecution() {
        timer.restart();
    }

    private void init() {
        if (null == mappingFile) {
            mappingFile = new File("Groovy.mapping");
        }
        groovyService = new GroovyService(mappingFile, bindingSource);
        if (mappingFile.exists()) {
            try {
                groovyService.read(mappingFile, loadListener);
            }
            catch (IOException e) {
                LOG.error("Error reading snippet", e);
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

    private void executeGroovyCode() {
        try {
            groovyService.compile(
                    getText(),
                    new GroovyService.CompileListener() {
                        @Override
                        public void compilationResult(String result) {
                            try {
                                groovyService.save(mappingFile, getText());
                            }
                            catch (IOException e) {
                                LOG.error("Error saving snippet", e);
                            }
                            listener.update(result);
                        }
                    }
            );
        }
        catch (Exception e) {
            LOG.error("Error executing code", e);
        }
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
        groovyService.setMappingFile(mappingFile);
        if (mappingFile.exists()) {
            try {
                groovyService.read(mappingFile, loadListener);
            }
            catch (IOException e) {
                LOG.error("Error reading snippet", e);
            }
        }
    }

    @Override
    public String toString() {
        return "GroovyEditor";
    }
}
