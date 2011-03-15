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

package eu.delving.core.util

import org.apache.log4j.Logger
import java.lang.String
import xml.{Node, NodeSeq, Elem, XML}
import javax.servlet.http.HttpServletRequest
import java.util.Properties
import reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 3/9/11 3:25 PM  
 */

class ThemeHandler {
  private val log: Logger = Logger.getLogger(getClass)

  private lazy val themeList: Seq[PortalTheme] = loadThemes(System.getProperty("delving.themes"))

  def hasSingleTheme : Boolean = themeList.length == 1

  def hasTheme(themeName : String) : Boolean = !themeList.filter(theme => theme.name == themeName).isEmpty

  def getDefaultTheme = themeList.filter(_.isDefault == true).head

  def getByThemeName(name : String) = {
    val theme = themeList.filter(_.name.equalsIgnoreCase(name))
    if (!theme.isEmpty) theme.head
    else getDefaultTheme
  }

  def getByBaseUrl(baseUrl : String) : PortalTheme = {
    val theme = themeList.filter(_.baseUrl.equalsIgnoreCase(baseUrl.replaceFirst("http[s]?://", "")))
    if (!theme.isEmpty) theme.head
    else getDefaultTheme
  }

  def getByBaseUrl(request : HttpServletRequest) : PortalTheme = getByBaseUrl(request.getRequestURI)

  private[util] def loadThemes(themeFilePath : String) : Seq[PortalTheme] = {

    if (themeFilePath == null) {
//      log.fatal("Theme file path must be defined with -Ddelving.themes=/path/to/property/file")
//      System.exit(1)
      def getProperty(prop : String) : (String) = launchProperties.getProperty(prop)

      return List[PortalTheme](PortalTheme(
        name = getProperty("portal.theme").toString.stripPrefix("theme/"),
        displayName = getProperty("portal.displayName"),
        templateDir = getProperty("portal.freemarker.path"),
        isDefault = true,
        baseUrl = getProperty("portal.baseUrl"),
        gaCode = getProperty("googleAnalytics.trackingCode"),
        addThisCode = getProperty("addThis.trackingCode")
      )).toSeq
    }

    def createPortalTheme(node : Node, isDefault : Boolean = false) : PortalTheme = {
      val templateDir = node \\ "templateDir"
      def getNodeText(label : String) = (node \\ label).text
      PortalTheme(
        name = getNodeText("name"),
        templateDir = getNodeText("templateDir"),
        isDefault = isDefault,
        hqf = getNodeText("hiddenQueryFilter"),
        baseUrl = getNodeText("portalBaseUrl"),
        displayName = getNodeText("portalDisplayName"),
        gaCode = getNodeText("googleAnalyticsTrackingCode"),
        addThisCode = getNodeText("addThisTrackingCode")
      )
    }

    val themes: Elem = XML.loadFile(themeFilePath)
    val themeList: NodeSeq = themes \\ "theme"
    val portalThemeSeq = themeList.map {
      themeNode =>
        val isDefault : Boolean = themeNode.attributes.get("default").head.text.toBoolean
        themeNode.child.filter(!_.label.startsWith("#PCDATA")).foreach(nd => println (nd.label + nd.text))
        createPortalTheme(themeNode, isDefault)
    }
    if (portalThemeSeq.isEmpty) {
        log.fatal("Error loading themes from " + themeFilePath)
        System.exit(1)
    }
    portalThemeSeq
  }

  @Autowired @BeanProperty var launchProperties:  Properties = _
}

case class PortalTheme (
  name : String,
  templateDir : String,
  isDefault : Boolean = false,
  hqf : String = "",
  baseUrl : String = "",
  displayName: String = "default",
  gaCode: String = "",
  addThisCode : String = "",
  defaultLanguage : String = "en"
)