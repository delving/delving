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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private File zipFile;
    private BoundedRangeModel zipProgress, uploadProgress;
    private UserNotifier userNotifier;

    public ZipUploader(
            String metaRepoSubmitUrl,
            FileSet fileSet,
            String zipFileName,
            BoundedRangeModel zipProgress,
            BoundedRangeModel uploadProgress,
            UserNotifier userNotifier
    ) {
        this.metaRepoSubmitUrl = metaRepoSubmitUrl;
        this.fileSet = fileSet;
        this.zipFile = new File(fileSet.getDirectory(), zipFileName + ".zip");
        this.zipProgress = zipProgress;
        this.uploadProgress = uploadProgress;
        this.userNotifier = userNotifier;
    }

    @Override
    public void run() {
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                userNotifier.tellUser("Unable to delete zip file");
                return;
            }
        }
        initializeProgressIndicators();
        try {
            buildZipFile();
        }
        catch (IOException e) {
            userNotifier.tellUser("Unable to build zip file", e);
            if (!zipFile.delete()) {
                userNotifier.tellUser("Unable to delete zip file");
                return;
            }
        }
        if (zipFile.exists()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int blocks = (int) (zipFile.length() / BLOCK_SIZE);
                    uploadProgress.setMaximum(blocks);
                    uploadProgress.setValue(0);
                }
            });
            try {
                uploadFile();
            }
            catch (IOException e) {
                userNotifier.tellUser("Unable to upload zip file");
            }
            finally {
                if (!zipFile.delete()) {
                    log.warn("Unable to delete " + zipFile.getAbsolutePath());
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        zipProgress.setValue(0);
                    }
                });
            }
        }
    }

    private void initializeProgressIndicators() {
        List<File> uploadFiles = fileSet.getUploadFiles();
        long totalFileSize = 0;
        for (File file : uploadFiles) {
            totalFileSize += file.length();
        }
        final int totalBlocks = (int) (totalFileSize / BLOCK_SIZE);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                zipProgress.setMaximum(totalBlocks);
                zipProgress.setValue(0);
                uploadProgress.setMaximum(100);
                uploadProgress.setValue(0);
            }
        });
    }

    private void buildZipFile() throws IOException {
        List<File> uploadFiles = fileSet.getUploadFiles();
        OutputStream outputStream = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        long totalFileSize = 0;
        for (File file : uploadFiles) {
            InputStream in = new FileInputStream(file);
            zos.putNextEntry(new ZipEntry(file.getName()));
            byte[] buffer = new byte[BLOCK_SIZE];
            int length;
            while ((length = in.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
                totalFileSize += length;
                final int totalBlocksZipped = (int)(totalFileSize / BLOCK_SIZE);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        zipProgress.setValue(totalBlocksZipped);
                    }
                });
            }
            zos.closeEntry();
        }
        zos.close();
    }

    private void uploadFile() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        String postUrl = metaRepoSubmitUrl + zipFile.getName();
        log.info("Posting to: " + postUrl);
        HttpPost httpPost = new HttpPost(postUrl);
        ZipEntity zipEntity = new ZipEntity(zipFile, "application/zip");
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
                    int blocks = (int) (bytesSent / BLOCK_SIZE);
                    if (blocks > blocksReported) {
                        blocksReported = blocks;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                uploadProgress.setValue(blocksReported);
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
