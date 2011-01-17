<#compress>
<#include "includeMarcos.ftl">
<#assign title><@spring.message '_action.advanced.search'/></#assign>
<@addHeader "${portalDisplayName} - ${title}", "",["advanced-search.js"],[]/>

<section id="search-advanced" class="grid_10 prefix_1 main"  role="search">

       <form method="POST" action="advancedsearch.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <fieldset>
            <legend><@spring.message '_action.advanced.search'/></legend>

        <table width="100%">

            <tr>
                <td width="200" align="right"><select name="facet0" id="m-facet1"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="dc_title"><@spring.message '_search.field.title'/></option><option value="dc_creator"><@spring.message '_search.field.creator'/></option><option value="dc_date"><@spring.message '_search.field.date'/></option><option value="dc_subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td width="600"><input type="text" name="value0" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right">
                    <select name="operator1" id="m-operator2"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select>
                    <select name="facet1" id="m-facet2"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="dc_title"><@spring.message '_search.field.title'/></option><option value="dc_creator"><@spring.message '_search.field.creator'/></option><option value="dc_date"><@spring.message '_search.field.date'/></option><option value="dc_subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="value1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right">
                    <select name="operator2" id="m-operator3"><option value="and"><@spring.message '_search.boolean.and'/> &nbsp;</option><option value="or"><@spring.message '_search.boolean.or'/> </option><option value="not"><@spring.message '_search.boolean.not'/> </option></select>
                    <select name="facet2" id="m-facet3"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="dc_title"><@spring.message '_search.field.title'/></option><option value="dc_creator"><@spring.message '_search.field.creator'/></option><option value="dc_date"><@spring.message '_search.field.date'/></option><option value="dc_subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td><input type="text" name="value3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><@spring.message '_action.search.order.by'/>:</td>
                <td>
                    <select name="sortBy" class="form_11">
                    <option selected="selected" value="">-</option>
                    <option value="title"><@spring.message '_metadata.dc.title'/></option>
                    <option value="creator"><@spring.message '_metadata.dc.creator'/></option>
                    <option value="YEAR"><@spring.message '_metadata.dc.date'/></option>
                    <#--<option value="COLLECTION">Collectie</option>-->
                </select></td>
            </tr>
            <#if dataProviders??>
            <tr>
                <td align="right"><@spring.message '_metadata.abm.data.provider'/>:</td>
                <td>
                    <select name="allProviders" id="sel-dataproviders">
                       <option value="true"><@spring.message '_metadata.type.all'/></option>
                       <option value="false"><@spring.message '_search.select'/></option>
                    </select>
                    <select id="provider-list" name="providersList" style="display:none">
                    <#list dataProviders?sort as row>
                        <option value="${row.name}">${row.name} (${row.count})</option>
                    </#list>
                    </select>

                </td>
            </tr>
            </#if>
            <#--<#if (user??) && (user.role=="ROLE_ADMINISTRATOR" || user.role=="ROLE_GOD")>-->
                <#--<#if collections??>-->
                <#--<tr>-->
                    <#--<td align="right"><@spring.message '_search.field.collection'/>:</td>-->
                    <#--<td>-->
                        <#--<select name="allCollections" id="sel-collections">-->
                           <#--<option value="true"><@spring.message '_metadata.type.all'/></option>-->
                           <#--<option value="false"><@spring.message '_search.select'/></option>-->
                       <#--</select>-->
                     <#--<select id="collections-list" name="collectionsList" style="display:none">-->
                    <#--<#list collections?sort as row>-->
                        <#--<option value="${row.name}">${row.name} (${row.count})</option>-->
                    <#--</#list>-->
                    <#--</select>-->
                       <#--&lt;#&ndash;<div id="collections-list" style="display:none">&ndash;&gt;-->
                           <#--&lt;#&ndash;<table class="squash">&ndash;&gt;-->
                            <#--&lt;#&ndash;<#list collections?chunk(4) as row>&ndash;&gt;-->
                               <#--&lt;#&ndash;<tr>&ndash;&gt;-->
                                   <#--&lt;#&ndash;<#list row as collection>&ndash;&gt;-->
                                        <#--&lt;#&ndash;<td><input type="checkbox" name="collectionList" value="${collection.name?url('utf-8')}"/>${collection.name} (${collection.count})</td>&ndash;&gt;-->
                                   <#--&lt;#&ndash;</#list>&ndash;&gt;-->

                                <#--&lt;#&ndash;</tr>&ndash;&gt;-->
                            <#--&lt;#&ndash;</#list>&ndash;&gt;-->
                           <#--&lt;#&ndash;</table>&ndash;&gt;-->
                       <#--&lt;#&ndash;</div>&ndash;&gt;-->
                    <#--</td>-->
                <#--</tr>-->
                <#--</#if>-->
            <#--</#if>-->
            <#if county??>
            <tr>
                <td align="right"><@spring.message '_metadata.abm.county'/>:</td>
                <td>
                    <#assign counties = ['&#248;stfold','akershus','oslo','hedmark','oppland','buskerud','vestfold','telemark','aust-agder','vest-agder','rogaland','hordaland','sogn og fjordane','m&#248;re og romsdal','s&#248;r-tr&#248;ndelag','nord-tr&#248;ndelag','nordland','troms','finnmark']/>
                    <select name="allCounties" id="sel-counties">
                       <option value="true"><@spring.message '_metadata.type.all'/></option>
                       <option value="false"><@spring.message '_search.select'/></option>
                     </select>
                        <#--<div id="county-list" style="display: none">-->
                        <#--<table style="font-size: .85em">-->
                            <#--<#list county?sort?chunk(4) as row>-->
                                <#--<tr>-->
                                    <#--<#list row as c>-->
                                        <#--<td><input type="checkbox" name="countyList" value="${c.name?url('utf-8')}"/>${c.name}(${c.count})</td>-->
                                    <#--</#list>-->
                                <#--</tr>-->

                            <#--</#list>-->
                        <#--</table>-->
                            <select id="county-list" name="countyList" style="display: none">
                            <#list county?sort as row>
                                <option value="${row.name}">${row.name} (${row.count})</option>
                            </#list>
                            </select>

                        <#--</div>-->

                </td>
            </tr>
            <tr id="municipalities-row" style="display:none">
                <td align="right"><@spring.message '_metadata.abm.municipality'/>:</td>
                <td><select id="municipality-list" name="municipalityList"></select></td>
            </tr>
            </#if>
            <#--<#if municipality??>-->
            <#--<tr>-->
                <#--<td align="right"><@spring.message '_metadata.abm.municipality'/>:</td>-->
                <#--<td>-->
                    <#--<select name="allCounties" id="sel-municipalities">-->
                       <#--<option value="true"><@spring.message '_metadata.type.all'/></option>-->
                       <#--<option value="false"><@spring.message '_search.select'/></option>-->
                     <#--</select>-->
                            <#--<select id="municipality-list" name="municipaltyList" style="display: none">-->
                            <#--<#list municipality?sort as row>-->
                                <#--<option value="${row.name}">${row.name} (${row.count})</option>-->
                            <#--</#list>-->
                            <#--</select>-->
                        <#--&lt;#&ndash;<div id="municipality-list" style="display:none;">&ndash;&gt;-->
                        <#--&lt;#&ndash;<table  style="font-size: .85em">&ndash;&gt;-->
                        <#--&lt;#&ndash;<#list municipality?sort?chunk(4) as row>&ndash;&gt;-->
                            <#--&lt;#&ndash;<tr>&ndash;&gt;-->
                                <#--&lt;#&ndash;<#list row as muni>&ndash;&gt;-->
                                    <#--&lt;#&ndash;<td><input type="checkbox" name="municipalityList" value="${muni.name?url('utf-8')}"/>${muni.name} (${muni.count})</td>&ndash;&gt;-->
                                <#--&lt;#&ndash;</#list>&ndash;&gt;-->
                            <#--&lt;#&ndash;</tr>&ndash;&gt;-->

                        <#--&lt;#&ndash;</#list>&ndash;&gt;-->
                        <#--&lt;#&ndash;</table>&ndash;&gt;-->
                        <#--&lt;#&ndash;</div>&ndash;&gt;-->

                <#--</td>-->
            <#--</tr>-->
            <#--</#if>-->
            <tr>
                <td align="right">Digital content only:</td>
                <td><input type="checkbox" value="true" name="onlyDigitalObjects"/> </td>
            </tr>
            <tr>
                <td align="right"><@spring.message '_metadata.europeana.type'/>:</td>
                <td>
                    <input type="checkbox" name="typeList" value="IMAGE"/><@spring.message '_metadata.type.images'/>
                    <input type="checkbox" name="typeList" value="TEXT"/><@spring.message '_metadata.type.texts'/>
                    <input type="checkbox" name="typeList" value="SOUND"/><@spring.message '_metadata.type.sounds'/>
                    <input type="checkbox" name="typeList" value="VIDEO"/><@spring.message '_metadata.type.videos'/>
                </td>
            </tr>
             <tr>
                <td align="right"><@spring.message '_metadata.dc.date'/>:</td>
                <td><input type="text" name="creationFrom" class="in-small" maxlength="4"/> - <input type="text" name="creationTo" class="in-small" maxlength="4" /></td>
            </tr>
            <tr>
                <td colspan=23">&#160;</td>
            </tr>
            <tr>
                <td align="left"><input type="reset" value="<@spring.message '_portal.ui.reset.searchbox' />" /></td>
                <td align="right"><input id="searchsubmit2" class="button btn-strong" type="submit" value="<@spring.message '_action.search' />" /></td>
            </tr>

         </table>
        </fieldset>
        </form>
</section><!-- end search -->

<@addFooter/>
</#compress>
