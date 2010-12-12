<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rdf:RDF
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:europeana="http://www.europeana.eu"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns="http://schemas.uche.ogbuji.net/rdfexample/">
 <#list result.briefDocs as doc>
 <rdf:Description about="${portalBaseUrl}/${portalName}/record/${doc.id}.html">
     <#assign dcTitle = doc.getFieldValue("dc_title")/>
     <#if dcTitle.isNotEmpty()>
     <dc:title>${dcTitle.getFirst()}</dc:title>
     </#if>
     <#assign dcCreator = doc.getFieldValue("dc_creator")/>
     <#if dcCreator.isNotEmpty()>
     <dc:creator>${dcCreator.getFirst()}</dc:creator>
     </#if>
     <#assign dcDate = doc.getFieldValue("dc_date")/>
     <#if dcDate.isNotEmpty()>
         <europeana:year>${dcDate.getFirst()}</europeana:year>
     </#if>
     <europeana:language>${doc.language}</europeana:language>
     <europeana:object>${doc.thumbnail}</europeana:object>
     <europeana:type>${doc.type}</europeana:type>
 </rdf:Description>
</#list>
</rdf:RDF>
