package eu.delving.logging.loganalyser

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import java.io.{FileInputStream, InputStream, File}
import java.util.zip.GZIPInputStream
import scala.io.Source
import scala.collection.JavaConversions._
import collection.mutable.{HashMap, ListBuffer}
import com.mongodb._

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Aug 6, 2010 10:14:52 PM
 */

class LogLoaderSpec extends Spec with ShouldMatchers {
  describe("A logloader") {
    val compressedTestLogs = new File("core/src/test/resources/test_log_files/compressed")
    val uncompressedTestLogs = new File("core/src/test/resources/test_log_files/uncompressed")
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
        val compressedLog = logProcessor("core/src/test/resources/test_log_files/compressed/portal4/portal4_ClickStreamLogger.log.gz")
        val unCompressedLog = logProcessor("core/src/test/resources/test_log_files/uncompressed/portal4/portal4_ClickStreamLogger.log.2010-08-01.txt")
        compressedLog should equal(unCompressedLog)
      }
    }

    describe("(when receiving a log entry string)") {
      val logEntry = "09:36:11:101 [action=FULL_RESULT, europeana_uri=03905/B085BB6BEA78222D1C06F6B832D03C8046D19AE9, query=, start=, numFound=, userId=, lang=EN, req=http://www.europeana.eu:80/portal/record/03905/B085BB6BEA78222D1C06F6B832D03C8046D19AE9.html, date=2010-08-02T09:36:11.101+02:00, ip=93.158.150.20, user-agent=Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots), referer=null, utma=, utmb=, utmc=, v=1.0]"
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

      it("should get the ip from logEnty and return the country 2 letter code") {
        val logEntryList = loader.processLogEntry(briefLogEntry)
        loader.createExpansions(logEntryList, List(ExpansionPlugin("country_ip", true))) should equal(List(("country_ip", "XX")))
        loader.createExpansions(logEntryList, List(ExpansionPlugin("country_ip", false))).isEmpty should equal(true)
      }

      it("should split date and time from the date string") {
        val logEntryList = loader.processLogEntry(briefLogEntry)
        val doc = new BasicDBObject;
        doc.put("d", "2010-08-01");
        doc.put("t", "00:00:12") // { "d" : "2010-08-01" , "t" : "00:00:12"}
        loader.createExpansions(logEntryList, List(ExpansionPlugin("invoked_at", true))) should equal(List(("invoked_at", doc)))
      }
    }

    describe("(when inserting entries in MongoDB)") {

      it("should insert new entries") {
        LogLoader.processLogFiles(new File("core/src/test/resources/test_log_files/uncompressed/"))
      }
    }

    describe("(when creating a Session based collection)") {

      it("should get all distinct session and iterate over it") {
        LogLoader.createSessionBasedCollection("logEntries", "noBots")
      }
    }
  }

  describe("A IpToCountryConvertor") {

    describe("(when given a ip adres)") {

      it("should give back 'Unknown' when the ip adres cannot be resolved") {
        IpToCountryConvertor.findCountryIpRecord("194.171.184.14").countryCode2 should equal("XX")
      }

      it("should give back the country") {
        IpToCountryConvertor.findCountryIpRecord("184.883.31.21").countryCode2 should equal("NL")
        IpToCountryConvertor.findCountryIpRecord("28.96.69.11").countryCode2 should equal("CN")
      }
    }
  }

  describe("A LogLoader") {

    describe("(when given an array of time entries)") {

      it("should get the first and last one and determine the duration") {
        val timeEntries = List("10:10:10", "10:10:20", "10:11:05", "10:12:30")
        LogLoader.calculateDuration(timeEntries).inMinutes should equal(2)
      }

      it("should correctly deal with cross hour boundaries") {
        val timeEntries = List("10:10:10", "10:10:20", "10:11:05", "11:12:30")
        LogLoader.calculateDuration(timeEntries).inMinutes should equal(62)
      }

      it("should correctly deal with with cross day boundaries") {
        val timeEntries = List("22:10:10", "23:10:20", "23:11:05", "00:12:30")
        val duration = LogLoader.calculateDuration(timeEntries)
        duration.inMinutes should equal(122)
        duration.start should equal("22:10:10")
        duration.end should equal("00:12:30")
      }
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

    val expansions = List(ExpansionPlugin("country_ip", true), ExpansionPlugin("locale", false), ExpansionPlugin("invoked_at", true))

    val entriesWithExpansions: List[(String, Any)] = (entryList ++ createExpansions(entryList, expansions)).sortBy(f => f._1)

    def updateOrInsertRecord(query: BasicDBObject): Unit = {
      val dbObject = coll.findOne(query)
      if (dbObject != null) {
        // remove expansionFields
        expansions.foreach(ex => dbObject.removeField(ex.fieldname))
        entriesWithExpansions.foreach(entry => dbObject.put(entry._1, entry._2))
        coll.save(dbObject)
      } else {
        val doc = new BasicDBObject()
        entriesWithExpansions.foreach(entry => doc.put(entry._1, entry._2))
        coll.insert(doc)
      }
    }

    def getField(fieldName: String): String = entryList.filter(_._1.equalsIgnoreCase(fieldName)).head._2.toString

    val sessionPageId = getField("utmb")
    if (!sessionPageId.equalsIgnoreCase("null")) updateOrInsertRecord(new BasicDBObject("utmb", sessionPageId))
    else {
      val query = new BasicDBObject
      query.put("date", getField("date"))
      query.put("ip", getField("ip"))
      updateOrInsertRecord(query)
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

  def createExpansions(entries: List[(String, String)], expandFields: List[ExpansionPlugin]): List[(String, Any)] = {
    // add recursive def for expansion list
    val entryMap = new HashMap[String, String]
    entries.foreach(entry => entryMap.put(entry._1, entry._2))
    val expansions = new ListBuffer[(String, Any)]
    expandFields foreach {
      field =>
        field match {
          case ExpansionPlugin("country_ip", true) =>
            val country2 = IpToCountryConvertor.findCountryIpRecord(entryMap.get("ip").getOrElse("000000")).countryCode2
            expansions.add(("country_ip", country2))
          case ExpansionPlugin("extract_locale", true) =>
          case ExpansionPlugin("invoked_at", true) =>
            val DateTimeExtractor = """([0-9\-]+)T([^.]+).+""".r
            // replace with extractor
            val dateString = entryMap.get("date").get
            dateString match {
              case DateTimeExtractor(date, time) =>
                val doc = new BasicDBObject
                doc.put("d", date)
                doc.put("t", time)
                expansions.add(("invoked_at", doc))
              case _ =>
            }
          case _ =>
        }
    }
    expansions.toList
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
    val indices = Map("utma" -> 1, "utmb" -> 1, "date" -> 1, "ip" -> 1)
    val collIndices = for (index <- logEntryCollection.getIndexInfo) yield index.get("name").toString.replaceAll("_[-1]{1,2}", "")
    collIndices diff indices.keys.toList foreach (collIndex => logEntryCollection.createIndex(new BasicDBObject(collIndex, 1)))
    logEntryCollection
  }

  private def freq[T](seq: Seq[T]) = seq.groupBy(x => x).mapValues(_.length)

  private def fromMapToDBObject(itemList: List[String]): BasicDBObject = {
    val dbObject = new BasicDBObject
    freq(itemList).foreach(item => dbObject.put(item._1.replace(".", ";"), item._2))
    dbObject
  }

  def calculateDuration(timeList: List[String]) = {
    def toTimeHourEntry(time: String) = {
      val timeArr = time.split(":")
      TimeHourEntry(timeArr(0).toInt, timeArr(1).toInt, timeArr(2).toInt)
    }

    def toSeconds(timeEntry: TimeHourEntry) = (timeEntry.hours * 60 * 60) + (timeEntry.minutes * 60) + timeEntry.seconds

    val first = toTimeHourEntry(timeList.head)
    val last = toTimeHourEntry(timeList.last)
    val duration = if (first.hours > last.hours)
      ((toSeconds(last) + 24 * 60 * 60) - toSeconds(first)) / 60
    else
      (toSeconds(last) - toSeconds(first)) / 60
    SessionTime(duration, timeList.head, timeList.last)
  }

  def createSessionBasedCollection(dbName: String, collName: String): Unit = {
    def addCheckedField(field: String, entry: DBObject, list: ListBuffer[String]): Unit =
      if (entry.containsField(field)) list.add(entry.get(field).toString)

    def addSessionAndStatsSubdocuments(entryList: List[DBObject], sessionDocument: BasicDBObject): Unit = {
      // create ListBuffers for interesting sections
      val queries, countries, languages, ipAddresses, actions, dates, userIds, languageChange, times = new ListBuffer[String]

      // create an ordered list of the session entries and collect the lists with relevant keys
      val sessionEntries = new BasicDBObject
      entryList.foreach {
        entry =>
          sessionEntries.put(entry.get("date").toString.replace(".", ";"), entry)
          addCheckedField("action", entry, actions)
          addCheckedField("lang", entry, languages)
          addCheckedField("ip", entry, ipAddresses)
          addCheckedField("userId", entry, userIds)
          addCheckedField("country_ip", entry, countries)
          addCheckedField("query", entry, queries)
          if (entry.containsField("invoked_at")) {
            val invoked: DBObject = entry.get("invoked_at").asInstanceOf[DBObject]
            dates.add(invoked.get("d").toString)
            times.add(invoked.get("t").toString)
          }
          if (entry.containsField("oldLang")) languageChange.add(entry.get("oldLang").toString + "->" + entry.get("lang").toString)
      }


      // create the session stats section
      val sessionStats = new BasicDBObject()
      def addStats(key: String, value: Any): Unit = sessionStats.put(key, value)

      addStats("pageViews", entryList.size)
      addStats("actions", fromMapToDBObject(actions.toList))
      addStats("actionOrder", actions.toString)
      val duration = calculateDuration(times.toList)
      addStats("duration", duration.inMinutes)
      addStats("start_session", duration.start)
      addStats("end_session", duration.end)


      val queryObject = fromMapToDBObject(queries.toList)
      addStats("queries", queryObject)
      addStats("uniqueQueriesNr", queryObject.size)

      val langObject = fromMapToDBObject(languages.toList)
      addStats("languages", langObject)
      addStats("uniqueLanguagesNr", langObject.size)
      addStats("hasLanguageChange", !languageChange.isEmpty)
      addStats("languageChangePairs", fromMapToDBObject(languageChange.toList))
      addStats("userTriggeredlanguageChange", actions.distinct.contains("LANGUAGE_CHANGE"))

      addStats("ip", ipAddresses.distinct.toArray)
      addStats("uniqueIpNr", ipAddresses.distinct.size)

      addStats("countries", countries.distinct.toArray)
      addStats("uniqueCountriesNr", countries.distinct.size)

      addStats("dates", dates.distinct.toArray)
      addStats("uniqueDatesNr", dates.distinct.size)

      addStats("userHasLoggedIn", userIds.forall(id => !id.equalsIgnoreCase("null")))

      // write the subsections to the main session document
      sessionDocument.put("sessionStats", sessionStats)
      sessionDocument.put("logEntries", sessionEntries)
    }

    val db: DB = mongoInstance.getDB(dbName)
    val sourceColl: DBCollection = db.getCollection(collName)
    val sessionCollection = "sessionDocs"
    val sessionIdBasedCollection: DBCollection = if (db.collectionExists(sessionCollection)) {
      db.getCollection(sessionCollection).drop
      db.createCollection(sessionCollection, null)
    }
    else
      db.createCollection(sessionCollection, null)
    if (sourceColl == null) throw new RuntimeException("collections with logEntries must exist.")
    val query = new BasicDBObject
    query.put("utma", new BasicDBObject("$ne", "null"))
    val output: MapReduceOutput = sourceColl.mapReduce(
      "function() { emit(this.utma, 1); }",
      """
      function(k,vals) {
            var sum=0;
            for(var i in vals) sum += vals[i];
            return sum;
      }
      """,
      "listOfIds",
      query
      )
    val result: DBCollection  = output.getOutputCollection
    println ("ids found" + result.count)
    val sessionsCursor: DBCursor = result.find()
    val sessions: List[DBObject] = sessionsCursor.iterator.toList
    println("unique sessions: " + sessions.size)
    sessions.foreach {
      session =>
        {
          val sessionString = session.get("_id")
          println("sessionId: " + session)
          val cursor: DBCursor = sourceColl.find(new BasicDBObject("utma", sessionString)).sort(new BasicDBObject("date", 1))
          val sessionCount = cursor.count
          println("entries for this sessionId: " + sessionCount)
          if (sessionCount > 0 && sessionCount < 5000) {
          val sessionDocument = new BasicDBObject()
          sessionDocument.put("sessionId", sessionString)

          addSessionAndStatsSubdocuments(cursor.iterator.toList, sessionDocument)

          // save the session document
          sessionIdBasedCollection.save(sessionDocument)
          }
          else println ("unable to process entries for sessionId: " + sessionString)
        }
    }
  }

  def main(args: Array[String]) {
    require(!args.isEmpty)
    if (args.contains("-createSession")) LogLoader.createSessionBasedCollection("logEntries", "noBots")
    else args.foreach(arg => processLogFiles(new File(arg)))
  }

}

object IpToCountryConvertor {
  val IpCountryExtractor = """^"(.+?)","(.+?)","(.+?)","(.+?)","(.+?)"$""".r
  val countryList = initCountryIpList

  private def initCountryIpList: List[CountryIpRecord] = {

    val countryIpRecords = Source.fromInputStream(
      new GZIPInputStream(
        new FileInputStream(
          new File("core/src/test/resources/ip2country/ip-to-country.csv.gz")
          )
        )
      , "utf-8")
    val countryIMList = new ListBuffer[CountryIpRecord]
    countryIpRecords.getLines.toList.foreach(line =>
      line match {
        case IpCountryExtractor(ipFrom, ipTo, countryCode2, countryCode3, countryName) =>
          countryIMList.add(CountryIpRecord(ipFrom.toLong, ipTo.toLong, countryCode2, countryCode3, countryName))
        case _ => println("unable to read line: " + line)
      }
      )
    countryIMList.toList
  }

  def findCountryIpRecord(ip: String) = {
    require(ip.matches("[0-9.]+"))
    val cleanIp = ip.replaceAll("[^0-9]", "").toLong
    val results = countryList.filter(entry =>
      entry.ipFrom <= cleanIp && entry.ipTo >= cleanIp)
    if (!results.isEmpty) results.head else CountryIpRecord(000000, 000000, "XX", "XXX", "Unknown")
  }
}

case class CountryIpRecord(ipFrom: Long, ipTo: Long, countryCode2: String, countryCode3: String, countryName: String)
case class ExpansionPlugin(fieldname: String, enabled: Boolean)
case class TimeHourEntry(hours: Int, minutes: Int, seconds: Int) {
  override def toString() = format("%d:%d:%d", hours, minutes, seconds)
}
case class SessionTime(inMinutes: Int, start: String, end: String)