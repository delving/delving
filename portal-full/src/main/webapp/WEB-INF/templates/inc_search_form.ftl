<#macro SearchForm className>

    <#assign showAdv="none"/>
    <#assign showSim="block"/>
    <#assign showRefine="none"/>
    <#if pageId??>
    <#if pageId="adv">
        <#assign showAdv="block"/>
        <#assign showSim="none"/>
        <#assign showRefine="none"/>
    </#if>
    <#assign  qt = ""/>
    <#if query?? && !query?starts_with('europeana_uri:')>
        <#assign  qt = query?replace("%20"," ")?html/>
    </#if>
</#if>

    <div id="search_simple" class="${className}" style="display:${showSim};">
        <#if result?? >
        <#-- todo fix this ?? wrong-->
            <#if result.badRequest?? >
                <div class="ui-widget" style="width:460px;">
                    <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <span style="font-style: italic;">Wrong query. ${result.errorMessage}</span></p>
                    </div>
                </div>
            </#if>
        </#if>
        <form method="get" action="brief-doc.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <input class="search-input" name="query" id="query" type="text" title="Europeana Search" <#if query?exists>value="${qt}"</#if> maxlength="75"/>
            <input id="submit_search" type="submit" class="button" value="<@spring.message 'Search_t' />" /><br/>
            <#if query?? && query?length &gt; 0 >
                <a class="advanced-search" href="" onclick="toggleObject('search_simple');toggleObject('search_refine');return false;" title="Refine Search">Refine Search</a>
            </#if>
            <a class="advanced-search" href="advancedsearch.html" onclick="toggleObject('search_simple');toggleObject('search_advanced');return false;" title="Advanced Search"><@spring.message 'AdvancedSearch_t' /></a>

            <label
        </form>
    </div>

    <#assign topPos = ""/>
    <#if pageId??>
    <#if pageId="adv">
        <#assign topPos="top:65px;">
    </#if>
    </#if>
    <div id="search_advanced" class="${className}" style="display:${showAdv};z-index:1001;${topPos}">
      <form method="get" action="brief-doc.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />

        <table id="tbl_adv_search">

            <#if pageId??>
            <#if pageId!="adv">
            <tr>
                <td>&#160;</td>
                <td>&#160;</td>
                <td align="right"><a href="#" onclick="toggleObject('search_advanced');toggleObject('search_simple');"><@spring.message 'HideAdvancedSearch_t' /></a></td>
            </tr>
            </#if>
            </#if>
            <tr>
                <td>&#160;</td>
                <td><select name="facet1" id="facet1"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator2" id="operator2"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet2" id="facet2"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query2" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator3" id="operator3"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet3" id="facet3"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td>&#160;</td>
                <td align="right"><input type="reset" class="button" value="<@spring.message 'Reset_t' />" /></td>
                <td align="right"><input id="searchsubmit2" type="submit" class="button" value="<@spring.message 'Search_t' />" /></td>
            </tr>
         </table>
        </form>
    </div>

<#if query??>
    <div id="search_refine" class="${className}" style="display:${showRefine};z-index:1012;" onsubmit="return checkFormSimpleSearch('rq');">
      <form method="get" action="brief-doc.html?" accept-charset="UTF-8" name="formRefineSearch" id="formRefineSearch">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <input type="hidden" name="query" id="rqq" value="${query}"/>
        <input type="hidden" id="queryWithFacets" value="<#if queryStringForPresentation??>${queryStringForPresentation?replace('query=','')}</#if>"/>
        <a href="#" onclick="toggleObject('search_refine');toggleObject('search_simple');">Hide Refine Search</a>
        <table id="tbl_refine_search">
            <tr>
                <td><input type="text" name="rq" id="rq" value=""></td>
                <td><input id="searchsubmit3" type="submit" class="button" value="<@spring.message 'Search_t' />" /></td>
            </tr>
            <#--<tr>-->
                <#--<td><input type="checkbox" name="withFacets" id="withFacets"/>&#160;Include facets</td>-->
            <#--</tr>-->

         </table>
        </form>
    </div>
</#if>
</#macro>