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

import eu.europeana.sip.groovy.GroovyService;
import eu.europeana.sip.model.FileSet;
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
import javax.xml.namespace.QName;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * The GroovyEditor for creating live Groovy snippets
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class GroovyEditor extends JPanel {

    public final static int VALIDATION_DELAY = 500;
    private final static Logger LOG = Logger.getLogger(GroovyEditor.class.getName());

    private JEditorPane codeArea = new JEditorPane();
    private JTextArea outputArea;
    private CompileTimer compileTimer;
    private GroovyService groovyService;
    private List<String> availableNodes;

    public void updateCodeArea(String groovyCode) {
        codeArea.setText(groovyCode);
        compileTimer.triggerSoon();
    }

    public GroovyEditor(final JTextArea outputArea) {
        super(new BorderLayout());
        this.outputArea = outputArea;
        add(createSplitPane());
        this.groovyService = new GroovyService(new GroovyService.Listener() {
            @Override
            public void setMapping(final String groovyCode) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        codeArea.setText(groovyCode);
                        compileTimer.triggerSoon();
                    }
                });
            }

            @Override
            public void setResult(final String result) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        outputArea.setText(result);
                    }
                });
            }
        });
        this.compileTimer = new CompileTimer();
        this.codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        autoCompleteDialog.setVisible(false);
                        codeArea.requestFocus();
                        break;
                    case KeyEvent.VK_SPACE:
                        if (event.getModifiers() == KeyEvent.CTRL_MASK && validatePrefix(codeArea.getCaretPosition())) {
                            autoCompleteDialog.setVisible(true);
                            autoCompleteDialog.requestFocus(codeArea.getCaret().getMagicCaretPosition());
                        }
                        return;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                        autoCompleteDialog.requestFocus(codeArea.getCaret().getMagicCaretPosition());
                        break;
                    case KeyEvent.VK_ENTER:
                        break;
                    default:
                        codeArea.requestFocus();
                }
                compileTimer.triggerSoon();
                autoCompleteDialog.updateElements(event, availableNodes);
                autoCompleteDialog.updateLocation(codeArea.getCaret().getMagicCaretPosition(), event.getComponent().getLocationOnScreen());
            }
        });
        codeArea.setContentType("text/groovy");
    }

    public void setFileSet(FileSet fileSet) {
        groovyService.setFileSet(fileSet);
    }

    public void setRecordRoot(QName recordRoot) {
        groovyService.setRecordRoot(recordRoot);
    }

    public void nextRecord() {
        groovyService.nextRecord();
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

    private void updateCodeArea(int caretPositon, Object selectedItem) {
        String begin = codeArea.getText().substring(0, caretPositon);
        String end = codeArea.getText().substring(caretPositon);
        codeArea.setText(begin + selectedItem + end);
    }

    public boolean validatePrefix(int caretPosition) {
        String text = codeArea.getText();
        if (text.length() < AutoCompleteDialog.DEFAULT_PREFIX.length()) {
            return false;
        }
        String prefixArea = text.substring(caretPosition - AutoCompleteDialog.DEFAULT_PREFIX.length(), caretPosition);
        LOG.debug(String.format("This is the found prefix : '%s'%n", prefixArea));
        return AutoCompleteDialog.DEFAULT_PREFIX.equals(prefixArea);
    }

    public void updateAvailableNodes(java.util.List<String> nodes) {
        this.availableNodes = nodes;
    }

    private class CompileTimer implements ActionListener {
        private Timer timer = new Timer(VALIDATION_DELAY, this);

        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            groovyService.setMapping(codeArea.getText());
        }

        public void triggerSoon() {
            timer.restart();
        }
    }

    private final AutoCompleteDialog autoCompleteDialog = new AutoCompleteDialog(
            new AutoCompleteDialog.Listener() {
                @Override
                public void itemSelected(Object selectedItem) {
                    updateCodeArea(codeArea.getCaretPosition(), selectedItem);
                }
            },
            codeArea
    );

    @Override
    public String toString() {
        return "GroovyEditor";
    }


}
