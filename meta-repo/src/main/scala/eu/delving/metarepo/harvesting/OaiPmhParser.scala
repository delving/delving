package eu.delving.metarepo.harvesting

import javax.servlet.http.{HttpServletRequest}

import java.lang.String
import xml.{Elem}
import java.util.{Map, Date}
import scala.collection.JavaConversions._
import eu.delving.metarepo.core.MetaRepo
import eu.delving.metarepo.core.MetaRepo.{PmhVerb, PmhRequest, HarvestStep, Record}
import eu.delving.metarepo.impl.PmhRequestImpl
import collection.mutable.HashMap
import java.util.Map.Entry

/**
 *  This class is used to parse an OAI-PMH instruction from an HttpServletRequest and return the proper XML response
 *
 *  This implementation is based on the v.2.0 specification that can be found here: http://www.openarchives.org/OAI/openarchivesprotocol.html
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 16, 2010 12:06:56 AM
 */

// todo: determine if schema validation is necessary


class OaiPmhParser(request: HttpServletRequest, metaRepo: MetaRepo) {

  private val VERB = "verb"
  private val legalParameterKeys = List("verb", "identifier", "metadataPrefix", "set", "from", "until", "resumptionToken")

  /**
   * receive an HttpServletRequest with the OAI-PMH parameters and return the correctly formatted xml as a string.
   */

  def parseRequest() : String = {

    val requestParams = request.getParameterMap.asInstanceOf[Map[String, Array[String]]]

    if (!isLegalPmhRequest(requestParams)) return createErrorResponse("badArgument").toString

    val params = HashMap[String, String]()
    requestParams.entrySet.foreach{entry: Entry[String, Array[String]] => params.put(entry.getKey, entry.getValue.head)}

    def pmhRequest(verb: PmhVerb) : PmhRequestEntry = createPmhRequest(params, verb)

    val response = params.get(VERB).get match {
        case "Identify" => processIdentify( pmhRequest(PmhVerb.IDENTIFY) )
        case "ListMetadataFormats" => processListMetadataFormats( pmhRequest(PmhVerb.List_METADATA_FORMATS) )
        case "ListSets" => processListSets( pmhRequest(PmhVerb.LIST_SETS) )
        case "ListRecords" => processListRecords( pmhRequest(PmhVerb.LIST_RECORDS) )
        case "ListIdentifiers" => processListIdentifiers( pmhRequest(PmhVerb.LIST_IDENTIFIERS) )
        case "GetRecord" => processGetRecord( pmhRequest(PmhVerb.GET_RECORD) )
        case _ => createErrorResponse("badVerb")
      }
    response.toString
  }

  def isLegalPmhRequest(params: Map[String, Array[String]]) : Boolean = {
    val isValid = true
    // request must contain the verb parameter
    if (!params.containsKey(VERB)) return false

    // no repeat queryParameters are allowed
    if (params.values.exists(value => value.length > 1)) return false

    // check for illegal queryParameter keys
    if (!(params.keys filterNot (legalParameterKeys contains)).isEmpty) return false

    isValid
  }

  /**
   */
  // TODO: add values from a message.properties file and complete oai identifier block
  def processIdentify(pmhRequestEntry: PmhRequestEntry) : Elem = {
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request verb="Identify">{request.getRequestURL}</request>
      <Identify>
        <repositoryName>Delving MetaRepo</repositoryName>
        <baseURL>{request.getRequestURL}</baseURL>
        <protocolVersion>2.0</protocolVersion>
        <adminEmail>somebody@delving.eu</adminEmail>
        <earliestDatestamp>1990-02-01T12:00:00Z</earliestDatestamp>
        <deletedRecord>persistent</deletedRecord>
        <granularity>YYYY-MM-DDThh:mm:ssZ</granularity>
        <compression>deflate</compression>
        <description>
          <oai-identifier
            xmlns="http://www.openarchives.org/OAI/2.0/oai-identifier"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation=
                "http://www.openarchives.org/OAI/2.0/oai-identifier
            http://www.openarchives.org/OAI/2.0/oai-identifier.xsd">
            <scheme>oai</scheme>
            <repositoryIdentifier>lcoa1.loc.gov</repositoryIdentifier>
            <delimiter>:</delimiter>
            <sampleIdentifier>oai:lcoa1.loc.gov:loc.music/musdi.002</sampleIdentifier>
          </oai-identifier>
        </description>
     </Identify>
</OAI-PMH>
  }

  def processListSets(pmhRequestEntry: PmhRequestEntry) : Elem = {
    val dataSets = metaRepo.getDataSets

    // when there are no collections throw "noSetHierarchy" ErrorResponse
    if (dataSets.size == 0) return createErrorResponse("noSetHierarchy")

    // todo: implement harvest steps for this verb.

    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
     <responseDate>{new Date}</responseDate>
     <request verb="ListSets">{request.getRequestURL}</request>
      <ListSets>
        { for (set <- dataSets.values) yield
          <set>
            <setSpec>{set.setSpec}</setSpec>
            <setName>{set.setName}</setName>
          </set>
        }
      </ListSets>
    </OAI-PMH>
  }

  /**
   * This method can give back the following Error and Exception conditions: idDoesNotExist, noMetadataFormats.
   */
  def processListMetadataFormats(pmhRequestEntry: PmhRequestEntry) : Elem = {

    // if no identifier present list all formats
    val identifier = pmhRequestEntry.pmhRequest.getIdentifier

    // otherwise only list the formats available for the identifier
    val metadataFormats = if (identifier.isEmpty) metaRepo.getMetadataFormats else metaRepo.getMetadataFormats(identifier)

    def formatRequest() : Elem = if (!identifier.isEmpty) <request verb="ListMetadataFormats" identifier={identifier}>{request.getRequestURL}</request>
                                    else <request verb="ListMetadataFormats">{request.getRequestURL}</request>

    // todo: remove dummy metadataFormat entry later

    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      {formatRequest}
      <ListMetadataFormats>
       {for (format <- metadataFormats) yield
        <metadataFormat>
          <metadataPrefix>{format.prefix}</metadataPrefix>
          <schema>{format.schema}</schema>
          <metadataNamespace>{format.namespace}</metadataNamespace>
       </metadataFormat>
        }
       <metadataFormat>
        <metadataPrefix>oai_dc</metadataPrefix>
        <schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>
        <metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace>
       </metadataFormat>
      </ListMetadataFormats>
    </OAI-PMH>
  }

  /**
   * This method can give back the following Error and Exception conditions: BadResumptionToken, cannotDisseminateFormat, noRecordsMatch, noSetHierachy
   */
  def processListIdentifiers(pmhRequestEntry: PmhRequestEntry) = {
        // parse all the params from map
    val harvestStep: HarvestStep = getHarvestStep(pmhRequestEntry)

      <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request verb="ListIdentifiers" from={harvestStep.pmhRequest.getFrom} until={harvestStep.pmhRequest.getUntil}
               metadataPrefix={harvestStep.pmhRequest.getMetadataPrefix}
               set="physics:hep">{request.getRequestURL}</request>
      <ListIdentifiers>
        { for (record <- harvestStep.records) yield
        <header status={recordStatus(record)}>
          <identifier>{record.identifier}</identifier>
          <datestamp>{record.modified}</datestamp>
          <setSpec>{record.set.getSetSpec}</setSpec>
       </header>
        }
       <header>
        <identifier>oai:arXiv.org:hep-th/9801002</identifier>
        <datestamp>1999-03-20</datestamp>
        <setSpec>physic:hep</setSpec>
       </header>
        {renderResumptionToken(harvestStep)}
     </ListIdentifiers>
    </OAI-PMH>
  }


  def processListRecords(pmhRequestEntry: PmhRequestEntry) = {
    val harvestStep: HarvestStep = getHarvestStep(pmhRequestEntry)
    val pmhObject = harvestStep.pmhRequest
    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
     <responseDate>{new Date}</responseDate>
     <request verb="ListRecords" from={pmhObject.getFrom}
              until={pmhObject.getUntil} metadataPrefix={pmhObject.getMetadataPrefix}>{request.getRequestURL}</request>
     <ListRecords>
          <metadata>
            {for (record <- harvestStep.records) yield
              renderRecord(record, pmhObject.getMetadataPrefix)
            }
          </metadata>
       {renderResumptionToken(harvestStep)}
     </ListRecords>
    </OAI-PMH>
  }

  def processGetRecord(pmhRequestEntry: PmhRequestEntry) : Elem = {
    val pmhRequest = pmhRequestEntry.pmhRequest
    // get identifier and format from map else throw BadArgument Error
    if (pmhRequest.getIdentifier.isEmpty || pmhRequest.getMetadataPrefix.isEmpty) return createErrorResponse("badArgument")
    // if identifier/record is not found throw NoRecordsMatch error
    val identifier = pmhRequest.getIdentifier
    val metadataFormat = pmhRequest.getMetadataPrefix

    val record = metaRepo.getRecord(identifier, metadataFormat)
    if (record == null) return createErrorResponse("idDoesNotExist")

    // if format is not found throw cannotDisseminateFormat error

// todo: the record can have a number of metadata formats, you have to ask for them.
//    if (record.metadataFormat != metadataFormat) return createErrorResponse("cannotDisseminateFormat")

    // else  render identifier below
    <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request verb="GetRecord" identifier={identifier}
               metadataPrefix={metadataFormat}>{request.getRequestURL}</request>
      <GetRecord>
        {renderRecord(record, metadataFormat)}
     </GetRecord>
    </OAI-PMH>
  }

  private def getHarvestStep(pmhRequestEntry: PmhRequestEntry) : HarvestStep = {
    if (!pmhRequestEntry.resumptionToken.isEmpty)
      metaRepo.getHarvestStep(pmhRequestEntry.resumptionToken)
    else
      metaRepo.getHarvestStep(pmhRequestEntry.pmhRequest)
  }

  private def recordStatus(record: Record) : String = if (record.deleted) "deleted" else ""

  private def renderRecord(record: Record, format: String) : Elem = {
    <record>
        <header>
          <identifier>{record.identifier}</identifier>
          <datestamp>{record.modified}</datestamp>
          <setSpec>{record.set}</setSpec>
        </header>
        <metadata>
          {record.xml(format)}
        </metadata>
    </record>
  }

  private def renderResumptionToken(step: HarvestStep) = {
    if (step.hasNext)
      <resumptionToken expirationDate={step.expiration.toString} completeListSize={step.listSize.toString}
                       cursor={step.cursor.toString}>{step.resumptionToken.toString}</resumptionToken>
    else
      <resumptionToken/>
  }

  private def createPmhRequest(params: HashMap[String, String], verb: PmhVerb) : PmhRequestEntry = {
    def getParam(key: String) = params.getOrElse(key, "")
    val pmh: PmhRequest = new PmhRequestImpl(verb,
      getParam("set"), getParam("from"), getParam("until"),
      getParam("metadataPrefix"), getParam("identifier"))
    PmhRequestEntry(pmh, getParam("resumptionToken"))
  }

  /**
   * This method is used to create all the OAI-PMH error responses to a given OAI-PMH request. The error descriptions have
   * been taken directly from the specifications document for v.2.0.
   */
  def createErrorResponse(errorCode: String): Elem = {
<OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
      <responseDate>{new Date}</responseDate>
      <request>{request.getRequestURL}</request>
      {errorCode match {
      case "badArgument" => <error code="badArgument">The request includes illegal arguments, is missing required arguments, includes a repeated argument, or values for arguments have an illegal syntax.</error>
      case "badResumptionToken" => <error code="badResumptionToken">The value of the resumptionToken argument is invalid or expired.</error>
      case "badVerb" => <error code="badVerb">Value of the verb argument is not a legal OAI-PMH verb, the verb argument is missing, or the verb argument is repeated.</error>
      case "cannotDisseminateFormat" => <error code="cannotDisseminateFormat">The metadata format identified by the value given for the metadataPrefix argument is not supported by the item or by the repository.</error>
      case "idDoesNotExist" => <error code="idDoesNotExist">The value of the identifier argument is unknown or illegal in this repository.</error>
      case "noMetadataFormats" => <error code="noMetadataFormats">There are no metadata formats available for the specified item.</error>
      case "noRecordsMatch" => <error code="noRecordsMatch">The combination of the values of the from, until, set and metadataPrefix arguments results in an empty list.</error>
      case "noSetHierarchy" => <error code="noSetHierarchy">This repository does not support sets</error> // Should never be used. We only use sets
      case _ => <error code="unknown">Unknown Error Corde</error> // should never happen.
    }}
</OAI-PMH>
  }

case class PmhRequestEntry(pmhRequest: PmhRequest, resumptionToken: String)

}
object OaiPmhParser {
   def parseHttpServletRequest(request: HttpServletRequest, metaRepo: MetaRepo) : String = {
     val parser = new OaiPmhParser(request, metaRepo)
     parser parseRequest
   }
 }


