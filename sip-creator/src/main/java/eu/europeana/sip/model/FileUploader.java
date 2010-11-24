package eu.europeana.sip.model;

import eu.delving.sip.ProgressListener;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
    private File file;
    private String serverUrl, serverAccessKey;
    private UserNotifier userNotifier;
    private ProgressListener progressListener;

    public FileUploader(File file, String serverUrl, String serverAccessKey, UserNotifier userNotifier, ProgressListener progressListener) {
        this.file = file;
        this.serverUrl = serverUrl;
        this.serverAccessKey = serverAccessKey;
        this.userNotifier = userNotifier;
        this.progressListener = progressListener;
    }

    @Override
    public void run() {
        final int totalBlocks = (int) (file.length() / BLOCK_SIZE);
        progressListener.setTotal(totalBlocks);
        try {
            uploadFile();
        }
        catch (IOException e) {
            userNotifier.tellUser("Unable to upload file "+file.getAbsolutePath());
            log.warn("Unable to upload file "+file.getAbsolutePath(), e);
        }
        finally {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressListener.finished();
                }
            });
        }
    }

    private void uploadFile() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        String postUrl = String.format(
                "%s/submit/%s?accessKey=%s",
                serverUrl,
                file.getName(),
                serverAccessKey
        );
        log.info("Posting to: " + postUrl);
        HttpPost httpPost = new HttpPost(postUrl);
        String contentType = file.getName().endsWith(".gz") ? "application/gzip" : "text/plain";
        FileEntity fileEntity = new FileEntity(file, contentType);
        fileEntity.setChunked(true);
        httpPost.setEntity(fileEntity);
        HttpResponse response = httpClient.execute(httpPost);
        log.info("Response: " + response.getStatusLine());
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            resEntity.consumeContent();
        }
        httpClient.getConnectionManager().shutdown();
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Response not OK");
        }
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
