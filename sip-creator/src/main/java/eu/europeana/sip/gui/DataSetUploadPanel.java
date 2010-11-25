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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Handle the uploading of files for the current data store
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetUploadPanel extends JPanel {
    private SipModel sipModel;

    public DataSetUploadPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        setBorder(BorderFactory.createTitledBorder("Upload"));
        setPreferredSize(new Dimension(480, 500));
        wireUp();
    }

    private void refresh() {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        FileStore.DataSetStore store = sipModel.getDataSetStore();
        if (store != null) {
            try {
                File detailsFile = store.getSourceDetailsFile();
                add(new JButton(new UploadFileAction("Data Set Details", detailsFile)), gbc);
                gbc.gridy++;
                File sourceFile = store.getSourceFile();
                add(new JButton(new UploadFileAction("Source XML", sourceFile)), gbc);
                gbc.gridy++;
                for (File mappingFile : store.getMappingFiles()) {
                    add(new JButton(new UploadFileAction("Mapping", mappingFile)), gbc);
                    gbc.gridy++;
                }
            }
            catch (FileStoreException e) {
                sipModel.tellUser("Unable to find the source file for "+store.getSpec(), e);
            }
        }
    }

    private void wireUp() {
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedDataSetStore(FileStore.DataSetStore dataSetStore) {
                refresh();
            }

            @Override
            public void updatedRecordRoot(Path recordRoot, int recordCount) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
            }
        });
    }

    private class UploadFileAction extends AbstractAction {
        private String name;
        private File file;

        private UploadFileAction(String name, File file) {
            super(String.format("Upload %s", name));
            this.name = name;
            this.file = file;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try {
                ProgressMonitor progressMonitor = new ProgressMonitor(
                        DataSetUploadPanel.this,
                        "Uploading",
                        String.format(
                                "Uploading %s for data set %s",
                                file.getName(),
                                sipModel.getDataSetStore().getSpec()
                        ),
                        0,
                        100
                );
                sipModel.uploadFile(sipModel.getDataSetStore().getSourceFile(), new ProgressListener.Adapter(progressMonitor, this));
            }
            catch (FileStoreException e) {
                sipModel.tellUser(String.format("Unable to get %s for upload", file), e);
            }
        }
    }
}