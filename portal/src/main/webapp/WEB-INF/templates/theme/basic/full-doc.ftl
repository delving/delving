<#compress>
<#include "includeMarcos.ftl">
<@addCustomAssigns/>
<@addHeader "Norvegiana", "",["results.js","fancybox/jquery.fancybox-1.3.1.pack.js"],["fancybox/jquery.fancybox-1.3.1.css"]/>
<script type="text/javascript">
    var msgItemSaveSuccess = "<@spring.message 'ItemSaved_t'/>";
    var msgItemSaveFail = "<@spring.message 'ItemSavedFailed_t'/>";
</script>
<section id="sidebar" class="grid_3" role="complementary">
    <header id="branding" role="banner">
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="${portalDisplayName}"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>
    
    <div id="facet-list">
       <h5><@spring.message 'RelatedContent_t' />:</h5>
        <table summary="related items" id="tbl-related-items" width="100%">
            <#assign max=3/><!-- max shown in list -->
            <#list result.relatedItems as doc>
                <#if doc_index &gt; 2><#break/></#if>
                <tr>
                    <td width="45" valign="top">
                        <div class="related-thumb-container">
                            <#if queryStringForPaging??>
                            <a href='${doc.fullDocUrl()}?query=europeana_uri:"${doc.id?url('utf-8')}"&amp;start=${doc.index()?c}&amp;view=${view}&amp;startPage=1&amp;pageId=brd&amp;tab='>
                                <#else>
                                <a href="${doc.fullDocUrl()}">
                            </#if>
                            <#if useCache="true">
                                <img src="${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${doc.type}&amp;view=${view}" alt="Click here to view related item" width="40"/>
                                <#else>
                                    <img src="${doc.thumbnail}" alt="Click here to view related item" width="40" onerror="showDefault(this,'${doc.type}')"/>
                            </#if>

                        </a>
                        </div>
                    </td>

                    <td class="item-titles" valign="top" width="130">
                        <#if queryStringForPaging??>
                            <a href='${doc.fullDocUrl()}?query=europeana_uri:"${doc.id?url('utf-8')}"&amp;start=${doc.index()?c}&amp;startPage=1&amp;pageId=brd'><@stringLimiter "${doc.title}" "50"/></a>
                            <#else>
                                <a href="${doc.fullDocUrl()}"><@stringLimiter "${doc.title}" "50"/></a>
                        </#if>
                    </td>
                </tr>

            </#list>
            <#if result.relatedItems?size &gt; max>
                <tr>
                    <td id="see-all" colspan="2"><a href='/${portalName}/brief-doc.html?query=europeana_uri:"${uri}"&amp;view=${view}'><@spring.message 'SeeAllRelatedItems_t' /></a></td>
                </tr>
            </#if>
        </table>

         <h5><@spring.message 'Actions_t' />:</h5>

        <#if addThisTrackingCode??>
            <p>
                <@addThis "${addThisTrackingCode}"/>
            </p>
        </#if>

        <#if user??>
            <p>
            <a href="inc_related_content.ftl#" onclick="saveItem('SavedItem','${postTitle?js_string}','${postAuthor?js_string}','${result.fullDoc.id?js_string}','${result.fullDoc.thumbnails[0]?js_string}','${result.fullDoc.europeanaType}');return false;"><@spring.message 'SaveToPersonalPage' /></a>

            <div id="msg-save-item" class="msg-hide"></div>
            </p>

            <#if result.fullDoc.europeanaType == "IMAGE">
                <#if result.fullDoc.europeanaIsShownBy[0]?? && imageAnnotationToolBaseUrl?? && imageAnnotationToolBaseUrl!="">
                <p>
                    <a href="${imageAnnotationToolBaseUrl}?user=${user.userName}&objectURL=${result.fullDoc.europeanaIsShownBy[0]}&id=${result.fullDoc.id}" target="_blank"><@spring.message 'AddAnnotation_t' /></a>
                </p>
                </#if>
            </#if>

            <p>
            <h6><@spring.message 'AddATag_t' /></h6>

        <#--<div id="ysearchautocomplete">-->
            <form action="inc_related_content.ftl#" method="post" onsubmit="addTag('SocialTag', document.getElementById('tag').value,'${result.fullDoc.id}','${result.fullDoc.thumbnails[0]?js_string}','${postTitle}','${result.fullDoc.europeanaType}'); return false;" id="form-addtag" name="form-addtag" accept-charset="UTF-8">
                <input type="text" name="tag" id="tag" maxlength="50" class="text"/>
                <input type="submit" class="button" value="Add"/>
            </form>
            <div id="msg-save-tag" class="hide"></div>
        </p>
        <#--</div>-->


            <#else>
                <div class="related-links">
                    <p>
                        <a href="/${portalName}/login.html" class="disabled" onclick="highLight('#mustlogin'); return false;"><@spring.message 'AddATag_t' /></a>
                    </p>

                    <p>
                        <a href="/${portalName}/login.html" class="disabled" onclick="highLight('#mustlogin'); return false;"><@spring.message 'SaveToPersonalPage' /></a>
                    </p>
                </div>

        </#if>
    </div>

</section>


<section id="item" class="grid_9" role="main">

    <div id="userBar" role="navigation">
        <div class="inner">
        <@userBar/>
        </div>
    </div>

    <div class="clear"></div>

    <div id="search" role="search">
        <div class="inner">
            <@simpleSearch/>
        </div>
    </div>

    <div class="clear"></div>

     <div id="nav_query_breadcrumbs">
        <div class="inner">
            <h4><@resultsFullQueryBreadcrumbs/></h4>
        </div>
    </div>

    <div class="clear"></div>

    <nav class="pagination" role="navigation">
        <div class="inner">
        <@resultFullPagination/>


        </div>
    </nav>

    <div class="clear"></div>

    <div class="inner">

        <div id="itemImage" class="grid_4 alpha">
            <div class="inner">
            <@resultFullImage/>
            </div>
        </div>

        <div id="itemMetaData" class="grid_5 omega">
            <@resultFullList/>
        </div>

    </div>

</section>

<@addFooter/>
</#compress>

<#macro addCustomAssigns>
    <#assign result = result/>
    <#assign uri = result.fullDoc.id/>
    <#if format??><#assign format = format/></#if>
    <#if pagination??>
        <#assign pagination = pagination/>
        <#assign queryStringForPaging = pagination.queryStringForPaging />
    </#if>
    <#assign dcTitle = result.fullDoc.getFieldValue("dc_title")/>
    <#if !dcTitle.isNotEmpty()>
        <#if dcTitle.getFirst()?length &gt; 110>
            <#assign postTitle = dcTitle.getFirst()?substring(0, 110)?url('utf-8') + "..."/>
        <#else>
            <#assign postTitle = dcTitle.getFirst()?url('utf-8')/>
        </#if>
    </#if>
    <#assign dcCreator = result.fullDoc.getFieldValue("dc_creator")/>
    <#if !dcCreator.isNotEmpty()>
        <#assign postAuthor = "none"/>
    <#else>
        <#assign postAuthor = dcCreator.getFirst()/>
    </#if>
    <#-- Removed ?url('utf-8') from query assignment -->
    <#if RequestParameters.query??><#assign query = "${RequestParameters.query}"/></#if>
</#macro>