<#compress>
    <#assign thisPage = "advancedsearch.html"/>
    <#assign pageId = "adv"/>

    <#include "inc_header.ftl"/>

<div class="main">

    <div class="grid_12 breadcrumb">
        <em>U bevindt zich op: </em>
        <span><a href="index.html" title="Homepagina">Home</a> <span class="imgreplacement">&rsaquo;</span></span> Uitgebreid zoeken
    </div>

    <div id="search" class="grid_12">

        <div class="search_advanced">

            <h1>Zoeken in de Collectie Database</h1>

            <h2>Geavanceerd zoeken</h2>

            <form method="get" action="brief-doc.html" accept-charset="UTF-8">
                <input type="hidden" name="start" value="1"/>
                <input type="hidden" name="view" value="${view}"/>

                <table>

                    <tr>
                        <td width="150">Zoek in:</td>
                        <td width="150"><select name="facet1" id="facet1">
                            <option value=""><@spring.message 'AnyField_t'/> &nbsp;</option>
                            <option value="title"><@spring.message 'Title_t'/></option>
                            <option value="creator"><@spring.message 'Creator_t'/></option>
                            <option value="date"><@spring.message 'Date_t'/></option>
                            <option value="subject"><@spring.message 'Subject_t'/></option>
                        </select></td>
                        <td><input type="text" name="query1" class="search-input" maxlength="75"/></td>
                    </tr>
                    <tr>
                        <td align="right"></td>
                        <td>
                            <select name="operator2" id="operator2">
                            <option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option>
                            <option value="or"><@spring.message 'OrBoolean_t'/> </option>
                            <option value="not"><@spring.message 'NotBoolean_t'/> </option>
                        </select>
                            <select name="facet2" id="facet2">
                            <option value=""><@spring.message 'AnyField_t'/> &nbsp;</option>
                            <option value="title"><@spring.message 'Title_t'/></option>
                            <option value="creator"><@spring.message 'Creator_t'/></option>
                            <option value="date"><@spring.message 'Date_t'/></option>
                            <option value="subject"><@spring.message 'Subject_t'/></option>
                        </select></td>
                        <td><input type="text" name="query2" class="search-input" maxlength="75"/></td>
                    </tr>
                    <tr>
                        <td align="right"></td>
                        <td>
                            <select name="operator3" id="operator3">
                            <option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option>
                            <option value="or"><@spring.message 'OrBoolean_t'/> </option>
                            <option value="not"><@spring.message 'NotBoolean_t'/> </option>
                        </select>
                            <select name="facet3" id="facet3">
                            <option value=""><@spring.message 'AnyField_t'/> &nbsp;</option>
                            <option value="title"><@spring.message 'Title_t'/></option>
                            <option value="creator"><@spring.message 'Creator_t'/></option>
                            <option value="date"><@spring.message 'Date_t'/></option>
                            <option value="subject"><@spring.message 'Subject_t'/></option>
                        </select></td>
                        <td><input type="text" name="query3" class="search-input" maxlength="75"/></td>
                    </tr>

                    <tr>
                        <td align="right" width="100">Collectie:</td>
                        <td>
                            <select name="select7" class="form_11">
                                <option selected="selected">Alle Collecties</option>
                                <option>Boymans van Beuningen</option>
                                <option>Instituut Collectie Nederland</option>
                                <option>Stedelijk Museum</option>
                                <option>Erfgoed Utrecht</option>
                            </select>
                        </td>
                        <td>&#160;</td>
                    </tr>
                    <tr>
                        <td align="right" width="100">Sorteren op:</td>
                        <td>
                            <select name="select8" class="form_11">
                                <option selected="selected">Titel</option>
                                <option>Jaar</option>
                                <option>Collectie</option>
                            </select>
                        </td>
                        <td>&#160;</td>
                    </tr>

                    <tr>
                        <td align="left"><input type="reset" value="<@spring.message 'Reset_t' />"/></td>
                        <td>&#160;</td>
                        <td align="right">
                            <input id="searchsubmit2" type="submit" value="<@spring.message 'Search_t' />"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>

    </div>

</div>

    <#include "inc_footer.ftl"/>
</#compress>