<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">

<#include "inc_header.ftl">

<div class="searchbox">
	<form method="get" action="brief-doc.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
		<fieldset>
			<input type="hidden" name="start" value="1" />
			<input type="hidden" name="view" value="${view}" />
        	<input class="txt-input" name="query" id="query" type="text" title="Europeana Search" maxlength="75"
                <#if proposedSearchTerms?? && proposedSearchTerms?first??>
                 placeholder="${proposedSearchTerms?first.proposedSearchTerm}"
                </#if>
            />
			<input id="submit_search" type="hidden" value="<@spring.message 'Search_t' />" />
		</fieldset>
    </form>
</div>

<div id="content">
	<ul class="pageitem">
		<li class="textbox" id="welcome">
			<img src="mobile/images/logo_connect.png" alt="Logo"/>
			<strong><@spring.message 'ThisIsEuropeana_t' />...</strong> 
			<@spring.message 'APlaceToShareIdeas_t' />
		</li>

		<#if coverflow_enabled?? && coverflow_enabled=true>
            <#-- Flow container -->
		    <li class="textbox" id="myFlow">
                <div class="zflow">
				    <div id="container" class="centering">
				        <div id="tray" class="tray"></div>
				    </div>
				    <div id="flowcaption" class="flowcaption"></div>
	    	    </div>
	    	</li>
	    <#else>
	        <li class="textbox" id="treasures">
	            <@treasures />
	        </li>
        </#if>

		<li class="menu"><a href="new-content.html">
			<span class="name"><@spring.message 'NewContent_t' /></span><span class="arrow"></span></a>
		</li>
		
		 <li class="select">
		 	<#include "../../language_select.ftl" >
	        <span class="arrow"></span>
	     </li>
	</ul>
</div>

<#include "../inc_footer.ftl"/>

</#compress>

