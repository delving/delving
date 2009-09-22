<#assign tagList = tagList/>
<#--<?xml version="1.0" encoding="UTF-8"?>-->
<#--<ResultSet xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:yahoo:srch" xsi:schemaLocation="urn:yahoo:srch http://api.search.yahoo.com/WebSearchService/V1/WebSearchResponse.xsd" type="web" totalResultsAvailable="31500000" totalResultsReturned="100" firstResultPosition="1" moreSearch="">-->
<#--<#list tagList as tagCount>-->
<#--<Result>-->
    <#--<Title>${tagCount.tag}</Title>-->
    <#--<Count>${tagCount.count}</Count>-->
<#--</Result>-->
<#--<li onselect="this.text.value = '${tagCount.tag}'">-->
	<#--<span>${tagCount.count}</span>${tagCount.tag} -->
<#--</li>-->
<#--</#list>-->
<#--</ResultSet>-->
<#list tagList as tagCount>${tagCount.tag} </#list>

