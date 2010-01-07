package eu.europeana.beans;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestAnnotationProcessor {

    private Logger log = Logger.getLogger(getClass());

    @Test
    public void processThem() throws Exception {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(IdBean.class);
        list.add(RequiredBean.class);
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
        annotationProcessor.setClasses(list);
        assertEquals(9, annotationProcessor.getFields(BriefBean.class).size());
        for (Field field : annotationProcessor.getFields(BriefBean.class)) {
            log.info("BriefBean field "+field.getName());
        }
        assertEquals(5, annotationProcessor.getFacetFields().size());
        for (FacetField ff : annotationProcessor.getFacetFields()) {
            log.info("facet " + ff.getFieldNameString());
        }

    }

}