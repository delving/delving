/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.delving.services.cache.image

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import eu.delving.core.util.MongoFactory
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.mock.MockitoSugar
import javax.servlet.http.HttpServletResponse
import org.mockito.Mockito._
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io._
import javax.servlet.ServletOutputStream

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/1/11 9:08 PM  
 */

@RunWith(classOf[JUnitRunner])
class ImageCacheServiceSpec extends Spec with ShouldMatchers with MockitoSugar {

  describe("A imageCache") {

    val factory = new MongoFactory
    factory.setTestContext("true")
    factory.afterPropertiesSet()

    val imageCacheService = new ImageCacheService(factory)
    val testUrl = """https://repository.uba.uva.nl:8443/fedora/objects/unicum:tud.images.f777593b7b667ab5d6efef5eb26d88f8/datastreams/IMAGE/content"""
//    val testUrl = """http://62.221.199.163:5294/imageproxy.asp?server=62.221.199.182&port=2107&maxwidth=500&filename=1154-A1.jpg"""

    describe("(when an object is not found in the cache)") {

      val sanitizedUrl: String = imageCacheService.sanitizeUrl(testUrl)

      it("should retrieve it from the remote source") {
        imageCacheService.retrieveImageFromUrl(sanitizedUrl).storable should be(true)
      }

      it("should store the image in Mongo") {
        imageCacheService.storeImage(sanitizedUrl).available should be(true)
      }

      it("should find the saved item") {
        val cachedImage = imageCacheService.findImageInCache(sanitizedUrl)
        cachedImage.getFilename should equal(sanitizedUrl)
        cachedImage.get("viewed").asInstanceOf[Int] should not equal (0)
      }

    }

    describe("(when asked to serve a thumbnail)") {

      val mockResponse:HttpServletResponse = mock[HttpServletResponse]
      val baos:ByteArrayOutputStream = new ByteArrayOutputStream()
      val fakeStream:ServletOutputStream = new ServletOutputStream {
        def write(p1: Int) {
          baos.write(p1)
        }
      }
      when(mockResponse.getOutputStream).thenReturn(fakeStream)

      it("should resize images to a width of 220px") {
        imageCacheService.retrieveImageFromCache(testUrl, "BRIEF_DOC", mockResponse)
        val resized:BufferedImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray))
        resized.getWidth should equal (220)
      }
    }

  }
}
