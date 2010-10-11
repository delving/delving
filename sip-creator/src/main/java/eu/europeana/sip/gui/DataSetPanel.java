package eu.europeana.sip.gui;

import eu.delving.core.rest.DataSetInfo;
import eu.europeana.sip.model.SipModel;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
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
    private static final int LIST_FETCH_DELAY_MILLIS = 5000;
    private Executor executor = Executors.newSingleThreadExecutor();
    private RestTemplate restTemplate;
    private JTextField keyField = new JTextField(20);
    private JButton checkKey = new JButton("Use this key");
    private JLabel errorMessage = new JLabel("", JLabel.CENTER);
    private SipModel sipModel;
    private DataSetControlPanel dataSetControlPanel;
    private Timer periodicListFetchTimer;

    public DataSetPanel(SipModel sipModel) {
        super(new BorderLayout(8, 8));
        this.sipModel = sipModel;
        Enable enable = new Enable() {
            @Override
            public void setEnabled(String datasetSpec, boolean enable) {
                executor.execute(new XAbler(datasetSpec, enable));
            }
        };
        JPanel grid = new JPanel(new GridLayout(1, 0, 8, 8));
        grid.add(new DataSetUploadPanel(sipModel));
        grid.add(dataSetControlPanel = new DataSetControlPanel(enable));
        add(grid, BorderLayout.CENTER);
        add(createSouthPanel(), BorderLayout.SOUTH);
        keyField.setText(sipModel.getServerAccessKey());
        periodicListFetchTimer = new Timer(LIST_FETCH_DELAY_MILLIS, new ActionListener() {
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
        checkKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipModel.setServerAccessKey(keyField.getText().trim());
                executor.execute(new ListFetcher());
            }
        });
        errorMessage.setForeground(Color.RED);
        JPanel flow = new JPanel();
        flow.add(new JLabel("Access Key:"));
        flow.add(keyField);
        flow.add(checkKey);
        JPanel grid = new JPanel(new GridLayout(0, 1, 4, 4));
        grid.setBorder(BorderFactory.createTitledBorder("Services Access"));
        grid.add(flow);
        grid.add(errorMessage);
        return grid;
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
            catch (RestClientException e) {
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
            catch (RestClientException e) {
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
