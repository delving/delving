package eu.europeana.core.querymodel.query

import _root_.scala.collection.JavaConversions._
import _root_.java.util.Date
import _root_.org.apache.solr.client.solrj.beans.Field
import _root_.eu.europeana.core.querymodel.beans.{AllFieldBean, FullBean, BriefBean, IdBean}
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.matchers.ShouldMatchers
import _root_.org.scalatest.Spec
import _root_.org.scalatest.junit.JUnitRunner
import collection.mutable.ListBuffer
import eu.europeana.definitions.annotations._
import java.lang.annotation.Retention
//import eu.europeana.definitions.annotations._

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Apr 5, 2010 10:55:58 PM
 */

// todo finish this spec with mockBeanClass and test explicitly through it clauses

@RunWith(classOf[JUnitRunner])
class AnnotationProcessorSpec extends Spec with ShouldMatchers {
  describe("An AnnotationProcessor") {

//    describe("(when it is given a List of annotated classes)") {
//      val beanClasses = new ListBuffer[Class[_]] += (classOf[IdBean], classOf[BriefBean], classOf[FullBean], classOf[AllFieldBean])
//      val mockBeans = new ListBuffer[Class[_]] += classOf[MockAnnotatedBean]
//      val processor = new AnnotationProcessorImpl
//      processor.setClasses(beanClasses)
//
//      it("should give EuropeanaBeans for each class") {
//        mockBeans.foreach {
//          beanClass: Class[_] =>
//            println(beanClass.getCanonicalName)
//            println(beanClass.getFields.foreach(println(_)))
//            println(beanClass.getAnnotations.size)
//            println(beanClass.getDeclaredFields.toString)
//          //            val bean: EuropeanaBean = processor.getEuropeanaBean (beanClass)
//          //            bean.getFieldStrings.foreach( println (_) )
//          //            europeanaBean.getFields.foreach(field => println (field) )
//        }
//      }
//
//      it("should have 11 brief bean fields") {
//        val briefBean: EuropeanaBean = processor.getEuropeanaBean(classOf[BriefBean])
//        briefBean.getFields.size should equal(11)
//      }
//    }

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

  class MockAnnotatedBean private() {
    @Europeana(briefDoc = true, id = true)
    @Solr(prefix = "europeana", localName = "uri", multivalued = false, required = true)
    @Field("europeana_uri")
    @scala.reflect.BeanProperty
    val europeanaUri: String = "europeanaUri"

    @Europeana()
    @Solr(localName = "timestamp", multivalued = false, defaultValue = "NOW")
    @Field("timestamp")
    @scala.reflect.BeanProperty
    val timestamp = new Date


    @Field("COUNTRY")
    //  @Europeana(validation = ValidationLevel.COPY_FIELD, facet = true, facetPrefix = "coun")
    @Solr(fieldType = "string")
    @scala.reflect.BeanProperty
    val countryFacet: String = "germany"
  }

  object MockAnnotatedBean {lazy val instance = new MockAnnotatedBean}

}

