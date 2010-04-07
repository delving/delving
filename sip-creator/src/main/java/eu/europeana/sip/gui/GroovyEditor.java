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
import jsyntaxpane.DefaultSyntaxKit;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The GroovyEditor for creating live Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class GroovyEditor extends JPanel implements GroovyService.Listener, AnalyzerPanel.RecordChangeListener {

    public final static int VALIDATION_DELAY = 500;
    private final static Logger LOG = Logger.getLogger(GroovyEditor.class.getName());

    private JEditorPane codeArea = new JEditorPane();
    private JTextArea outputArea = new JTextArea();
    private CompileTimer compileTimer;
    private GroovyService groovyService;
    private JDialog dialog = new JDialog();
    private JScrollPane pane;

    private NormalizationParserBindingSource bindingSource = new NormalizationParserBindingSource(
            new NormalizationParserBindingSource.Listener() {

                @Override
                public void updateAvailableNodes(java.util.List<String> groovyNodes) {
                    // todo: contents of list should go in autocompletion window
                    JList list = new JList(groovyNodes.toArray());
                    pane.getViewport().setView(list);
                    dialog.setVisible(true);
                }
            }
    );

    public GroovyEditor() {
        super(new BorderLayout());
        pane = new JScrollPane();
        dialog.setSize(new Dimension(300, 200));
        dialog.add(pane);
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
        scroll.doLayout();
        split.setBottomComponent(new JScrollPane(outputArea));
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        return split;
    }

    public void triggerExecution() {
        bindingSource.nextRecord();
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

    @Override
    public void recordRootChanged(File file, QName recordRoot) {
        try {
            bindingSource.prepareInputFile(file, recordRoot);
        }
        catch (XMLStreamException e) {
            LOG.error("XML Stream error", e);
        }
        catch (FileNotFoundException e) {
            LOG.error("File not found", e);
        }
    }

    @Override
    public void save(File file, QName recordRoot) throws IOException {
        File recordFile = new File(file.getName() + ".record");
        FileOutputStream fileOutputStream = new FileOutputStream(recordFile);
        fileOutputStream.write(recordRoot.toString().getBytes());
        fileOutputStream.close();
        LOG.info(String.format("Written '%s' to file %s", recordRoot, recordFile.getAbsoluteFile()));
    }

    @Override
    public QName load(File file) throws IOException {
        File recordFile = new File(file.getName() + ".record");
        if (!recordFile.exists()) {
            LOG.warn(String.format("File %s not found, will use the default delimiter '%s'", recordFile.getAbsoluteFile(), AnalyzerPanel.DEFAULT_RECORD));
            return new QName(AnalyzerPanel.DEFAULT_RECORD);
        }
        FileInputStream fileInputStream = new FileInputStream(recordFile);
        StringBuffer b = new StringBuffer();
        int i;
        while (-1 != (i = fileInputStream.read())) {
            b.append((char) i);
        }
        LOG.info(String.format("Loaded '%s' from file %s", b, recordFile.getAbsoluteFile()));
        return new QName(b.toString());
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
