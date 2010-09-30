package eu.delving.web.controller

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.apache.solr.client.solrj.SolrQuery

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 30, 2010 2:08:46 PM
 */

class AdvancedSearchFormSpec extends Spec with ShouldMatchers {

  describe("An Advanced Search Form") {

      describe("(when converting to SolrQuery)") {


        it("should ignore empty fields") {
          val form = new AdvancedSearchForm
          form setFacet0 ("text"); form setValue0 ("max")
          form.toSolrQuery.toString should equal (new SolrQuery("text:max").toString)
        }

        it("should render the search fields as one lucene query string as toString")(pending)

      }

    }

}