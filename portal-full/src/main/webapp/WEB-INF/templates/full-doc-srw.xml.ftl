<#assign result = result/>
<#assign model = result/>
<#assign doc = model.fullDoc/>
<#--<#assign query = query/>-->
<#--<#assign pagination = pagination/>-->
<#assign cacheUrl = cacheUrl/>
<#--<#assign startRecord = pagination.start/>-->
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
    <#--<srw:numberOfRecords>${pagination.numFound}</srw:numberOfRecords>-->
    <srw:records>
        <#--<#list model.briefDocWindow.docs as doc>-->
        <srw:record>
            <srw:recordSchema>info:srw/schema/1/dc-v1.1</srw:recordSchema>
            <srw:recordPacking>xml</srw:recordPacking>
            <srw:recordData>
                <dc:dc>
                    <@show_value "europeana:uri" doc.id/>
                    <@show_array_values "europeana:country" doc.europeanaCountry />
                    <@show_value "europeana:hasObject" doc.europeanaHasObject?string />
                    <@show_array_values "europeana:provider" doc.europeanaProvider />
                    <@show_value "europeana:collectionName" doc.europeanaCollectionName />
                    <@show_array_values "europeana:isShownAt" doc.europeanaIsShownAt />
                    <@show_array_values "europeana:isShownBy" doc.europeanaIsShownBy />
                    <#--<@show_array_values "europeana:unstored" doc.europeanaUnstored />-->
                    <@show_array_values "europeana:object" doc.thumbnails />
                    <@show_array_values "europeana:language" doc.europeanaLanguage />
                    <@show_value "europeana:type" doc.europeanaType />
                    <@show_array_values "europeana:userTag" doc.europeanaUserTag />
                    <@show_array_values "europeana:year" doc.europeanaYear />

                    <!-- here the dcterms namespaces starts -->
                    <@show_array_values "dcterms:alternative" doc.dcTermsAlternative />
                    <@show_array_values "dcterms:conformsTo" doc.dcTermsConformsTo />
                    <@show_array_values "dcterms:created" doc.dcTermsCreated />
                    <@show_array_values "dcterms:extent" doc.dcTermsExtent />
                    <@show_array_values "dcterms:hasFormat" doc.dcTermsHasFormat />
                    <@show_array_values "dcterms:hasPart" doc.dcTermsHasPart />
                    <@show_array_values "dcterms:hasVersion" doc.dcTermsHasVersion />
                    <@show_array_values "dcterms:isFormatOf" doc.dcTermsIsFormatOf />
                    <@show_array_values "dcterms:isPartOf" doc.dcTermsIsPartOf />
                    <@show_array_values "dcterms:isReferencedBy" doc.dcTermsIsReferencedBy />
                    <@show_array_values "dcterms:isReplacedBy" doc.dcTermsIsReplacedBy />
                    <@show_array_values "dcterms:isRequiredBy" doc.dcTermsIsRequiredBy />
                    <@show_array_values "dcterms:issued" doc.dcTermsIssued />
                    <@show_array_values "dcterms:isVersionOf" doc.dcTermsIsVersionOf />
                    <@show_array_values "dcterms:medium" doc.dcTermsMedium />
                    <@show_array_values "dcterms:provenance" doc.dcTermsProvenance />
                    <@show_array_values "dcterms:references" doc.dcTermsReferences />
                    <@show_array_values "dcterms:replaces" doc.dcTermsReplaces />
                    <@show_array_values "dcterms:requires" doc.dcTermsRequires />
                    <@show_array_values "dcterms:spatial" doc.dcTermsSpatial />
                    <@show_array_values "dcterms:tableOfContents" doc.dcTermsTableOfContents />
                    <@show_array_values "dcterms:temporal" doc.dcTermsTemporal />

                    <!-- here the dc namespaces starts -->
                    <@show_array_values "dc:contributor" doc.dcContributor />
                    <@show_array_values "dc:coverage" doc.dcCoverage />
                    <@show_array_values "dc:creator" doc.dcCreator />
                    <@show_array_values "dc:date" doc.dcDate />
                    <@show_array_values "dc:description" doc.dcDescription />
                    <@show_array_values "dc:format" doc.dcFormat />
                    <@show_array_values "dc:identifier" doc.dcIdentifier />
                    <@show_array_values "dc:language" doc.dcLanguage />
                    <@show_array_values "dc:publisher" doc.dcPublisher />
                    <@show_array_values "dc:relation" doc.dcRelation />
                    <@show_array_values "dc:rights" doc.dcRights />
                    <@show_array_values "dc:source" doc.dcSource />
                    <@show_array_values "dc:subject" doc.dcSubject />
                    <@show_array_values "dc:title" doc.dcTitle />
                    <@show_array_values "dc:type" doc.dcType />


                    <#--<dc:creator>${doc.dcCreator}</dc:creator>-->
                    <#--<dc:date>${doc.europeanaYear?string}</dc:date >-->
                    <#--<dc:language>${doc.europeanaLanguage.code}</dc:language>-->
                    <#--<tel:thumbnail xsi:type="dcterms:URI">${cacheUrl}uri=${doc.thumbnail?url('utf-8')}</tel:thumbnail>-->
                    <#--<dc:identifier xsi:type="dcterms:URI">${cacheUrl}uri=${doc.thumbnail?url('utf-8')}</dc:identifier >-->
                    <#--<dc:type>${doc.europeanaType}</dc:type>-->
                </dc:dc>
            </srw:recordData>
            <#--<srw:recordPosition>${startRecord}</srw:recordPosition>-->
        </srw:record>
        <#--<#assign startRecord = startRecord + 1 />-->
        <#--</#list>-->
    </srw:records>
    <#--<#if pagination.isNext() >-->
    <#--<srw:nextRecordPosition>${pagination.nextPage}</srw:nextRecordPosition>-->
    <#--</#if>-->
    <srw:echoedSearchRetrieveRequest>
        <srw:version>1.1</srw:version>
        <srw:query></srw:query>
        <#--<srw:maximumRecords>${pagination.rows}</srw:maximumRecords>-->
        <srw:recordPacking>xml</srw:recordPacking>
        <#--<srw:startRecord>${pagination.start}</srw:startRecord>-->
    </srw:echoedSearchRetrieveRequest>
</srw:searchRetrieveResponse>

<#macro show_array_values fieldName values>
<#list values as value>
<#if value?matches(" ")>
<${fieldName}/>
<#else>
<${fieldName}>${value?html}</${fieldName}>
</#if>
</#list>
</#macro>

<#macro show_value fieldName value>
<${fieldName}>${value?html}</${fieldName}>
</#macro>