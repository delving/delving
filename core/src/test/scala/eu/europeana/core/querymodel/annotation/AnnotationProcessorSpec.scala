package eu.europeana.core.querymodel.query

import _root_.eu.europeana.core.querymodel.annotation._
import _root_.java.util.Date
import _root_.org.apache.solr.client.solrj.beans.Field
import _root_.eu.europeana.core.querymodel.beans.{AllFieldBean, FullBean, BriefBean, IdBean}
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner
import collection.mutable.ListBuffer

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

@RunWith(classOf[JUnitRunner])
class AnnotationProcessorSpec extends Spec with ShouldMatchers {

  describe("An AnnotationProcessor") {

    describe("(when it is given a List of annotated classes)") {
      val beanClasses = new ListBuffer[Class[_]] += ((new IdBean).getClass, (new BriefBean).getClass, (new FullBean).getClass, (new AllFieldBean).getClass)
      val mockBeans = new ListBuffer[Class[_]] += ((new MockAnnotatedBean).getClass)
      val processor = new AnnotationProcessorImpl
      processor setClasses mockBeans

      it("should give EuropeanaBeans for each class") {
        beanClasses.foreach {
          beanClass: Class[_] =>
              println (beanClass.getCanonicalName)
              println (beanClass.getFields.foreach( println (_)))
              println (beanClass.getAnnotations.size)
//            val bean: EuropeanaBean = processor.getEuropeanaBean (beanClass)
//            bean.getFieldStrings.foreach( println (_) )
//            europeanaBean.getFields.foreach(field => println (field) )
        }
      }
    }

  }



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

class MockAnnotatedBean {

  @Europeana(briefDoc = true, id = true)
  @Solr(namespace = "europeana", name = "uri", multivalued = false, required = true)
  @Field("europeana_uri")
  @scala.reflect.BeanProperty
  val europeanaUri: String = "europeanaUri"

  @Europeana()
  @Solr(name = "timestamp", multivalued = false, defaultValue = "NOW")
  @Field("timestamp")
  @scala.reflect.BeanProperty
  val timestamp = new Date
}