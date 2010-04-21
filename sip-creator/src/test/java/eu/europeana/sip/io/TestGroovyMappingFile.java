package eu.europeana.sip.io;

import eu.europeana.core.querymodel.beans.AllFieldBean;
import eu.europeana.core.querymodel.beans.BriefBean;
import eu.europeana.core.querymodel.beans.FullBean;
import eu.europeana.core.querymodel.beans.IdBean;
import eu.europeana.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.definitions.annotations.EuropeanaField;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test the GroovyMappingFile
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class TestGroovyMappingFile {

    private final static String TEST_FIELD = "dc_relation";

    private GroovyMappingFile groovyMappingFile;
    private AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
    List<Class<?>> list = new ArrayList<Class<?>>();

    @Before
    public void setUp() throws FileNotFoundException {
        groovyMappingFile = new GroovyMappingFileImpl(new File("Somefile.xml"));
        list.add(IdBean.class);
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        annotationProcessor.setClasses(list);
    }

    @Test
    public void testStoreAll() throws IOException {
        for (EuropeanaField europeanaField : annotationProcessor.getMappableFields()) {
            groovyMappingFile.storeNode(new GroovyMappingFile.Delimiter(europeanaField.getFacetName()), GroovyService.generateGroovyLoop(europeanaField.getFacetName()));
        }
        groovyMappingFile.close();
    }

    @Test
    public void testStore() throws IOException {
        groovyMappingFile.storeNode(new GroovyMappingFile.Delimiter(TEST_FIELD), GroovyService.generateGroovyLoop(TEST_FIELD));
        groovyMappingFile.close();
        System.out.printf("Stored%n%s%n%s%n", new GroovyMappingFile.Delimiter(TEST_FIELD), GroovyService.generateGroovyLoop(TEST_FIELD));
    }

    @Test
    public void testRetrieve() throws IOException {
        String node = groovyMappingFile.findNode(new GroovyMappingFile.Delimiter(TEST_FIELD));
        System.out.printf("The following node has been found%n%s%n", node);
    }

    @Test
    public void testDelete() throws IOException {
        groovyMappingFile.deleteNode(new GroovyMappingFile.Delimiter(TEST_FIELD));
    }
}
