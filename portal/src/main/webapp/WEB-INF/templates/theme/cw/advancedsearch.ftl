<#compress>
    <#assign thisPage = "advancedsearch.html"/>
    <#assign pageId = "adv"/>

    <#include "inc_header.ftl"/>
    <#assign collections = collections/>

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
            <td width="200">
                <select name="facet1" id="facet1">
                    <option selected="selected">Alles</option>
                    <option>Titel</option>
                    <option>Vervaardiger</option>
                    <option>Jaar</option>
                    <option>Onderwerp</option>
                    <option>Beschrijving</option>
                </select>
            </td>
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
                    <option selected="selected">Alles</option>
                    <option>Titel</option>
                    <option>Vervaardiger</option>
                    <option>Jaar</option>
                    <option>Onderwerp</option>
                    <option>Beschrijving</option>
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
                    <option selected="selected">Alles</option>
                    <option>Titel</option>
                    <option>Vervaardiger</option>
                    <option>Jaar</option>
                    <option>Onderwerp</option>
                    <option>Beschrijving</option>
                </select></td>
            <td><input type="text" name="query3" class="search-input" maxlength="75"/></td>
        </tr>

        <tr>
            <td align="right">Collectie:</td>
            <td>
                <select name="select7" class="form_11">
                    <option selected="selected">Alle Collecties</option>
                    <#list collections as coll>
                        <option>${coll.name}</option>
                    </#list>
                </select>
            </td>
            <td>&#160;</td>
        </tr>
        <tr>
            <td align="right">Sorteren op:</td>
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
            <td align="left"><input type="reset" value="<@spring.message 'Erase_t' />"/></td>
            <td>&#160;</td>
            <td align="right">
                <input id="searchsubmit2" type="submit" value="<@spring.message 'Search_t' />"/>
            </td>
        </tr>
    </table>
</form>

    <#--<#if (user??) && (user.role=="ROLE_RESEARCH_USER")>-->
    <h2>Uitegebreid zoeken Museometrie</h2>

    <form method="POST" action="advancedsearch.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1"/>
        <input type="hidden" name="view" value="${view}"/>

        <table>

            <tr>
                <td width="150">Zoek in:</td>
                <td width="200">
                    <select name="facet0" id="m-facet1">
                        <option selected="selected">Alles</option>
                        <option>Titel</option>
                        <option>Vervaardiger</option>
                        <option>Jaar</option>
                        <option>Onderwerp</option>
                        <option>Beschrijving</option>
                    </select>
                </td>
                <td><input type="text" name="value0" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"></td>
                <td>
                    <select name="operator1" id="m-operator2">
                        <option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option>
                        <option value="or"><@spring.message 'OrBoolean_t'/> </option>
                        <option value="not"><@spring.message 'NotBoolean_t'/> </option>
                    </select>
                    <select name="facet1" id="m-facet2">
                        <option selected="selected">Alles</option>
                        <option>Titel</option>
                        <option>Vervaardiger</option>
                        <option>Jaar</option>
                        <option>Onderwerp</option>
                        <option>Beschrijving</option>
                    </select></td>
                <td><input type="text" name="value1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"></td>
                <td>
                    <select name="operator2" id="m-operator3">
                        <option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option>
                        <option value="or"><@spring.message 'OrBoolean_t'/> </option>
                        <option value="not"><@spring.message 'NotBoolean_t'/> </option>
                    </select>
                    <select name="facet2" id="m-facet3">
                        <option selected="selected">Alles</option>
                        <option>Titel</option>
                        <option>Vervaardiger</option>
                        <option>Jaar</option>
                        <option>Onderwerp</option>
                        <option>Beschrijving</option>
                    </select></td>
                <td><input type="text" name="value2" class="search-input" maxlength="75"/></td>
            </tr>

            <tr>
                <td>Vervaardigingsjaar</td>
                <td>van <input type="text" name="creationFrom" class="in-small"/></td>
                <td>tot <input type="text" name="creationTo" class="in-small"/></td>
            </tr>

            <tr>
                <td>Geboortejaar vervaardiger</td>
                <td>van <input type="text" name="birthFrom" class="in-small"/></td>
                <td>tot <input type="text" name="birthTo" class="in-small"/></td>
            </tr>

            <tr>
                <td>Jaar van verwerving</td>
                <td>van <input type="text" name="acquisitionFrom" class="in-small"/></td>
                <td>tot <input type="text" name="acquisitionTo" class="in-small"/></td>
            </tr>

            <tr>
                <td>Aankoop bedrag</td>
                <td>
                    <select name="purchasePrice">
                        <option>Selecteer</option>
                        <option value="100"> &lt; 100</option>
                        <option value="1000">100 - 1,000</option>
                        <option value="10000">1,000 - 10,000</option>
                        <option value="100000" >10,000 - 100,000</option>
                        <option value="1000000">100,000 - 1,000,000</option>
                    </select>
                </td>
                <td></td>
            </tr>

            <tr>
                <td>Provincie</td>
                <td>
                    <select name="allProvinces" id="sel-province">
                        <option value="true">Alle provincies</option>
                        <option value="false">Selecteer provincies</option>
                    </select>
                </td>
                <td>
                    <div id="province-list" style="display:none;">
                        <table>
                            <tr>
                                <td width="140">
                                    <input type="checkbox" name="province" value="Noor Holland"/>Noord Holland
                                </td>
                                <td width="195">
                                    <input type="checkbox" name="province" value="Noord Brabant"/>Noord Brabant
                                </td>
                                <td width="165"><input type="checkbox" name="province" value="Limburg"/>Limburg</td>
                            </tr>
                            <tr>
                                <td><input type="checkbox" name="province" value="Zuid Holland"/>Zuid Holland</td>
                                <td><input type="checkbox" name="province" value="Gelderland"/>Gelderland</td>
                                <td><input type="checkbox" name="province" value="Drente"/>Drente</td>
                            </tr>
                            <tr>
                                <td><input type="checkbox" name="province" value="Zeeland"/>Zeeland</td>
                                <td><input type="checkbox" name="province" value="Overijssel"/>Overijssel</td>
                                <td><input type="checkbox" name="province" value="Groningen"/>Groningen</td>
                            </tr>
                            <tr>
                                <td><input type="checkbox" name="province" value="Friesland"/>Friesland</td>
                                <td><input type="checkbox" name="province" value="Flevoland"/>Flevoland</td>
                                <td><input type="checkbox" name="province" value="Utrecht"/>Utrecht</td>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>

            <tr>
                <td>Collectie</td>
                <td>
                    <select name="collections" id="sel-collections">
                        <option value="all">Alle collecties</option>
                        <option value="all-cb">Alle deelnemers Collectiebalans</option>
                        <option value="select">Selecteer deelnemers Collectiebalans</option>
                    </select>
                </td>
                <td>
                    <div id="collectiebalans-list" style="display:none;">
                        <table>
                            <tr>
                                <td width="140">
                                    <input type="checkbox" name="collection" value="Bonnefantenmuseum"/>Bonnefantenmuseum
                                </td>
                                <td width="195">
                                    <input type="checkbox" name="collection" value="Audax Textielmuseum"/>Audax Textielmuseum
                                </td>
                                <td width="165">
                                    <input type="checkbox" name="collection" value="Museum Het Valkhof"/>Museum Het Valkhof
                                </td>
                            </tr>
                            <tr>
                                <td><input type="checkbox" name="collection" value="Van Abbemuseum"/>Van Abbemuseum
                                </td>
                                <td>
                                    <input type="checkbox" name="collection" value="Stedelijk Museum de Lakenhal"/>Stedelijk Museum de Lakenhal
                                </td>
                                <td>
                                    <input type="checkbox" name="collection" value="Noord Brabantsmuseum"/>Noord Brabantsmuseum
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <input type="checkbox" name="collection" value="Rijksmuseum Twente"/>Rijksmuseum Twente
                                </td>
                                <td>
                                    <input type="checkbox" name="collection" value="Groninger Museum"/>Groninger Museum
                                </td>
                                <td>
                                    <input type="checkbox" name="collection" value="Centraal Museum Utrecht"/>Centraal Museum Utrecht
                                </td>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>

            <tr>
                <td align="right">Sorteren op:</td>
                <td>
                    <select name="sortBy" class="form_11">
                        <option selected="selected">Titel</option>
                        <option>Jaar</option>
                        <option>Collectie</option>
                    </select>
                </td>
                <td>&#160;</td>
            </tr>

            <tr>
                <td align="left"><input type="reset" value="<@spring.message 'Erase_t' />"/></td>
                <td>&#160;</td>
                <td align="right">
                    <input id="m-searchsubmit2" type="submit" value="<@spring.message 'Search_t' />"/>
                </td>
            </tr>
        </table>
    </form>

    </div>
    <#--</#if>-->

</div>

</div>

<#include "inc_footer.ftl"/>

<script type="text/javascript">
    $(document).ready(function(){
       $("#sel-province").change(function(){
           if($("#sel-province :selected").val()=="select"){
               $("#province-list").show("slow");
           };
           if($("#sel-province :selected").val()=="all"){
               $("#province-list").hide();
           };
       })
       $("#sel-collections").change(function(){
           if($("#sel-collections :selected").val()=="select"){
               $("#collectiebalans-list").show("slow");
           };
           if($("#sel-collections :selected").val()=="all"||$("#sel-collections :selected").val()=="all-cb"){
               $("#collectiebalans-list").hide();
           };
       })
    });
</script>
</#compress>