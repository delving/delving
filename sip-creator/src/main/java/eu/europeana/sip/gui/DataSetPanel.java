package eu.europeana.sip.gui;

import eu.europeana.sip.model.SipModel;

import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * The tab related to interacting with the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetPanel extends JPanel {

    public DataSetPanel(SipModel sipModel) {
        super(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        add(new DataSetDetailsPanel(sipModel), gbc);
        gbc.gridx++;
        add(new DataSetControlPanel(sipModel.getDataSetControllerUrl()), gbc);
    }

}
