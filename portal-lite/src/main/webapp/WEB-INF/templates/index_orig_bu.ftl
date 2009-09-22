<#import "spring.ftl" as spring >
<#assign view = "table">
<#assign query = "">
<#assign recentQueries = recentQueries>
<#if RequestParameters.view?exists><#assign view = "${RequestParameters.view}"></#if>
<#if RequestParameters.query?exists><#assign query = "${RequestParameters.query}"></#if>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#compress>
<#include "inc_header.ftl">
<#include "inc_search_form.ftl">
<script>
<#--
// You must create the carousel after the page is loaded since it is
// dependent on an HTML element (in this case 'mycarousel'.) See the
// HTML code below.
-->
var carousel; // for ease of debugging; globals generally not a good idea
var pageLoad = function()
{
    var csize = 19; var rsize = ${recent?size};
    if (rsize < csize){csize = rsize;}
	carousel = new YAHOO.extension.Carousel("mycarousel",
		{
			numVisible:        5,
			animationSpeed:    0.65,
			scrollInc:         2,
			navMargin:         50,
			prevElement:     "prev-arrow",
			nextElement:     "next-arrow",
			size:              csize,
			prevButtonStateHandler:   handlePrevButtonState,
			nextButtonStateHandler:   handleNextButtonState
		}
	);
};
YAHOO.util.Event.addListener(window, 'load', pageLoad);
</script>
<body id="home">
<div id="doc4" class="yui-t7">
    <div id="hd"><#include "inc_top_nav.ftl"></div>
    <div id="bd">
        <div class="yui-gd" id="primary-content">
            <div class="yui-u first">
                <script>showRandomLogo();</script>
                <script>showRandomSlogan();</script>
                <noscript>
                    <img src="images/think_culture_logo_top_2.gif" alt="logo Europeana think culture" title="logo Europeana think culture" />
                    <img src="images/think_culture_fr.gif" alt="slogan Europeana think culture" title="slogan Europeana think culture" />
                </noscript>
            </div>
            <div class="yui-u">
                <div  class="yui-g" id="top">
                    <h1><span class="introKeyPhrase"><@spring.message 'ThisIsEuropeana_t' /></span> - <@spring.message 'APlaceToShareIdeas_t' /><a href="aboutus.html"><@spring.message 'FindOutMore_t' /></a>.</h1>
                </div>
                <div  class="yui-g" id="middle">
                    <div  id="search"><@SearchForm "search_home"/></div>
                </div>
                <div  class="yui-g" id="bottom">
                    <div id="mycarousel" class="carousel-component">
                        <div class="carousel-prev"> <img id="prev-arrow" class="left-button-image" src="images/left-enabled.gif" alt="Previous Button" title="<@spring.message 'Previous_t' />"/> </div>
                        <div class="carousel-next"> <img id="next-arrow" class="right-button-image" src="images/right-enabled.gif" alt="Next Button" title="<@spring.message 'Next_t' />"/> </div>
                        <div class="carousel-clip-region">
                          <ul class="carousel-list" id="carousel-list">
                              <#--<li><img src="images/item-page.gif" /></li>-->
                              <#--<li><img src="images/item-image.gif" /></li>-->
                              <#--<li><img src="images/item-video.gif" /></li>-->
                              <#--<li><img src="images/item-sound.gif" /></li>-->
                              <#--<li><img src="images/item-page.gif" /></li>-->
                              <#--<li><img src="images/item-image.gif" /></li>-->
                              <#--<li><img src="images/item-video.gif" /></li>-->
                              <#--<li><img src="images/item-sound.gif" /></li>-->
                            <#list recent as latest>
                               <#assign i = latest_index +1><#assign tl = latest.title />
                               <#if tl?length &gt; 50><#assign tl = latest.title?substring(0, 50) + "..."/></#if>
                               <li id="mycarousel-item-${i}" class="category_${latest.type}">
                                    <label id="itemlb_${i}" class="speechbubble"><@stringLimiter "${latest.title}" "50"/></label>
                                    <a href="full-doc.html?uri=${latest.id}"  onmouseover="showCaption('itemlb_${i}');" onmouseout="hideCaption('itemlb_${i}');">
                                        <#if useCache="true">
                                            <img src="${cacheUrl}uri=${latest.thumbnail?url('utf-8')}&size=BRIEF_DOC&type=${latest.type}" alt="item ${i}" id="item_${i}" style="z-index:99"/>
                                        <#else>
                                            <img src="${latest.thumbnail}" alt="item ${i}" id="item_${i}" style="z-index:99" onerror="showDefault(this,'${latest.type}')"/>
                                        </#if>
                                    </a>
                               </li>
                            </#list>
                          </ul>
                        </div>
				    </div>
                </div>
            </div>
        </div>
        <div class="yui-g"  id="secondaryContent">
            <div class="yui-g first">
                <div class="yui-u first">
                    <ul class="share-ideas">
                      <h1><@spring.message 'ShareYourIdeas_t' />:</h1>
                      <li><a href="#"><@spring.message 'Tagging_t' /></a></li>
                      <li><a href="contact.html"><@spring.message 'SendUsFeedback_t' /></a></li>
                    </ul>
                </div>
                <div class="yui-u">
                    <ul class="people-talk">
                        <h1><@spring.message 'PeopleAreCurrentlyThinkingAbout_t' />:</h1>
                        <#list recentQueries as queryCount>
                        <#assign showThought = queryCount.query>
                        <#if showThought?length &gt; 30>
                            <#assign showThought = queryCount.query?substring(0, 30) + "...">
                        </#if>
                           <li><a href="brief-doc.html?query=${queryCount.query?url('utf-8')}">${showThought}</a></li>
                           <#if queryCount_index &gt;= 2><#break></#if>
                        </#list>
                    </ul>
                </div>
            </div>
            <div class="yui-g">
                <div class="yui-u first">
                    <ul class="timeline">
                      <h1><@spring.message 'TimelineNavigator_t' />:</h1>
                      <li><a href="year-grid.html"><@spring.message 'BrowseThroughTime_t' />.</a></li>
                    </ul>
                </div>
                <div class="yui-u">
                    <ul class="new-content">
                      <h1><@spring.message 'NewContent_t' />:</h1>
                      <li><a href="new-content.html"><@spring.message 'FromOurParnters_t' /></a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <div id="ft"><#include "inc_footer.ftl"></div>
</div>

</body>
</html>
</#compress>