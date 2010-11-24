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
import eu.delving.sip.FileStore;
import eu.delving.sip.FileStoreException;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.model.SipModel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SpringLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

/**
 * Present a number of fields in a form which can be used as global
 * values during mapping/normalization
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetUploadPanel extends JPanel {
    private SipModel sipModel;

    public DataSetUploadPanel(SipModel sipModel) {
        super(new SpringLayout());
        this.sipModel = sipModel;
        setBorder(BorderFactory.createTitledBorder("Data Set Details"));
        // button line
        add(new JLabel(""));
        add(new JButton(uploadSourceAction));
        // progress bars
        add(new JLabel("Upload File:", JLabel.RIGHT));
        // finish up
        LayoutUtil.makeCompactGrid(this, getComponentCount() / 2, 2, 5, 5, 5, 5);
        setPreferredSize(new Dimension(480, 500));
        wireUp();
    }

    private void wireUp() {
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedDataSetStore(FileStore.DataSetStore dataSetStore) {
                uploadSourceAction.setEnabled(dataSetStore != null); // todo: check that normalization has happened!
            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
                uploadSourceAction.setEnabled(complete);
            }
        });
    }

    private Action uploadSourceAction = new AbstractAction("Upload XML Source") {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try {
                ProgressMonitor progressMonitor = new ProgressMonitor(DataSetUploadPanel.this, "Uploading", "Uploading  " + sipModel.getDataSetStore().getSourceFile(), 0, 100);
                sipModel.uploadFile(sipModel.getDataSetStore().getSourceFile(), new ProgressListener.Adapter(progressMonitor, this));
            }
            catch (FileStoreException e) {
                sipModel.tellUser("Unable to get source file for upload", e);
            }
        }
    };
}