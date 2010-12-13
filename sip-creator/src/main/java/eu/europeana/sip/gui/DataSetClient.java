package eu.europeana.sip.gui;

import com.thoughtworks.xstream.XStream;
import eu.delving.sip.DataSetCommand;
import eu.delving.sip.DataSetInfo;
import eu.delving.sip.DataSetResponse;
import eu.delving.sip.DataSetResponseCode;
import eu.delving.sip.FileType;
import eu.delving.sip.Hasher;
import eu.delving.sip.ProgressListener;
import eu.europeana.sip.model.SipModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The tab related to interacting with the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class DataSetClient {
    private static final int LIST_FETCH_DELAY_MILLIS = 5000;
    private Logger log = Logger.getLogger(getClass());
    private Executor executor = Executors.newSingleThreadExecutor();
    private SipModel sipModel;
    private Timer periodicListFetchTimer;
    private Listener listener;

    public interface Listener {
        void setInfo(DataSetInfo dataSetInfo);

        void setList(List<DataSetInfo> list);

        void disconnected();
    }

    public DataSetClient(SipModel sipModel, Listener listener) {
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

    public void setListFetchingEnabled(boolean enable) {
        if (enable) {
            executor.execute(new ListFetcher());
            periodicListFetchTimer.restart();
        }
        else {
            periodicListFetchTimer.stop();
        }
    }

    public void sendCommand(String spec, DataSetCommand command) {
        executor.execute(new CommandSender(spec, command));
    }

    public void uploadFile(FileType fileType, File file, ProgressListener progressListener) {
        executor.execute(new FileUploader(fileType, file, progressListener));
    }

    private class ListFetcher implements Runnable {
        @Override
        public void run() {
            try {
                String url = String.format(
                        "%s?accessKey=%s",
                        sipModel.getServerUrl(),
                        sipModel.getAccessKey()
                );
                final DataSetResponse response = execute(new HttpGet(url));
                if (response.isEverythingOk()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            listener.setList(response.getDataSetList());
                        }
                    });
                }
                else {
                    periodicListFetchTimer.stop();
                    notifyUser(response.getResponseCode());
                }
            }
            catch (final Exception e) {
                periodicListFetchTimer.stop();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        sipModel.getUserNotifier().tellUser("Disconnected from Repository", e);
                        listener.disconnected();
                    }
                });
            }
        }
    }

    private class CommandSender implements Runnable {
        private String spec;
        private DataSetCommand command;

        private CommandSender(String spec, DataSetCommand command) {
            this.spec = spec;
            this.command = command;
        }

        @Override
        public void run() {
            try {
                String url = String.format(
                        "%s/%s/%s?accessKey=%s",
                        sipModel.getServerUrl(),
                        spec,
                        command,
                        sipModel.getAccessKey()
                );
                final DataSetResponse response = execute(new HttpGet(url));
                if (response.isEverythingOk()) {
                    if (response.getDataSetList() == null || response.getDataSetList().size() != 1) {
                        throw new RuntimeException("Expected exactly one Info object");
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            listener.setInfo(response.getDataSetList().get(0));
                        }
                    });
                }
                else {
                    notifyUser(response);
                }
            }
            catch (final Exception e) {
                notifyUser(e);
            }
        }
    }

    /**
     * Zip up a file and upload it
     *
     * @author Gerald de Jong <geralddejong@gmail.com>
     */

    public class FileUploader implements Runnable {
        private static final int BLOCK_SIZE = 4096;
        private Logger log = Logger.getLogger(getClass());
        private File file;
        private FileType fileType;
        private ProgressListener progressListener;

        public FileUploader(FileType fileType, File file, ProgressListener progressListener) {
            this.fileType = fileType;
            this.file = file;
            this.progressListener = progressListener;
        }

        @Override
        public void run() {
            final int totalBlocks = (int) (file.length() / BLOCK_SIZE);
            progressListener.setTotal(totalBlocks);
            try {
                file = Hasher.hashFile(file);
                log.info("Uploading " + file);
                final DataSetResponse response = uploadFile();
                boolean success = response.getResponseCode() == DataSetResponseCode.THANK_YOU;
                progressListener.finished(success);
                if (!success) {
                    notifyUser(response);
                }
            }
            catch (Exception e) {
                log.warn("Unable to upload file " + file.getAbsolutePath(), e);
                progressListener.finished(false);
                notifyUser(e);
            }
        }

        private DataSetResponse uploadFile() throws IOException {
            HttpPost httpPost = new HttpPost(createRequestUrl());
            httpPost.setEntity(createEntity());
            return execute(httpPost);
        }

        private FileEntity createEntity() {
            FileEntity fileEntity = new FileEntity(file, fileType.getContentType());
            fileEntity.setChunked(true);
            return fileEntity;
        }

        private String createRequestUrl() {
            return String.format(
                    "%s/submit/%s/%s/%s?accessKey=%s",
                    sipModel.getServerUrl(),
                    sipModel.getDataSetStore().getSpec(),
                    fileType,
                    file.getName(),
                    sipModel.getAccessKey()
            );
        }

        private class FileEntity extends AbstractHttpEntity implements Cloneable {

            private final File file;
            private long bytesSent;
            private int blocksReported;
            private boolean abort = false;

            public FileEntity(final File file, final String contentType) {
                if (file == null) {
                    throw new IllegalArgumentException("File may not be null");
                }
                this.file = file;
                setContentType(contentType);
            }

            public boolean isRepeatable() {
                return true;
            }

            public long getContentLength() {
                return this.file.length();
            }

            public InputStream getContent() throws IOException {
                return new FileInputStream(this.file);
            }

            public void writeTo(final OutputStream outputStream) throws IOException {
                if (outputStream == null) {
                    throw new IllegalArgumentException("Output stream may not be null");
                }
                InputStream inputStream = new FileInputStream(this.file);
                try {
                    byte[] buffer = new byte[BLOCK_SIZE];
                    int bytes;
                    while (!abort && (bytes = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytes);
                        bytesSent += bytes;
                        int blocks = (int) (bytesSent / BLOCK_SIZE);
                        if (blocks > blocksReported) {
                            blocksReported = blocks;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (!progressListener.setProgress(blocksReported)) {
                                        abort = true;
                                    }
                                }
                            });
                        }
                    }
                    outputStream.flush();
                }
                finally {
                    inputStream.close();
                }
            }

            /**
             * Tells that this entity is not streaming.
             *
             * @return <code>false</code>
             */
            public boolean isStreaming() {
                return false;
            }

            public Object clone() throws CloneNotSupportedException {
                // File instance is considered immutable
                // No need to make a copy of it
                return super.clone();
            }
        }
    }

    private DataSetResponse execute(HttpGet httpGet) throws IOException {
        log.info("GET: " + httpGet.getURI());
        HttpClient httpClient = new DefaultHttpClient();
        try {
            return translate(httpClient.execute(httpGet));
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    private DataSetResponse execute(HttpPost httpPost) throws IOException {
        log.info("POST: " + httpPost.getURI());
        HttpClient httpClient = new DefaultHttpClient();
        try {
            return translate(httpClient.execute(httpPost));
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    private DataSetResponse translate(HttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = httpResponse.getEntity();
            DataSetResponse response = (DataSetResponse) xstream().fromXML(entity.getContent());
            entity.consumeContent();
            return response;
        }
        else {
            throw new IOException("Response not OK: " + httpResponse.getStatusLine());
        }
    }

    private void notifyUser(final Exception exception) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                sipModel.getUserNotifier().tellUser("Sorry, there was a problem communicating with Repository", exception);
                periodicListFetchTimer.stop();
                listener.disconnected();
            }
        });
    }

    private void notifyUser(DataSetResponse response) {
        notifyUser(response.getResponseCode());
    }

    private void notifyUser(final DataSetResponseCode responseCode) {
        switch (responseCode) {
            case THANK_YOU:
            case GOT_IT_ALREADY:
            case READY_TO_RECEIVE:
                log.info("Received " + responseCode);
                break;
            case ACCESS_KEY_FAILURE:
                sipModel.getUserNotifier().tellUser("The Access Key is not correct for this Repository");
                break;
            case STATE_CHANGE_FAILURE:
                sipModel.getUserNotifier().tellUser("Could not execute state change"); // todo
                break;
            case DATA_SET_NOT_FOUND:
                sipModel.getUserNotifier().tellUser("The Data Set was not found in the Repository");
                break;
            case NEWORK_ERROR:
                sipModel.getUserNotifier().tellUser("There was a network error while communicating with the Repository");
                break;
            case SYSTEM_ERROR:
                sipModel.getUserNotifier().tellUser("Sorry, there was a system error in the Repository");
                break;
            case UNKNOWN_RESPONSE:
                log.error(responseCode.toString());
                break;
            default:
                break;
        }
    }

    private XStream xstream() {
        XStream stream = new XStream();
        stream.processAnnotations(new Class<?>[]{DataSetResponse.class});
        return stream;
    }

}
