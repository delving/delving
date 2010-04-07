/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.sip.gui;

import eu.europeana.sip.io.GroovyService;
import eu.europeana.sip.xml.GroovyNode;
import eu.europeana.sip.xml.NormalizationParser;
import eu.europeana.sip.xml.QNameBuilder;
import groovy.lang.Binding;
import groovy.xml.MarkupBuilder;
import groovy.xml.NamespaceBuilder;

import javax.swing.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
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

    private GroovyEditor groovyEditor = new GroovyEditor();
    private NormalizationParser normalizationParser;
    private JButton next = new JButton("Next");
    private GroovyNode record;
    private File mappingFile;
    private JTextField recordRootDelimiterField = new JTextField();

    public GroovyEditorGUI() {
        super("Standalone Groovy editor");
        getContentPane().add(createMainPanel());
        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
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
        p.add(recordRootDelimiterField, BorderLayout.SOUTH);
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
        QName recordRoot;
        if (null == recordRootDelimiterField.getText() || "".equals(recordRootDelimiterField.getText())) {
            recordRoot = QNameBuilder.createQName("record");
        }
        else {
            recordRoot = QNameBuilder.createQName(recordRootDelimiterField.getText());
        }
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
            binding.setVariable(INPUT, record);
            binding.setVariable(OUTPUT, builder);
            binding.setVariable(DC, xmlns.namespace("http://purl.org/dc/elements/1.1/", "dc"));
            binding.setVariable(DCTERMS, xmlns.namespace("http://purl.org/dc/terms/", "dcterms"));
            binding.setVariable(EUROPEANA, xmlns.namespace("http://www.europeana.eu/schemas/ese/", "europeana"));
            return binding;
        }
    }

    public static void main(String... args) throws FileNotFoundException, XMLStreamException {
        GroovyEditorGUI groovyEditorGUI = new GroovyEditorGUI();
        groovyEditorGUI.setVisible(true);
    }
}
