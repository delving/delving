package eu.europeana.core.querymodel.query

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.core.CoreContainer
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument
import scala.collection.JavaConversions._

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since May 10, 2010 9:22:37 AM
 */

trait SolrTester {

  System.setProperty("solr.solr.home", "./trunk/core/src/test/solr/home")
  System.setProperty("solr.data.dir", "/tmp/solr-test/")
  val initializer: CoreContainer.Initializer  = new CoreContainer.Initializer();
  val coreContainer: CoreContainer = initializer.initialize();
  val solrServer = new EmbeddedSolrServer(coreContainer, "")

  def getSolrSever(): SolrServer = solrServer

  def stopSolrServer : Unit = coreContainer.shutdown

  def loadDefaultData(server: SolrServer, numberOfRecords: Int, collId: String): Unit = {
    server.deleteByQuery("*:*") // delete all records in the index
    val docs = for (identifier <- 1 to numberOfRecords) yield createSolrRecord(identifier.toString, collId)
    server.add(docs)
    server.commit // commit so that data becomes available immediately
  }

  def createEuropeanaUri(identifier: String, collId: String) : String = format("%s/%s", collId, identifier)


  private def createSolrRecord(identifier: String, collId: String) : SolrInputDocument = {
    val doc = new SolrInputDocument
    doc.addField("europeana_uri", createEuropeanaUri(identifier, collId))
    List("dc_title", "dc_creator", "dc_date", "europeana_collectionName").foreach(field =>
      doc.addField(field, format("%s=%s", field, identifier)))
    doc
  }

}