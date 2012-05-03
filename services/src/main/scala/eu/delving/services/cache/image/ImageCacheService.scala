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

package eu.delving.services.cache.image

import javax.servlet.http.HttpServletResponse
import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.methods.GetMethod
import com.mongodb.gridfs. {GridFSInputFile, GridFSDBFile, GridFS}
import org.apache.commons.io.IOUtils
import org.apache.commons.httpclient. {Header, HttpClient, MultiThreadedHttpConnectionManager}
import org.apache.log4j.Logger
import java.util.Date
import eu.delving.core.util.MongoFactory
import java.awt.image.BufferedImage
import com.thebuzzmedia.imgscalr.Scalr
import javax.imageio.ImageIO
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream, InputStream}
import eu.delving.services.core.TrustfulManager

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/2/11 10:09 PM  
 */

class ImageCacheService(mongoFactory : MongoFactory) {

  val imageCache = mongoFactory.getMongo.getDB("imageCache")
  val myFS = new GridFS(imageCache)

  // HttpClient Settings
  TrustfulManager.install()
  val connectionParams = new HttpConnectionManagerParams
  connectionParams setDefaultMaxConnectionsPerHost (15)
  connectionParams setMaxTotalConnections (150)
  connectionParams setConnectionTimeout (5000)
  connectionParams setSoTimeout(5000)
  val multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager()
  multiThreadedHttpConnectionManager setParams (connectionParams)

  // General Settings
  val thumbnailWidth = 220
  val thumbnailSizeString = "BRIEF_DOC"
  val thumbnailSuffix = "_THUMBNAIL"
  val cacheDuration = 60 * 60 * 24
  private val log : Logger = Logger.getLogger("ImageCacheService")

  // findImageInCache
  def findImageInCache(url: String, thumbnail: Boolean = false) : GridFSDBFile = {
    log info ("attempting to retrieve %s: " format(if(thumbnail) "thumbnail for image" else "image") + " " + url)
    val image : GridFSDBFile = myFS.findOne( if(thumbnail) url + thumbnailSuffix else url )
    if (image != null) {
      image.put("lastViewed", new Date)
      val viewed  = image.get("viewed")
      if (viewed != null) image.put("viewed", viewed.asInstanceOf[Int] + 1) else image.put("viewed", 1)
      image.save()
    }
    image
  }

  private[image] def sanitizeUrl(url: String) : String = {
    val sanitizeUrl : String = url.replaceAll("""\\""", "%5C").replaceAll("\\[", "%5B").replaceAll("\\]", "%5D")
    sanitizeUrl
  }

  def retrieveImageFromCache(url: String, sizeString:String, response : HttpServletResponse) : HttpServletResponse = {
    // catch try block to harden the application and always give back a 404 for the application
    try {
      require(url != null)
      require(url != "noImageFound")
      require(!url.isEmpty)
      findOrInsert(sanitizeUrl(url), isThumbnail(Option(sizeString)), response)
    }
    catch {
      case ia : IllegalArgumentException =>
        log.error("problem with processing this url: \"" + url + "\"")
//        log.error(ia.getStackTraceString)//move to debug later
        respondWithNotFound(url, response)
      case ex: Exception =>
        log.error("unable to find image: \"" + url + "\"\n" + ex.getStackTraceString)
        respondWithNotFound(url, response)
    }
  }

  def findOrInsert(url: String, thumbnail: Boolean, response: HttpServletResponse) : HttpServletResponse = {
    val image : GridFSDBFile = findImageInCache(url, thumbnail)
    if (image == null) {
      log.info ("cache miss for an image, going to retrieve it from " + url)
      val item = storeImage(url)
      if (item.available) {
        val storedImage = findImageInCache(url, thumbnail)
        addImageToResponseStream(storedImage, response)
        setImageCacheControlHeaders(storedImage, response)
        log.info ("image succesfully cached from " + url)
        response
      } else {
        log.info ("unable to store image at " + url + ": image not found or of wrong mime type")
        respondWithNotFound(url, response)
      }
    }
    else {
      addImageToResponseStream(image, response)
      setImageCacheControlHeaders(image, response)
    }
  }

  // storeImage
  def storeImage(url: String) : CachedItem = {
    val image = retrieveImageFromUrl(url)
    if (image.storable) {
      val inputFile = myFS.createFile(image.dataAsStream, image.url)
      inputFile setContentType(image.contentType)
      inputFile put ("viewed", 0)
      inputFile put ("lastViewed", new Date)
      inputFile.save()

      // also create a thumbnail on the fly
      // for this, fetch the stream again
      val os: ByteArrayOutputStream = new ByteArrayOutputStream();
      try {
        val thumbnail: BufferedImage = resizeImage(retrieveImageFromUrl(url).dataAsStream, thumbnailWidth)
        ImageIO.write(thumbnail, "jpg", os);
        val is: InputStream = new ByteArrayInputStream(os.toByteArray);
        val thumbnailFile = myFS.createFile(is, image.url + thumbnailSuffix)
        thumbnailFile setContentType(image.contentType)
        thumbnailFile put ("viewed", 0)
        thumbnailFile put ("lastViewed", new Date)
        thumbnailFile.save()
      } catch {
        case t => {
          log.info("Could not create a thumbnail for image at %s. Error message is: '%s'".format(url, t.getMessage), t)
        }
      } finally {
        os.close()
      }

      CachedItem(true, inputFile)
    }
    else {
      CachedItem(false, null)
    }
  }

  // retrieveImageFromUrl
  def retrieveImageFromUrl(url: String) : WebResource = {
    val httpClient = new HttpClient(multiThreadedHttpConnectionManager)
    val method = new GetMethod(url)
    httpClient executeMethod (method)
    method.getResponseHeaders.foreach(header => log debug (header) )
//    println(method.getStatusText)
    val storable = isStorable(method)
    WebResource(url, method.getResponseBodyAsStream, storable._1, storable._2)
  }

  def isStorable(method: GetMethod) = {
    val contentType : Header = method.getResponseHeader("Content-Type")
    val contentLength : Header = method.getResponseHeader("Content-Length")
    val mimeTypes = List("image/png", "image/jpeg", "image/jpg", "image/gif", "image/tiff", "image/pjpeg")
    //todo build a size check in later
    (mimeTypes.contains(contentType.getValue.toLowerCase.split(",").head), contentType.getValue)
  }

  private def respondWithNotFound(url: String, response: HttpServletResponse) : HttpServletResponse = {
    response.setStatus(404)
    response.setContentType("text/xml")
    response.setCharacterEncoding("UTF-8")
    response.getWriter.write(
   format("""<?xml encoding="utf-8"?>
    <error>
      <message>Unable to retrieve your image (%s) through the CacheProxy</message>
    </error> """, url))
    response.getWriter.close()
    response
  }

  private def addImageToResponseStream(image : GridFSDBFile, response : HttpServletResponse) : HttpServletResponse = {
    val in : InputStream = image.getInputStream
    val out : OutputStream = response.getOutputStream
    try {
      IOUtils.copy(in, out)
    } finally {
      in.close()
      out.close()
    }
    response
  }

  private def setImageCacheControlHeaders(image : GridFSDBFile, response : HttpServletResponse) : HttpServletResponse = {
    val now = System.currentTimeMillis();
    response.setContentType(image.getContentType)
    response.addHeader("Cache-Control", "max-age=" + cacheDuration)
    response.addHeader("Cache-Control", "must-revalidate")
    response.setDateHeader("Last-Modified", now)
    response.setDateHeader("Expires", now + cacheDuration * 1000)
    response
  }

  private def isThumbnail(thumbnail: Option[String]):Boolean = {
    thumbnail.getOrElse(return false) == thumbnailSizeString
  }

  private def resizeImage(imageStream: InputStream, width: Int): BufferedImage = {
    val bufferedImage:BufferedImage = ImageIO.read(imageStream)
    Scalr.resize(bufferedImage, Scalr.Mode.FIT_TO_WIDTH, width)
  }

}

case class WebResource(url: String, dataAsStream: InputStream, storable: Boolean, contentType : String)

case class CachedItem(available: Boolean, item : GridFSInputFile)