package eu.delving.loganalyser

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import java.io.{FileInputStream, InputStream, File}
import java.util.zip.GZIPInputStream
import scala.io.Source
import com.mongodb._
import scala.collection.JavaConversions._

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Aug 6, 2010 10:14:52 PM
 */

class LogLoaderSpec extends Spec with ShouldMatchers {
  describe("A logloader") {
    val compressedTestLogs = new File("loganalyser/src/test/resources/test_log_files/compressed")
    val uncompressedTestLogs = new File("loganalyser/src/test/resources/test_log_files/uncompressed")
    val coll = LogLoader.getMongoCollection("testLogEntries", "testCollection")

    val loader = new LogLoader(coll)

    describe("(when given a directory)") {

      it("should recurse into all of them") {
        var filesProcessed = 0
        loader.processDirectory(compressedTestLogs) {
          fileName =>
            if (fileName.contains("ClickStreamLogger.log")) filesProcessed += 1
        }
        filesProcessed should equal(4)
      }
    }

    describe("(when given a gzipped file)") {

      it("should process a gzipped file the same as a unzipped file") {
        def logProcessor(file: String) = loader.processLogFile(new File(file)) {line =>}
        val compressedLog = logProcessor("loganalyser/src/test/resources/test_log_files/compressed/portal4/portal4_ClickStreamLogger.log.gz")
        val unCompressedLog = logProcessor("loganalyser/src/test/resources/test_log_files/uncompressed/portal4/portal4_ClickStreamLogger.log.2010-08-01.txt")
        compressedLog should equal(unCompressedLog)
      }
    }

    describe("(when receiving a log entry string)") {
      val logEntry = "09:36:11:101 [action=FULL_RESULT, europeana_uri=http://www.europeana.eu/resolve/record/03905/B085BB6BEA78222D1C06F6B832D03C8046D19AE9, query=, start=, numFound=, userId=, lang=EN, req=http://www.europeana.eu:80/portal/record/03905/B085BB6BEA78222D1C06F6B832D03C8046D19AE9.html, date=2010-08-02T09:36:11.101+02:00, ip=93.158.150.20, user-agent=Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots), referer=null, utma=, utmb=, utmc=, v=1.0]"
      val briefLogEntry = """00:00:12:981 [action=BRIEF_RESULT, view=brief-doc-window, query=Rugby, queryType=advanced, queryConstraints="YEAR:"1990",YEAR:"1981",YEAR:"1955"", page=1, numFound=6, langFacet=fr (3),nl (3), countryFacet=france (3),netherlands (3), userId=, lang=EN, req=http://europeana.eu:80/portal/brief-doc.html?query=Rugby&qf=YEAR:1990&qf=YEAR:1981&qf=YEAR:1955&view=table, date=2010-08-01T00:00:12.981+02:00, ip=66.249.66.101, user-agent=Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html), referer=null, utma=, utmb=, utmc=, v=1.0]"""
      val unicodeLogEntry = """14:15:51:654 [action=BRIEF_RESULT_FROM_PACTA, view=brief-doc-window, query=Zoran MuÅ¡iÄ, queryType=advanced, queryConstraints="", page=1, numFound=0, langFacet=null, countryFacet=null, userId=, lang=EN, req=http://www.europeana.eu:80/portal/brief-doc.html?query=Zoran%20MuÃÂ¡iÃÂ%20&bt=pacta, date=2010-08-01T14:15:51.653+02:00, ip=207.46.13.52, user-agent=msnbot/2.0b (+http://search.msn.com/msnbot.htm), referer=null, utma=, utmb=, utmc=, v=1.0]"""

      it("should extract the log entry from a string") {
        val logEntryList = loader.processLogEntry(logEntry)
        logEntryList.head should equal("action", "FULL_RESULT")
        logEntryList.last should equal("v", "1.0")
        logEntryList.length should equal("""=""".r.findAllIn(logEntry).length)
      }

      it("should process BRIEF_RESULT actions differently") {
        val logEntryList = loader.processLogEntry(briefLogEntry)
        logEntryList.head should equal("action", "BRIEF_RESULT")
        logEntryList.last should equal("view", "brief-doc-window")
        logEntryList.length should equal(""", """.r.findAllIn(briefLogEntry).length + 1)
        logEntryList.filter(entries => entries._1.equalsIgnoreCase("langFacet")).head._2 should equal("fr (3);nl (3)")
        logEntryList.filter(entries => entries._1.equalsIgnoreCase("queryConstraints")).head._2 should equal(""" "YEAR:"1990";YEAR:"1981";YEAR:"1955"" """.trim)
      }

      it("should deal with bodged unicode correctly") {
        val logEntryList = loader.processLogEntry(briefLogEntry)
        logEntryList.head should equal("action", "BRIEF_RESULT")
        logEntryList.last should equal("view", "brief-doc-window")
        logEntryList.length should equal(""", """.r.findAllIn(briefLogEntry).length + 1)
      }
    }

    describe("(when inserting entries in MongoDB)") {

      it("should insert new entries") {
        LogLoader.processLogFiles(new File("loganalyser/src/test/resources/test_log_files/compressed/portal4"))
      }

      it("should ")(pending)

    }
  }
}

private[loganalyser] class LogLoader(coll: DBCollection) {
  def processDirectory(base: File)(codeBlock: String => Unit): Unit =
    base.listFiles.foreach {f => if (!f.isDirectory) (codeBlock(f.toString)) else processDirectory(f)(codeBlock)}

  def processLogFile(logFile: File)(codeBlock: List[(String, String)] => Unit): Int = {
    def processLines(lines: Iterator[String]): Int = {
      val logEntries = lines.toList
      logEntries foreach (line =>
        try {codeBlock(processLogEntry(line))}
        catch {
          case ia: IllegalArgumentException => println("unable to process line: " + line)
          case e: Exception => println("don't know what happened = " + line + "\n" + e)
        })
      logEntries length
    }

    val lines = Source.fromInputStream(createInputStream(logFile), "utf-8").getLines
    val linesProcessed = processLines(lines)
    println(format("Processed file %s with %d entries.", logFile, linesProcessed))
    linesProcessed
  }


  def storeEntryList(entryList: List[(String, String)]): Unit = {
    require(!entryList.isEmpty)

    def updateOrInsertRecord(query: BasicDBObject): Unit = {
      val dbObject = coll.findOne(query)
      if (dbObject != null) {
        entryList.foreach(entry => dbObject.put(entry._1, entry._2))
        coll.save(dbObject)
      } else {
        val doc = new BasicDBObject()
        entryList.foreach(entry => doc.put(entry._1, entry._2))
        coll.insert(doc)
      }
    }

    def getField(fieldName: String): String = entryList.filter(_._1.equalsIgnoreCase(fieldName)).head._2

    val sessionPageId = getField("utmb")
    if (!sessionPageId.equalsIgnoreCase("null")) updateOrInsertRecord(new BasicDBObject("utmb", sessionPageId))
    else {
      val query = new BasicDBObject
      query.put("date", getField("date"))
      query.put("ip", getField("ip"))
      updateOrInsertRecord(query) // this need to become more exact, double check against Ip
    }
  }

  def processLogEntry(logEntry: String): List[(String, String)] = {
    val ActionExtractor = """[0-9 :]{0,20}\[action=([^,]+),\s+(.+)\]""".r
    logEntry match {
      case ActionExtractor(action, entries) =>
        List(("action", action)) ++ createEntryMap(entries)
      case _ => println(logEntry); List()
    }
  }

  def createEntryMap(entries: String): List[(String, String)] = {
    val EntryExtractor = """([a-zA-Z_]+)=([^,]+)""".r
    val EmptyEntryExtractor = """([a-zA-Z_]+)=,""".r

    val withoutAmbiguousCommas = entries.replaceAll(",([^ ])", ";$1")
    val contentEntries = for (EntryExtractor(key, value) <- EntryExtractor findAllIn withoutAmbiguousCommas) yield (key, value)
    val emptyEntries = for (EmptyEntryExtractor(key) <- EmptyEntryExtractor findAllIn withoutAmbiguousCommas) yield (key, "null")
    (contentEntries.toList ++ emptyEntries.toList).sortBy(f => f._1)
  }

  private def createInputStream(logFile: File): InputStream = {
    if (logFile.toString.endsWith(".gz")) {
      new GZIPInputStream(new FileInputStream(logFile))
    }
    else {
      new FileInputStream(logFile)
    }
  }
}

object LogLoader {
  private[loganalyser] val mongoInstance: Mongo = new Mongo()

  private[loganalyser] val loader = new LogLoader(getMongoCollection("logEntries", "clickStreamLogs"))

  def processLogFiles(baseDir: File): Unit = {
    loader.processDirectory(baseDir) {
      fileName =>
        if (fileName.contains("ClickStreamLogger.log")) loader.processLogFile(new File(fileName)) {entries => loader.storeEntryList(entries)}
    }
  }

  private[loganalyser] def getMongoCollection(dbName: String, collName: String): DBCollection = {
    val db: DB = mongoInstance.getDB(dbName)
    val coll: DBCollection = db.getCollection(collName)
    val logEntryCollection = if (coll == null)
      db.createCollection(collName, null)
    else
      coll
    // add / check for indices
    val indices = Map("utmb" -> 1, "date" -> 1, "ip" -> 1)
    val collIndices = for (index <- logEntryCollection.getIndexInfo) yield index.get("name").toString.replaceAll("_[-1]{1,2}", "")
    collIndices diff indices.keys.toList foreach (collIndex => logEntryCollection.createIndex(new BasicDBObject(collIndex, 1)))
    logEntryCollection
  }

}