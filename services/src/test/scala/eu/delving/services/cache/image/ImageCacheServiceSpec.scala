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

        val testUrl = """http://www.sffarkiv.no/webdb/fileStream.aspx?fileName=dbatlas_leks\1412-sol\1412001004.jpg"""
        val sanitizedUrl : String = ImageCacheService.sanitizeUrl(testUrl)

        it("should retrieve it from the remote source") {
          ImageCacheService.retrieveImageFromUrl(sanitizedUrl).storable should be (true)
        }

        it("should store the image in Mongo") {
          ImageCacheService.storeImage(sanitizedUrl).available should be (true)
        }

        it("should find the saved item") {
          val cachedImage = ImageCacheService.findImageInCache(sanitizedUrl)
          cachedImage.getFilename should equal (sanitizedUrl)
          cachedImage.get("viewed").asInstanceOf[Int] should not equal (0)
        }

      }
      
    }
}