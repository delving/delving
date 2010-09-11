<#macro SearchForm className>

    <#assign showAdv="none"/>
    <#assign showSim="block"/>
    <#if pageId??>
    <#if pageId="adv">
        <#assign showAdv="block"/>
        <#assign showSim="none"/>
    </#if>
</#if>

    <div id="search_simple" class="${className}" style="display:${showSim};">
        <#if result?? >
            <#if result.badRequest >
                <span style="font-style: italic;">Wrong query. ${result.errorMessage}</span>
            </#if>
        </#if>
        <form method="get" action="brief-doc.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');" name="form-simple-search" id="form-simple-search">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <input class="search-input" name="query" id="query" type="text" title="Europeana Search" <#if query?exists>value="${qt}"</#if> maxlength="75"/>
            <input id="submit_search" type="submit" class="button" value="<@spring.message 'Search_t' />" /><br/>
            <#--<#if query?? && query?length &gt; 0 && enableRefinedSearch??>-->
                <a class="advanced-search" href="" onclick="toggleObject('search_simple');toggleObject('search_refine');return false;" title="Refine Search">Refine Search</a>
            <#--</#if>-->
            <a class="advanced-search" href="advancedsearch.html" onclick="toggleObject('search_simple');toggleObject('search_advanced');return false;" title="Advanced Search"><@spring.message 'AdvancedSearch_t' /></a>
        </form>
    </div>

    <div id="search_advanced" class="${className}" style="display:${showAdv};" title="<@spring.message 'AdvancedSearch_t' />">
       <form method="get" action="brief-doc.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <table>
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
                <td colspan="3">&#160;</td>
            </tr>
            <tr>
                <td align="left"><input type="reset" value="<@spring.message 'Reset_t' />" /></td>
                <td>&#160;</td> 
                <td align="right"><input id="searchsubmit2" type="submit" value="<@spring.message 'Search_t' />" /></td>
            </tr>
         </table>
        </form>
    </div>
</#macro>