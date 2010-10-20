package eu.delving.core.binding

import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import scala.collection.JavaConversions._
import org.scalatest. {BeforeAndAfterAll, Spec}
import eu.europeana.core.querymodel.query.SolrTester

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10 /18/10 9:04 PM
 */

@RunWith(classOf[JUnitRunner])
class SolrBindingServiceSpec extends Spec with ShouldMatchers with BeforeAndAfterAll with SolrTester {

  override def afterAll() = stopSolrServer

  describe("A SolrBindingService") {

    describe("(when retrieving a SolrDocumentList)") {

      it("should give back all values as a List") {
        val response = getQueryResponse("*:*")
        val ids = SolrBindingService.getSolrDocumentList(response)
        ids.length should equal (10)
        ids.head.getFieldNames.isEmpty should be (false)
        ids.head.get("europeana_uri").head should equal ("92001/1")
        ids.head.get("unknown_field").isEmpty should be (true)
      }


      it("should ")(pending)

    }

    describe("(when retrieving a DocId List)") {

      it("should give back a list of DocIds") {
        val response = getQueryResponse("*:*")
        val idList = SolrBindingService.getDocIds(response)
        val firstId = idList.head
        firstId.getEuropeanaUri should equal("92001/1")
        firstId.getTimestamp.toString.isEmpty should be(false)
      }

    }

  }

}