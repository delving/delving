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

  private lazy val themeList: Seq[PortalTheme] = loadThemes()

  private lazy val debug = launchProperties.getProperty("debug").trim.toBoolean

  def hasSingleTheme : Boolean = themeList.length == 1

  def hasTheme(themeName : String) : Boolean = !themeList.filter(theme => theme.name == themeName).isEmpty

  def getDefaultTheme = themeList.filter(_.isDefault == true).head

  def getByThemeName(name : String) = {
    val theme = themeList.filter(_.name.equalsIgnoreCase(name))
    if (!theme.isEmpty) theme.head
    else getDefaultTheme
  }

  def getByBaseUrl(baseUrl : String) : PortalTheme = {
    val theme = themeList.filter(_.baseUrl.equalsIgnoreCase(baseUrl))
    if (!theme.isEmpty) theme.head
    else getDefaultTheme
  }

  def getByBaseUrl(request : HttpServletRequest) : PortalTheme = getByBaseUrl(request.getServerName)

  def getByRequest(request : HttpServletRequest) : PortalTheme = {
    if (hasSingleTheme) getDefaultTheme
    else if (debug && request.getParameterMap.containsKey("theme")) getByThemeName(request.getParameter("theme"))
    else getByBaseUrl(request)
  }

  private[util] def loadThemes() : Seq[PortalTheme] = {

    def getProperty(prop : String) : String = launchProperties.getProperty(prop).trim

    val themeFilePath = getProperty("portal.theme.file")

    if (themeFilePath == null) {
        log.fatal("portal.theme.file path must be defined in -Dlaunch.properties=/path/to/property/file");
        System.exit(1);
    }

    def createPortalTheme(node : Node, isDefault : Boolean = false) : PortalTheme = {
      val templateDir = node \\ "templateDir"
      def getNodeText(label : String) : String = (node \\ label).text

      def createEmailTarget(node: Node): EmailTarget = {
        EmailTarget(
          adminTo = getNodeText("adminTo"),
          exceptionTo = getNodeText("exceptionTo"),
          feedbackTo = getNodeText("feedbackTo"),
          registerTo = getNodeText("registerTo"),
          systemFrom = getNodeText("systemFrom"),
          feedbackFrom = getNodeText("feedbackFrom")
        )
      }

      PortalTheme(
        name = getNodeText("name"),
        templateDir = getNodeText("templateDir"),
        isDefault = isDefault,
        hqf = getNodeText("hiddenQueryFilter"),
        baseUrl = getNodeText("portalBaseUrl"),
        solrSelectUrl = getNodeText("solrSelectUrl"),
        cacheUrl = getNodeText("cacheUrl"),
        displayName = getNodeText("portalDisplayName"),
        gaCode = getNodeText("googleAnalyticsTrackingCode"),
        addThisCode = getNodeText("addThisTrackingCode"),
        defaultLanguage = getNodeText("defaultLanguage"),
        colorScheme = getNodeText("colorScheme"),
        emailTarget = createEmailTarget(node) ,
        homePage = getNodeText("homePage")
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
  defaultLanguage : String = "en",
  colorScheme : String = "azure",
  solrSelectUrl : String = "http://localhost:8983/solr",
  cacheUrl : String = "http://localhost:8983/services/image?",
  emailTarget : EmailTarget = EmailTarget(),
  homePage : String = ""
) {
  def getName = name
  def getTemplateDir = templateDir
  def getHiddenQueryFilters = hqf
  def getSolrSelectUrl = solrSelectUrl
  def getBaseUrl = baseUrl
  def getCacheUrl = cacheUrl
  def getDisplayName = displayName
  def getGaCode = gaCode
  def getAddThisCode = addThisCode
  def getDefaultLanguage = defaultLanguage
  def getColorScheme = colorScheme
  def getEmailTarget = emailTarget
  def getHomePage = homePage
}

case class EmailTarget(
   adminTo: String = "test-user@delving.eu",
   exceptionTo: String = "test-user@delving.eu",
   feedbackTo: String = "test-user@delving.eu",
   registerTo: String = "test-user@delving.eu",
   systemFrom: String = "noreply@delving.eu",
   feedbackFrom: String = "noreply@delving.eu"
) {
  def getAdminTo = adminTo
  def getExceptionTo = exceptionTo
  def getFeedbackTo = feedbackTo
  def getRegisterTo = registerTo
  def getSystemFrom = systemFrom
  def getFeebackFrom = feedbackFrom
}
