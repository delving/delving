package eu.delving.metarepo.impl;

import eu.delving.metarepo.util.MongoDaemonRunner;
import eu.delving.metarepo.util.ZipUploader;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.IOException;

/**
 * Go through the whole cycle with test data
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMetaRepo {
    private static final Logger LOG = Logger.getLogger(TestMetaRepo.class);
    private static MongoDaemonRunner daemonRunner;
    private static Server server;

    @BeforeClass
    public static void startUp() throws Exception {
        LOG.info("Starting mongo daemon");
        daemonRunner = new MongoDaemonRunner();
        daemonRunner.start();
        daemonRunner.waitUntilRunning();
        LOG.info("Launching web application");
        server = new Server(8080);
        server.setHandler(new WebAppContext("meta-repo/src/main/webapp", "/meta-repo"));
        server.start();
        LOG.info("Uploading test collection");
        ZipUploader zipUploader = new ZipUploader();
        zipUploader.upload();
        LOG.info("Collection ready");
    }

    @AfterClass
    public static void stop() throws Exception {
        daemonRunner.kill();
        server.stop();
    }

    @Test
    public void query() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:8080/meta-repo/92017/index.html");
        HttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Response not OK");
        }
        String responseString = EntityUtils.toString(response.getEntity());
        LOG.info("GET response content type:"+response.getEntity().getContentType());
        LOG.info("Entity returned: "+ responseString);
        Assert.assertTrue("Response contents not right", responseString.contains("398fa12b-169d-4ae2-b086-0af78d4454d1"));
        Assert.assertTrue("Response contents not right", responseString.contains("783972d2-b37b-4884-be6c-eacbf6183ac6"));
        httpClient.getConnectionManager().shutdown();
    }
}
