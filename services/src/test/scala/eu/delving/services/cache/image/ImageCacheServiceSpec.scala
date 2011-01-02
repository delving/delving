package eu.delving.services.cache.image

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/1/11 9:08 PM  
 */

class ImageCacheServiceSpec extends Spec with ShouldMatchers {

  describe("A imageCache") {
      
      describe("(when an object is not found in the cache)") {

        val testUrl = "http://www.delving.eu/images/erasmus-bridge-1.png"

        it("should retrieve it from the remote source") {
          ImageCacheService.retrieveImageFromUrl(testUrl).storable should be (true)
        }

        it("should store the image in Mongo") {
          ImageCacheService.storeImage(testUrl).found should be (true)
        }

        it("should find the saved item") {
          val cachedImage = ImageCacheService.findImage(testUrl)
          cachedImage.getFilename should equal (testUrl)
        }

      }
      
    }
}