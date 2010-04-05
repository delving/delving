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

  /*
  @Test
    public void testGetMessage() {
        EuropeanaQueryException exception = new EuropeanaQueryException(QueryProblem.RECORD_NOT_FOUND.toString());
        QueryProblem problem = QueryProblem.get(exception.getMessage());
        Assert.assertEquals("Should be full-doc not found", QueryProblem.RECORD_NOT_FOUND, problem);
    }
   */
}