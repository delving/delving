<#import "spring.ftl" as spring >
<#assign view = "table">
<#assign query = "">
<#assign cacheUrl = cacheUrl>
<#if RequestParameters.view??><#assign view = "${RequestParameters.view}"></#if>
<#if RequestParameters.query??><#assign query = "${RequestParameters.query}"></#if>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#compress>
<#include "inc_header.ftl">
<#include "inc_search_form.ftl">

<div id="doc4" class="yui-t7">

    <div id="hd"><#include "inc_top_nav.ftl"></div>

    <div id="bd">
        <div class="yui-gd" id="primary-content">
            <div class="yui-u first">
                <script type="text/javascript">
                    showRandomLogo();
                    showRandomSlogan();
                </script>
                <noscript>
                    <img src="images/think_culture_logo_top_2.gif" alt="logo Europeana think culture" title="logo Europeana think culture" />
                    <img src="images/think_culture_fr.gif" alt="slogan Europeana think culture" title="slogan Europeana think culture" />
                </noscript>
           </div>
            <div class="yui-u" id="primary-content-second">
                <div  class="yui-g" id="top">
                    <h1>
                        <span class="introKeyPhrase"><@spring.message 'ThisIsEuropeana_t' /></span> - <@spring.message 'APlaceToShareIdeas_t' />&#160;&#160;<a href="aboutus.html"><@spring.message 'FindOutMore_t' /></a>
                    </h1>
                </div>
                <div  class="yui-g" id="middle">
                    <div  id="search"><@SearchForm "search_home"/></div>
                </div>
                <div  class="yui-g" id="bottom">
                    <noscript>
                        <div class="attention" style="margin: 40px 0 0 50px; width:500px;">
                        	<@spring.message 'NoScript_t' />
                        </div>
                    </noscript>

                    <#--Carousel-->
                    <div id="carousel-script-wrapper">
                        <ul id="mycarousel" class="jcarousel-skin-tango">
                            <#list carouselItems as carouselItem>
                                <#assign doc = carouselItem.doc/>
                                <li class="category_${doc.type}">
                                    <a href="full-doc.html?uri=${doc.id}">
                                    	<#assign title = ""/>
                                        <#if doc.title??>
                                         	<#assign title = doc.title />
                                 		</#if>
                                        <#if (title?length <= 1)>
                                           	<#assign title = "..." />
                                  		</#if>
                                        <#if useCache="true">
                                            <img src="${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&size=BRIEF_DOC&type=${doc.type}"  onerror="showDefaultCarousel(this,'${doc.type}')" style="z-index:99" width="70" alt="<@stringLimiter '${title?html}' '50'/>" title="<@stringLimiter '${title?html}' '50'/>"/>
                                        <#else>
                                        	<img src="${doc.thumbnail}" onerror="showDefaultCarousel(this,'${doc.type}')" width="70" alt="<@stringLimiter '${title?html}' '50'/>" title="<@stringLimiter '${title?html}' '50'/>"/>
                                        </#if>
                                    </a>
                                </li>
                            </#list>
                        </ul>
                     </div>
                </div>
            </div>
        </div>
        <div class="yui-g"  id="secondaryContent">
            <div class="yui-g first">
                <div class="yui-u first">
                    <h4><@spring.message 'ShareYourIdeas_t' />:</h4>
                    <ul class="share-ideas">
                        <#--<li><a target="_blank" href="http://www.univie.ac.at/esec/php/wordpress/?p=459#more-459">Europeana wins Erasmus award</a><a target="_blank" href="http://www.univie.ac.at/esec/php/wordpress/?p=459#more-459"><img height="60" width="60" src="images/erasmus.png" class="erasmus"/></a></li>-->
                    <li>
                   <!-- AddThis Button BEGIN -->
                    <#assign  showthislang = locale>
                    <#if  locale = "mt" || locale = "et">
                       <#assign  showthislang = "en">
                    </#if>
                       <a class="addthis_button" addthis:url="http://www.europeana.eu/portal/images/think_culture_logo_top_4.jpg"
                          href="http://www.addthis.com/bookmark.php?v=250&amp;username=xa-4b4f08de468caf36">
                         <img src="http://s7.addthis.com/static/btn/lg-share-${showthislang}.gif" alt="Bookmark and Share" style="border:0"/></a>
                        <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=xa-4b4f08de468caf36"></script>
                        <script type="text/javascript">
                            var addthis_config = {
                                 ui_language: "${showthislang}"
                            }
                          </script>
                    </li>

                    </ul>
                </div>
                <div class="yui-u">
                        <h4><@spring.message 'PeopleAreCurrentlyThinkingAbout_t' />:</h4>
                    <ul class="people-talk">
                        <#list proposedSearchTerms as searchTerm>
                            <li><a href="brief-doc.html?query=${searchTerm.proposedSearchTerm}">${searchTerm.proposedSearchTerm}</a></li>
                        </#list>
                    </ul>
                </div>
            </div>
            <div class="yui-g">
                <div class="yui-u first">
                      <h4><@spring.message 'TimelineNavigator_t' />:</h4>
                    <ul class="timeline">
                      <li><a href="year-grid.html"><@spring.message 'BrowseThroughTime_t' />.</a></li>
                    </ul>
                </div>
                <div class="yui-u">
                      <h4><@spring.message 'NewContent_t' />:</h4>
                    <ul class="new-content">
                      <li><a href="new-content.html"><@spring.message 'FromOurParnters_t' /></a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <div id="ft"><#include "inc_footer.ftl"></div>
</div>
<script type="text/javascript">
    // only show carousel if javascript is active
    $("#carousel-script-wrapper").css("display","block");
    <#if user??>
         $(document).ready(function(){
            if ($.browser.mozilla) {
//                $(".jcarousel-skin-tango .jcarousel-container").css("position","absolute");
                $(".jcarousel-skin-tango .jcarousel-container").css("top","327px");
            }
            else if ($.browser.opera) {
//                $(".jcarousel-skin-tango .jcarousel-container").css("position","absolute");
                $(".jcarousel-skin-tango .jcarousel-container").css("top","329px");
            }
          });
    </#if>
</script>


<#--
    Oddly, on 150DPI IE7 normal font, the second grid div jumps below the first.
    So, when that happens, just setting the width with an extra pixel makes the grid jump into place again.
-->
<script type="text/javascript">$(document).ready(function(){

    if ( $.browser.msie )
    {
            var primary_content_offset_top = $("#primary-content").position().top + 100 ;
            var primary_content_second_offset_top = $("#primary-content-second").position().top ;

            if ( primary_content_offset_top < primary_content_second_offset_top )
                $("#primary-content").width( $("#primary-content").width() + 1 ) ;
    }

});</script>

</body>
</html>
</#compress>
