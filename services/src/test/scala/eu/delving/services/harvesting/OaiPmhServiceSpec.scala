package eu.delving.services.harvesting

import org.scalatest.matchers.ShouldMatchers
import javax.servlet.http.HttpServletRequest
import org.springframework.mock.web.MockHttpServletRequest
import java.lang.String
import collection.immutable.List
import scala.collection.JavaConversions._
import eu.delving.services.impl.MetaRepoImpl
import org.scalatest.{PrivateMethodTester, Spec}
import collection.mutable.HashMap
import eu.delving.services.core.MetaRepo.PmhVerb
import java.util.{Date, Map}
import eu.delving.services.exceptions.BadArgumentException

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 16, 2010 12:05:48 AM
 */

class OaiPmhServiceSpec extends Spec with ShouldMatchers {
  val metaRepo = new MetaRepoImpl

  describe("A OaiPmhService") {

    describe("(when creating a PmhRequest)") {

      it("should give back an empty string for each missing Request Parameters") {
        val request = createRequest(List("verb" -> "ListRecords", "set" -> "92017", "metadataPrefix" -> "abm"))
        val parser = new OaiPmhService(request, metaRepo)
        val pmhRequest = parser.createPmhRequest(parser.asSingleValueMap(parser.getRequestParams(request)), PmhVerb.LIST_RECORDS)
        pmhRequest.resumptionToken should equal("")
        pmhRequest.pmhRequestItem.identifier should equal("")
        pmhRequest.pmhRequestItem.set should equal("92017")
      }

      it("should only give back an resumptionToken when it is part of the Request Parameters") {
        val resumptionToken = "abc123"
        val request = createRequest(List("verb" -> "ListRecords", "resumptionToken" -> resumptionToken, "set" -> "92017", "metadataPrefix" -> "abm"))
        val parser = new OaiPmhService(request, metaRepo)
        val pmhRequest = parser.createPmhRequest(parser.asSingleValueMap(parser.getRequestParams(request)), PmhVerb.LIST_RECORDS)
        pmhRequest.resumptionToken should equal(resumptionToken)
      }

    }

    describe("(when checking for legal PmhRequest)") {

      it("should return false when no 'verb' parameter is present") {
        val request = createRequest(List("set" -> "92017", "metadataPrefix" -> "abm"))
        val parser = new OaiPmhService(request, metaRepo)
        parser.isLegalPmhRequest(parser.getRequestParams(request)) should be(false)
      }

      it("should return false when repeat parametersa are used") {
        val request = createRequest(List("verb" -> "ListRecords", "set" -> "92017", "set" -> "92018", "metadataPrefix" -> "abm"))
        val parser = new OaiPmhService(request, metaRepo)
        val params = HashMap[String, Array[String]]("set" -> Array[String]("a", "b"))
        parser.isLegalPmhRequest(params) should be(false)
      }

      it("should return false when false when non-allowed parameters are used") {
        val request = createRequest(List("verb" -> "ListRecords", "seta" -> "92017", "metadataPrefix" -> "abm"))
        val parser = new OaiPmhService(request, metaRepo)
        parser.isLegalPmhRequest(parser.getRequestParams(request)) should be(false)
      }

    }

    describe("(when parsing dates from the request)") {
      val request = createRequest(List("set" -> "92017", "metadataPrefix" -> "abm", "from" -> "01-01-1970"))
      val parser = new OaiPmhService(request, metaRepo)

      it("should parse a default date correctly") {
        parser.getDate("01-01-1970").toString should equal ("Thu Jan 01 00:00:00 CET 1970")
      }

      it("should ignore time") {
        parser.getDate("01-01-1970 00:00:01").toString should equal ("Thu Jan 01 00:00:00 CET 1970")
      }

      it("should complain when given a bad dateString") {
          evaluating { parser getDate ("01/01/1970") } should produce [BadArgumentException]
          evaluating { parser getDate ("1970-01-01") } should produce [BadArgumentException]
      }

    }
  }

  private def createRequest(paramList: List[(String, String)]): HttpServletRequest = {
    val request = new MockHttpServletRequest
    request.setRequestURI("http://www.delving.eu/meta-repo/oai-pmh")
    paramList foreach (param => request setParameter (param._1, param._2))
    request
  }

}