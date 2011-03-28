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

package eu.europeana.core.querymodel.query

import org.springframework.mock.web.MockHttpServletRequest
import java.util.{Arrays, Properties}
import eu.delving.core.util.{ThemeHandler, LaunchProperties}
import scala.collection.JavaConversions._
import eu.delving.metadata.{RecordDefinition, MetadataModelImpl}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Oct 1, 2010 11:30:16 PM
 */

trait DelvingTestUtil {

  private val analyzerBean = new QueryAnalyzer()
  System.setProperty("launch.properties", classOf[DelvingTestUtil].getResource("/mock-launch.properties").getFile)

  private val launchProperties: Properties = new LaunchProperties(Arrays.asList("portal.theme.file"))
  private val themeHandler = new ThemeHandler()
  themeHandler.setLaunchProperties(launchProperties)
  private val metadataModel = new MetadataModelImpl()
  metadataModel setDefaultPrefix ("abm")
  metadataModel setRecordDefinitionResources (asJavaList(List[String]("/ese-record-definition.xml", "/abm-record-definition.xml")))
  themeHandler.setMetadataModel(metadataModel)

  def getQueryAnalyser : QueryAnalyzer = analyzerBean
  def getThemeHandler : ThemeHandler = themeHandler
  def getLaunchProperties : Properties = launchProperties
  def getRecordDefinition : RecordDefinition = themeHandler.getDefaultTheme.getRecordDefinition

  // RequestBased helpers
  def createParamsMap(params: List[(String, String)]) = {
    val request: MockHttpServletRequest = new MockHttpServletRequest
    params.foreach(param =>
      request.addParameter(param._1, param._2))
    request.getParameterMap
  }

  def createRequest(params: List[(String, String)]) = {
    val request: MockHttpServletRequest = new MockHttpServletRequest
    params.foreach(param =>
      request.addParameter(param._1, param._2))
    request
  }
}