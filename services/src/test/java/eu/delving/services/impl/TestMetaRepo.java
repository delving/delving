package eu.delving.services.impl;

import eu.delving.services.util.MongoDaemonRunner;
import eu.delving.services.util.ZipUploader;
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
    public void queryOriginalRecords() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:8080/meta-repo/00101_Ag_NO_sffDF/abm.html");
        HttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Response not OK");
        }
        String responseString = EntityUtils.toString(response.getEntity());
        LOG.info("GET response content type:"+response.getEntity().getContentType());
        LOG.info("Entity returned: "+ responseString);
        Assert.assertTrue("Response contents not right", responseString.contains("Solan og Ludvik, Reodor og r\u00E5skinnet Desperados endelig hjem."));
        Assert.assertFalse("Response contents not right", responseString.contains("europeana:uri"));
        Assert.assertTrue("Response contents not right", responseString.contains("abm:county"));
        httpClient.getConnectionManager().shutdown();
    }

    @Test
    public void queryMappedRecords() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:8080/meta-repo/00101_Ag_NO_sffDF/ese.html");
        HttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Response not OK");
        }
        String responseString = EntityUtils.toString(response.getEntity());
        LOG.info("GET response content type:"+response.getEntity().getContentType());
        LOG.info("Entity returned: "+ responseString);
        Assert.assertTrue("Response contents not right", responseString.contains("Solan og Ludvik, Reodor og r\u00E5skinnet Desperados endelig hjem."));
        Assert.assertTrue("Response contents not right", responseString.contains("europeana:uri"));
        Assert.assertFalse("Response contents not right", responseString.contains("abm:county"));
        httpClient.getConnectionManager().shutdown();
    }

    @Test
    public void queryFormats() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:8080/meta-repo/formats.html");
        HttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Response not OK");
        }
        String responseString = EntityUtils.toString(response.getEntity());
        LOG.info("Entity returned: "+ responseString);
        Assert.assertTrue("Response contents not right", responseString.contains("to_be_decided"));
        Assert.assertTrue("Response contents not right", responseString.contains("ESE-V3.2.xsd"));
        httpClient.getConnectionManager().shutdown();
    }
}
