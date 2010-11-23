package eu.europeana.sip.model;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

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
    private BoundedRangeModel progressModel;

    public FileUploader(File file, String serverUrl, String serverAccessKey, UserNotifier userNotifier, BoundedRangeModel progressModel) {
        this.file = file;
        this.serverUrl = serverUrl;
        this.serverAccessKey = serverAccessKey;
        this.userNotifier = userNotifier;
        this.progressModel = progressModel;
    }

    @Override
    public void run() {
        final int totalBlocks = (int) (file.length() / BLOCK_SIZE);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressModel.setMaximum(totalBlocks);
                progressModel.setValue(0);
            }
        });
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
                    progressModel.setValue(0);
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

        protected final File file;
        private long bytesSent;
        private int blocksReported;

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
            InputStream inputStream;
            if (this.file.getName().endsWith(".gz")) {
                inputStream = new GZIPInputStream(new FileInputStream(this.file));
            }
            else {
                inputStream = new FileInputStream(this.file);
            }
            try {
                byte[] buffer = new byte[BLOCK_SIZE];
                int bytes;
                while ((bytes = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytes);
                    bytesSent += bytes;
                    int blocks = (int) (bytesSent / BLOCK_SIZE);
                    if (blocks > blocksReported) {
                        blocksReported = blocks;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressModel.setValue(blocksReported);
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
