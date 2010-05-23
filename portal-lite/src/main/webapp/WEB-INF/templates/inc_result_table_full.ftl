<#-- Assign values(sequences) to array names. These are used to populate the 'more'  area below the meta-data fields that are always shown -->
<#assign arrsubj = model.fullDoc.dcSubject + result.fullDoc.dcTermsTemporal + result.fullDoc.dcTermsSpatial + result.fullDoc.dcCoverage >
<#assign formatArr = result.fullDoc.dcFormat + result.fullDoc.dcTermsExtent + result.fullDoc.dcTermsMedium />
<#assign providerArr = result.fullDoc.europeanaProvider + result.fullDoc.europeanaCountry />
<#assign sourceArr = result.fullDoc.dcSource />
<#assign indentifierArr = result.fullDoc.dcIdentifier />
<#assign typeArr = result.fullDoc.dcType />
<#assign publisherArr = result.fullDoc.dcPublisher />
<#assign provenanceArr = result.fullDoc.dcTermsProvenance />
<#assign relationsArr = result.fullDoc.dcRelation + result.fullDoc.dcTermsReferences + result.fullDoc.dcTermsIsReferencedBy
                + result.fullDoc.dcTermsIsReplacedBy + result.fullDoc.dcTermsIsRequiredBy + result.fullDoc.dcTermsIsPartOf + result.fullDoc.dcTermsHasPart
                + result.fullDoc.dcTermsReplaces + result.fullDoc.dcTermsRequires + result.fullDoc.dcTermsIsVersionOf + result.fullDoc.dcTermsHasVersion
                + result.fullDoc.dcTermsConformsTo + result.fullDoc.dcTermsHasFormat />
<#assign moreArr = indentifierArr + publisherArr + provenanceArr + arrsubj + typeArr + relationsArr />

     <h1 class="${result.fullDoc.europeanaType}">
        <#assign tl = "">
        <#if !model.fullDoc.dcTitle[0]?matches(" ")>
            <#assign tl= result.fullDoc.dcTitle[0]>
        <#elseif !model.fullDoc.dcTermsAlternative[0]?matches(" ")>
            <#assign tl=result.fullDoc.dcTermsAlternative[0]>
        <#else>
            <#assign tl = result.fullDoc.dcDescription[0] />
            <#if tl?length &gt; 50>
                <#assign tl = result.fullDoc.dcDescription[0]?substring(0, 50) + "..."/>
         </#if>
        </#if>
        <@stringLimiter "${tl}" "150"/>
    </h1>

                <div class="grid_5 alpha" id="img-full">
                    <#assign imageRef = "#"/>
                    <#if !result.fullDoc.europeanaIsShownBy[0]?matches(" ")>
                        <#assign imageRef = result.fullDoc.europeanaIsShownBy[0]/>
                    <#elseif !result.fullDoc.europeanaIsShownAt[0]?matches(" ")>
                        <#assign imageRef = result.fullDoc.europeanaIsShownAt[0]/>
                    </#if>
                       <a href="redirect.html?shownBy=${imageRef?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"
                          target="_blank"
                          alt="<@spring.message 'ViewInOriginalContext_t' />  <@spring.message 'OpensInNewWindow_t'/>"
                          >
                        <#if useCache="true">
                            <img src="${cacheUrl}uri=${result.fullDoc.thumbnails[0]?url('utf-8')}&amp;size=FULL_DOC&amp;type=${result.fullDoc.europeanaType}"
                                 class="full" 
                                 alt="Image title: ${result.fullDoc.dcTitle[0]}"
                                 id="imgview"
                                 onload="checkSize('full',this.width);"
                             />
                        <#else>

                            <img src="${result.fullDoc.thumbnails[0]}"
                                 alt="Image title: ${result.fullDoc.dcTitle[0]}"
                                 id="imgview"
                                 onload="checkSize(this.id,'full',this.width);"
                                 onerror="showDefaultLarge(this,'${result.fullDoc.europeanaType}')"
                                 alt="<@spring.message 'ViewInOriginalContext_t' />  <@spring.message 'OpensInNewWindow_t'/>"
                             />
                        </#if>
                    </a>
                </div>

                <div id="item-detail" class="grid_7 omega">
                  <#if format?? && format?contains("labels")>
                        <#assign doc = result.fullDoc />
                        <#assign showFieldNames = true />

                        <@show_value "europeana:uri" doc.id showFieldNames />
                        <@show_array_values "europeana:country" doc.europeanaCountry  showFieldNames />
                        <#--<@show_array_values "europeana:source" doc.europeanaSource  showFieldNames />-->
                        <@show_array_values "europeana:provider" doc.europeanaProvider  showFieldNames />
                        <@show_value "europeana:collectionName" doc.europeanaCollectionName showFieldNames/>
                        <@show_value "europeana:hasObject" doc.europeanaHasObject?string  showFieldNames />
                        <@show_array_values "europeana:isShownAt" doc.europeanaIsShownAt  showFieldNames />
                        <@show_array_values "europeana:isShownBy" doc.europeanaIsShownBy  showFieldNames />
                        <#--<@show_array_values "europeana:unstored" doc.europeanaUnstored  showFieldNames />-->
                        <@show_array_values "europeana:object" doc.thumbnails  showFieldNames />
                        <@show_array_values "europeana:language" doc.europeanaLanguage  showFieldNames />
                        <@show_value "europeana:type" doc.europeanaType  showFieldNames />
                        <@show_array_values "europeana:userTag" doc.europeanaUserTag  showFieldNames />
                        <@show_array_values "europeana:year" doc.europeanaYear  showFieldNames />
                        <!-- here the dcterms namespaces starts -->
                        <@show_array_values "dcterms:alternative" doc.dcTermsAlternative  showFieldNames />
                        <@show_array_values "dcterms:conformsTo" doc.dcTermsConformsTo  showFieldNames />
                        <@show_array_values "dcterms:created" doc.dcTermsCreated  showFieldNames />
                        <@show_array_values "dcterms:extent" doc.dcTermsExtent  showFieldNames />
                        <@show_array_values "dcterms:hasFormat" doc.dcTermsHasFormat  showFieldNames />
                        <@show_array_values "dcterms:hasPart" doc.dcTermsHasPart  showFieldNames />
                        <@show_array_values "dcterms:hasVersion" doc.dcTermsHasVersion  showFieldNames />
                        <@show_array_values "dcterms:isFormatOf" doc.dcTermsIsFormatOf  showFieldNames />
                        <@show_array_values "dcterms:isPartOf" doc.dcTermsIsPartOf  showFieldNames />
                        <@show_array_values "dcterms:isReferencedBy" doc.dcTermsIsReferencedBy  showFieldNames />
                        <@show_array_values "dcterms:isReplacedBy" doc.dcTermsIsReplacedBy  showFieldNames />
                        <@show_array_values "dcterms:isRequiredBy" doc.dcTermsIsRequiredBy  showFieldNames />
                        <@show_array_values "dcterms:issued" doc.dcTermsIssued  showFieldNames />
                        <@show_array_values "dcterms:isVersionOf" doc.dcTermsIsVersionOf  showFieldNames />
                        <@show_array_values "dcterms:medium" doc.dcTermsMedium  showFieldNames />
                        <@show_array_values "dcterms:provenance" doc.dcTermsProvenance  showFieldNames />
                        <@show_array_values "dcterms:references" doc.dcTermsReferences  showFieldNames />
                        <@show_array_values "dcterms:replaces" doc.dcTermsReplaces  showFieldNames />
                        <@show_array_values "dcterms:requires" doc.dcTermsRequires  showFieldNames />
                        <@show_array_values "dcterms:spatial" doc.dcTermsSpatial  showFieldNames />
                        <@show_array_values "dcterms:tableOfContents" doc.dcTermsTableOfContents  showFieldNames />
                        <@show_array_values "dcterms:temporal" doc.dcTermsTemporal  showFieldNames />
                        <!-- here the dc namespaces starts -->
                        <@show_array_values "dc:contributor" doc.dcContributor  showFieldNames />
                        <@show_array_values "dc:coverage" doc.dcCoverage  showFieldNames />
                        <@show_array_values "dc:creator" doc.dcCreator  showFieldNames />
                        <@show_array_values "dc:date" doc.dcDate  showFieldNames />
                        <@show_array_values "dc:description" doc.dcDescription  showFieldNames />
                        <@show_array_values "dc:format" doc.dcFormat  showFieldNames />
                        <@show_array_values "dc:identifier" doc.dcIdentifier  showFieldNames />
                        <@show_array_values "dc:language" doc.dcLanguage  showFieldNames />
                        <@show_array_values "dc:publisher" doc.dcPublisher  showFieldNames />
                        <@show_array_values "dc:relation" doc.dcRelation  showFieldNames />
                        <@show_array_values "dc:rights" doc.dcRights  showFieldNames />
                        <@show_array_values "dc:source" doc.dcSource  showFieldNames />
                        <@show_array_values "dc:subject" doc.dcSubject  showFieldNames />
                        <@show_array_values "dc:title" doc.dcTitle  showFieldNames />
                        <@show_array_values "dc:type" doc.dcType  showFieldNames />
                    <#else>

                    <table width="100%" summary="This table contains the metadata for the object being viewed">
                        <caption>Object metadata</caption>
                        <tbody>
                     <#-- TITLE   -------------------------------------------------------------------------------->
                        <#assign titleArr = result.fullDoc.dcTitle + result.fullDoc.dcTermsAlternative />
                        <#if isNonEmpty(titleArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dc_title_t' />:</th>
                                <td><@simple_list_dual result.fullDoc.dcTitle result.fullDoc.dcTermsAlternative '<br />'/></td>
                            </tr>
                        </#if>

                      <#-- DC DATE, DC TERMS CREATED, DC TERMS ISSUED --------------------------------->
                        <#assign dateArr = result.fullDoc.dcDate + result.fullDoc.dcTermsCreated + result.fullDoc.dcTermsIssued /></td>
                        <#if isNonEmpty(dateArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dc_date_t' />:</th>
                                <td><@simple_list dateArr ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- DC CREATOR    -------------------------------------------------------------------------------->
                        <#assign creatorArr = model.fullDoc.dcCreator + model.fullDoc.dcContributor />
                        <#if isNonEmpty(creatorArr)>
                            <tr>
                                <th scope="row"><@spring.message 'Creator_t' />:</th>
                                <td><@simple_list_dual model.fullDoc.dcCreator model.fullDoc.dcContributor ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- DC DESCRIPTION -------------------------------------------------------------------------------->
                        <#assign descriptionArr = model.fullDoc.dcDescription />
                        <#if isNonEmpty(descriptionArr)>
                            <tr>
                                <th scope="row"><@spring.message 'Description_t' />:</th>
                                <td>
                                    <@simple_list_truncated descriptionArr "<br/>" "800"/>
                                    <#--<span id="opener"><@spring.message 'More_t' /></span>-->
                                    <#--<div class="dialog">-->
                                       <#--<@simple_list descriptionArr "<br/>"/> -->
                                    <#--</div>-->
                                </td>
                            </tr>
                        </#if>
                        <#-- LANGUAGE      -------------------------------------------------------------------------------->
                        <#assign languageArr = result.fullDoc.dcLanguage />
                        <#if isNonEmpty(languageArr)>
                            <tr>
                                <th scope="row"><@spring.message 'languageDropDownList_t' />:</th>
                                <td><@simple_list languageArr ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- DC FORMAT   -------------------------------------------------------------------------------->
                         <#if isNonEmpty(formatArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dc_format_t' />:</th>
                                <td><@simple_list formatArr ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- DC SOURCE     -------------------------------------------------------------------------------->
                        <#if isNonEmpty(sourceArr)>
                            <tr>
                                <td><@spring.message 'dc_source_t' />:</td>
                                <td><@simple_list sourceArr '<br/>'/></td>
                            </tr>
                        </#if>
                        <#-- DC RIGHTS     -------------------------------------------------------------------------------->
                        <#assign rightsArr = result.fullDoc.dcRights />
                        <#if isNonEmpty(rightsArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dc_rights_t' />:</th>
                                <td><@simple_list rightsArr ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- Europeana PROVIDER   -------------------------------------------------------------------------------->
                        <#if isNonEmpty(providerArr) >
                            <tr>
                                <th scope="row"><@spring.message 'Provider_t' />:</th>
                                <td>
                                <#if isNonEmpty(result.fullDoc.europeanaProvider) && isNonEmpty(result.fullDoc.europeanaCountry)>
                                    ${result.fullDoc.europeanaProvider[0]} ;&#160; ${result.fullDoc.europeanaCountry[0]}
                                <#elseif isNonEmpty(result.fullDoc.europeanaProvider)>
                                    ${result.fullDoc.europeanaProvider[0]}
                                <#elseif isNonEmpty(result.fullDoc.europeanaCountry)>
                                    ${result.fullDoc.europeanaCountry[0]}
                                </#if>
                                </td>
                            </tr>
                        </#if>


                    <#-- check if there is more content, if so show 'more' link and render content -->
                    <#--<#if isNonEmpty(moreArr) >-->
                    <#--<p id="morelink">-->
                        <#--<a-->
                            <#--href="#"-->
                            <#--class="fg-button ui-state-default fg-button-icon-left ui-corner-all"-->
                            <#--onclick="toggleObject('moremetadata');toggleObject('lesslink');toggleObject('morelink');return false;"-->
                            <#--alt="<@spring.message 'More_t' />"-->
                            <#--title="<@spring.message 'More_t' />"-->
                        <#-->-->
                            <#--<span class="ui-icon ui-icon-circle-plus"></span><@spring.message 'More_t' />-->
                        <#--</a>-->
                    <#--</p>-->

                    <#--<p id="lesslink" style="display:none;">-->
                        <#--<a-->
                            <#--href="#"-->
                            <#--class="fg-button ui-state-default fg-button-icon-left ui-corner-all"-->
                            <#--onclick="toggleObject('lesslink');toggleObject('morelink');toggleObject('moremetadata'); return false;"-->
                            <#--alt="<@spring.message 'Less_t' />"-->
                            <#--title="<@spring.message 'Less_t' />"-->
                        <#-->-->
                            <#--<span class="ui-icon ui-icon-circle-minus"></span><@spring.message 'Less_t' />-->
                        <#--</a>-->
                    <#--</p>-->
                    <#--<div class="clearfix"></div>-->
                    <#--<div id="moremetadata" style="display:none  ">-->
                     
                        <#-- DC IDENTIFIER   -------------------------------------------------------------------------->
                        <#if isNonEmpty(indentifierArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dc_identifier_t' />:</th>
                                <td><@simple_list indentifierArr ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- DC PUBLISHER------------------------------------------------------------------------------>
                         <#if isNonEmpty(publisherArr) >
                            <tr>
                                <th scope="row"><@spring.message 'dc_publisher_t' />:</th>
                                <td><@simple_list publisherArr ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- DC TERMS PROVENANCE----------------------------------------------------------------------->
                        <#if isNonEmpty(provenanceArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dcterms_provenance_t' />:</th>
                                <td><@simple_list provenanceArr ';&#160;'/></td>
                            </tr>
                        </#if>

                        <#-- SUBJECTS, TEMPORAL, SPATIAL  ----------------------------------------------------------------->
                        <#if isNonEmpty(arrsubj)>
                            <tr>
                                <th scope="row"><@spring.message 'Subject_t' />:</th>
                                <td><@simple_list arrsubj ';&#160;'/></td>
                            </tr>
                        </#if>
                        <#-- DC TYPE   -------------------------------------------------------------------------------->
                        <#if isNonEmpty(typeArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dc_type_t' />:</th>
                                <td><@simple_list typeArr ';&#160;'/></td>
                            </tr>
                        </#if>

                        <#-- DC RELATIONS------------------------------------------------------------------------------>
                        <#if isNonEmpty(relationsArr)>
                            <tr>
                                <th scope="row"><@spring.message 'dc_relation_t' />:</th>
                                <td><@simple_list relationsArr '<br/>'/></td>
                            </tr>
                        </#if>

                        <tr>
                            <th>&#160;</th>
                            <td>
                                <#assign UrlRef = "#"/>
                                <#if !result.fullDoc.europeanaIsShownAt[0]?matches(" ")>
                                    <#assign UrlRef = result.fullDoc.europeanaIsShownAt[0]/>
                                <#elseif !result.fullDoc.europeanaIsShownBy[0]?matches(" ")>
                                    <#assign UrlRef = result.fullDoc.europeanaIsShownBy[0]/>
                                </#if>
                                <a
                                    href="redirect.html?shownAt=${UrlRef?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"
                                    target="_blank"
                                    alt="<@spring.message 'ViewInOriginalContext_t' /> - <@spring.message 'OpensInNewWindow_t'/>"
                                    title="<@spring.message 'ViewInOriginalContext_t' /> - <@spring.message 'OpensInNewWindow_t'/>"
                                >
                                    <@spring.message 'ViewInOriginalContext_t' />
                                </a>
                            </td>
                        </tr>

                    </tbody>
                    </table>

                  </#if>
                </div>