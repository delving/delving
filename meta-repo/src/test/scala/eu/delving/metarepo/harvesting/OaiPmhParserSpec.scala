package eu.delving.metarepo.harvesting

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import javax.servlet.http.HttpServletRequest
import org.springframework.mock.web.MockHttpServletRequest
import java.lang.String
import collection.immutable.List
import java.util.Map
import eu.delving.metarepo.impl.MetaRepoImpl

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 16, 2010 12:05:48 AM
 */

class OaiPmhParserSpec extends Spec with ShouldMatchers {

  val metaRepo = new MetaRepoImpl

  describe("A OaiPmhParser") {

    describe("(when receiving a Identify request)") {
      val paramList = List("verb" -> "Identify", "metadataPrefix" -> "oai_dc")
      val request = new MockHttpServletRequest
      request.setRequestURI("http://www.delving.eu/meta-repo/oai-pmh")
      paramList foreach (param => request setParameter (param._1, param._2))
//      request.addParameter("verba", Array[String]("zozo", "ozoz"))

      it("should process only one of each") {
        val parser = new OaiPmhParser(request, metaRepo)
        println (parser.parseRequest)
      }

      it("should give back an Identify response")(pending)

      it("should ignore all other parameters")(pending)

    }

    describe("(when receiving a ListMedataFormats Request)") {

      it("should bla")(pending)
    }

    describe("(when receiving a request without a verb)") {

      it("should give a badVerb response")(pending)

      it("should bla")(pending)

    }

    describe("(when giving a httpRequest)") {



      it("should determine the verb") {

      }

      it("should ")(pending)

    }

    describe("(when creating an ErrorResponse)") {
      val request: HttpServletRequest = createRequest(List("bad" -> "attributes"))

      it("should give back badVerb when BadVerb is given") {
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