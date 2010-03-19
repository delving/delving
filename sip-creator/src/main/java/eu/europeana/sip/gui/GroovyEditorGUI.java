package eu.europeana.sip.gui;

import eu.europeana.sip.xml.NormalizationParser;
import eu.europeana.sip.xml.QNameBuilder;
import groovy.util.Node;

import javax.swing.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class GroovyEditorGUI extends JFrame {

    private static final String DEFAULT_INPUT_FILE = "92001_Ag_EU_TELtreasures.xml"; // todo: replace
    private JTextArea outputWindow = new JTextArea();
    private NormalizationParser normalizationParser;

    public GroovyEditorGUI(File inputFile) throws FileNotFoundException, XMLStreamException {
        super("Standalone Groovy editor");
        setLayout(new BorderLayout());
        setSize(1200, 800);
        add(new JScrollPane(new GroovyEditor(
                new GroovyEditor.Listener() {
                    @Override
                    public void update(boolean result) {

                    }
                }
        )), BorderLayout.CENTER);
        add(new JScrollPane(outputWindow), BorderLayout.SOUTH);
        QName recordRoot = QNameBuilder.createQName("record");
        normalizationParser = new NormalizationParser(new FileInputStream(inputFile), recordRoot);
        for (Node node : normalizationParser) {
            // todo: pass node to GroovyCompiler
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
