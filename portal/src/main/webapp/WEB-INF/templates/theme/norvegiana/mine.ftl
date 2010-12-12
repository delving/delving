<#assign thisPage = "mine.html">

<#compress>


<#include "includeMarcos.ftl">

<@addHeader "${portalDisplayName}", "",["mine.js"],["mine.css"]/>


<style>

</style>
<script type="text/javascript">

    $(function() {
        $("#savedItems").tabs('selected', 0);
    });

</script>

<section id="main" class="grid_12">

    <div id="savedItems">
        <ul>
            <li><a href="#tab-0" onclick="$.cookie('ui-tabs-3', '0', { expires: 1 });" title="<@spring.message '_mine.user.information' />"><span><@spring.message '_mine.user.information' /></span></a></li>
            <li><a href="#tab-1" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });" title="<@spring.message '_mine.saved.items'/>"><span><@spring.message '_mine.saved.items'/></span></a></li>
            <li><a href="#tab-2" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });" title="<@spring.message '_mine.saved.searches'/>"><span><@spring.message '_mine.saved.searches'/></span></a></li>
        </ul>
       <div id="tab-0">
          <table summary="table with user information" id="tbl-user" >
              <caption><@spring.message '_mine.user.information' /></caption>
            <tr>
                <td width="150">
                    <@spring.message '_mine.username' />:
                </td>
                <td>${user.userName}</td>
            </tr>
            <tr>
              <td><@spring.message '_mine.email.address'/>:</td>
              <td>${user.email}</td>
            </tr>
            <#if user.firstName??>
            <tr>
              <td><@spring.message '_mine.firstname'/>:</td>
              <td>${user.firstName}</td>
            </tr>
            </#if>
            <#if user.lastName??>
            <tr>
              <td><@spring.message '_mine.last.name'/>:</td>
              <td>${user.lastName}</td>
            </tr>
            </#if>
            <#if user.registrationDate??>
            <tr>
              <td><@spring.message '_mine.user.registration.date'/>:</td>
              <td>${user.registrationDate}</td>
            </tr>
            </#if>
            <#if user.lastLogin??>
            <tr>
              <td><@spring.message '_mine.user.last.login.date'/>:</td>
              <td>${user.lastLogin?string("yyyy-MM-dd HH:mm:ss")}</td>
            </tr>
            </#if>
            <#--<tr>-->
                <#--<td></td>-->
                <#--<td><button class="delete" id="rem-acc" name="${user.email}">Remove my account</button></td>-->
            <#--</tr>-->
        </table>
       </div>
        <div id="tab-1">
            <table class="zebra" summary="list with saved items">
                <caption><@spring.message '_mine.saved.items'/></caption>
                <tbody>
                <#if user.savedItems?size &gt; 0>
                    <#list user.savedItems as item>
                        <#assign rowId = "item_row_"+item_index/>
                        <tr id="${rowId}">
                            <td width="60">
                                <a href="/${portalName}/record/${item.europeanaId.europeanaUri}.html">
                                 <#if useCache="true">
                                    <img class="thumb" id="img_${item_index}" align="middle" src="${cacheUrl}uri=${item.europeanaObject}&size=BRIEF_DOC" alt="Click for more information" width="50" style="float:left" onerror="showDefaultSmall(this,'${item.docType}')"/>
                                 <#else>
                                    <#if item.docType??>
                                      <img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefaultSmall(this,'${item.docType}')"/>
                                    <#else>
                                      <img class="thumb" align="middle" src="${item.europeanaObject}" alt="Click for more information" height="50" style="float:left" onerror="showDefaultSmall(this)"/>
                                    </#if>
                                 </#if>
                                </a>
                            </td>
                            <td valign="top" class="item-info">
                                <a href="/${portalName}/record/${item.europeanaId.europeanaUri}.html">
                                    <strong><@stringLimiter "${item.title}" "50"/></strong>
                                </a>
                                <br/>
                                <@spring.message '_search.field.creator'/>: <em>${item.author}</em>
                                <br/>
                                <@spring.message '_mine.date.saved'/>: <em>${item.dateSaved?datetime}</em>

                            </td>
                            <td width="60">
                                <a href="#" class="fg-button ui-state-default fg-button-icon-left ui-corner-all" onclick="removeSavedItem(${item.id?string('0')},'${rowId}');"><span class="ui-icon ui-icon-trash"></span><@spring.message '_mine.delete'/></a>
                            </td>
                        </tr>
                    </#list>

                <#else>
                    <tr><td><@spring.message '_mine.nosaved.items'/></td></tr>
                </#if>
                </tbody>
            </table>
        </div>
       <div id="tab-2">
            <table class="zebra" summary="list with saved searches">
                <caption><@spring.message '_mine.saved.searches'/></caption>
                <#if user.savedSearches?size &gt; 0>
                    <#list user.savedSearches as search>
                       <#assign rowId = "search_row_"+search_index/>
                        <tr id="${rowId}">
                            <td width="5"><a href="/${portalName}/brief-doc.html?${search.query}"><span class="ui-icon ui-icon-search"></span></a></td>
                            <td valign="top" class="item-info">
                                <a href="/${portalName}/brief-doc.html?${search.query}">${search.queryString}</a><br/>
                                <@spring.message '_mine.date.saved'/>: <em>${search.dateSaved?datetime}</em>
                            </td>
                            <td width="60"><a href="#" class="fg-button ui-state-default fg-button-icon-left ui-corner-all" onclick="removeSavedSearch(${search.id?string('0')},'${rowId}');"><span class="ui-icon ui-icon-trash"></span><@spring.message '_mine.delete'/></a></td>
                        </tr>
                    </#list>
                <#else>
                    <tr><td><@spring.message '_mine.nosavedsearches'/></td></tr>
                </#if>
            </table>
        </div>
    </div>
</section>


    <@addFooter/>

</#compress>
