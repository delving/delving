<#compress>
<#assign thisPage = "advancedsearch.html"/>
<#include "inc_header.ftl"/>
 <div id="header">

    <div id="identity" class="grid_3">
        <h1>Delving</h1>
        <a href="/${portalName}/index.html" title="ABM"><img src="/${portalName}/${portalTheme}/images/abm-logo.jpg" alt="ABM"/></a>
    </div>

    <div class="grid_9">

        <div id="top-bar">
            <div class="inner">
                <@userbar/>
            </div>
        </div>

    </div>

</div>

<div class="clear"></div>


<div id="search" class="grid_12" style="background: #fff;">
    <div id="search_advanced" title="<@spring.message '_action.advanced.search' />" class="grid_4 alpha">
       <form method="POST" action="advancedsearch.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <table>
            <tr>
                <td>&#160;</td>
                <td><select name="facet0" id="m-facet1"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="value0" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator1" id="m-operator2"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select></td>
                <td><select name="facet1" id="m-facet2"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="value1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator2" id="m-operator3"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select></td>
                <td><select name="facet2" id="m-facet3"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="title"><@spring.message '_search.field.title'/></option><option value="creator"><@spring.message '_search.field.creator'/></option><option value="date"><@spring.message '_search.field.date'/></option><option value="subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="value3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td></td>
                <td align="right"><@spring.message '_delving-macros.search.order.by'/>:</td>
                <td>
                    <select name="sortBy" class="form_11">
                    <option selected="selected" value="">-</option>
                    <option value="title"><@spring.message '_metadata.dc.title'/></option>
                    <option value="creator"><@spring.message '_metadata.dc.creator'/></option>
                    <option value="YEAR"><@spring.message '_metadata.dc.date'/></option>
                    <#--<option value="COLLECTION">Collectie</option>-->
                </select></td>
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
</div><!-- end search -->

<#include "inc_footer.ftl"/>
</#compress>
