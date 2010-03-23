package eu.europeana.sip.gui;

import eu.europeana.sip.xml.NormalizationParser;
import eu.europeana.sip.xml.QNameBuilder;
import groovy.lang.Binding;
import groovy.util.Node;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;

import javax.swing.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Iterator;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditorGUI extends JFrame {

    private static final String DEFAULT_INPUT_FILE = "core/src/test/sample-metadata/92001_Ag_EU_TELtreasures.xml"; // todo: replace
    private GroovyEditor groovyEditor;
    private JTextArea outputTextArea = new JTextArea();
    private NormalizationParser normalizationParser;
    private Node record;
    private Iterator<Node> nodeIterator;
    private FileMenu fileMenu;
    private File mappingFile;

    public GroovyEditorGUI(File inputFile) throws FileNotFoundException, XMLStreamException {
        super("Standalone Groovy editor");
        getContentPane().add(createMainPanel());
        QName recordRoot = QNameBuilder.createQName("record");
        normalizationParser = new NormalizationParser(new FileInputStream(inputFile), recordRoot);
        nodeIterator = normalizationParser.iterator();
        nextRecord();
        setSize(1200, 900);
        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(File file) {
                mappingFile = new File(file.getParent() + "/Groovy.mapping");
            }
        });
        bar.add(fileMenu);
        return bar;
    }

    private JComponent createMainPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(createSplitPane(), BorderLayout.CENTER);
        p.add(createNextButton(), BorderLayout.NORTH);
        return p;
    }

    private JComponent createNextButton() {
        JButton next = new JButton("Next");
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextRecord();
                groovyEditor.triggerExecution();
            }
        });
        return next;
    }

    private JComponent createSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        if (null == mappingFile) {
            try {
                mappingFile = new File("Groovy.mapping").getCanonicalFile();
            }
            catch (IOException e) {
                e.printStackTrace();  // todo: handle catch
            }
        }
        groovyEditor = new GroovyEditor(
                new GroovyEditor.Listener() {
                    @Override
                    public void update(String result) {
                        outputTextArea.setText(result);
                    }
                },
                new Source(),
                mappingFile
        );
        JScrollPane scroll;
        split.setTopComponent(scroll = new JScrollPane(groovyEditor));
        scroll.setPreferredSize(new Dimension(1200, 400));
        split.setBottomComponent(scroll = new JScrollPane(outputTextArea));
        scroll.setPreferredSize(new Dimension(1200, 400));
        split.setDividerLocation(0.5);
        return split;
    }

    private void nextRecord() {
        record = nodeIterator.next();
    }

    private class Source implements GroovyEditor.BindingSource {

        @Override
        public Binding createBinding(Writer writer) {
            MarkupBuilder builder = new MarkupBuilder(writer);
            NamespaceBuilder xmlns = new NamespaceBuilder(builder);
            Binding binding = new Binding();
            binding.setVariable("record", record);
            binding.setVariable("builder", builder);
            binding.setVariable("dc", xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
            binding.setVariable("dcterms", xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
            binding.setVariable("europeana", xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
            return binding;
        }
    }

    public static void main(String... args) throws FileNotFoundException, XMLStreamException {
        File file;
        if (args.length < 1) {
            file = new File(DEFAULT_INPUT_FILE);
        }
        else {
            file = new File(args[0]);
        }
        GroovyEditorGUI groovyEditorGUI = new GroovyEditorGUI(file);
        groovyEditorGUI.setVisible(true);
    }
}
