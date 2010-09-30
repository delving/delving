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
        val form = new AdvancedSearchForm

        it("should ignore empty fields") {
          form setFacet0 ("text"); form setValue0 ("max"); form setOperator1 ("AND")
          form.toSolrQuery should equal ("text:max")
        }

        it("should insert operators between inputs when not AND") {
          form setFacet1 ("title"); form setValue1 ("kaiser"); form setOperator2 ("AND")
          form.toSolrQuery should equal ("text:max title:kaiser")
          form setOperator1 ("OR")
          form.toSolrQuery should equal ("text:max OR title:kaiser")
        }

        it("should not insert an operator after the third query term") {
          form setFacet2 ("YEAR"); form setValue2 ("1977");
          form.toSolrQuery should equal ("text:max OR title:kaiser YEAR:1977")
        }

        it("should only include collection when the value is not 'all_collections' ") {
          val localForm = new AdvancedSearchForm
          localForm setFacet0 ("text"); localForm setValue0 ("max"); localForm setOperator1 ("AND")
          localForm setCollection ("all_collections")
          localForm.toSolrQuery should equal ("text:max")
          localForm setCollection ("tel_treasures")
          localForm.toSolrQuery should equal ("text:max&qf=COLLECTION:tel_treasures")
        }

        it("should only include sortBy when the value is not 'bla' ") {
          val localForm = new AdvancedSearchForm
          localForm setFacet0 ("text"); localForm setValue0 ("max"); localForm setOperator1 ("AND")
          localForm setSortBy ("title")
          localForm.toSolrQuery should equal ("text:max&sortBy=title")
        }
      }

    }

}