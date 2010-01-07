package eu.europeana.beans;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals(5, processor.getFacetFields().size());
        for (FacetField ff : processor.getFacetFields()) {
            log.info("facet " + ff.getFieldNameString());
        }
    }

}