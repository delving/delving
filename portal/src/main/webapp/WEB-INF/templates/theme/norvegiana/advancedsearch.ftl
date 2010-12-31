<#compress>
<#include "includeMarcos.ftl">
<#assign title><@spring.message '_action.advanced.search'/></#assign>
<@addHeader "${portalDisplayName} - ${title}", "",[],[]/>
<section id="search_advanced" class="grid_10 prefix_2 main"  role="search">
    <div class="inner">
       <h1><@spring.message '_action.advanced.search'/></h1>
       <form method="POST" action="advancedsearch.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />



        <table>

            <tr>
                <td width="200" align="right"><select name="facet0" id="m-facet1"><option value=""><@spring.message '_search.field.any.field'/> &nbsp;</option><option value="dc_title"><@spring.message '_search.field.title'/></option><option value="dc_creator"><@spring.message '_search.field.creator'/></option><option value="dc_date"><@spring.message '_search.field.date'/></option><option value="dc_subject"><@spring.message '_search.field.subject'/></option></select></td>
                <td width=""><input type="text" name="value0" class="search-input" maxlength="75"/></td>
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
                    <select name="allDataProviders" id="sel-dataproviders">
                       <option value="true"><@spring.message '_metadata.type.all'/></option>
                       <option value="false"><@spring.message '_search.select'/></option>
                   </select>
                   <div id="provider-list" style="display:none">
                       <table class="squash">
                        <#list dataProviders?chunk(4) as row>
                           <tr>
                               <#list row as dataprovider>
                                    <td><input type="checkbox" name="collectionList" value="${dataprovider.name?url('utf-8')}"/>${dataprovider.name} (${dataprovider.count})</td>
                               </#list>

                            </tr>
                        </#list>
                       </table>
                   </div>
                </td>
            </tr>
            </#if>
            <#if (user??) && (user.role=="ROLE_ADMINISTRATOR" || user.role=="ROLE_GOD")>
                <#if collections??>
                <tr>
                    <td align="right"><@spring.message '_metadata.abm.data.collections'/>:</td>
                    <td>
                        <select name="allCollections" id="sel-collections">
                           <option value="true"><@spring.message '_metadata.type.all'/></option>
                           <option value="false"><@spring.message '_search.select'/></option>
                       </select>
                       <div id="collections-list" style="display:none">
                           <table class="squash">
                            <#list collections?chunk(4) as row>
                               <tr>
                                   <#list row as collection>
                                        <td><input type="checkbox" name="collectionList" value="${collection.name?url('utf-8')}"/>${collection.name} (${collection.count})</td>
                                   </#list>

                                </tr>
                            </#list>
                           </table>
                       </div>
                    </td>
                </tr>
                </#if>
            </#if>
            <#if county??>
            <tr>
                <td align="right"><@spring.message '_metadata.abm.county'/>:</td>
                <td>
                    <select name="allCounties" id="sel-counties">
                       <option value="true"><@spring.message '_metadata.type.all'/></option>
                       <option value="false"><@spring.message '_search.select'/></option>
                     </select>
                        <div id="county-list" style="display: none">
                        <table class="squash"
                        <#list county?chunk(4) as row>
                            <tr>
                                <#list row as county>
                                    <td><input type="checkbox" name="countyList" value="${county.name?url('utf-8')}"/>${county.name} (${county.count})</td>
                                </#list>
                            </tr>

                        </#list>
                        </table>
                        </div>

                </td>
            </tr>
            </#if>
            <tr>
                <td align="right">Digital content only:</td>
                <td><input type="checkbox" value="true" name="onlyDigitalObjects"/> </td>
            </tr>
            <tr>
                <td align="right"><@spring.message '_metadata.europeana.type'/>:</td>
                <td>
                    <input type="checkbox" name="type" value="TYPE:IMAGE"/><@spring.message '_metadata.type.images'/>
                    <input type="checkbox" name="type" value="TYPE:TEXT"/><@spring.message '_metadata.type.texts'/>
                    <input type="checkbox" name="type" value="TYPE:SOUND"/><@spring.message '_metadata.type.sounds'/>
                    <input type="checkbox" name="type" value="TYPE:VIDEO"/><@spring.message '_metadata.type.videos'/>
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
        </form>
</div>
</section><!-- end search -->
<script type="text/javascript">
    $(document).ready(function() {
        $("#sel-counties").change(function() {
            if ($("#sel-counties :selected").val() == "false") {
                $("#county-list").show("slow");
            }
            if ($("#sel-counties :selected").val() == "true") {
                $("#county-list").hide("slow");
            }
        });
        $("#sel-dataproviders").change(function() {
            if ($("#sel-dataproviders :selected").val() == "false") {
                $("#provider-list").show("slow");
            }
            if ($("#sel-dataproviders :selected").val() == "true") {
                $("#provider-list").hide("slow");
            }
        });
        if($("#sel-collections")){
            $("#sel-counties").change(function() {
            if ($("#sel-counties :selected").val() == "false") {
                $("#county-list").show("slow");
            }
            if ($("#sel-counties :selected").val() == "true") {
                $("#county-list").hide("slow");
            }
            });
         }

    });

</script>
<@addFooter/>
</#compress>
