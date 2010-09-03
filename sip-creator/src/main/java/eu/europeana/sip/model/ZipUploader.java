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

/**
 * Zip up a file and upload it
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ZipUploader implements Runnable {
    private static final int BLOCK_SIZE = 4096;
    private Logger log = Logger.getLogger(getClass());
    private String metaRepoSubmitUrl;
    private FileSet fileSet;
    private String zipFileName;
    private BoundedRangeModel progress;

    public ZipUploader(String metaRepoSubmitUrl, FileSet fileSet, String zipFileName, BoundedRangeModel progress) {
        this.metaRepoSubmitUrl = metaRepoSubmitUrl;
        this.fileSet = fileSet;
        this.zipFileName = zipFileName;
        this.progress = progress;
    }

    @Override
    public void run() {
        final File zipFile = fileSet.createZipFile(zipFileName);
        if (zipFile != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int blocks = (int) (zipFile.length() / BLOCK_SIZE);
                    progress.setMaximum(blocks);
                    progress.setValue(0);
                }
            });
            try {
                uploadFile(zipFile);
            }
            catch (IOException e) {
                e.printStackTrace();  // todo: something
            }
            finally {
                if (!zipFile.delete()) {
                    log.warn("Unable to delete "+zipFile.getAbsolutePath());
                }
            }
        }
    }

    private void uploadFile(File file) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        String postUrl = metaRepoSubmitUrl + file.getName();
        log.info("Posting to: " + postUrl);
        HttpPost httpPost = new HttpPost(postUrl);
        ZipEntity zipEntity = new ZipEntity(file, "application/zip");
        zipEntity.setChunked(true);
        httpPost.setEntity(zipEntity);
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

    private void stream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BLOCK_SIZE];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    private class ZipEntity extends AbstractHttpEntity implements Cloneable {

        protected final File file;
        private long bytesSent;
        private int blocksReported;

        public ZipEntity(final File file, final String contentType) {
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
                while ((bytes = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytes);
                    bytesSent += bytes;
                    int blocks = (int)(bytesSent / BLOCK_SIZE);
                    if (blocks > blocksReported) {
                        blocksReported = blocks;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progress.setValue(blocksReported);
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
