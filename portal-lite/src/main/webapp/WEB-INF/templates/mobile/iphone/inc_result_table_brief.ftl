<#compress>

<#if seq?size &gt; 0>
	<#if view = "image_only">
		<@show_result_flow seq = seq/>
    <#elseif view = "text_only">
        <@show_result_list seq = seq/>
    <#else> <#-- mixed -->
        <@show_result_table seq = seq/>
    </#if>
<#else>
    <div id="no-result"><@spring.message 'NoItemsFound_t' /></div>
</#if>


<#-- text/image mixed display -->
<#macro show_result_table seq>

<#list seq as cell>
<li class="withimage" >
	<a id="${cell.type}_gradientitem" href="full-doc.html?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">

	<#if useCache="true">
		<img src="${cacheUrl}uri=${cell.thumbnail?url('utf-8')}&amp;size=BRIEF_DOC&amp;type=${cell.type}" />
	<#else>
		<img src="${cell.thumbnail}" onerror="showDefaultSmall(this,'${cell.type}')" />
	</#if>

	<#if !cell.creator[0]?matches(" ")>
		<span class="comment">${cell.creator}</span>
	</#if>

	<span class="name">${cell.title}</span>

	<#if !cell.provider?matches(" ")>
    	<span class="iteminfo">
        	<#if !cell.year?matches(" ")>
				<#if cell.year != "0000">
					${cell.year},
				</#if>
			</#if>
        	${cell.provider}
        </span>
	<#else>
    	<#if !cell.year?matches(" ")>
			<#if cell.year != "0000">
				<span class="iteminfo">${cell.year}</span>
			</#if>
		</#if>
	</#if>
	<span class="arrow"></span>
	</a>
</li>
</#list>
</#macro>

<#-- text only display -->
<#macro show_result_list seq>
<#list seq as cell>
 <li class="textonly">
	<a id="${cell.type}item" href="full-doc.html?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${cell.index?c}&amp;startPage=${pagination.start?c}&amp;uri=${cell.id}&amp;view=${view}&amp;pageId=brd">
		<strong>${cell.title}</strong><br />
		<#if !cell.creator[0]?matches(" ")>${cell.creator}<br/></#if>
		<#if !cell.year?matches(" ")><#if cell.year != "0000">${cell.year}<br/></#if></#if>
		<#if !cell.provider?matches(" ")>${cell.provider}</#if>
		<span class="arrow"></span>
	</a>
 </li>
</#list>
</#macro>

<#-- image only display -->
<#macro show_result_flow seq>
 <div id="imagelist">
   <#list seq as briefDoc>
		<a class="imageresult" id="${briefDoc.type}item" href="full-doc.html?${queryStringForPresentation}&amp;tab=${tab}&amp;start=${briefDoc.index?c}&amp;startPage=${pagination.start?c}&amp;uri=${briefDoc.id}&amp;view=${view}&amp;pageId=brd">
			<#if useCache="true">
				<img src="${cacheUrl}uri=${briefDoc.thumbnail}&amp;size=BRIEF_DOC&amp;type=${briefDoc.type}" />
			<#else>
				<img src="${briefDoc.thumbnail}" onerror="showDefaultSmall(this,'${briefDoc.type}')" />
			</#if>
		</a>
  </#list>
 </div>
</#macro>

</#compress>