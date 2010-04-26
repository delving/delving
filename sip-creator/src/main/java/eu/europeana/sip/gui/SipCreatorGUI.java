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
import eu.europeana.sip.model.SipModel;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import java.io.IOException;

/**
 * The main GUI class for the sip creator
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

public class SipCreatorGUI extends JFrame {
    private SipModel sipModel = new SipModel();
    private AnalysisPanel analysisPanel = new AnalysisPanel(sipModel);
    private MappingPanel mappingPanel = new MappingPanel(sipModel);
    private NormPanel normPanel = new NormPanel(sipModel);

    public SipCreatorGUI() {
        super("Europeana Ingestion SIP Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Analyzer", analysisPanel);
        tabs.addTab("Mapping", mappingPanel);
        tabs.addTab("Normalizer", normPanel);
        getContentPane().add(tabs);
        setJMenuBar(createMenuBar());
        setSize(1024, 768);
//        setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        FileMenu fileMenu = new FileMenu(this, new FileMenu.SelectListener() {
            @Override
            public void select(FileSet fileSet) {
                try {
                    sipModel.setFileSet(fileSet);
                }
                catch (IOException e) {
                    JOptionPane.showConfirmDialog(SipCreatorGUI.this, "Unable to use file set");
                }
            }
        });
        analysisPanel.setFileMenuEnablement(fileMenu.getEnablement());
        bar.add(fileMenu);
        return bar;
    }

    public static void main(String[] args) {
        SipCreatorGUI sipCreatorGUI = new SipCreatorGUI();
        sipCreatorGUI.setVisible(true);
    }
}