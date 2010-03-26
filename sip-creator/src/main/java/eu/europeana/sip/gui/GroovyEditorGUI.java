package eu.europeana.sip.gui;

import eu.europeana.sip.io.GroovyService;
import eu.europeana.sip.xml.GroovyNode;
import eu.europeana.sip.xml.NormalizationParser;
import eu.europeana.sip.xml.QNameBuilder;
import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Writer;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditorGUI extends JFrame {

    private static final String DEFAULT_INPUT_FILE = "core/src/test/sample-metadata/92001_Ag_EU_TELtreasures.xml"; // todo: replace
    private GroovyEditor groovyEditor = new GroovyEditor(new Source());
    private NormalizationParser normalizationParser;
    private JButton next = new JButton("Next");
    private GroovyNode record;
    private File mappingFile;

    public GroovyEditorGUI() {
        super("Standalone Groovy editor");
        getContentPane().add(createMainPanel());
        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        FileMenu fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(File file) {
                try {
                    prepareInputFile(file);
                    GroovyEditorGUI.this.setTitle("Metadata: " + file.getAbsolutePath());
                    next.setEnabled(true);
                }
                catch (Exception e) {
                    GroovyEditorGUI.this.setTitle("No Metadata");
                    next.setEnabled(false);
                }
                mappingFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")) + ".mapping");
                groovyEditor.setGroovyFile(mappingFile);
            }
        });
        bar.add(fileMenu);
        return bar;
    }

    private JComponent createMainPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(groovyEditor, BorderLayout.CENTER);
        p.add(createNextButton(), BorderLayout.NORTH);
        return p;
    }

    private JComponent createNextButton() {
        next.setEnabled(false);
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextRecord();
                groovyEditor.triggerExecution();
            }
        });
        return next;
    }

    private void prepareInputFile(File inputFile) throws XMLStreamException, FileNotFoundException {
        if (normalizationParser != null) {
            normalizationParser.close();
        }
        QName recordRoot = QNameBuilder.createQName("record");
        normalizationParser = new NormalizationParser(new FileInputStream(inputFile), recordRoot);
        nextRecord();
    }

    private void nextRecord() {
        try {
            record = normalizationParser.nextRecord();
        }
        catch (Exception e) {
            next.setEnabled(false);
        }
    }

    private class Source implements GroovyService.BindingSource {

        @Override
        public Binding createBinding(Writer writer) {
            MarkupBuilder builder = new MarkupBuilder(writer);
            NamespaceBuilder xmlns = new NamespaceBuilder(builder);
            Binding binding = new Binding();
            binding.setVariable("input", record);
            binding.setVariable("output", builder);
            binding.setVariable("dc", xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
            binding.setVariable("dcterms", xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
            binding.setVariable("europeana", xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
            return binding;
        }
    }

    public static void main(String... args) throws FileNotFoundException, XMLStreamException {
        GroovyEditorGUI groovyEditorGUI = new GroovyEditorGUI();
        groovyEditorGUI.setVisible(true);
    }
}
