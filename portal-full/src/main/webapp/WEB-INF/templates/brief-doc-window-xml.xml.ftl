<#assign result = result/>
<#assign model = result/>
<#assign query = query/>
<#assign pagination = pagination/>
<#assign cacheUrl = cacheUrl/>
<#assign startRecord = pagination.start/>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<metadata xmlns:srw="http://www.loc.gov/zing/srw/"
          xmlns:diag="http://www.loc.gov/zing/srw/diagnostic/"
          xmlns:xcql="http://www.loc.gov/zing/cql/xcql/"
          xmlns:mods="http://www.loc.gov/mods/v3"
          xmlns:europeana="http://www.europeana.eu"
          xmlns:dcterms="http://purl.org/dc/terms/"
          xmlns:dc="http://purl.org/dc/elements/1.1/">
    <#list model.briefDocWindow.docs as doc>
    <record>
            <europeana:uri>${doc.id}</europeana:uri>
            <dc:title>${doc.title?html}</dc:title>
            <dc:contributor>${doc.contributor?html}</dc:contributor>
            <europeana:year>${doc.year?string}</europeana:year>
            <europeana:language>${doc.language.code}</europeana:language>
            <europeana:object>${cacheUrl}${doc.thumbnail?url("utf-8")}</europeana:object>
            <europeana:type>${doc.type}</europeana:type>
    </record>
  </#list>
</metadata>