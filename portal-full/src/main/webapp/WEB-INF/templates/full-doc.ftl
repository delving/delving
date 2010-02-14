<#import "spring.ftl" as spring />
<#assign model = result/>
<#assign result = result/>
<#assign uri = uri>
<#assign view = "table"/>
<#assign thisPage = "full-doc.html"/>
<#compress>
<#if startPage??><#assign startPage = startPage/></#if>
<#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
<#if format??><#assign format = format/></#if>
<#if pagination??>
    <#assign pagination = pagination/>
    <#assign queryStringForPaging = pagination.queryStringForPaging />
</#if>
<#if queryStringForPaging??>
    <#assign defaultQueryParams = "full-doc.html?"+queryStringForPaging+"&start="+pagination.docIdWindow.offset?c+"&uri="+result.fullDoc.id+"&view="+view />
<#else>
    <#assign defaultQueryParams = "full-doc.html?uri="+result.fullDoc.id />
</#if>
<#if result.fullDoc.dcTitle[0]?length &gt; 110>
    <#assign postTitle = result.fullDoc.dcTitle[0]?substring(0, 110)?url('utf-8') + "..."/>
<#else>
    <#assign postTitle = result.fullDoc.dcTitle[0]?url('utf-8')/>
</#if>
<#if result.fullDoc.dcCreator[0]?matches(" ")>
    <#assign postAuthor = "none"/>
<#else>
    <#assign postAuthor = result.fullDoc.dcCreator[0]/>
</#if>
<#-- Removed ?url('utf-8') from query assignment -->
<#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>
<#include "inc_header.ftl">
<#include "inc_search_form.ftl"/>

<div id="doc4" class="yui-t2">
<div id="hd">
    <#include "inc_top_nav.ftl"/>
</div>
<div id="bd">
<div id="yui-main">
<div class="yui-b">
<div class="yui-g" id="search">

    <@SearchForm "search_result"/>

</div>
<div class="yui-g" id="display">

<div id="breadcrumb">
    <#if query?exists>
    <ul>
        <li class="first"><@spring.message 'MatchesFor_t' />:</li>
        <li><strong><a href="#">${query?replace("%20"," ")?html}</a></strong></li>
    </ul>
    <#else> <ul>
        <li>&#160;</li>
    </ul>
    </#if>
</div>

<div id="navResultTabs">
    <ul>
        <li class="selected"><a><em><@spring.message 'ItemDetails_t'/></em></a></li>
        <li><a><em>&#160;</em></a></li>
        <li><a><em>&#160;</em></a></li>
        <li><a><em>&#160;</em></a></li>
        <li><a><em>&#160;</em></a></li>
    </ul>
</div>

<div class="pagination">
    <div class="viewselect">
        <#if pagination??>
            <#if pagination.returnToResults??>
                <a href="${pagination.returnToResults?html}">
                    <img src="images/arr-up.gif" hspace="5" width="7" height="9"
                         alt="click to return to results page"/>
                    <span><@spring.message 'ReturnToResults_t' /></span>
                </a>
            </#if>
        </#if>
    </div>
    <div class="nav full">
        <#if pagination??>
        <ul>
            <li>
                <#if pagination.previous>
                <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.previousInt?c}&amp;uri=${pagination.previousUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}">
                    <img src="images/arr-left.gif" alt="previous button" hspace="5" width="9" height="7" title="click here for previous item"/>
                </a>
                </#if>
            </li>
            <li>
                <#if pagination.next>
                <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.nextInt?c}&amp;uri=${pagination.nextUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}">
                    <img src="images/arr-right.gif" alt="next button" hspace="5" width="9" height="7" title="click here for next item"/>
                </a>
                </#if>
            </li>
        </ul>
        <#else>
        &#160;
        </#if>
    </div>

    <div class="printpage">
       <#-- <a href="javascript:" onclick="window.print();return false;"><img src="images/btn-print.gif" alt="<@spring.message 'AltPrint_t' />" vspace="4"/></a>-->
           <!-- AddThis Button BEGIN -->
           <!-- AddThis Button BEGIN -->
                    <#assign  showthislang = locale>
                    <#if  locale = "mt" || locale = "et">
                       <#assign  showthislang = "en">
                    </#if>
                       <a class="addthis_button"
                          href="http://www.addthis.com/bookmark.php?v=250&amp;username=xa-4b4f08de468caf36">
                         <img src="http://s7.addthis.com/static/btn/lg-share-${showthislang}.gif" alt="Bookmark and Share" style="border:0"/></a>
                        <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=xa-4b4f08de468caf36"></script>
                        <script type="text/javascript">
                            var addthis_config = {
                                 ui_language: "${showthislang}"
                            }
                          </script>
           <#--<a class="addthis_button" href="http://www.addthis.com/bookmark.php?v=250&amp;username=xa-4b4f08de468caf36"><img src="http://s7.addthis.com/static/btn/sm-share-en.gif" width="83" height="16" alt="Bookmark and Share" style="border:0"/></a><script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=xa-4b4f08de468caf36"></script>-->
           <!-- AddThis Button END -->
    </div>
</div>

<div id="wrapper">
    <table id="multi" border="0" cellspacing="10" cellpadding="10" summary="results - item detail">
        <tr>
            <td>
                <div style="width:200px; max-height: 400px; overflow-x:hidden; overflow-y:hidden; scrolling: none;  text-align:center; ">
                    <#assign imageRef = "#"/>
                    <#if !result.fullDoc.europeanaIsShownBy[0]?matches(" ")>
                        <#assign imageRef = result.fullDoc.europeanaIsShownBy[0]/>
                    <#elseif !result.fullDoc.europeanaIsShownAt[0]?matches(" ")>
                        <#assign imageRef = result.fullDoc.europeanaIsShownAt[0]/>
                    </#if>
                       <a about=${result.fullDoc.id} rel="rdfs:seeAlso" resource="${imageRef}" href="redirect.html?shownBy=${imageRef?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}" target="_blank">
                        <#if useCache="true">
                        <img src="${cacheUrl}uri=${result.fullDoc.thumbnails[0]?url('utf-8')}&amp;size=FULL_DOC&amp;type=${result.fullDoc.europeanaType}"
                         class="full" alt="Image title: ${result.fullDoc.dcTitle[0]}" />
                        <#else>
                        <script>
                            function checkSize(h){
                                if (h > 300) {
                                    h = 200;
                                    document.getElementById("imgview").height=h;
                                }
                            }
                        </script>
                        <img src="${result.fullDoc.thumbnail[0]}" alt="Image title: ${result.fullDoc.dcTitle[0]}" id="imgview" onload="checkSize(this.height);" onerror="showDefault(this,'${result.fullDoc.europeanaType}','full')"/>
                        </#if>
                    </a>

                </div>
               </td>
            <td>
                <div about="${result.fullDoc.id}" id="item-detail">
                    <h2 class="${result.fullDoc.europeanaType}">
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
                        <@stringLimiter "${tl}" "250"/>
                    </h2>
                    <#assign doc = result.fullDoc />
                    <#--<meta about="${doc.id}" property="dcterms:date" content="${doc.dcDate}"/>-->
                    <#assign siwaCounter = 0 />
                    <#if format?? && format?contains("labels")>
                        <#assign showFieldNames = true />
                        <@listMetadataFields showFieldNames/>
                     <p id="morelink">
                        <a href="#"  class="fg-green" onclick="toggleObject('moremetadata');toggleObject('lesslink');toggleObject('morelink')"><@spring.message 'More_t' /></a>
                    </p>

                    <p id="lesslink" style="display:none;">
                        <a href="#" class="fg-green" onclick="toggleObject('lesslink');toggleObject('morelink');toggleObject('moremetadata')"><@spring.message 'Less_t' /></a>
                    </p>
                    <#else>
                    <@listMetadataFields false />
                     <#-- TITLE   -------------------------------------------------------------------------------->
                    <#assign titleArr = result.fullDoc.dcTitle + result.fullDoc.dcTermsAlternative />
                     <#if isNonEmpty(titleArr)>
                        <p><strong><@spring.message 'dc_title_t' />:</strong>
                            <@simple_list_dual result.fullDoc.dcTitle result.fullDoc.dcTermsAlternative '<br />'/>
                        </p>
                    </#if>
                      <#-- DC DATE, DC TERMS CREATED, DC TERMS ISSUED --------------------------------->
                    <#assign dateArr = result.fullDoc.dcDate + result.fullDoc.dcTermsCreated + result.fullDoc.dcTermsIssued />
                        <#if isNonEmpty(dateArr)>
                            <p><strong><@spring.message 'dc_date_t' />:</strong>
                                <@simple_list dateArr ';&#160;'/>
                            </p>
                        </#if>
                    <#-- DC CREATOR    -------------------------------------------------------------------------------->
                    <#assign creatorArr = model.fullDoc.dcCreator + model.fullDoc.dcContributor />
                    <#if isNonEmpty(creatorArr)>
                        <p><strong><@spring.message 'Creator_t' />:</strong>
                            <@simple_list_dual model.fullDoc.dcCreator model.fullDoc.dcContributor ';&#160;'/>
                        </p>
                    </#if>
                    <#-- DC DESCRIPTION -------------------------------------------------------------------------------->
                    <#assign descriptionArr = model.fullDoc.dcDescription />
                    <#if isNonEmpty(descriptionArr)>
                        <p><strong><@spring.message 'Description_t' />:</strong>
                            <@simple_list_truncated descriptionArr "<br/>" "800"/>
                        </p>
                    </#if>
                    <#-- LANGUAGE      -------------------------------------------------------------------------------->
                    <#assign languageArr = result.fullDoc.dcLanguage />
                    <#if isNonEmpty(languageArr)>
                        <p><strong><@spring.message 'languageDropDownList_t' />:</strong>
                            <@simple_list languageArr ';&#160;'/>
                        </p>
                    </#if>
                    <#-- DC FORMAT   -------------------------------------------------------------------------------->
                    <#assign formatArr = result.fullDoc.dcFormat + result.fullDoc.dcTermsExtent + result.fullDoc.dcTermsMedium />
                     <#if isNonEmpty(formatArr)>
                        <p><strong><@spring.message 'dc_format_t' />:</strong>
                            <@simple_list formatArr ';&#160;'/>
                        </p>
                    </#if>
                    <#-- DC SOURCE     -------------------------------------------------------------------------------->
                    <#assign sourceArr = result.fullDoc.dcSource />
                    <#if isNonEmpty(sourceArr)>
                        <p><strong><@spring.message 'dc_source_t' />:</strong>
                            <@simple_list sourceArr '<br/>'/>
                        </p>
                    </#if>
                    <#-- DC RIGHTS     -------------------------------------------------------------------------------->
                    <#assign rightsArr = result.fullDoc.dcRights />
                    <#if isNonEmpty(rightsArr)>
                        <p><strong><@spring.message 'dc_rights_t' />:</strong>
                            <@simple_list rightsArr ';&#160;'/>
                        </p>
                    </#if>
                    <#-- Europeana PROVIDER   -------------------------------------------------------------------------------->
                    <#assign providerArr = result.fullDoc.europeanaProvider + result.fullDoc.europeanaCountry />
                    <#if isNonEmpty(providerArr) >
                        <p><strong><@spring.message 'Provider_t' />:</strong>
                            <#if isNonEmpty(result.fullDoc.europeanaProvider) && isNonEmpty(result.fullDoc.europeanaCountry)>
                                ${result.fullDoc.europeanaProvider[0]} ;&#160; ${result.fullDoc.europeanaCountry[0]}
                            <#elseif isNonEmpty(result.fullDoc.europeanaProvider)>
                                ${result.fullDoc.europeanaProvider[0]}
                            <#elseif isNonEmpty(result.fullDoc.europeanaCountry)>
                                ${result.fullDoc.europeanaCountry[0]}
                            </#if>
                        </p>
                    </#if>
                    <p id="morelink" class="fg-green">
                        <a href="#" onclick="toggleObject('moremetadata');toggleObject('lesslink');toggleObject('morelink')"><@spring.message 'More_t' /></a>
                    </p>

                    <p id="lesslink" class="fg-green" style="display:none;">
                        <a href="#" onclick="toggleObject('lesslink');toggleObject('morelink');toggleObject('moremetadata')"><@spring.message 'Less_t' /></a>
                    </p>

                    <div id="moremetadata" style="display:none  ">
                        <#-- DC IDENTIFIER   -------------------------------------------------------------------------->
                        <#assign indentifierArr = result.fullDoc.dcIdentifier />
                        <#if isNonEmpty(indentifierArr)>
                            <p><strong><@spring.message 'dc_identifier_t' />:</strong>
                            <@simple_list indentifierArr ';&#160;'/>
                            </p>
                        </#if>
                        <#-- DC PUBLISHER------------------------------------------------------------------------------>
                        <#assign publisherArr = result.fullDoc.dcPublisher />
                         <#if isNonEmpty(publisherArr) >
                            <p><strong><@spring.message 'dc_publisher_t' />:</strong>
                            <@simple_list publisherArr ';&#160;'/>
                            </p>
                        </#if>
                        <#-- DC TERMS PROVENANCE----------------------------------------------------------------------->
                        <#assign provenanceArr = result.fullDoc.dcTermsProvenance />
                        <#if isNonEmpty(provenanceArr)>
                            <p><strong><@spring.message 'dcterms_provenance_t' />:</strong>
                             <@simple_list provenanceArr ';&#160;'/>
                            </p>
                        </#if>

                        <#-- SUBJECTS, TEMPORAL, SPATIAL  ----------------------------------------------------------------->
                        <#assign arrsubj = model.fullDoc.dcSubject + result.fullDoc.dcTermsTemporal + result.fullDoc.dcTermsSpatial + result.fullDoc.dcCoverage >
                        <#if isNonEmpty(arrsubj) >
                        <p>
                            <strong><@spring.message 'Subject_t' />:</strong>
                            <@simple_list arrsubj ';&#160;'/>
                        </p>
                        </#if>
                        <#-- DC TYPE   -------------------------------------------------------------------------------->
                        <#assign typeArr = result.fullDoc.dcType />
                        <#if isNonEmpty(typeArr)>
                            <p><strong><@spring.message 'dc_type_t' />:</strong>
                            <@simple_list typeArr ';&#160;'/>
                            </p>
                        </#if>

                        <#-- DC RELATIONS------------------------------------------------------------------------------>
                        <#assign relationsArr = result.fullDoc.dcRelation + result.fullDoc.dcTermsReferences + result.fullDoc.dcTermsIsReferencedBy
                                    + result.fullDoc.dcTermsIsReplacedBy + result.fullDoc.dcTermsIsRequiredBy + result.fullDoc.dcTermsIsPartOf + result.fullDoc.dcTermsHasPart
                                    + result.fullDoc.dcTermsReplaces + result.fullDoc.dcTermsRequires + result.fullDoc.dcTermsIsVersionOf + result.fullDoc.dcTermsHasVersion
                                    + result.fullDoc.dcTermsConformsTo + result.fullDoc.dcTermsHasFormat />
                        <#if isNonEmpty(relationsArr)>
                            <p><strong><@spring.message 'dc_relation_t' />:</strong>
                                <@simple_list relationsArr '<br/>'/>
                            </p>
                        </#if>

                    </div>
                    </#if>
                    <p class="view-orig-green">
                        <#assign UrlRef = "#"/>
                        <#if !result.fullDoc.europeanaIsShownAt[0]?matches(" ")>
                            <#assign UrlRef = result.fullDoc.europeanaIsShownAt[0]/>
                        <#elseif !result.fullDoc.europeanaIsShownBy[0]?matches(" ")>
                            <#assign UrlRef = result.fullDoc.europeanaIsShownBy[0]/>
                        </#if>
                        <a rel="rdfs:seeAlso" resource="${UrlRef}" href="redirect.html?shownAt=${UrlRef?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"
                                                  target="_blank"><@spring.message 'ViewInOriginalContext_t' /></a>
                        <@spring.message 'OpensInNewWindow_t'/>
                    </p>
                </div>
            </td>
        </tr>
    </table>
    </div>
    </div>
    </div>
</div>

<div class="yui-b">
    <center>
        <#include "inc_logo_sidebar.ftl"/>
    </center>
<div id="leftOptions" class="full">



<h3><@spring.message 'RelatedContent_t' />:</h3>

    <div class="toggler-c toggler-c-opened" title="Items">

        <table summary="related items" id="tbl-related-items" width="100%">
            <#assign max=3/><!-- max shown in list -->
            <#list result.relatedItems as doc>
            <#if doc_index &gt; 2><#break/></#if>
            <tr>
                <td width="45" valign="top">
                    <div class="related-thumb-container">
                        <#if queryStringForPaging??>
                            <a rel="rdfs:seeAlso" resource="${doc.id}" href="full-doc.html?${queryStringForPaging?html}&amp;start=${doc.index?c}&amp;uri=${doc.id}&amp;view=${view}&amp;startPage=1&amp;pageId=brd&amp;tab=">
                         <#else>
                            <a rel="rdfs:seeAlso" resource="${doc.id}" href="full-doc.html?uri=${doc.id}">
                         </#if>
                         <#if useCache="true">
                            <img src="${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${doc.type}&amp;view=${view}" alt="Click here to view related item" width="40"/>
                        <#else>
                            <img src="${doc.thumbnail}" alt="Click here to view related item" width="40" onerror="showDefault(this,'${doc.type}','full')"/>
                        </#if>

                            </a>
                    </div>
                </td>

                <td class="item-titles" valign="top" width="130">
                    <#if queryStringForPaging??>
                    <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${doc.index?c}&amp;uri=${doc.id}&amp;startPage=1&amp;pageId=brd"><@stringLimiter "${doc.title}" "50"/></a>
                    <#else>
                    <a href="full-doc.html?uri=${doc.id}"><@stringLimiter "${doc.title}" "50"/></a>
                    </#if>
                </td>
            </tr>

            </#list>
            <#if result.relatedItems?size &gt; max>
            <tr>
                <td>&#160;</td>
                <td align="right" id="see-all"><a href='brief-doc.html?query=europeana_uri:"${uri}"&amp;view=${view}'><@spring.message 'SeeAllRelatedItems_t' /></a></td>
            </tr>
            </#if>
        </table>
    </div>

    <#-- todo: ReImplement this after good solution wrt managing content of UserTags is found -->
    <#--<div class="toggler-c"title="<@spring.message 'UserTags_t' />">-->
        <#--<p>-->
            <#--<#list model.fullDoc.europeanaUserTag as userTag>-->
            <#--<a href="brief-doc.html?query=europeana_userTag:${userTag}&view=${view}">${userTag}</a><br/>-->
            <#--</#list>-->
        <#--</p>-->
    <#--</div>-->

    <h3><@spring.message 'Actions_t' />:</h3>
    <#if user??>
    <div class="toggler-c" title="<@spring.message 'AddATag_t' />">
        <#--<div id="ysearchautocomplete">-->
              <form action="#" method="post" onsubmit="addTag(document.getElementById('tag').value,'${result.fullDoc.id}','${result.fullDoc.thumbnails[0]}','${postTitle}','${result.fullDoc.europeanaType}'); return false;"  id="form-addtag" name="form-addtag" accept-charset="UTF-8">
                <input type="text" name="tag" id="tag" maxlength="50" class="text"/>
                <input type="submit" class="button" value="Add"/>
            </form>
            <div id="msg-save-tag" class="hide fg-green"></div>
        <#--</div>-->
   </div>

    <#-- todo: remove later. functionality replaced by addThis/shareThis badge -->
    <#--<div class="toggler-c" title="<@spring.message 'ShareWithAFriend_t' />">-->

        <#--<form action="#" method="post" onsubmit='sendEmail("${result.fullDoc.id}"); return false;' id="form-sendtoafriend" accept-charset="UTF-8">-->
            <#--<label for="friendEmail"></label>-->
            <#--<input type="text" name="friendEmail" class="required email text" id="friendEmail" maxlength="50" value="<@spring.message 'EmailAddress_t' />"-->
                   <#--onfocus="this.value=''"/>-->
            <#--<input type="submit" id="mailer" class="button" value="<@spring.message 'Send_t' />"/>-->

        <#--</form>-->
        <#--<span id="msg-send-email" style="display:block" class="fg-green"></span>-->
    <#--</div>-->

    <div class="related-links">
        <p class="linetop">
            <a id="saveToMyEuropeana" href="#" onclick="saveItem('${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.thumbnails[0]?js_string}','${result.fullDoc.europeanaType}');"><@spring.message 'SaveToMyEuropeana_t' /></a>
            <span id="msg-save-item" class="msg-hide fg-green" style="font-size: 105%;"></span>
        </p>
    </div>

<#else>
    <div class="related-links">
    <p class="linetop">
        <a  href="#" class="disabled" onclick="highLight('mustlogin');"><@spring.message 'AddATag_t' /></a>
    </p>
<#--    <p>
        <a  href="#" class="disabled" onclick="highLight('mustlogin');"><@spring.message 'ShareWithAFriend_t' /></a>
    </p>-->
    <p>
        <a  href="#" class="disabled" onclick="highLight('mustlogin');"><@spring.message 'SaveToMyEuropeana_t' /></a>
    </p>
    <div  id="mustlogin" class="msg">
        <a href="login.html?pageId=fd"><u><@spring.message 'LogIn_t'/></u></a> | <a href="login.html?pageId=fd"><u><@spring.message 'Register_t'/></u></a>
    </div>
    </div>

</#if>

</div>
</div>

<#if RequestParameters.siwa?? && RequestParameters.siwa?matches("true")>
    <#-- SIWA Europeana Connect Webservices badges -->
    <script type="text/javascript" src="http://81.204.248.129/ec/portal/siwa.js"></script>
    <script type="text/javascript">initServices(this.document);</script>
</#if>
</div>
<div id="ft">
    <#include "inc_footer.ftl"/>
</div>
</div>
</body>
</html>

<#macro show_array_values fieldName values showFieldName>
    <#list values as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#assign siwaCounter = siwaCounter + 1 />
            <#if showFieldName>
                <p><strong>${fieldName}</strong> = <span <@rdfaProperty fieldName/> id="${siwaCounter}" name="${fieldName}" class="siwa">${value?html}</span></p>
            <#else>
                <#if !fieldName?starts_with("europeana:")>
                    <meta about="${doc.id}" property="${fieldName?replace("dc:","dcterms:")}" content="${value}"/>
                </#if>
            </#if>
        </#if>
    </#list>
</#macro>

<#macro show_value fieldName value showFieldName>
    <#assign siwaCounter = siwaCounter + 1 />
    <#if showFieldName>
        <p><strong>${fieldName}</strong> = <span <@rdfaProperty fieldName/> id="${siwaCounter}" name="${fieldName}" class="siwa">${value}</span></p>
    <#else>
        <#if !fieldName?starts_with("europeana:")>
            <meta about="${doc.id}" property="${fieldName?replace("dc:","dcterms:")}" content="${value}"/>
        </#if>
    </#if>
</#macro>

<#macro simple_list values separator>
    <#list values?sort as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#assign siwaCounter = siwaCounter + 1 />
            ${value}<#if value_has_next>${separator} </#if>
    <#--${value}${separator}-->
        </#if>
    </#list>
</#macro>

<#macro simple_list_dual values1 values2 separator>
    <#assign siwaCounter = siwaCounter + 1 />
    <#if isNonEmpty(values1) && isNonEmpty(values2)>
        <@simple_list values1 separator />${separator} <@simple_list values2 separator />
    <#elseif isNonEmpty(values1)>
        <@simple_list values1 separator />
    <#elseif isNonEmpty(values2)>
        <@simple_list values2 separator />
    </#if>
</#macro>

<#macro simple_list_truncated values separator trunk_length>
    <#list values?sort as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#assign siwaCounter = siwaCounter + 1 />
            <@stringLimiter "${value}" "${trunk_length}"/><#if value_has_next>${separator} </#if>
    <#--${value}${separator}-->
        </#if>
    </#list>
</#macro>

<#macro rdfaProperty fieldName >
    <#if !fieldName?starts_with("europeana")> property="${fieldName?replace("dc:","dcterms:")}" </#if>
</#macro>

<#macro listMetadataFields showFieldNames>
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
</#macro>

    <#function isNonEmpty values>
    <#assign nonEmptyValue = false />
    <#list  values?reverse as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#assign nonEmptyValue = true />
            <#return nonEmptyValue />
        </#if>
    </#list>
    <#return nonEmptyValue />
</#function>
</#compress>