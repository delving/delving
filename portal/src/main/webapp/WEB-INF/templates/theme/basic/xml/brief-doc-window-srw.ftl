<#assign startRecord = pagination.start/>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<srw:searchRetrieveResponse xmlns:srw="http://www.loc.gov/zing/srw/"
                            xmlns:diag="http://www.loc.gov/zing/srw/diagnostic/"
                            xmlns:xcql="http://www.loc.gov/zing/cql/xcql/"
                            xmlns:mods="http://www.loc.gov/mods/v3"
                            xmlns:europeana="http://www.europeana.eu"
                            xmlns:dcterms="http://purl.org/dc/terms/"
                            xmlns:dc="http://purl.org/dc/elements/1.1/"
                            xmlns:dcx="http://purl.org/dc/elements/1.1/"
                            xmlns:tel="http://krait.kb.nl/coop/tel/handbook/telterms.html"
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <srw:version>1.1</srw:version>
  <srw:numberOfRecords>${pagination.numFound}</srw:numberOfRecords>
  <srw:records>
    <#list result.briefDocs as doc>
    <srw:record>
      <srw:recordSchema>info:srw/schema/1/dc-v1.1</srw:recordSchema>
      <srw:recordPacking>xml</srw:recordPacking>
      <srw:recordData>
        <dc:dc>
            <#list doc.getFieldValueList() as field>
                <#list field.getValueAsArray() as value>
                    <${field.getKeyAsXml()}<#if value?starts_with("http://")> xsi:type="dcterms:URI"</#if>>${value}</${field.getKeyAsXml()}>
                </#list>
            </#list>
        </dc:dc>
      </srw:recordData>
      <srw:recordPosition>${startRecord}</srw:recordPosition>
    </srw:record>
    <#assign startRecord = startRecord + 1 />
  </#list>
</srw:records>
<#if pagination.isNext() >
<srw:nextRecordPosition>${pagination.nextPage}</srw:nextRecordPosition>
</#if>
<srw:echoedSearchRetrieveRequest>
    <srw:version>1.1</srw:version>
    <srw:query></srw:query>
    <srw:maximumRecords>${pagination.rows}</srw:maximumRecords>
    <srw:recordPacking>xml</srw:recordPacking>
    <srw:startRecord>${pagination.start}</srw:startRecord>
</srw:echoedSearchRetrieveRequest>
</srw:searchRetrieveResponse>
