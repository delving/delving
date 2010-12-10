package eu.delving.core.binding

import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import scala.collection.JavaConversions._
import org.scalatest. {BeforeAndAfterAll, Spec}
import xml. {MetaData, NodeSeq, Elem}
import eu.europeana.core.querymodel.query.{DocType, SolrTester}

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 10 /18/10 9:04 PM
 */

@RunWith(classOf[JUnitRunner])
class SolrBindingServiceSpec extends Spec with ShouldMatchers with BeforeAndAfterAll with SolrTester {

  override def afterAll() = stopSolrServer

  describe("A SolrBindingService") {

    describe("(when retrieving a SolrDocumentList)") {

      it("should give back all values as a List") {
        val response = getQueryResponse("*:*")
        val ids = SolrBindingService.getSolrDocumentList(response)
        ids.length should equal(10)
        ids.head.getFieldNames.isEmpty should be(false)
        ids.head.get("europeana_uri").head should equal("92001/1")
        ids.head.get("unknown_field").size should equal(0)
        ids.head.get("unknown_field").isEmpty should be(true)
      }
    }

    describe("(when retrieving a DocId List)") {

      it("should give back a list of DocIds") {
        val response = getQueryResponse("*:*")
        val idList = SolrBindingService.getDocIds(response)
        val firstId = idList.head
        firstId.getEuropeanaUri should equal("92001/1")
      }

    }

    describe("(when giving back a BriefDocItem List)") {

      val response = getQueryResponse("*:*")
      val briefDocList = SolrBindingService.getBriefDocs(response)

      it("should have bound all the object") {
        briefDocList.length should equal(10)
        briefDocList.head.getId should equal("92001/1")
      }

      it("should give access to the getters") {
        briefDocList.head.getDataProvider should equal("europeana_dataProvider=1")
        briefDocList.head.getType should equal(DocType.TEXT)
      }

      it("should give access to the setters") {
        val item = briefDocList.head
        item.getIndex should be(0)
        item.setIndex(1)
        item.getIndex should be(1)
      }

    }

    describe("(when giving back a FullDoc List)") {

      val response = getQueryResponse("*:*")
      val fullDocList = SolrBindingService.getFullDocs(response)

      it("should bind the fields to the results") {
        fullDocList.head.getId should equal("92001/1")
      }

      it("should give access to the FieldValue for a key") {
        val fullDoc = fullDocList.head
        val getField = "dc_creator"
        val fv = fullDoc.getFieldValue(getField)
        fv.isNotEmpty should  be (true)
        fv.getKey should equal (getField)
        fv.getFirst should  equal ("dc_creator=1")
        fv.getValueAsArray.length should equal (2)
      }

    }

    describe("(when giving parsing a Record from a OAI-PMH GetRecord response)") {

      val sampleRecord : Elem =
<OAI-PMH xmlns:ese="http://www.europeana.eu/schemas/ese/" xmlns:dcterms="http://purl.org/dc/terms/" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:europeana="http://www.europeana.eu/schemas/ese/" xmlns:abm="http://to_be_decided/abm/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.openarchives.org/OAI/2.0/">
      <responseDate>2010-11-02T14:49:21Z</responseDate>
      <request verb="GetRecord" identifier="00103_Ag_sff_NO_sffKL:4ccffdadc35e61688c0a28e2" metadataPrefix="ese">http://localhost:8983/services/oai-pmh</request>
      <GetRecord>
        <record>
        <header>
          <identifier>00103_Ag_sff_NO_sffKL:4ccffdadc35e61688c0a28e2</identifier>
          <datestamp>2010-11-02T13:01:49Z</datestamp>
          <setSpec>00103_Ag_sff_NO_sffKL</setSpec>
        </header>
        <metadata>
<record>
<dc:contributor xml:lang="no">Hermund Kleppa</dc:contributor>
<dc:contributor xml:lang="nl">Lisabet Risa</dc:contributor>
<dc:contributor>Botolv H. Tynning</dc:contributor>
<dc:contributor>Ukjend</dc:contributor>
<dc:creator>Margrethe Henden Aaraas og Sigurd Vengen</dc:creator>
<dc:description xml:lang="no">Brekke kyrkje er ei langkyrkje i tre som står i bygda Brekke ved Risnefjorden i Gulen kommune. Kyrkja, som har 390 sitjeplassar, vart vigsla 19. november 1862 av prost Thomas Erichsen. Arkitekt Christian Henrik Grosch laga teikningane til kyrkja, som skal vera den tredje på staden. Brekke kyrkje er soknekyrkje for Brekke sokn i Gulen prestegjeld.</dc:description>
<dc:identifier>SFFkl-100183</dc:identifier>
<dc:language>no</dc:language>
<dc:publisher>Fylkesarkivet i Sogn og Fjordane</dc:publisher>
<dc:subject>Historie og geografi kyrkjer (kirker)</dc:subject>
<dc:title>Brekke kyrkje</dc:title>
<dc:type>Text</dc:type>
<dcterms:created>2002</dcterms:created>
<dcterms:references>Aaraas, Margrethe Henden m.fl.: På kyrkjeferd i Sogn og Fjordane - 2. Sogn. Selja Forlag. Førde 2000.</dcterms:references>
<dcterms:spatial>SRID=32633;POINT(-14389 6802532)</dcterms:spatial>
<europeana:dataProvider>Fylkesarkivet i Sogn og Fjordane</europeana:dataProvider>
<europeana:isShownAt>http://www.sffarkiv.no/sffbasar/default.asp?p=result&amp;db=dbatlas_leks&amp;art_id=626&amp;spraak_id=1&amp;ptype=single</europeana:isShownAt>
<europeana:object>http://www.sffarkiv.no/webdb/fileStream.aspx?fileName=dbatlas_leks\1411-gul\1411039001.jpg</europeana:object>
<europeana:provider>ABM-utvikling</europeana:provider>
<europeana:rights>unknown</europeana:rights>
<europeana:type>TEXT</europeana:type>
<europeana:unstored xml:lang="no">Brekke kyrkje er soknekyrkje for Brekke sokn i Gulen prestegjeld. Frå 1859 til 1968 høyrde Brekke sokn til Lavik prestegjeld, men høyrer no til Gulen prestegjeld. Kyrkja vart måla opp att utvendig i 2001.</europeana:unstored>
<europeana:unstored xml:lang="no">Det solide kyrkjetårnet frå 1930-talet er det tredje som har pryda Brekke kyrkje. Dei to fyrste tolte ikkje den sterke sønnavinden i området.</europeana:unstored>
<europeana:unstored xml:lang="en">Kyrkjekoret er enkelt og lunt, og det lyse treverket er ein god bakgrunn for altertavla og biletvevnaden på sidene. Biletteppa er laga av Emma Breidvik, t.v. ein engel og t.h. Golgatagruppa. Altertavla er måla av Anders Askevold og har &quot;Jesus i Getsemane&quot; som motiv.</europeana:unstored>
<europeana:unstored xml:lang="nl">På kyrkjeveg frå Verkland i 1915.</europeana:unstored>
<europeana:unstored xml:lang="no">Kyrkjebåt på fjorden på veg til Brekke kyrkje.</europeana:unstored>
</record>
    </metadata>
  </record>
 </GetRecord>
</OAI-PMH>

      val response = getQueryResponse("*:*")
      val fullDoc = SolrBindingService.parseSolrDocumentFromGetRecordResponse(sampleRecord)

      it("should bind to the OAI-PMH record") {
        fullDoc should not be (null)
        fullDoc.getAsArray("europeana_unstored").size should equal (5)
        fullDoc.getFieldValueList.foreach(fv => println(fv.getKeyAsXml + fv.getFirst))
        val fieldValueNode = fullDoc.solrDocument.getFieldValueNode("europeana_unstored")
        println(fieldValueNode)
        fieldValueNode.foreach{
          node =>
            node.hasAttributes should be (true)
            node.hasLanguageAttribute should be (true)
            node.getAttribute("xml:lang") should equal (node.getLanguage)
        }
        val fvnGroupedByLanguage = fullDoc.solrDocument.getFieldValueNodeGroupedByLanguage("dc_contributor")
        fvnGroupedByLanguage.keys.size should equal (3)
        fvnGroupedByLanguage.getOrElse("unknown", List[FieldValueNode]()).size should equal (2)
      }

    }


  }

}