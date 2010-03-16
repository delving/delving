<#-- no 'more' area available on mobile devices -->

<#assign formatArr = result.fullDoc.dcFormat + result.fullDoc.dcTermsExtent + result.fullDoc.dcTermsMedium />
<#assign providerArr = result.fullDoc.europeanaProvider + result.fullDoc.europeanaCountry />
<#assign sourceArr = result.fullDoc.dcSource />

<ul class="autolist">
	<li class="title">
	    <div id="resultinformation_title">
		    <#assign tl = "">
            <#if !model.fullDoc.dcTitle[0]?matches(" ")>
                <#assign tl= result.fullDoc.dcTitle[0]>
            <#elseif !model.fullDoc.dcTermsAlternative[0]?matches(" ")>
                <#assign tl=result.fullDoc.dcTermsAlternative[0]>
            <#else>
                <#assign tl = result.fullDoc.dcDescription[0] />
                <#if tl?length &gt; 50>
                    <#assign tl = result.fullDoc.dcDescription[0]?substring(0, 50) + "..."/>
        	    </#if>
            </#if>
            <@stringLimiter "${tl}" "150"/>
        </div>
    </li>
    <li class="textbox" id="detailviewbox">
		<#if useCache="true">
			<img src="${cacheUrl}uri=${result.fullDoc.thumbnails[0]?url('utf-8')}&amp;size=FULL_DOC&amp;type=${result.fullDoc.europeanaType}" 
			id="detailview" alt="Image title: ${result.fullDoc.dcTitle[0]}" />
		<#else>
        	<script>
        		function checkSize(h){
            		if (h > 300) {
                		h = 200;
                    	document.getElementById("imgview").height=h;
					}
				}
			</script>
			<img src="${result.fullDoc.thumbnails[0]}"
				alt="Image title: ${result.fullDoc.dcTitle[0]}"
                class="detailview"
                id="imgview"
                onload="checkSize(this.height);"
                onerror="showDefaultLarge(this,'${result.fullDoc.europeanaType}')"
                alt="<@spring.message 'ViewInOriginalContext_t' />  <@spring.message 'OpensInNewWindow_t'/>"/>
		</#if>         
	</li>

	<#-- TITLE   -------------------------------------------------------------------------------->
    <#assign titleArr = result.fullDoc.dcTitle + result.fullDoc.dcTermsAlternative />
    <#if isNonEmpty(titleArr)>
    	<li class="textbox"><strong><@spring.message 'dc_title_t' />:</strong>
        	<@simple_list_dual result.fullDoc.dcTitle result.fullDoc.dcTermsAlternative '<br />'/>
		</li>
	</#if>
    <#-- DC DATE, DC TERMS CREATED, DC TERMS ISSUED --------------------------------->
    <#assign dateArr = result.fullDoc.dcDate + result.fullDoc.dcTermsCreated + result.fullDoc.dcTermsIssued />
    <#if isNonEmpty(dateArr)>
    	<li class="textbox"><strong><@spring.message 'dc_date_t' />:</strong>
        	<@simple_list dateArr ';&#160;'/>
		</li>
	</#if>
    <#-- DC CREATOR    -------------------------------------------------------------------------------->
    <#assign creatorArr = model.fullDoc.dcCreator + model.fullDoc.dcContributor />
    <#if isNonEmpty(creatorArr)>
    	<li class="textbox"><strong><@spring.message 'Creator_t' />:</strong>
        	<@simple_list_dual model.fullDoc.dcCreator model.fullDoc.dcContributor ';&#160;'/>
		</li>
	</#if>
    <#-- DC DESCRIPTION -------------------------------------------------------------------------------->
    <#assign descriptionArr = model.fullDoc.dcDescription />
    <#if isNonEmpty(descriptionArr)>
    	<li class="textbox"><strong><@spring.message 'Description_t' />:</strong>
        	<@simple_list_truncated descriptionArr "<br/>" "800"/>
		</li>
	</#if>
    <#-- LANGUAGE      -------------------------------------------------------------------------------->
    <#assign languageArr = result.fullDoc.dcLanguage />
    <#if isNonEmpty(languageArr)>
    	<li class="textbox"><strong><@spring.message 'languageDropDownList_t' />:</strong>
        	<@simple_list languageArr ';&#160;'/>
		</li>
	</#if>
    <#-- DC FORMAT   -------------------------------------------------------------------------------->
    <#if isNonEmpty(formatArr)>
    	<li class="textbox"><strong><@spring.message 'dc_format_t' />:</strong>
        	<@simple_list formatArr ';&#160;'/>
		</li>
	</#if>
    <#-- DC SOURCE     -------------------------------------------------------------------------------->
    <#if isNonEmpty(sourceArr)>
    	<li class="textbox"><strong><@spring.message 'dc_source_t' />:</strong>
        	<@simple_list sourceArr '<br/>'/>
		</li>
	</#if>
    <#-- DC RIGHTS     -------------------------------------------------------------------------------->
    <#assign rightsArr = result.fullDoc.dcRights />
    <#if isNonEmpty(rightsArr)>
    	<li class="textbox"><strong><@spring.message 'dc_rights_t' />:</strong>
        	<@simple_list rightsArr ';&#160;'/>
		</li>
	</#if>
    <#-- Europeana PROVIDER   -------------------------------------------------------------------------------->
	<#if isNonEmpty(providerArr) >
    	<li class="textbox"><strong><@spring.message 'Provider_t' />:</strong>
        	<#if isNonEmpty(result.fullDoc.europeanaProvider) && isNonEmpty(result.fullDoc.europeanaCountry)>
				${result.fullDoc.europeanaProvider[0]};&#160; ${result.fullDoc.europeanaCountry[0]}
			<#elseif isNonEmpty(result.fullDoc.europeanaProvider)>
            	${result.fullDoc.europeanaProvider[0]}
			<#elseif isNonEmpty(result.fullDoc.europeanaCountry)>
				${result.fullDoc.europeanaCountry[0]}
			</#if>
		</li>
	</#if>
                        
    <#-- currently: fake geo data! TODO: change when geo data is available -->                    
    <li class="textbox">
    	<a href="http://maps.google.com/maps/ms?f=q&hl=fr&geocode=&ie=UTF8&msa=0&msid=106431329189139452431.000458c25e140ead6df80&ll=43.313438,3.417091&spn=0.084934,0.22316&z=13">
        	<img src="mobile/iwebkit/thumbs/maps.png" />
            <span class="maplink">Locate in map application</span>
            <span class="arrow"></span>
		</a>
	</li>
                        
	<li class="textbox">
    	<#assign UrlRef = "#"/>
		<#if !result.fullDoc.europeanaIsShownAt[0]?matches(" ")>
        	<#assign UrlRef = result.fullDoc.europeanaIsShownAt[0]/>
		<#elseif !result.fullDoc.europeanaIsShownBy[0]?matches(" ")>
        	<#assign UrlRef = result.fullDoc.europeanaIsShownBy[0]/>
		</#if>
        <a class="originalcontext" href="redirect.html?shownAt=${UrlRef?url('utf-8')}&provider=${result.fullDoc.europeanaProvider[0]}&id=${result.fullDoc.id}"
			target="_blank"
			alt="<@spring.message 'ViewInOriginalContext_t' /> - <@spring.message 'OpensInNewWindow_t'/>"
			title="<@spring.message 'ViewInOriginalContext_t' /> - <@spring.message 'OpensInNewWindow_t'/>">
			<@spring.message 'ViewInOriginalContext_t' />
			<span class="arrow"></span>
		</a>
	</li>          
</ul>