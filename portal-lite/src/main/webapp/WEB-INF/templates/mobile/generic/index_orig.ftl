<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">

<#include "../inc_header.ftl">

<center>
<div id="logo">
	<a href="index.html"><img src="mobile/images/logo_slogan.png" alt="Logo"/></a>
</div>
<div id="language_select">
    <#include "../../language_select.ftl">
</div>

<div id="welcome">

    <div id="thisis">
    <strong><@spring.message 'ThisIsEuropeana_t' />...</strong>
			<@spring.message 'APlaceToShareIdeas_t' />
    </div>
</div>
<div id="search">
                      <@SearchForm "search_result"/>
</div>
<div id="treasures">
<#assign x = 0>

    			<#list carouselItems as carouselItem>
    <#assign x = x + 100>
				 <#if carouselItem_index <=12 && x <= device_screen_width> <#-- we only want to see a maximum of 12 items, otherwise page-load will be too slow -->
                  <#assign doc = carouselItem.doc/>
                                   <a href="full-doc.html?uri=${doc.id}">
					<#if useCache="true">
                      <img src="${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&size=BRIEF_DOC&type=${doc.type}" />
                    <#else>
                      <img src="${doc.thumbnail}" />
					</#if>
</a>

                    <#assign title = ""/>
<#--                    <#if doc.title??>
                      <#assign title = doc.title />
                    </#if>
                    <#if (title?length <= 1)>
                      <#assign title = "..." />
                    </#if>
    -->
                   </#if>
             	</#list>
</div>
<div id="new_in_europeana">
 <a href="new-content.html"><@spring.message 'NewContent_t' /></a> <img src="mobile/images/arrowright.gif" />
</div>

<#-- this is useful for debugging
    Width: ${device_screen_width} <br />
    Height: ${device_screen_height}
-->

<#include "../inc_footer.ftl">


</#compress>

