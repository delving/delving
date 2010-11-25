package eu.europeana.sip.gui;

import com.thoughtworks.xstream.XStream;
import eu.delving.sip.DataSetInfo;
import eu.europeana.sip.model.SipModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
    private JTextField keyField = new JTextField(20);
    private JButton checkKey = new JButton("Use this key");
    private JLabel errorMessage = new JLabel("", JLabel.CENTER);
    private SipModel sipModel;
    private DataSetControlPanel dataSetControlPanel;
    private Timer periodicListFetchTimer;
    private HttpClient httpClient = new DefaultHttpClient();

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
                HttpGet get = new HttpGet(url);
                HttpResponse response = httpClient.execute(get);
                HttpEntity entity = response.getEntity();
                final List list = (List) xstream().fromXML(entity.getContent());
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
                HttpGet get = new HttpGet(url);
                HttpResponse response = httpClient.execute(get);
                HttpEntity entity = response.getEntity();
                final DataSetInfo info = (DataSetInfo) xstream().fromXML(entity.getContent());
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

//    private class UploadFileAction extends AbstractAction {
//        private File file;
//
//        private UploadFileAction(File file) {
//            super(String.format("Upload %s", file));
//            this.file = file;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent actionEvent) {
//            try {
//                ProgressMonitor progressMonitor = new ProgressMonitor(SwingUtilities.getRoot(DataSetPanel.this), "Uploading", "Uploading  " + file, 0, 100);
//                sipModel.uploadFile(sipModel.getDataSetStore().getSourceFile(), new ProgressListener.Adapter(progressMonitor, this));
//            }
//            catch (FileStoreException e) {
//                sipModel.tellUser(String.format("Unable to get %s for upload", file), e);
//            }
//        }
//    }

    private XStream xstream() {
        XStream stream = new XStream();
        stream.processAnnotations(new Class<?>[] {DataSetInfo.class, List.class});
        return stream;
    }
}
