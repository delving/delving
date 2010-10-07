package eu.europeana.sip.gui;

import eu.delving.core.rest.DataSetInfo;
import eu.europeana.sip.model.SipModel;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.client.RestTemplate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The tab related to interacting with the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetPanel extends JPanel {
    private static final int PERIODIC_LIST_FETCH_DELAY_MILLIS = 10000;
    private Executor executor = Executors.newSingleThreadExecutor();
    private RestTemplate restTemplate;
    private JTextField keyField = new JTextField(20);
    private JButton checkKey = new JButton("Use this key");
    private JLabel errorMessage = new JLabel("");
    private SipModel sipModel;
    private DataSetControlPanel dataSetControlPanel;
    private Timer periodicListFetchTimer;

    public DataSetPanel(SipModel sipModel) {
        super(new GridBagLayout());
        this.sipModel = sipModel;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridx = gbc.gridy = 0;
        add(new DataSetUploadPanel(sipModel), gbc);
        gbc.gridx++;
        Enable enable = new Enable() {
            @Override
            public void setEnabled(String datasetSpec, boolean enable) {
                executor.execute(new XAbler(datasetSpec, enable));
            }
        };
        add(dataSetControlPanel = new DataSetControlPanel(enable), gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        add(createSouthPanel(), gbc);
        keyField.setText(sipModel.getServerAccessKey());
        periodicListFetchTimer = new Timer(PERIODIC_LIST_FETCH_DELAY_MILLIS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executor.execute(new ListFetcher());
            }
        });
        periodicListFetchTimer.setRepeats(false);
        if (!sipModel.getServerAccessKey().isEmpty()) {
            executor.execute(new ListFetcher());
        }
    }

    public interface Enable {
        void setEnabled(String datasetSpec, boolean enable);
    }

    private JPanel createSouthPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Services Access"));
        p.add(new JLabel("Access Key:"), BorderLayout.WEST);
        p.add(keyField, BorderLayout.CENTER);
        checkKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.setServerAccessKey(keyField.getText().trim());
                periodicListFetchTimer.start();
            }
        });
        p.add(checkKey, BorderLayout.EAST);
        p.add(errorMessage, BorderLayout.SOUTH);
        return p;
    }

    private class ListFetcher implements Runnable {
        @Override
        public void run() {
            try {
                String url = String.format(
                        "%s?accessKey=%s",
                        sipModel.getServerUrl(),
                        sipModel.getServerAccessKey()
                );
                final List list = rest().getForObject(url, List.class);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dataSetControlPanel.setList(list);
                        errorMessage.setText(null);
                        periodicListFetchTimer.restart();
                    }
                });
            }
            catch (Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        errorMessage.setText("Access to list failed with this key");
                    }
                });
            }
        }
    }

    private class XAbler implements Runnable {
        private String spec;
        private boolean enable;

        private XAbler(String spec, boolean enable) {
            this.spec = spec;
            this.enable = enable;
        }

        @Override
        public void run() {
            try {
                String url = String.format(
                        "%s/%s?enable=%s&accessKey=%s",
                        sipModel.getServerUrl(),
                        spec,
                        String.valueOf(enable),
                        sipModel.getServerAccessKey()
                );
                final DataSetInfo info = rest().getForObject(url, DataSetInfo.class);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dataSetControlPanel.setInfo(info);
                    }
                });
            }
            catch (Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        errorMessage.setText("Access to enable/disable control failed");
                    }
                });
            }
        }
    }

    private RestTemplate rest() {
        if (restTemplate == null) {
            XStreamMarshaller xStream = new XStreamMarshaller();
            xStream.setAnnotatedClass(DataSetInfo.class);
            List<HttpMessageConverter<?>> converterList = new ArrayList<HttpMessageConverter<?>>();
            converterList.add(new MarshallingHttpMessageConverter(xStream, xStream));
            restTemplate = new RestTemplate();
            restTemplate.setMessageConverters(converterList);
        }
        return restTemplate;
    }

}
