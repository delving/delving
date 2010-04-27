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
<#-- page title and description -->
<#assign metaTitle = result.fullDoc.dcTitle[0]?xhtml />
<#assign metaDescription = result.fullDoc.dcDescription[0]?xhtml />
<#if metaDescription?length &lt; 10>
     <#assign metaDescription = result.fullDoc.dcTermsAlternative[0]?xhtml />
</#if>
<#if metaDescription?length &gt; 200>
   <#assign metaDescription = metaDescription?substring(0, 200) + "..."/>
</#if>

<#assign metaCanonicalUrl = uri />

<#assign useCache = "true">

<#include "inc_header.ftl">

<div id="wrapper">
    <table border="0" cellspacing="1" cellpadding="1" width="200px" summary="results - item detail">
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
           </tr>
           <tr>    
            <td>
                <div about="${result.fullDoc.id}" style="max-width:200px">
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
                        ${tl}
                    </h2>
                </div>
            </td>
           </tr>
           <tr>
           <table>
            <#assign max=3/><!-- max shown in list -->
            <#list result.relatedItems as doc>
            <#if doc_index &gt; 2><#break/></#if>
                <td width="70" valign="top">
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
                </td>
            </#list>
            </table>
        </tr>
    </table>
    </div>

</body>
</html>

</#compress>