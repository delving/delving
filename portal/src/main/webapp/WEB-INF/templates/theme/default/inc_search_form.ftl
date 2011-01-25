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
        <form method="get" action="/${portalName}/search" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <input class="txt-input" name="query" id="query" type="text" title="Europeana Search" maxlength="75" />
            <input id="submit_search" type="submit" value="<@spring.message '_action.search' />" />
            <a href="/${portalName}/advancedsearch.html" id="href-advanced" title="<@spring.message '_action.advanced.search' />"><@spring.message '_action.advanced.search' /></a>
        </form>
    </div>

    <div id="search_advanced" class="${className}" style="display:${showAdv};" title="<@spring.message '_action.advanced.search' />">
       <form method="get" action="/${portalName}/search" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <table>
            <tr>
                <td>&#160;</td>
                <td><select name="facet1" id="facet1"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="query1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator2" id="operator2"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select></td>
                <td><select name="facet2" id="facet2"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="query2" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator3" id="operator3"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select></td>
                <td><select name="facet3" id="facet3"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="query3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td colspan="3">&#160;</td>
            </tr>
            <tr>
                <td align="left"><input type="reset" value="<@spring.message '_portal.ui.reset.searchbox' />" /></td>
                <td>&#160;</td>
                <td align="right"><input id="searchsubmit2" type="submit" value="<@spring.message '_action.search' />" /></td>
            </tr>
         </table>
        </form>
    </div>
</#macro>
