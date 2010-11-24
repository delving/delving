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

import eu.europeana.sip.model.SipModel;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import java.awt.event.ActionEvent;

/**
 * The menu for choosing data sets
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class DataSetMenu extends JMenu {
    private SipModel sipModel;
    private SelectListener selectListener;

    public interface SelectListener {
        void selectDataSet(String spec);
    }

    public DataSetMenu(SipModel sipModel, SelectListener selectListener) {
        super("Data Sets");
        this.sipModel = sipModel;
        this.selectListener = selectListener;
        refresh();
    }

    private class LoadDataSetAction extends AbstractAction {
        private String spec;

        private LoadDataSetAction(String spec) {
            super(String.format("Load Data Set %s", spec));
            this.spec = spec;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            selectListener.selectDataSet(spec);
        }
    }

    private void refresh() {
        removeAll();
        for (String spec : sipModel.getFileStore().getDataSetSpecs()) {
            add(new LoadDataSetAction(spec));
        }
    }
}
