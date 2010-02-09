<#assign result = result/>
<#assign model = result/>
<#assign query = query/>
<#assign pagination = pagination/>
<#assign cacheUrl = cacheUrl/>
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
  <srw:numberOfRecords>${pagination.numFound?c}</srw:numberOfRecords>
  <srw:records>
    <#list model.briefDocWindow.docs as doc>
    <srw:record>
      <srw:recordSchema>info:srw/schema/1/dc-v1.1</srw:recordSchema>
      <srw:recordPacking>xml</srw:recordPacking>
      <srw:recordData>
        <dc:dc>
            <tel:recordId xsi:type="dcterms:URI">${doc.id}</tel:recordId>
            <dc:title>${doc.title?html}</dc:title>
            <dc:creator>${doc.creator?html}</dc:creator>
            <dc:date>${doc.year?string}</dc:date >
            <dc:language>${doc.language}</dc:language>
            <tel:thumbnail xsi:type="dcterms:URI">${cacheUrl}uri=${doc.thumbnail?url('utf-8')}</tel:thumbnail>
            <dc:identifier xsi:type="dcterms:URI">${cacheUrl}uri=${doc.thumbnail?url('utf-8')}</dc:identifier >
            <dc:type>${doc.type}</dc:type>
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