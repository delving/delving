package eu.europeana.core.querymodel.query

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class AnnotationProcessorSpec extends Spec with ShouldMatchers {

  /*
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
        assertEquals(11, bean.getFields().size());
        for (EuropeanaField field : bean.getFields()) {
            log.info("BriefBean field "+field.getName());
        }
        assertEquals(5, annotationProcessor.getFacetFields().size());
        for (EuropeanaField ff : annotationProcessor.getFacetFields()) {
            log.info("facet " + ff.getFieldNameString());
        }

    }
    */

}