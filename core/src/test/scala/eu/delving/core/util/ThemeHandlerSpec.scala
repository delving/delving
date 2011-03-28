/*
 * Copyright 2011 DELVING BV
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

package eu.delving.core.util

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import org.junit.runner.RunWith
import java.util.{Arrays, Properties}
import eu.delving.metadata.MetadataModelImpl
import eu.europeana.core.querymodel.query.DelvingTestUtil

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 3/9/11 3:46 PM  
 */

@RunWith(classOf[JUnitRunner])
class ThemeHandlerSpec extends Spec with ShouldMatchers with DelvingTestUtil {
  
  describe("A ThemeHandler") {
      
      describe("(when receiving a path to the theme file)") {
        val themeHandler = getThemeHandler

        it("should load it the themes from the property path") {
          themeHandler.hasSingleTheme should be (false)
        }

        it("should give access to the themes") {
          themeHandler.getDefaultTheme.name should equal ("norvegiana")
          themeHandler.getByBaseUrl("localhost").name should equal ("friesmuseum")
        }
     }
  }
}