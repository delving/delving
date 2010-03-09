
<#-- macros from index_orig -->
<#macro localeBased type toSwitch>
    <#-- if locale is something like en_us, then shorten to en -->
    <#if locale?length &gt; 2>
        <#assign locale =  locale?substring(0, 2)>
    </#if>
    <#if type == "include">
        <#-- current available languages -->
        <#assign langs = ["ca","cs","da","de","el","en","es","et","fi","fr","ga","hu","is","it","lt","lv","mt","nl","no","pl","pt","sk","sl","sv"]>
    <#if toSwitch == "inc_usingeuropeana_$$.ftl">
    <#-- current available languages for this particular file -->
        <#assign langs = ["de","en","es","fr"]>
    </#if>
    <#if langs?seq_index_of(locale) != -1>
        <#include "${toSwitch?replace('$$', locale)}">
    <#else>
        <#include "${toSwitch?replace('$$','en')}">
    </#if>
    <#else>
        <img src="images/${toSwitch?replace('$$',locale)!'think_culture_en_small.gif'}" alt=""/>
    </#if>
</#macro>

<#-- shorten a given string and append with ellipses -->
<#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
    <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
    ${newStr}
</#macro>


<#macro SearchForm className>

    <#assign showAdv="none"/>
    <#assign showSim="block"/>
    <#if pageId??>
    <#if pageId=="adv">
        <#assign showAdv="block"/>
        <#assign showSim="none"/>
    </#if>
</#if>

    <div id="search_simple" class="${className}" style="display:${showSim};">
        <#if result?? >
            <#if result.badRequest?? >
                <span style="font-style: italic;">Wrong query. ${result.errorMessage}</span>
            </#if>
        </#if>
        <form method="get" action="brief-doc.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <input class="txt-input" name="query" id="query" type="text" title="Europeana Search" maxlength="75"
                <#if proposedSearchTerms?? && proposedSearchTerms?first??>
                 placeholder="${proposedSearchTerms?first.proposedSearchTerm}"
                </#if>
            />
            <input id="submit_search" type="submit" value="<@spring.message 'Search_t' />" />
        </form>
    </div>
</#macro>


<#macro treasures>
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
<#--                <#assign title = ""/>
                    <#if doc.title??>
                      <#assign title = doc.title />
                    </#if>
                    <#if (title?length <= 1)>
                      <#assign title = "..." />
                    </#if>
    -->
                   </#if>
    </#list>
</#macro>


<#-- macros from brief-doc-window -->

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


<#-- macros from full-doc -->
<#macro show_array_values fieldName values showFieldName>
    <#list values as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#if showFieldName>
                <p><strong>${fieldName}</strong> = ${value?html}</p>
            <#else>
                <p>${value?html}</p>
            </#if>
        </#if>
    </#list>
</#macro>

<#macro show_value fieldName value showFieldName>
    <#if showFieldName>
        <p><strong>${fieldName}</strong> = ${value}</p>
    <#else>
        <p>${value}</p>
    </#if>
</#macro>

<#macro simple_list values separator>
    <#list values?sort as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            ${value}<#if value_has_next>${separator} </#if>
    <#--${value}${separator}-->
        </#if>
    </#list>
</#macro>

<#macro simple_list_dual values1 values2 separator>
    <#if isNonEmpty(values1) && isNonEmpty(values2)>
        <@simple_list values1 separator />${separator} <@simple_list values2 separator />
    <#elseif isNonEmpty(values1)>
        <@simple_list values1 separator />
    <#elseif isNonEmpty(values2)>
        <@simple_list values2 separator />
    </#if>
</#macro>

<#macro simple_list_truncated values separator trunk_length>
    <#list values?sort as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <@stringLimiter "${value}" "${trunk_length}"/><#if value_has_next>${separator} </#if>
    <#--${value}${separator}-->
        </#if>
    </#list>
</#macro>

<#function isNonEmpty values>
    <#assign nonEmptyValue = false />
    <#list  values?reverse as value>
        <#if !value?matches(" ") && !value?matches("0000")>
            <#assign nonEmptyValue = true />
            <#return nonEmptyValue />
        </#if>
    </#list>
    <#return nonEmptyValue />
</#function>

<#macro resultnavigation>
<#if pagination??>
    <#if pagination.previous>
        <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.previousInt?c}&amp;uri=${pagination.previousUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}" class="pagination">&lt;</a>
    </#if>
    &#160;&#160;
    <#if pagination.next>
        <a href="full-doc.html?${queryStringForPaging?html}&amp;start=${pagination.nextInt?c}&amp;uri=${pagination.nextUri}&amp;view=${view}&amp;pageId=${pagination.pageId}&amp;tab=${pagination.tab}" class="pagination">&gt;</a>
    </#if>
</#if>
</#macro>