package eu.delving.services.cache.image

import java.io. {OutputStream, InputStream}
import javax.servlet.http.HttpServletResponse
import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.methods.GetMethod
import com.mongodb.gridfs. {GridFSInputFile, GridFSDBFile, GridFS}
import org.apache.commons.io.IOUtils
import org.apache.commons.httpclient. {Header, HttpClient, MultiThreadedHttpConnectionManager}
import com.mongodb. {MongoOptions, Mongo}
import annotation.tailrec
import org.apache.log4j.Logger

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/2/11 10:09 PM  
 */

object ImageCacheService {


  // Mongo Settings
  val mongoOptions = new MongoOptions()
  mongoOptions.connectionsPerHost = 100
  val mongo = new Mongo("localhost", mongoOptions)
  val imageCache = mongo.getDB("imageCache")
  val myFS = new GridFS(imageCache)

  // HttpClient Settings
  val connectionParams = new HttpConnectionManagerParams
  connectionParams setDefaultMaxConnectionsPerHost (10)
  connectionParams setMaxTotalConnections (200)
  val multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager()
  multiThreadedHttpConnectionManager setParams (connectionParams)

  // General Settings
  val cacheDuration = 60 * 60 * 24
  private val log : Logger = Logger.getLogger("ImageCacheService")

  // findImageInCache
  def findImageInCache(url: String) : GridFSDBFile = {
    log info ("attempting to retrieve: " + url)
    myFS.findOne(url)
  }

  private[image] def sanitizeUrl(url: String) : String = {
    val sanitizeUrl : String = url.replaceAll("""\\""", "%5C")
    sanitizeUrl
  }

  def retrieveImageFromCache(url: String, response : HttpServletResponse) : HttpServletResponse = {
    // catch try block to harden the application and always give back a 404 for the application
    require(url != null)
    require(url != "noImageFound")
    try {
      findOrInsert(sanitizeUrl(url), response)
    }
    catch {
      case ia : IllegalArgumentException =>
        log.error(ia.getStackTraceString)    //move to debug later
        respondWithNotFound(url, response)
      case ex: Exception =>
        log.error("unable to find image: " + url + "\n" + ex.getStackTraceString)
        respondWithNotFound(url, response)
    }
  }

  def findOrInsert(url: String, response: HttpServletResponse) : HttpServletResponse = {
    val image : GridFSDBFile = findImageInCache(url)
    if (image == null) {
      log info ("image not found attempting to store in cache " + url)
      val item = storeImage(url)
      if (item.available) {
        val storedImage = findImageInCache(url)
        addImageToResponseStream(storedImage, response)
        setImageCacheControlHeaders(storedImage, response)
      }
      else {
        log info ("unable to store " + url)
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
    method.getResponseHeaders.foreach(header => println(header) )
    println(method.getStatusText)
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
    response.getWriter.write(format("""
    <?xml encoding="utf-8"?>;
    <error>
      <message>Unable to retrieve your image (%s) through the CacheProxy</message>
    </error>
    """, url))
    response.getWriter.close
    response
  }

  private def addImageToResponseStream(image : GridFSDBFile, response : HttpServletResponse) : HttpServletResponse = {
    val in : InputStream = image.getInputStream
    val out : OutputStream = response.getOutputStream
    try {
      IOUtils.copy(in, out)
    }
    finally {
      in.close()
      out.close()
    }
    setImageCacheControlHeaders(image, response)
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
}

case class WebResource(url: String, dataAsStream: InputStream, storable: Boolean, contentType : String)

case class CachedItem(available: Boolean, item : GridFSInputFile)