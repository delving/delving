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
class EuropeanaQueryExceptionSpec extends Spec with ShouldMatchers {

  describe("A EuropeanaQueryException") {
    val problem = QueryProblem.RECORD_NOT_FOUND
    val exception = new EuropeanaQueryException(problem toString)

    describe("(when it contains a message)") {

      it("should give return that message through getMessage()") {
        exception.getMessage should equal(problem toString)
      }
    }

    describe("(when the exception message is given to QueryProblem)") {

      it("should find the correct QueryProblem enumeration") {
        QueryProblem get (exception getMessage) should equal (problem)
      }
    }
  }
}