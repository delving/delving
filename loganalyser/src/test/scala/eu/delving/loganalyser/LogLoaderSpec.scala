package eu.delving.loganalyser

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import java.io.{FileInputStream, InputStream, File}
import java.util.zip.GZIPInputStream
import io.Source

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Aug 6, 2010 10:14:52 PM
 */

class LogLoaderSpec extends Spec with ShouldMatchers {
  describe("A logloader") {
    val cslLogs = new File("/Volumes/Data/Desktop/Sandbox/csl/")
    val loader = new LogLoader

    describe("(when given a directory)") {

      it("should recurse into all of them") (pending )
//                {
//        loader.processDirectory(cslLogs) {
//          fileName =>
//            if (fileName.contains("ClickStreamLogger.log")) loader.processLogFile(new File(fileName))
//        }
//      }

      it("should ")(pending)

    }

    describe("(when given a gzipped file)") {

      it("should process it in stream")(pending)

    }

    describe("(when receiving a log entry string)"){
          val logEntry = "09:36:11:101 [action=FULL_RESULT, europeana_uri=http://www.europeana.eu/resolve/record/03905/B085BB6BEA78222D1C06F6B832D03C8046D19AE9, query=, start=, numFound=, userId=, lang=EN, req=http://www.europeana.eu:80/portal/record/03905/B085BB6BEA78222D1C06F6B832D03C8046D19AE9.html, date=2010-08-02T09:36:11.101+02:00, ip=93.158.150.20, user-agent=Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots), referer=null, utma=, utmb=, utmc=, v=1.0]"
          val briefLogEntry = """00:00:12:981 [action=BRIEF_RESULT, view=brief-doc-window, query=Rugby, queryType=advanced, queryConstraints="YEAR:"1990",YEAR:"1981",YEAR:"1955"", page=1, numFound=6, langFacet=fr (3),nl (3), countryFacet=france (3),netherlands (3), userId=, lang=EN, req=http://europeana.eu:80/portal/brief-doc.html?query=Rugby&qf=YEAR:1990&qf=YEAR:1981&qf=YEAR:1955&view=table, date=2010-08-01T00:00:12.981+02:00, ip=66.249.66.101, user-agent=Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html), referer=null, utma=, utmb=, utmc=, v=1.0]"""

          it("should extract the log entry from a string") {
            loader.processLogEntry(logEntry).head should equal ("action", "FULL_RESULT")
            loader.processLogEntry(logEntry).last should equal ("v", "1.0")
            loader.processLogEntry(logEntry).length should equal (13)
          }

          it("should ") (pending)

        }

  }


}

class LogLoader {
  def processDirectory(base: File)(codeBlock: String => Unit): Unit =
    base.listFiles.foreach {f => if (!f.isDirectory) (codeBlock(f.toString)) else processDirectory(f)(codeBlock)}

  def processLogFile(logFile: File): Unit = {
    val lines = Source.fromInputStream(createInputStream(logFile), "utf-8").getLines
    val linesProcessed = processLines(lines)
    println ( format("Processed file %s with %d entries.", logFile, linesProcessed) )
  }

  def processLines(lines: Iterator[String]) : Int = {
    val logEntries = lines.toList
//    logEntries foreach (line => processLogEntry(line))
    logEntries length
  }

  def storeEntryList(entryList : List[(String, String)]) : Unit = {
    // todo write mongoDb Store method
  }

  def processLogEntry(logEntry: String) : List[(String, String)]  = {
    val ActionExtractor = """[0-9 :]{0,20}\[action=([^,]+),\s+(.+)\]""".r
    logEntry match {
      case ActionExtractor("BRIEF_RESULT", entries) =>
        processBriefResult(entries)
      case ActionExtractor(action, entries) =>
        List(("action", action)) ++ createEntryMap(entries)
      case _ => List()
    }
  }

  def createEntryMap(entries: String) : List[(String, String)] = {
    val EntryExtractor = """([a-zA-Z_]+)=([^,]+)""".r
    val iterator = for (EntryExtractor(key, value) <- EntryExtractor findAllIn entries) yield (key, value)
    iterator.toList
  }

  def processBriefResult(entries: String) : List[(String, String)] = {
    List(("action", "BRIEF_RESULT"))  ++ createEntryMap(entries)
  }

  private def createInputStream(logFile: File) : InputStream = {
    if (logFile.toString.endsWith(".gz")) {
      new GZIPInputStream(new FileInputStream(logFile))
    }
    else {
      new FileInputStream(logFile)
    }
  }
}