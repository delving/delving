<#compress>
<#include "delving-macros.ftl">

<@addHeader "Norvegiana", "",[],[]/>

<section id="sidebar" class="grid_3" role="complementary">
    <header id="branding" role="banner">
        <a href="/${portalName}/" title=""/>
        <img src="/${portalName}/${portalTheme}/images/norvegiana.jpg" alt="Norvegiana"/>
        </a>
        <h1 class="large">${portalDisplayName}</h1>
    </header>

</section>


<section id="search_advanced" class="grid_9" style="background: #fff;" role="search">
       <h1><@spring.message 'AdvancedSearch_t'/></h1>
       <form method="POST" action="advancedsearch.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <table>
            <tr>
                <td width="100">&#160;</td>
                <td width="110"><select name="facet0" id="m-facet1"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td width="200"><input type="text" name="value0" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator1" id="m-operator2"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet1" id="m-facet2"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="value1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator2" id="m-operator3"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet2" id="m-facet3"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="value3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td></td>
                <td align="right"><@spring.message 'search.order.by'/>:</td>
                <td>
                    <select name="sortBy" class="form_11">
                    <option selected="selected" value="">-</option>
                    <option value="title"><@spring.message 'dc_title_t'/></option>
                    <option value="creator"><@spring.message 'dc_creator_t'/></option>
                    <option value="YEAR"><@spring.message 'dc_date_t'/></option>
                    <#--<option value="COLLECTION">Collectie</option>-->
                </select></td>
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

</section><!-- end search -->

<#include "inc_footer.ftl"/>
</#compress>