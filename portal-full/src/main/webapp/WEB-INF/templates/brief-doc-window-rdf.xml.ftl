<#assign result = result/>
<#assign model = result/>
<#assign query = query/>
<#assign pagination = pagination/>
<#assign cacheUrl = cacheUrl/>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rdf:RDF
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:europeana="http://www.europeana.eu"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns="http://schemas.uche.ogbuji.net/rdfexample/">
 <#list model.briefDocWindow.docs as doc>
 <rdf:Description about="${doc.id}">
     <#if doc.title != " ">
     <dc:title>${doc.title?html}</dc:title>
     </#if>
     <#if doc.creator != " ">
     <dc:creator>${doc.creator?html}</dc:creator>
     </#if>
     <europeana:year>${doc.year?string}</europeana:year>
     <europeana:language>${doc.language}</europeana:language>
     <europeana:object>${cacheUrl}${doc.thumbnail?url("utf-8")}</europeana:object>
     <europeana:type>${doc.type}</europeana:type>
 </rdf:Description>
</#list>
</rdf:RDF>
