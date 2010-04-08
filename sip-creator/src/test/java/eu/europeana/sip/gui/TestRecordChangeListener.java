package eu.europeana.sip.gui;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;

/**
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class TestRecordChangeListener {

    private final Logger LOG = Logger.getLogger(TestRecordChangeListener.class);

    private GroovyEditor groovyEditor = new GroovyEditor();
    private File file;
    private QName recordRoot;

    @Before
    public void setUp() {
        file = new File("SomeFile.xml");
        recordRoot = new QName("TEST_DELIMITER");
    }

    @Test
    public void save() throws IOException {
        LOG.info(String.format("Saving '%s' to file %s.record%n", recordRoot, file.getAbsoluteFile()));
        groovyEditor.save(file, recordRoot);
    }


    @Test
    public void load() throws IOException {
        LOG.info(String.format("Loaded '%s' from file %s.record%n", recordRoot, file.getAbsoluteFile()));
        assert recordRoot == groovyEditor.load(file);
    }

    @After
    public void tearDown() {
        LOG.info(String.format("Cleaning up %s%n", file.getAbsoluteFile()));
        if (file.exists()) {
            file.delete();
        }
    }

}
