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
    private final String GROOVY_SNIPPET = "println(\"this is a groovy test\");";
    private final static String GROOVY_MAPPING_FILE = "Groovy.mapping";
    private File mappingFile;
    private GroovyService groovyService;

    @Before
    public void setUp() {
        mappingFile = new File(GROOVY_MAPPING_FILE);
        groovyService = new GroovyService(mappingFile);
    }

    @Test
    public void testSave() throws IOException {
        groovyService.save(mappingFile, GROOVY_SNIPPET);
        LOG.info(String.format("Writing to %s; %s [%d bytes written]%n", mappingFile, GROOVY_SNIPPET, GROOVY_SNIPPET.length()));
    }

    @Test
    public void readSnippet() throws IOException {
        groovyService.read(mappingFile,
                new GroovyService.LoadListener() {
                    @Override
                    public void loadComplete(String groovySnippet) {
                        Assert.assertTrue(GROOVY_SNIPPET.equals(groovySnippet));
                        LOG.info(String.format("Reading from %s; %s [%d bytes read]%n", mappingFile, groovySnippet, groovySnippet.length()));
                    }
                }
        );
    }

    @Test
    public void tearDown
            () {
        if (mappingFile.delete()) {
            LOG.info("File deleted ...");
        }
    }

}
