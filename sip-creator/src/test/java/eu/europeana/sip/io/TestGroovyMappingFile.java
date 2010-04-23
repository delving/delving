package eu.europeana.sip.io;

import eu.europeana.core.querymodel.beans.AllFieldBean;
import eu.europeana.core.querymodel.beans.BriefBean;
import eu.europeana.core.querymodel.beans.FullBean;
import eu.europeana.core.querymodel.beans.IdBean;
import eu.europeana.definitions.annotations.AnnotationProcessorImpl;
import eu.europeana.definitions.annotations.EuropeanaField;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test the GroovyMapping
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class TestGroovyMappingFile {

    private final static String TEST_FIELD = "dc_relation";

    private GroovyMapping groovyMapping;
    private AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
    List<Class<?>> list = new ArrayList<Class<?>>();

    @Before
    public void setUp() throws FileNotFoundException {
        groovyMapping = new GroovyMappingImpl();
        list.add(IdBean.class);
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        annotationProcessor.setClasses(list);
    }

    @Test
    public void testStoreAll() throws IOException {
        for (EuropeanaField europeanaField : annotationProcessor.getMappableFields()) {
            groovyMapping.storeNode(new GroovyMapping.Delimiter(String.format("%s:%s", europeanaField.getFieldNameString(), europeanaField.getFieldNameString())), GroovyService.generateGroovyLoop("src", europeanaField.getFieldNameString()));
        }
    }

    @Test
    public void testStore() throws IOException {
        groovyMapping.storeNode(new GroovyMapping.Delimiter(TEST_FIELD), GroovyService.generateGroovyLoop("src", TEST_FIELD));
        System.out.printf("Stored%n%s%n%s%n", new GroovyMapping.Delimiter(TEST_FIELD), GroovyService.generateGroovyLoop("src", TEST_FIELD));
    }

    @Test
    public void testRetrieve() throws IOException {
        String node = groovyMapping.findNode(new GroovyMapping.Delimiter(TEST_FIELD));
        System.out.printf("The following node has been found%n%s%n", node);
    }

    @Test
    public void testDelete() throws IOException {
        groovyMapping.deleteNode(new GroovyMapping.Delimiter(TEST_FIELD));
    }
}
