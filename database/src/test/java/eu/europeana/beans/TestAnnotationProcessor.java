package eu.europeana.beans;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestAnnotationProcessor {

    private Logger log = Logger.getLogger(getClass());

    @Test
    public void readAnnotations() throws Exception {
        AnnotationProcessor processor = new AnnotationProcessor(
                IdBean.class,
                RequiredBean.class,
                BriefBean.class,
                FullBean.class,
                AllFieldBean.class
        );
        assertEquals(9, processor.getFields(BriefBean.class).size());
        for (Field field : processor.getFields(BriefBean.class)) {
            log.info("BriefBean field "+field.getName());
        }
        assertEquals(5, processor.getFacetFields().size());
        for (FacetField ff : processor.getFacetFields()) {
            log.info("facet " + ff.getFieldNameString());
        }

    }

}