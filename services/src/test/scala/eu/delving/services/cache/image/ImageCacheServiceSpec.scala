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

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/1/11 9:08 PM  
 */

class ImageCacheServiceSpec extends Spec with ShouldMatchers {

  describe("A imageCache") {

      val factory = new MongoFactory
      factory.setTestContext("true")

      val imageCacheService = new ImageCacheService(factory)

      describe("(when an object is not found in the cache)") {

        val testUrl = """http://www.sffarkiv.no/webdb/fileStream.aspx?fileName=dbatlas_leks\1412-sol\1412001004.jpg"""
        val sanitizedUrl : String = imageCacheService.sanitizeUrl(testUrl)

        it("should retrieve it from the remote source") {
          imageCacheService.retrieveImageFromUrl(sanitizedUrl).storable should be (true)
        }

        it("should store the image in Mongo") {
          imageCacheService.storeImage(sanitizedUrl).available should be (true)
        }

        it("should find the saved item") {
          val cachedImage = imageCacheService.findImageInCache(sanitizedUrl)
          cachedImage.getFilename should equal (sanitizedUrl)
          cachedImage.get("viewed").asInstanceOf[Int] should not equal (0)
        }
        
        }

      }
    }
