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
    <#include "inc_result_table_brief.ftl"/>
  </ul>
  <div id="resultnavigation">
    <@resultnav/>
  </div>
  
</div>

<#include "inc_footer.ftl"/>


<#--------------------------------------------------------------------------------------------------------------------->
<#--  RESULT NAVIGATION/PAGINATION MACROS --->
<#--------------------------------------------------------------------------------------------------------------------->


<#-- UI STYLED  -->
<#macro resultnav>
	<#if pagination.previous>           
    	<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.previousPage?c}&amp;view=${view}" 
        	class="pagination">&lt;</a>
	</#if>
	<#list pagination.pageLinks as link>
    	<#if link.linked>
        	<#assign lstart = link.start/>
            	<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${link.start?c}&amp;view=${view}" 
            		class="pagination">${link.display?c}</a>
		<#else>
        		<a class="pagination" id="pagination-current">${link.display?c}</a>
		</#if>
	</#list>

	<#if pagination.next>
    	<a href="?${queryStringForPresentation?html}&amp;tab=${tab}&amp;start=${pagination.nextPage?c}&amp;view=${view}" 
    		class="pagination">&gt;</a>
	</#if>            
</#macro>


<#macro viewSelect>  
	<#if queryStringForPresentation?exists>
    	<#switch view>
    		<#case "text_only">
      			<a href="${thisPage}?${queryStringForPresentation?html}&amp;view=mixed"><img src="mobile/images/mixed.gif" /></a>
				<a class="buttonpressed" href="${thisPage}?${queryStringForPresentation?html}&amp;view=text_only"><img src="mobile/images/text_only.gif" /></a>
				<a href="${thisPage}?${queryStringForPresentation?html}&amp;view=image_only"><img src="mobile/images/image_only.gif" /></a>
    		<#break/>
    		<#case "image_only">
      			<a href="${thisPage}?${queryStringForPresentation?html}&amp;view=mixed"><img src="mobile/images/mixed.gif" /></a>
				<a href="${thisPage}?${queryStringForPresentation?html}&amp;view=text_only"><img src="mobile/images/text_only.gif" /></a>
				<a class="buttonpressed" href="${thisPage}?${queryStringForPresentation?html}&amp;view=image_only"><img src="mobile/images/image_only.gif" /></a>
    		<#break/>
    		<#default>
      			<a class="buttonpressed" href="${thisPage}?${queryStringForPresentation?html}&amp;view=mixed"><img src="mobile/images/mixed.gif" /></a>
				<a href="${thisPage}?${queryStringForPresentation?html}&amp;view=text_only"><img src="mobile/images/text_only.gif" /></a>
				<a href="${thisPage}?${queryStringForPresentation?html}&amp;view=image_only"><img src="mobile/images/image_only.gif" /></a>
    		<#break/>
		</#switch>
	</#if>   
</#macro>

