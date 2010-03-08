<#assign thisPage = "brief-doc.html"/>
<#assign queryStringForPresentation = queryStringForPresentation/>

<#if result??><#assign result = result/></#if>

<#assign seq = briefDocs/>
<#assign pagination = pagination/>

<#include "../inc_header.ftl">

<#-- image tab class assignation, currently not used  -->
<#assign tab = ""/>
<#if RequestParameters.tab?exists>
<#assign tab = RequestParameters.tab/>
</#if>

<div id="logo">
	<a href="index.html"><img src="mobile/images/logo_slogan.png" alt="Logo"/></a>
</div>
<div id="viewselectnav">
   	<@viewSelect/>
</div>

<div id="content">
  <div id="resultinformation">
	  <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
  </div>
  <ul class="result_list">
    <#include "../inc_result_table_brief.ftl"/>
  </ul>
  <div id="resultnavigation">
    <@resultnav/>
  </div>
</div>

<#include "../inc_footer.ftl"/>

