<#compress>
<#if user??><#if user.role == "ROLE_EDITOR">
<script type="text/javascript">
    // change to j
    var curButton = "";
    var handleSuccessEP = function(o, oId) {
        document.getElementById(curButton).style.backgroundColor = '#339900';
        document.getElementById(curButton).style.color = '#FFFFFF';
    };
    var handleFailureEP = function(o) {
        document.getElementById(curButton).style.backgroundColor = '#990000';
        document.getElementById(curButton).style.color = '#FFFFFF';
    };
    var callbackEP =
    {
        success:handleSuccessEP,
        failure: handleFailureEP
    };
</script>
</#if></#if>
<div id="breadcrumb">
    <ul>
        <#if !query?starts_with("europeana_uri:")>
        <li class="first"><@spring.message 'MatchesFor_t' />:</li>
        <#list breadcrumbs as crumb><#if !crumb.last>
        <li><a href="${thisPage}?${crumb.href}">${crumb.display?html}</a>&#160;>&#160;</li>
        <#else>
        <li><strong>${crumb.display?html}</strong></li>
        </#if></#list>
        <#else>
        <li class="first">

            <@spring.message 'ViewingRelatedItems_t' />
            <#assign match = result.matchDoc/>
            <a href="full-doc.html?&amp;uri=${match.id}">
                <#if useCache="true"><img src="${cacheUrl}uri=${match.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${match.type}" alt="${match.title}" height="25"/>
                <#else><img src="${match.thumbnail}" alt="${match.title}" height="25"/>
                </#if>
            </a>

        </li>
        </#if>
    </ul>

</div>
<#assign tab = ""/><#assign showAll = ""/><#assign showText = ""/><#assign showImage = ""/><#assign showVideo = ""/><#assign showSound = ""/><#assign showText = ""/>
<#if RequestParameters.tab?exists>
<#assign tab = RequestParameters.tab/>
<#switch RequestParameters.tab>
<#case "text"><#assign showText = "selected"/><#break/>
<#case "image"><#assign showImage = "selected"/><#break/>
<#case "video"><#assign showVideo = "selected"/><#break/>
<#case "sound"><#assign showSound = "selected"/><#break/>
<#default> <#assign showAll = "selected"/><#break/>
</#switch>
<#else>
<#assign showAll = "selected"/>
</#if>

<div id="navResultTabs">
    <ul>
        <li class="${showAll}">
            <a href="${thisPage}?${typeUrl}&amp;view=${view}"><em><@spring.message 'All_t' /> </em></a></li>
        <li class="${showText}">
            <a href="${thisPage}?${typeUrl}&amp;qf=TYPE:text&amp;tab=text&amp;view=${view}"><em><@spring.message 'Texts_t' />
                <@print_tab_count showAll showText textCount />
                </em></a></li>
        <li class="${showImage}">
            <a href="${thisPage}?${typeUrl}&amp;qf=TYPE:image&amp;tab=image&amp;view=${view}"><em><@spring.message 'Images_t' />
                <@print_tab_count showAll showImage imageCount />
            </em></a></li>
        <li class="${showVideo}">
            <a href="${thisPage}?${typeUrl}&amp;qf=TYPE:video&amp;tab=video&amp;view=${view}"><em><@spring.message 'Videos_t' />
                <@print_tab_count showAll showVideo videoCount />
            </em></a></li>
        <li class="${showSound}">
            <a href="${thisPage}?${typeUrl}&amp;qf=TYPE:sound&amp;tab=sound&amp;view=${view}"><em><@spring.message 'Sounds_t' />
                <@print_tab_count showAll showSound audioCount />
            </em></a></li>
    </ul>
</div>

    <#if seq?size &gt; 0>
    <@resultnav place="top"/>
        <#if view = "table">
        <@show_result_table seq = seq/>
            <#elseif view = "flow">
            <@show_result_flow seq = seq/>
            <#else>
            <@show_result_list seq = seq/>
        </#if>
    <@resultnav place="bot"/>
        <#else>
        <br><br><br>
        <#-- todo match ui-widget ui-info to fall within result panel -->
        <div id="no-result" class="ui-widget ui-info" style="width: 720px"><@spring.message 'NoItemsFound_t' /></div>
    </#if>

<#macro resultnav place>
<div class="pagination ${place}">
    <div class="viewselect">
        <#if queryStringForPresentation?exists>
        <#if view="table">
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table"><img src="images/btn-multiview-hi.gif" alt="<@spring.message 'AltTableView_t' />" hspace="5"/></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list"><img src="images/btn-listview-lo.gif" alt="<@spring.message 'AltListView_t' />" hspace="5"/></a>

        <#else>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=table"><img src="images/btn-multiview-lo.gif" alt="<@spring.message 'AltTableView_t' />" hspace="5"/></a>
        <a href="${thisPage}?${queryStringForPresentation?html}&amp;view=list"><img src="images/btn-listview-hi.gif" alt="<@spring.message 'AltListView_t' />" hspace="5"/></a>

        </#if>
        </#if>
    </div>
    <div class="nav brief">
        <ul>
            <li class="first"><@spring.message 'Results_t' /> ${pagination.getStart()}
                - ${pagination.getLastViewableRecord()} <@spring.message 'Of_t' /> ${pagination.getNumFound()}</li>
            <li class="strong"><@spring.message 'Page_t' />:</li>
            <#list pagination.pageLinks as link>
            <#if link.linked>
            <#assign lstart = link.start/>

            <li><a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}">${link.display?c}</a></li>
            <#else>
            <li><a href=""><strong>${link.display?c}</strong></a></li>
            </#if>
            </#list>
            <#if pagination.previous>
            <li>
                <a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}"><img src="images/arr-left.gif" hspace="5" width="9" height="7" alt="<@spring.message 'AltPreviousPage_t' />"/></a>
            </li>
            </#if>
            <#if pagination.next>
            <li>
                <a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}"><img src="images/arr-right.gif" hspace="5" width="9" height="7" alt="<@spring.message 'AltNextPage_t' />"/></a>
            </li>
            </#if>
        </ul>
    </div>
    <div class="printpage">
       <#-- <a href="#" onclick="window.print();"><img src="images/btn-print.gif" alt="<@spring.message 'AltPrint_t' />" vspace="4"/></a>-->
           <!-- AddThis Button BEGIN -->
        <#assign  showthislang = locale>
        <#if  locale = "mt" || locale = "et">
           <#assign  showthislang = "en">
        </#if>
           <a class="addthis_button" href="http://www.addthis.com/bookmark.php?v=250&amp;username=xa-4b4f08de468caf36"><img src="http://s7.addthis.com/static/btn/lg-share-${showthislang}.gif" alt="Bookmark and Share" style="border:0"/></a>
           <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=xa-4b4f08de468caf36"></script>
            <script type="text/javascript">
                var addthis_config = {
                     ui_language: "${showthislang}"
                }
              </script>
           <!-- AddThis Button END -->
    </div>

</div>
</#macro>


<#macro show_result_table seq>
<table id="multi" summary="search results">
    <#list seq?chunk(4) as row>
    <tr>
        <#list row as cell>
        <td valign="bottom" width="25%" class="${cell.type}">
            <div class="brief-thumb-container">
                <a href="full-doc.html?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true">
                         <img class="thumb" id="thumb_${cell.index?c}" align="middle" src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}" alt="<@spring.message 'AltMoreInfo_t' />" height="110"/>
                    <#else>
                        <img class="thumb" id="thumb_${cell.index?c}" align="middle" src="${cell.thumbnail}" alt="Click for more information" height="110" onerror="showDefault(this,'${cell.type}','brief')"/>
                    </#if>
                </a>
            </div>
            <h2><@stringLimiter "${cell.title}" "40"/></h2>
            <ul>
                <#if cell.creator??><#if cell.creator != " ">
                <li><@stringLimiter "${cell.creator}" "120"/></li>
                </#if></#if>
                <#if cell.year != ""><#if cell.year != "0000">
                <li>${cell.year}</li>
                </#if></#if>
                <#if cell.provider != "">
                <#assign pr = cell.provider />
                <#if pr?length &gt; 80>
                <#assign pr = cell.provider?substring(0, 80) + "..."/>
                </#if>
                <li title="${cell.provider}">${pr}</li>
                </#if>
            </ul>
        </td>
        </#list>
    </tr>
    </#list>
</table>
</#macro>

<#macro show_result_list seq>
<table cellspacing="1" cellpadding="0" width="100%" border="0" summary="search results" id="multi" class="list">
    <#list seq as cell>
    <tr>
        <td valign="top" width="80">
            <div class="brief-thumb-container-listview">
                <a href="full-doc.html?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">
                    <#if useCache="true"><img class="thumb" id="thumb_${cell.index}" align="middle"
                                              src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}"
                                              alt="<@spring.message 'AltMoreInfo_t' />" height="50"/>
                    <#else><img class="thumb" id="thumb_${cell.index?c}" align="middle" src="${cell.thumbnail}"
                                alt="Click for more information" height="50"
                                onerror="showDefault(this,'${cell.type}','brief')"/>
                    </#if>
                </a>
            </div>
        </td>
        <td class="${cell.type}">
            <div>
                <h3>
                    <a class="fg-gray" href="full-doc.html?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd"><@stringLimiter "${cell.title}" "250"/></a>
                </h3>
                <#-- without labels -->
                <#if !cell.creator[0]?matches(" ")>${cell.creator}<br/></#if>
                <#if !cell.year?matches(" ")><#if cell.year != "0000">${cell.year}<br/></#if></#if>
                <#if !cell.provider?matches(" ")>${cell.provider}</#if>
                <#-- with labels -->
                <#--<#if !cell.creator[0]?matches(" ")><span><@spring.message 'Creator_t' />: </span>${cell.creator}<br/></#if>-->
                <#--<#if !cell.year?matches(" ")><#if cell.year != "0000"><span><@spring.message 'Date_t' />: </span>${cell.year}<br/></#if></#if>-->
                <#--<#if !cell.provider?matches(" ")><@spring.message 'Provider_t' />: ${cell.provider}</#if>-->
            </div>
                <#--<textarea style="width:1000px;height:150px;">-->
                    <#--<li class="category_${cell.type}">-->
                    <#--<a href="full-doc.html?uri=${cell.id}">-->
                    <#--<#if useCache="true">-->
                        <#--<img src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&size=BRIEF_DOC&type=${cell.type}"  style="z-index:99" width="70" alt="<@stringLimiter "${cell.title?html}" "50"/>" title="<@stringLimiter "${cell.title?html}" "50"/>"/>-->
                    <#--<#else>-->
                        <#--<img src="${cell.thumbnail}" onerror="showDefault(this,'${cell.europeanaType}')" width="70" alt="<@stringLimiter "${cell.title?html}" "50"/>" title="<@stringLimiter "${cell.title?html}" "50"/>"/>-->
                    <#--</#if>-->
                    <#--</a>-->
                    <#--</li>-->
                <#--</textarea>-->
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
        <img src="images/loading.gif" width="208" height="13" alt="loading"/>
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

<#macro print_tab_count showAll tabName countName>
    <#if showAll?matches("selected") || tabName?matches("selected")>
        (${countName})
    </#if>
</#macro>
</#compress>