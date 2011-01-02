package eu.delving.services.cache.image

import java.io. {OutputStream, InputStream}
import javax.servlet.http.HttpServletResponse
import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.methods.GetMethod
import com.mongodb.gridfs. {GridFSInputFile, GridFSDBFile, GridFS}
import org.apache.commons.io.IOUtils
import org.apache.commons.httpclient. {Header, HttpClient, MultiThreadedHttpConnectionManager}
import com.mongodb. {MongoOptions, Mongo}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/2/11 10:09 PM  
 */

object ImageCacheService {


  // get the mongoStuff
  val mongoOptions = new MongoOptions()
  mongoOptions.connectionsPerHost = 100
  val mongo = new Mongo("localhost", mongoOptions)
  val imageCache = mongo.getDB("imageCache")
  val myFS = new GridFS(imageCache)

  // get the connectionParams
  val connectionParams = new HttpConnectionManagerParams
  connectionParams setDefaultMaxConnectionsPerHost (10)
  connectionParams setMaxTotalConnections (200)
  val multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager()
  multiThreadedHttpConnectionManager setParams (connectionParams)

  // settings
  val cacheDuration = 60 * 60 * 24

  // findImage
  def findImage(url: String) = {
    println ("attempting to retrieve: " + url)
    myFS.findOne(url)
  }

  def retrieveImageFromCache(url: String, response : HttpServletResponse) : HttpServletResponse = {
    var image = findImage(url)
    if (image == null) {
      val item = storeImage(url)
      if (item.found){
        image = findImage(url)
      }
    }
    val in: InputStream = image.getInputStream
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
    val storable = isStorable(method)
    WebResource(url, method.getResponseBodyAsStream, storable._1, storable._2)
  }

  def isStorable(method: GetMethod) = {
    val contentType : Header = method.getResponseHeader("Content-Type")
    val contentLength : Header = method.getResponseHeader("Content-Length")
    val mimeTypes = List("image/png", "image/jpeg")
    //todo build a size check in later
    (mimeTypes.contains(contentType.getValue.toLowerCase), contentType.getValue)
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

case class CachedItem(found: Boolean, item : GridFSInputFile)