package eu.delving.core.binding

import scala.collection.JavaConversions._
import org.apache.solr.client.solrj.response.QueryResponse
import reflect.BeanProperty
import collection.mutable.ListBuffer
import org.apache.solr.common.SolrDocumentList
import eu.europeana.core.querymodel.query. {DocId, EuropeanaQueryException}
import java.util. {Date, ArrayList, List => JList}
import scala.collection.mutable.Map

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10 /18/10 9:01 PM
 */

object SolrBindingService {

  private val docIdFields = List("europeana_uri", "timestamp")
  private val briefDocFields = List("europeana_uri", "timestamp", "europeana_collectionName", "europeana_collectionTitle",
    "PROVIDER", "DATAPROVIDER", "europeana_object", "COUNTRY", "TYPE", "LANGUAGE", "YEAR", "creator", "index", "fullDocUrl")
  private val fullDocFields = List("europeana_type", "europeana_userTag", "europeana_language", "europeana_country",
    "europeana_source", "europeana_isShownAt", "europeana_isShownBy", "europeana_year", "europeana_hasObject",
    "europeana_provider", "europeana_dataProvider", "europeana_rights", "COLLECTION")

  private val dcFields = List("dc_coverage", "dc_contributor", "dc_description", "dc_creator", "dc_date", "dc_format",
    "dc_identifier", "dc_language", "dc_publisher", "dc_relation", "dc_rights", "dc_source", "dc_subject", "dc_title",
    "dc_type")
  private val dcTermsFields = List("dcterms_alternative", "dcterms_created", "dcterms_conformsTo", "dcterms_extent",
    "dcterms_hasFormat", "dcterms_hasPart", "dcterms_hasVersion", "dcterms_isFormatOf", "dcterms_isPartOf",
    "dcterms_isReferencedBy", "dcterms_isReplacedBy", "dcterms_isRequiredBy", "dcterms_issued", "dcterms_isVersionOf",
    "dcterms_medium", "dcterms_provenance", "dcterms_references", "dcterms_replaces", "dcterms_requires", "dcterms_spatial",
    "dcterms_tableOfContents", "dcterms_temporal")

  private val icnFields = List("icn_creatorYearOfBirth", "icn_technique", "icn_material", "icn_location", "icn_province",
    "icn_collectionPart", "icn_acquisitionMeans", "icn_collectionType", "icn_acquisitionYear", "icn_purchasePrice",
    "icn_acquiredWithHelpFrom", "icn_physicalState")

  private val IndexAdditionFields = List("DCTYPE")

  def getSolrDocumentList(solrDocumentList : SolrDocumentList) : List[SolrDocument] = {
    val ArrayListObject = classOf[ArrayList[Any]]
    val StringObject = classOf[String]
    val DateObject = classOf[Date]
    val docs = new ListBuffer[SolrDocument]
    // check for required fields else check exception
    solrDocumentList.foreach{
        doc =>
          val solrDoc = SolrDocument()
          doc.entrySet.foreach{
            field =>
              val FieldValueClass: Class[_] = field.getValue.getClass
               FieldValueClass match {
                case ArrayListObject => solrDoc.add(field.getKey, field.getValue.asInstanceOf[ArrayList[Any]].toList)
                case StringObject => solrDoc.add(field.getKey, List(field.getValue))
                case DateObject => solrDoc.add(field.getKey, List(field.getValue))
                case _ => println("unknown class " + field.getKey)
              }
          }
      docs add solrDoc
    }
    docs.toList
  }

  def getSolrDocumentList(queryResponse : QueryResponse) : List[SolrDocument] = getSolrDocumentList(queryResponse.getResults)

  def getDocIds(queryResponse: QueryResponse): JList[SolrDocId] = {
    val docIds = new ListBuffer[SolrDocId]
    getSolrDocumentList(queryResponse).foreach(doc => docIds add (SolrDocId(doc)))
    asList(docIds)
  }

  def getBriefDocs(queryResponse: QueryResponse): JList[BriefDocItem] = getBriefDocs(queryResponse.getResults)

  def getBriefDocs(resultList: SolrDocumentList): JList[BriefDocItem] = {
    val briefDocs = new ListBuffer[BriefDocItem]
    asList(briefDocs)
  }

  def getFullDoc(queryResponse: QueryResponse): FullDocItem = {
    val results = getFullDocs(queryResponse.getResults)
    if (results.isEmpty) throw new EuropeanaQueryException("Full Doc not found")
    results.head
  }

  def getFullDocs(matchDoc: SolrDocumentList): JList[FullDocItem] = asList(new ListBuffer[FullDocItem])
}


case class SolrDocId(solrDocument : SolrDocument) extends DocId {
  def getEuropeanaUri : String = solrDocument.get("europeana_uri").head.asInstanceOf[String]
  def getTimestamp : Date = solrDocument.get("timestamp").head.asInstanceOf[Date]
}

case class BriefDocItem(@BeanProperty europeanaUri: String, @BeanProperty timestamp: String = "") //extends BriefDoc

case class FullDocItem(@BeanProperty europeanaUri: String = "") //extends FullDoc

case class SolrDocument(fieldMap : Map[String, List[Any]] = Map[String, List[Any]]()) {
  def get(field: String) : List[Any] = fieldMap.getOrElse(field, List[Any]())
  private[binding] def add(field: String, value : List[Any]) = fieldMap.put(field, value)
  private[binding] def getFieldNames = fieldMap.keySet.toString
}