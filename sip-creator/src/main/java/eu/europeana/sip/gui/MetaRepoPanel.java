package eu.europeana.sip.gui;

import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.DataSetDetails;
import eu.europeana.sip.core.RecordRoot;
import eu.europeana.sip.model.FileSet;
import eu.europeana.sip.model.SipModel;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The tab related to interacting with the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoPanel extends JPanel {
    private SipModel sipModel;
    private DataSetDetailsPanel dataSetDetailsPanel = new DataSetDetailsPanel();
    private JButton createUploadZipButton = new JButton("Create and upload ZIP File");

    public MetaRepoPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(dataSetDetailsPanel, gbc);
        gbc.gridy++;
        add(createUploadZipButton, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel("Create ZIP File:", JLabel.RIGHT), gbc);
        gbc.gridy++;
        add(new JLabel("Upload ZIP File:", JLabel.RIGHT), gbc);
        gbc.gridy--;
        gbc.gridx++;
        add(new JProgressBar(sipModel.getZipProgress()), gbc);
        gbc.gridy++;
        add(new JProgressBar(sipModel.getUploadProgress()), gbc);
        wireUp();
    }

    private void wireUp() {
        createUploadZipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.setDataSetDetails(dataSetDetailsPanel.getDetails());
                sipModel.createUploadZipFile();
            }
        });
        sipModel.addUpdateListener(new SipModel.UpdateListener() {
            @Override
            public void templateApplied() {
            }

            @Override
            public void updatedFileSet(FileSet fileSet) {
                createUploadZipButton.setEnabled(fileSet.getReport() != null);
            }

            @Override
            public void updatedDetails(DataSetDetails dataSetDetails) {
                dataSetDetailsPanel.setDetails(sipModel.getDataSetDetails());
            }

            @Override
            public void updatedRecordRoot(RecordRoot recordRoot) {
            }

            @Override
            public void updatedConstantFieldModel(ConstantFieldModel constantFieldModel) {
            }

            @Override
            public void normalizationMessage(boolean complete, String message) {
                createUploadZipButton.setEnabled(complete);
            }
        });
    }
}
