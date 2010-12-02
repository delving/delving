package eu.europeana.sip.model;

import eu.delving.sip.DataSetResponse;
import eu.delving.sip.FileType;
import eu.delving.sip.Hasher;
import eu.delving.sip.ProgressListener;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Zip up a file and upload it
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class FileUploader implements Runnable {
    private static final int BLOCK_SIZE = 4096;
    private Logger log = Logger.getLogger(getClass());
    private String dataSetSpec;
    private File file;
    private FileType fileType;
    private String serverUrl, serverAccessKey;
    private UserNotifier userNotifier;
    private ProgressListener progressListener;
    private Receiver receiver;

    public interface Receiver {
        void acceptResponse(DataSetResponse dataSetResponse);
    }

    public FileUploader(String dataSetSpec, FileType fileType, File file, String serverUrl, String serverAccessKey, UserNotifier userNotifier, ProgressListener progressListener, Receiver receiver) {
        this.dataSetSpec = dataSetSpec;
        this.fileType = fileType;
        this.file = file;
        this.serverUrl = serverUrl;
        this.serverAccessKey = serverAccessKey;
        this.userNotifier = userNotifier;
        this.progressListener = progressListener;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        final int totalBlocks = (int) (file.length() / BLOCK_SIZE);
        progressListener.setTotal(totalBlocks);
        DataSetResponse response = DataSetResponse.SYSTEM_ERROR;
        try {
            file = Hasher.hashFile(file);
            log.info("Checking "+file);
            response = checkFile();
            if (response == DataSetResponse.READY_TO_RECEIVE) {
                log.info("Server is ready to receive "+file);
                response = uploadFile();
                log.info("Upload response "+response);
            }
            else {
                log.info("Check response "+response);
            }
        }
        catch (Exception e) {
            userNotifier.tellUser("Unable to upload file " + file.getAbsolutePath());
            log.warn("Unable to upload file " + file.getAbsolutePath(), e);
        }
        finally {
            final DataSetResponse finalResponse = response;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    receiver.acceptResponse(finalResponse);
                    progressListener.finished();
                }
            });
        }
    }

    private DataSetResponse checkFile() {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String requestUrl = createRequestUrl();
            HttpGet httpGet = new HttpGet(requestUrl);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException(String.format("<html>Response to <br>%s<br> not OK,<br> but instead %s", requestUrl, response.getStatusLine()));
            }
            DataSetResponse dataSetResponse = DataSetResponse.valueOf(EntityUtils.toString(response.getEntity()));
            httpClient.getConnectionManager().shutdown();
            return dataSetResponse;
        }
        catch (Exception e) {
            log.error("Network problem", e);
            userNotifier.tellUser("Sorry there was a network problem", e); // todo: not only network
            return DataSetResponse.NEWORK_ERROR;
        }
    }

    private DataSetResponse uploadFile() {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String requestUrl = createRequestUrl();
            log.info("Posting to: " + requestUrl);
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.setEntity(createEntity());
            HttpResponse response = httpClient.execute(httpPost);
            log.info("Response: " + response.getStatusLine());
            DataSetResponse dataSetResponse = DataSetResponse.valueOf(EntityUtils.toString(response.getEntity()));
            httpClient.getConnectionManager().shutdown();
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException("Response not OK, but " + response.getStatusLine().getStatusCode());
            }
            return dataSetResponse;
        }
        catch (IOException e) {
            log.error("Network problem", e);
            return DataSetResponse.NEWORK_ERROR;
        }
        catch (Exception e) {
            log.error("Network problem", e);
            return DataSetResponse.NEWORK_ERROR;
        }

    }

    private FileEntity createEntity() {
        FileEntity fileEntity = new FileEntity(file, fileType.getContentType());
        fileEntity.setChunked(true);
        return fileEntity;
    }

    private String createRequestUrl() {
        return String.format(
                "%s/submit/%s/%s/%s?accessKey=%s",
                serverUrl,
                dataSetSpec,
                fileType,
                file.getName(),
                serverAccessKey
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

    } // class ZipEntity

}
