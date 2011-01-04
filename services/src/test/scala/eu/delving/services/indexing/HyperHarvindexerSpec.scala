package eu.delving.services.indexing

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/3/11 9:06 PM  
 */

class HyperHarvindexerSpec extends Spec with ShouldMatchers {

  describe("A HyperHarvindexer") {

      describe("(when running a harvesting run)") {

        it("should harvest all records from sffKL") {
          HyperHarvindexer.importFromPmh("sffKL", "abm", "http://localhost:8983/services/oai-pmh") should equal ((8, 1866))
        }

        it("should harvest all records from sffFoto") {
          HyperHarvindexer.importFromPmh("sffFoto", "abm", "http://localhost:8983/services/oai-pmh") should equal ((286, 71390))
        }
      }

    }
}