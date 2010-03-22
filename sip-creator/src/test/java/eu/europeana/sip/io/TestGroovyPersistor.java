package eu.europeana.sip.io;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class TestGroovyPersistor {

    private final Logger LOG = Logger.getLogger(this.getClass().getName());
    private final String GROOVY_SNIPPET = "println(\"aap\");";
    private final static String GROOVY_MAPPING_FILE = "File.mapping";
    private File mappingFile;
    private GroovyPersistor groovyPersistor;

    @Before
    public void setUp() {
        mappingFile = new File(GROOVY_MAPPING_FILE);
        groovyPersistor = new GroovyPersistorImpl(mappingFile);
    }

    @Test
    public void testSave() throws IOException {
        groovyPersistor.save(new StringBuffer(GROOVY_SNIPPET));
        LOG.info(String.format("Writing to %s; %s [%d bytes written]%n", mappingFile, GROOVY_SNIPPET, GROOVY_SNIPPET.length()));
    }


    @Test
    public void readSnippet() throws IOException {
        String result = groovyPersistor.read();
        Assert.assertTrue(GROOVY_SNIPPET.equals(result));
        LOG.info(String.format("Reading from %s; %s [%d bytes read]%n", mappingFile, result, result.length()));
    }

    @Test
    public void tearDown() {
        if (mappingFile.delete()) {
            LOG.info("File deleted ...");
        }
    }

}
