package eu.europeana.sip.gui;

import com.thoughtworks.xstream.XStream;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetState;
import eu.delving.sip.FileType;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.model.FileUploader;
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
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The tab related to interacting with the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MetaRepoClient {
    private static final int LIST_FETCH_DELAY_MILLIS = 5000;
    private Executor executor = Executors.newSingleThreadExecutor();
    private SipModel sipModel;
    private Timer periodicListFetchTimer;
    private HttpClient httpClient = new DefaultHttpClient();
    private Listener listener;

    public interface Listener {
        void setInfo(DataSetInfo dataSetInfo);
        void setList(List<DataSetInfo> list);
        void disconnected();
    }

    public MetaRepoClient(SipModel sipModel, Listener listener) {
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
            case EMPTY:
            case UPLOADED:
            case DISABLED:
            case ERROR:
                return true;
            default:
                throw new RuntimeException();
        }
    }

    public void uploadFile(FileType fileType, File file, ProgressListener progressListener, FileUploader.Receiver receiver) {
        executor.execute(
                new FileUploader(
                        sipModel.getDataSetStore().getSpec(),
                        fileType,
                        file,
                        sipModel.getServerUrl(),
                        sipModel.getServerAccessKey(),
                        sipModel.getUserNotifier(),
                        progressListener,
                        receiver
                )
        );
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
                periodicListFetchTimer.stop();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        sipModel.getUserNotifier().tellUser("Access to list failed with this key", e);
                        listener.disconnected();
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
                periodicListFetchTimer.stop();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        sipModel.getUserNotifier().tellUser("Access to enable/disable control failed", e);
                    }
                });
            }
        }
    }

    private XStream xstream() {
        XStream stream = new XStream();
        stream.processAnnotations(new Class<?>[] {DataSetInfo.class, List.class});
        return stream;
    }
}
