<#compress>

<#if seq?size &gt; 0>
    <#if view = "table">
        <@show_result_table seq = seq/>
    <#elseif view = "flow">
        <@show_result_flow seq = seq/>
    <#else>
        <@show_result_list seq = seq/>
    </#if>

<#else>
    <div id="no-result"><@spring.message 'NoItemsFound_t' /></div>
</#if>


<#macro show_result_list seq>
<table cellspacing="1" cellpadding="0" width="100%" border="0" summary="search results" id="multi">
    <#list seq as cell>
    <tr>
        <td valign="top" width="50">
            <div class="brief-thumb-container-listview">
                <a href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true">
                        <img class="thumb"
                             id="thumb_${cell.index()}"
                             align="middle"
                             src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}"
                             alt="<@spring.message '_action.alt.more.info' />"
                             height="50"
                          />
                    <#else>
                        <img class="thumb"
                             id="thumb_${cell.index()?c}"
                             align="middle"
                             src="${cell.thumbnail}"
                             alt="Click for more information"
                             height="50"
                             onerror="showDefaultSmall(this,'${cell.type}')"
                         />
                    </#if>
                </a>
            </div>
        </td>
        <td class="${cell.type} ">
                <h6>
                    <a class="fg-gray" href="${cell.fullDocUrl()}?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index()?c}&amp;startPage=${pagination.start?c}&amp;view=${view}&amp;pageId=brd">
                        <@stringLimiter "${cell.title}" "100"/></a>
                </h6>
                <p>
                <#-- without labels -->
                <#--
                <#if !cell.creator[0]?matches(" ")>${cell.creator}<br/></#if>
                <#if !cell.year?matches(" ")><#if cell.year != "0000">${cell.year}<br/></#if></#if>
                <#if !cell.provider?matches(" ")>${cell.provider}</#if>
                --->
                <#-- with labels -->
                <#if !cell.creator[0]?matches(" ")><span><@spring.message '_search.field.creator' />: </span>${cell.creator}<br/></#if>
                <#if !cell.year?matches(" ")><#if cell.year != "0000"><span><@spring.message '_search.field.date' />: </span>${cell.year}<br/></#if></#if>
                <#if !cell.provider?matches(" ")><@spring.message 'Provider_t' />: <span class="provider">${cell.provider}</span></#if>
                </p>
        </td>
    </tr>
    </#list>
</table>
</#macro>

<#macro show_result_flow seq>
<div id="imageflow">
    <noscript>
        <div class="attention"><@spring.message 'NoScript_t' /></div>
    </noscript>
    <div id="loading">
        <b>Loading images</b><br/>
        <img src="/${portalName}/${portalTheme}/images/loading.gif" width="208" height="13" alt="loading"/>
    </div>
    <div id="images">
        <#list seq as briefDoc>
        <img class="flow" src="${cacheUrl}type=${briefDoc.type}&amp;uri=${briefDoc.thumbnail}" longdesc="${briefDoc.title}" alt="${briefDoc.id}"/>
        </#list>
    </div>
    <div id="captions"></div>
    <div id="scrollbar">
        <div id="slider" style="left: 0px;"></div>
    </div>
    <div id="numinfo">Number of objects found: <strong><span id="numFound">${pagination.numFound}&nbsp;</span></strong>
        <br/>
        results ${pagination.start} - ${pagination.lastViewableRecord} of ${pagination.numFound} <br/>
    </div>
    <div id="objectPageing">
    </div>
</div>
</#macro>

</#compress>
