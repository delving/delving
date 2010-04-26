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

import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.RecentFileSets;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private AnalyzerPanel analyzerPanel = new AnalyzerPanel();
    private NormalizerPanel normalizerPanel = new NormalizerPanel();

    public SipCreatorGUI() {
        super("Europeana Ingestion SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Analyzer", analyzerPanel);
        tabs.addTab("Normalizer", normalizerPanel);
        getContentPane().add(tabs);
        setJMenuBar(createMenuBar());
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension((int) Math.round(d.getWidth() * .8), (int) Math.round(d.getHeight() * .8)));
        tabs.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (KeyEvent.ALT_MASK == e.getModifiers() && KeyEvent.VK_L == e.getKeyCode()) {
                            RecentFileSets recentFiles = new RecentFileSets(new File("."));
                            if (0 < recentFiles.getList().size()) {
                                FileSet fileSet = recentFiles.getList().get(0);
                                analyzerPanel.setFileSet(fileSet);
                                normalizerPanel.setFileSet(fileSet);
                            }
                        }
                    }
                }
        );
        tabs.requestFocus();
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        FileMenu fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(FileSet fileSet) {
                analyzerPanel.setFileSet(fileSet);
                normalizerPanel.setFileSet(fileSet);
            }
        });
        analyzerPanel.setFileMenuEnablement(fileMenu.getEnablement());
        bar.add(fileMenu);
        return bar;
    }

    public static void main(String[] args) {
        SipCreatorGUI sipCreatorGUI = new SipCreatorGUI();
        sipCreatorGUI.setVisible(true);
    }
}