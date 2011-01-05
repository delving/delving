package eu.delving.services.indexing

import org.apache.log4j.Logger
import org.apache.solr.common.SolrInputDocument
import java.net.URL
import java.util.Date
import annotation.tailrec
import xml. {Node, NodeSeq, Elem, XML}
import org.apache.solr.client.solrj.impl. {StreamingUpdateSolrServer}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/3/11 9:00 PM  
 */

object HyperHarvindexer {

  val log : Logger = Logger.getLogger(this.getClass)
  val solrServer = new StreamingUpdateSolrServer ( "http://localhost:8983/solr/", 500,  10)

  def importFromPmh(setName : String, metadataPrefix : String, servicesUrl : String) = {

    val accessKeyString = "CW-E02A6689E8B183A5915A" //accessKey.createKey("HARVINDEXER")

    @tailrec def getRecords(baseUrl : String, resumptionToken : String = "" : String, pagesRetrieved : Int = 0,
                            recordsRetrieved : Int = 0) : (Int, Int) = {


      val requestUrl = format("%s?verb=ListRecords&accessKey=%s&metadataPrefix=%s&set=%s", baseUrl, accessKeyString, metadataPrefix, setName)
      val elem : Elem =
        if (resumptionToken.isEmpty)
          XML.load(new URL(requestUrl))
        else
          XML.load(new URL(format("%s?verb=ListRecords&accessKey=%s&resumptionToken=%s", baseUrl, accessKeyString, resumptionToken)))

      val token : NodeSeq = elem \\ "resumptionToken"
      val completeListSize = (elem \\ "@completeListSize").text
      val cursor = (elem \\ "@cursor").text
      val records = elem \\ "record" \\ "metadata" \\ "record"
      records.foreach(rec =>
        processRecord(rec, setName)
      )
      val totalRecordsRetrieved = records.size + recordsRetrieved
      if (totalRecordsRetrieved % 100 == 0)
        log.info(format("[%s of %s] => %s : (set %s with prefix %s ) : (%d records in %d requests on %s)",
          cursor, completeListSize, baseUrl, setName, metadataPrefix, totalRecordsRetrieved, pagesRetrieved, (new Date().toString)))
      if (token.text.isEmpty) // use for testing || pagesRetrieved >= 1
        (pagesRetrieved + 1, totalRecordsRetrieved)
      else
        getRecords(baseUrl, token.text, pagesRetrieved + 1, totalRecordsRetrieved)
    }
    solrServer.commit
    val indexRun : (Int, Int) = getRecords(servicesUrl)
    indexRun
  }

  def processRecord(record: NodeSeq, collectionName: String) : SolrInputDocument = {
    val doc : SolrInputDocument = new SolrInputDocument()
    record.foreach{recLevel =>
      recLevel.nonEmptyChildren.foreach{field =>
        if (field.label != "#PCDATA" && !field.text.isEmpty) {
          val prefix = if (!field.prefix.isEmpty) field.prefix + "_" else ""
//          println(prefix + field.label + " : " + field.text)
          doc.addField(prefix + field.label, field.text)
        }
      }
    }
    val hasDigitalObject = doc.containsKey("europeana_object")
    doc.addField("europeana_hasDigitalObject", hasDigitalObject)
//    solrServer.add(doc)
    doc
  }


//String accessKeyString = accessKey.createKey("HARVINDEXER");
//            String url = String.format(
//                    "%s/oai-pmh?verb=ListRecords&metadataPrefix=%s&set=%s&accessKey=%s",
//                    servicesUrl,
//                    metadataPrefix,
//                    collection.getName(),
//                    accessKeyString
//            );
//            HttpMethod method = new GetMethod(url);
//            httpClient.executeMethod(method);
//            Indexer indexer = new Indexer(collection);
//            InputStream inputStream = method.getResponseBodyAsStream();
//            String resumptionToken = importXmlInternal(inputStream, indexer);
//            while (!resumptionToken.isEmpty()) {
//                log.info(String.format("So far %d records, resumption token %s", indexer.getRecordCount(), resumptionToken));
//                method = new GetMethod(String.format(
//                        "%s/oai-pmh?verb=ListRecords&resumptionToken=%s&accessKey=%s",
//                        servicesUrl,
//                        resumptionToken,
//                        accessKeyString
//                ));
//                httpClient.executeMethod(method);
//                inputStream = method.getResponseBodyAsStream();
//                resumptionToken = importXmlInternal(inputStream, indexer);
//                MetaRepo.DataSet dataSet = metaRepo.getDataSet(collection.getName());
//                if (dataSet == null) {
//                    throw new RuntimeException("Data set not found!");
//                }
//                if (dataSet.getState() != DataSetState.INDEXING) {
//                    break;
//                }
//                if (indexer.isFull()) {
//                    log.info(String.format("Indexer full with %d records", indexer.getRecordCount()));
//                    executor.execute(indexer);
//                    indexer = new Indexer(collection);
//                }
//            }
//            if (indexer.hasRecords()) {
//                log.info(String.format("Harvest finished with %d records to index", indexer.getRecordCount()));
//                executor.execute(indexer);
//            }
//        }

}