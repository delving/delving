package eu.delving.services.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Upload a test zip file to the controller
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MockContentLoader {
    private static File ZIP_FILE = new File("services/target/92001_Ag_EU_TELtreasures.zip");
    private Logger log = Logger.getLogger(MockContentLoader.class);
    private String serviceKey;

    public MockContentLoader(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public void run() throws IOException {
        if (!ZIP_FILE.getParentFile().exists() && !ZIP_FILE.getParentFile().mkdirs()) {
            throw new RuntimeException("Can't create the zip file");
        }
        OutputStream zipOutput = new FileOutputStream(ZIP_FILE);
        zipToOutputStream(zipOutput);
        uploadFile(ZIP_FILE);
    }

    private void stream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    private void zipToOutputStream(OutputStream outputStream) throws IOException {
        InputStream detailsInput = MockContentLoader.class.getResourceAsStream("/testdata/treasures/92001_Ag_EU_TELtreasures.xml.details");
        InputStream xmlInput = MockContentLoader.class.getResourceAsStream("/testdata/treasures/92001_Ag_EU_TELtreasures.xml");
        InputStream mappingInput = MockContentLoader.class.getResourceAsStream("/testdata/treasures/92001_Ag_EU_TELtreasures.xml.mapping");
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        zos.putNextEntry(new ZipEntry("testdata/treasures/92001_Ag_EU_TELtreasures.xml.details"));
        stream(detailsInput, zos);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("testdata/treasures/92001_Ag_EU_TELtreasures.xml"));
        stream(xmlInput, zos);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("testdata/treasures/92001_Ag_EU_TELtreasures.xml.mapping"));
        stream(mappingInput, zos);
        zos.closeEntry();
        zos.close();
    }

    private void uploadFile(File file) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        String postUrl = String.format(
                "http://localhost:8983/services/dataset/submit/%s?accessKey=%s",
                file.getName(),
                serviceKey
        );
        log.info("Posting to: " + postUrl);
        HttpPost httpPost = new HttpPost(postUrl);
        FileEntity fileEntity = new FileEntity(file, "application/zip");
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

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: <accessKey>");
        }
        else {
            MockContentLoader uploader = new MockContentLoader(args[0]);
            uploader.run();
        }
    }
}
