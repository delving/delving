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

import java.io. {OutputStream, InputStream}
import javax.servlet.http.HttpServletResponse
import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.methods.GetMethod
import com.mongodb.gridfs. {GridFSInputFile, GridFSDBFile, GridFS}
import org.apache.commons.io.IOUtils
import org.apache.commons.httpclient. {Header, HttpClient, MultiThreadedHttpConnectionManager}
import annotation.tailrec
import org.apache.log4j.Logger
import com.mongodb. {DBObject, MongoOptions, Mongo}
import java.util.Date
import eu.delving.core.util.MongoFactory
import org.springframework.beans.factory.annotation.Autowired
import java.awt.image.BufferedImage
import com.thebuzzmedia.imgscalr.Scalr
import javax.management.remote.rmi._RMIConnection_Stub
import sun.awt.SunHints.Value
import javax.imageio.ImageIO

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/2/11 10:09 PM  
 */

class ImageCacheService(mongoFactory : MongoFactory) {

  val imageCache = mongoFactory.getMongo.getDB("imageCache")
  val myFS = new GridFS(imageCache)

  // HttpClient Settings
  val connectionParams = new HttpConnectionManagerParams
  connectionParams setDefaultMaxConnectionsPerHost (15)
  connectionParams setMaxTotalConnections (250)
  connectionParams setConnectionTimeout (2000)
  val multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager()
  multiThreadedHttpConnectionManager setParams (connectionParams)

  // General Settings
  val cacheDuration = 60 * 60 * 24
  private val log : Logger = Logger.getLogger("ImageCacheService")

  // findImageInCache
  def findImageInCache(url: String) : GridFSDBFile = {
    log info ("attempting to retrieve: " + url)
    val image : GridFSDBFile = myFS.findOne(url)
    if (image != null) {
      image.put("lastViewed", new Date)
      val viewed  = image.get("viewed")
      if (viewed != null) image.put("viewed", viewed.asInstanceOf[Int] + 1) else image.put("viewed", 1)
      image.save
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
      findOrInsert(sanitizeUrl(url), parseSize(Option(sizeString)), response)
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

  def findOrInsert(url: String, targetSize:Option[(Int, Int)], response: HttpServletResponse) : HttpServletResponse = {
    val image : GridFSDBFile = findImageInCache(url)
    if (image == null) {
      log info ("image not found attempting to store in cache " + url)
      val item = storeImage(url)
      if (item.available) {
        val storedImage = findImageInCache(url)
        addImageToResponseStream(storedImage, targetSize, response)
        setImageCacheControlHeaders(storedImage, response)
      }
      else {
        log info ("unable to store " + url)
        respondWithNotFound(url, response)
      }
    }
    else {
      addImageToResponseStream(image, targetSize, response)
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
      inputFile.save
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
    (mimeTypes.contains(contentType.getValue.toLowerCase), contentType.getValue)
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
    response.getWriter.close
    response
  }

  private def addImageToResponseStream(image : GridFSDBFile, targetSize: Option[(Int, Int)], response : HttpServletResponse) : HttpServletResponse = {
    val out : OutputStream = response.getOutputStream
    if (targetSize.isDefined) {
      val resizedImage:BufferedImage = resizeImage(image.getInputStream, targetSize.get._1, targetSize.get._2)
      writeImage(None, out, {
        outputStream => ImageIO.write(resizedImage, "jpeg", outputStream)
      })
    } else {
      val in : InputStream = image.getInputStream
      writeImage(Some(in), out, {
        outputStream => IOUtils.copy(in, outputStream)
      })
    }
    response
  }

  private def writeImage(in:Option[InputStream], out:OutputStream, callback:OutputStream => Any) {
    try {
      callback(out)
    } finally {
      in.map { stream => stream.close()}
      out.close()
    }
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

  // width x height
  val SizeMatch = """^(\d{0,9}+)x(\d{0,9}+)""".r

  private def parseSize(size:Option[String]): Option[(Int, Int)] = {
    size.getOrElse(None) match {
      case SizeMatch(width, height) => Some((Integer.parseInt(width), Integer.parseInt(height)))
      case _ => None
    }
  }

  private def resizeImage(imageStream: InputStream, width: Int, height: Int): BufferedImage = {
    val bufferedImage:BufferedImage = ImageIO.read(imageStream)
    Scalr.resize(bufferedImage, Scalr.Mode.AUTOMATIC, width, height)
  }

}

case class WebResource(url: String, dataAsStream: InputStream, storable: Boolean, contentType : String)

case class CachedItem(available: Boolean, item : GridFSInputFile)