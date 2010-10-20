package eu.delving.core.binding

import scala.collection.JavaConversions._
import org.apache.solr.client.solrj.response.QueryResponse
import scala.reflect.BeanProperty
import collection.mutable.ListBuffer
import org.apache.solr.common.SolrDocumentList
import java.util. {Date, ArrayList, List => JList}
import scala.collection.mutable.Map
import eu.europeana.core.querymodel.query. {BriefDoc, DocType, DocId, EuropeanaQueryException}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10 /18/10 9:01 PM
 */

object SolrBindingService {

  def getSolrDocumentList(solrDocumentList : SolrDocumentList) : List[SolrDocument] = {
    val docs = new ListBuffer[SolrDocument]
    val ArrayListObject = classOf[ArrayList[Any]]
    val StringObject = classOf[String]
    val DateObject = classOf[Date]
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
    getSolrDocumentList(resultList).foreach(doc => briefDocs add (BriefDocItem(doc)))
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

case class BriefDocItem(solrDocument : SolrDocument) extends BriefDoc {
    private def assign(key: String) = solrDocument.getFirst(key)

    def getId : String = assign("europeana_uri")
    def getTitle : String = assign("title")
    def getThumbnail : String = assign("europeana_object")
    def getCreator : String = assign("creator")
    def getYear : String = assign("YEAR")
    def getProvider : String = assign("PROVIDER")
    def getDataProvider : String = assign("DATAPROVIDER")
    def getLanguage : String = assign("LANGUAGE")
    def getType : DocType = DocType.get(assign("TYPE"))

    @BeanProperty var index : Int = _
    @BeanProperty var fullDocUrl: String = _

    // debug and scoring information
    @BeanProperty var score : Int = _
    @BeanProperty var debugQuery : String = _
}

case class FullDocItem(solrDocument : SolrDocument) //extends FullDoc

case class SolrDocument(fieldMap : Map[String, List[Any]] = Map[String, List[Any]]()) {

  def get(field: String) : List[Any] = fieldMap.getOrElse(field, List[Any]())

  def getFirst(field: String) : String = fieldMap.getOrElse(field, List[Any]()).headOption.getOrElse("").asInstanceOf[String] // todo made generic later

  private[binding] def add(field: String, value : List[Any]) = fieldMap.put(field, value)

  private[binding] def getFieldNames = fieldMap.keySet.toString
}