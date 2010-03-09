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
    <@treasures />
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

