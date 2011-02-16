package eu.europeana.core.querymodel.query

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.core.CoreContainer
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj. {SolrQuery, SolrServer}
import java.lang.System

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since May 10, 2010 9:22:37 AM
 */

trait SolrTester {

  val dir = System.getProperty("user.dir");
  println("CURRENT WORKING DIRECTORY IS " + dir)

  System.setProperty("solr.solr.home", dir + "/core/src/test/solr/single-core")
  System.setProperty("solr.data.dir", "/tmp/solr-test/")
  val initializer: CoreContainer.Initializer  = new CoreContainer.Initializer();
  val coreContainer: CoreContainer = initializer.initialize();
  val solrServer = new EmbeddedSolrServer(coreContainer, "")

  def getSolrSever(): SolrServer = solrServer

  def stopSolrServer : Unit = coreContainer.shutdown

  def loadDefaultData(server: SolrServer, numberOfRecords: Int = 14, collId: String = "92001"): Unit = {
    server.deleteByQuery("*:*") // delete all records in the index
    val docs = for (identifier <- 1 to numberOfRecords) yield createSolrRecord(identifier.toString, collId)
    server.add(docs)
    server.commit // commit so that data becomes available immediately
  }

  def createEuropeanaUri(identifier: String, collId: String) : String = format("%s/%s", collId, identifier)

  private def createSolrRecord(identifier: String, collId: String) : SolrInputDocument = {
    val doc = new SolrInputDocument
    doc.addField("europeana_uri", createEuropeanaUri(identifier, collId))
    doc.addField("europeana_type", "TEXT")
    List("europeana_collectionName", "europeana_collectionTitle", "europeana_provider", "europeana_dataProvider").foreach(field =>
      doc.addField(field, format("%s=%s", field, identifier))
    )
    List("dc_title", "dc_creator", "dc_date").foreach{field =>
      doc.addField(field, format("%s=%s", field, identifier))
      doc.addField(field, format("%s=%s", field, identifier + "2"))
    }
    doc
  }

  def getQueryResponse(query: String = "*:*", start: Int = 0) : QueryResponse = {
    loadDefaultData(solrServer)
    solrServer.query(new SolrQuery(query).setStart(start))
  }

}