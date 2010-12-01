package eu.europeana.sip.gui;

import com.thoughtworks.xstream.XStream;
import eu.delving.sip.DataSetInfo;
import eu.europeana.sip.model.SipModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
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

public class RepositoryConnection {
    private static final int LIST_FETCH_DELAY_MILLIS = 5000;
    private Executor executor = Executors.newSingleThreadExecutor();
    private SipModel sipModel;
    private Timer periodicListFetchTimer;
    private HttpClient httpClient = new DefaultHttpClient();
    private Listener listener;

    public interface Listener {
        void setInfo(DataSetInfo dataSetInfo);
        void setList(List<DataSetInfo> list);
    }

    public enum DataSetState {
        DISABLED,
        UPLOADED,
        QUEUED,
        INDEXING,
        ENABLED,
        ERROR
    }

    public RepositoryConnection(SipModel sipModel, Listener listener) {
        this.sipModel = sipModel;
        this.listener = listener;
        periodicListFetchTimer = new Timer(LIST_FETCH_DELAY_MILLIS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executor.execute(new ListFetcher());
            }
        });
        periodicListFetchTimer.setRepeats(true);
    }

    public void enablePolling(boolean enable) {
        if (enable) {
            executor.execute(new ListFetcher());
            periodicListFetchTimer.restart();
        }
        else {
            periodicListFetchTimer.stop();
        }
    }

    public void setEnabled(String spec, boolean enable) {
        executor.execute(new XAbler(spec, enable));
    }

    public boolean enableAction(DataSetInfo info) {
        switch (DataSetState.valueOf(info.state)) {
            case INDEXING:
            case ENABLED:
            case QUEUED:
                return false;
            case UPLOADED:
            case DISABLED:
            case ERROR:
                return true;
            default:
                throw new RuntimeException();
        }
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
                @SuppressWarnings("unchecked")
                final List<DataSetInfo> list = (List<DataSetInfo>) xstream().fromXML(entity.getContent());
                entity.consumeContent();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.setList(list);
                    }
                });
            }
            catch (final Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        sipModel.tellUser("Access to list failed with this key", e);
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
                final DataSetInfo dataSetInfo = (DataSetInfo) xstream().fromXML(entity.getContent());
                entity.consumeContent();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.setInfo(dataSetInfo);
                    }
                });
            }
            catch (final Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        sipModel.tellUser("Access to enable/disable control failed", e);
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
