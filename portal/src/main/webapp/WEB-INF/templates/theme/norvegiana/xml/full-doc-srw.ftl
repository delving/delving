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
    <srw:records>
        <srw:record>
            <srw:recordSchema>info:srw/schema/1/dc-v1.1</srw:recordSchema>
            <srw:recordPacking>xml</srw:recordPacking>
            <srw:recordData>
                <europeana:record>
                <#list result.fullDoc.getFieldValueList() as field>
                    <#list field.getValueAsArray() as value>
                        <${field.getKeyAsXml()}<#if value?starts_with("http://")> xsi:type="dcterms:URI">${value?html}<#else>>${value}</#if></${field.getKeyAsXml()}>
                    </#list>
                </#list>
                </europeana:record>
            </srw:recordData>
        </srw:record>
    </srw:records>
    <srw:echoedSearchRetrieveRequest>
        <srw:version>1.1</srw:version>
        <srw:query></srw:query>
        <srw:recordPacking>xml</srw:recordPacking>
    </srw:echoedSearchRetrieveRequest>
</srw:searchRetrieveResponse>
