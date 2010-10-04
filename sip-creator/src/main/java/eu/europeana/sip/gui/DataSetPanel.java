package eu.europeana.sip.gui;

import eu.europeana.sip.model.SipModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
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

public class DataSetPanel extends JPanel {
    private JTextField keyField = new JTextField(40);
    private JButton refreshButton = new JButton("Refresh");
    private DataSetControlPanel dataSetControlPanel;

    public DataSetPanel(SipModel sipModel) {
        super(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.VERTICAL;
//        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = gbc.gridy = 0;
        add(new DataSetDetailsPanel(sipModel), gbc);
        gbc.gridx++;
        add(dataSetControlPanel = new DataSetControlPanel(sipModel.getServerUrl()), gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel p = new JPanel(new BorderLayout(8, 8));
        add(createSouthPanel(), gbc);
    }

    private JPanel createSouthPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Services Access"));
        p.add(new JLabel("Access Key:"), BorderLayout.WEST);
        p.add(keyField, BorderLayout.CENTER);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataSetControlPanel.refreshList(keyField.getText().trim());
            }
        });
        p.add(refreshButton, BorderLayout.EAST);
        return p;
    }

}
