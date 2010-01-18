package eu.europeana.beans;

import eu.europeana.beans.annotation.AnnotationProcessorImpl;
import eu.europeana.beans.annotation.EuropeanaBean;
import eu.europeana.beans.annotation.EuropeanaField;
import org.apache.log4j.Logger;
import org.junit.Test;

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
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
        annotationProcessor.setClasses(list);
        EuropeanaBean bean = annotationProcessor.getEuropeanaBean(BriefBean.class);
        assertEquals(10, bean.getFields().size());
        for (EuropeanaField field : bean.getFields()) {
            log.info("BriefBean field "+field.getName());
        }
        assertEquals(5, annotationProcessor.getFacetFields().size());
        for (EuropeanaField ff : annotationProcessor.getFacetFields()) {
            log.info("facet " + ff.getFieldNameString());
        }

    }

}