<#assign thisPage = "brief-doc.html"/>
<#assign queryStringForPresentation = queryStringForPresentation/>

<#if result??><#assign result = result/></#if>

<#assign seq = briefDocs/>
<#assign pagination = pagination/>

<#include "inc_header.ftl">

<#-- image tab class assignation, currently not used  -->
<#assign tab = ""/>
<#if RequestParameters.tab?exists>
<#assign tab = RequestParameters.tab/>
</#if>
<div id="centernav">
	<@viewSelect/>
</div>

</div> <#-- this tag was opened in inc_header -->
<div id="content">
  <ul class="autolist">
    <li class="title">
	  <@spring.message 'Results_t' /> ${pagination.getStart()?c} - ${pagination.getLastViewableRecord()?c} <@spring.message 'Of_t' /> ${pagination.getNumFound()?c}
    </li>
    <#include "../inc_result_table_brief.ftl"/>
  </ul>
  <div id="resultnavigation">
    <@resultnav/>
  </div>
  
</div>

<#include "../inc_footer.ftl"/>

