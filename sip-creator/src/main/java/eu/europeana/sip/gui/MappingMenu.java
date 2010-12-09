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

import eu.delving.metadata.Path;
import eu.delving.metadata.Statistics;
import eu.delving.sip.FileStore;
import eu.europeana.sip.model.SipModel;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import java.awt.event.ActionEvent;
import java.util.Set;

/**
 * The menu for choosing data sets
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class MappingMenu extends JMenu {
    private SipModel sipModel;
    private SelectListener selectListener;

    public interface SelectListener {
        void selectDataSet(String spec, String prefix);
    }

    public MappingMenu(SipModel sipModel, SelectListener selectListener) {
        super("Mappings");
        this.sipModel = sipModel;
        this.selectListener = selectListener;
        sipModel.addUpdateListener(new SipModel.UpdateListener() {

            @Override
            public void updatedDataSetStore(FileStore.DataSetStore dataSetStore) {
                refresh();
            }

            @Override
            public void updatedStatistics(Statistics statistics) {
            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
            }
        });
        refresh();
    }

    private void refresh() {
        removeAll();
        for (String specString : sipModel.getFileStore().getDataSetStores().keySet()) {
            final String spec = specString;
            final Set<String> prefixes = sipModel.getMetadataModel().getPrefixes();
            if (prefixes.size() == 1) {
                final String prefix = prefixes.iterator().next();
                add(new AbstractAction(String.format("%s - %s", spec, prefix)) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        selectListener.selectDataSet(spec, prefix);
                    }
                });
            }
            else {
                JMenu menu = new JMenu(spec);
                for (String prefixString : sipModel.getMetadataModel().getPrefixes()) {
                    final String prefix = prefixString;
                    menu.add(new AbstractAction(String.format("%s - %s", spec, prefix)) {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            selectListener.selectDataSet(spec, prefix);
                        }
                    });
                }
                add(menu);
            }
        }
    }
}
