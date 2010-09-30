<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#include "inc_header.ftl">



<div id="main">


    <div class="grid_12 breadcrumb">
        <em>U bevindt zich op: </em>
        <span><a href="index.html" title="Homepagina">Home</a> <span class="imgreplacement">&rsaquo;</span></span> Onderwerpen
    </div>

    <div id="search" class="grid_8">

        <h1>Vind objecten uit de Digitale Collectie Nederland. </h1>

         <noscript>
            <div class="ui-widget grid_5 alpha">
                <div class="ui-state-highlight ui-corner-all" style="padding: 0pt 0.7em; margin-top: 20px;">
                    <@spring.message 'NoScript_t' />
                </div>
            </div>
        </noscript>

        <@SearchForm "search_home"/>

    </div>

    <div id="news" class="grid_4">
        <#-- content loaded via AJAX -->
    </div>

    <div class="clear"></div>

    <div class="grid_4">
        <div class="box open">
            <h4 class="head">Title</h4>
            <p>
                Sed consectetur purus ac erat semper condimentum. Nunc ultricies commodo velit, non posuere ligula ullamcorper porta. Sed sed turpis magna, vel mattis tellus. Donec sed mi dolor. Nam luctus massa eget odio accumsan vel dapibus quam varius. Donec ut mi nulla. Aliquam in consectetur purus. Nunc nec dui est. Suspendisse et gravida neque. Ut egestas purus justo.
            </p>
        </div>
    </div>
    <div class="grid_4">
        <div class="box open">
            <h4 class="head">Title</h4>
            <p>
                Etiam consequat egestas vehicula. Vivamus augue nulla, aliquam ut blandit et, vestibulum eu ligula. Suspendisse euismod dapibus tellus auctor convallis. Aliquam felis nulla, adipiscing ac ornare vitae, consectetur non odio. Nam in magna sit amet urna malesuada ultricies.
            </p>
        </div>
    </div>
    <div class="grid_4">
        <div class="box closed">
            <h4 class="head">Collectiewijzer</h4>

            <p class="intro">
                Netwerk voor het delen van kennis, praktijkervaring en nieuws over conservering, restauratie, beheer en behoud van cultureel erfgoed collecties.
            </p>

            <ul>
                <li><a href="http://www.collectiewijzer.nl">Lees het Collectiewijzer Blog</a></li>
                <li><a href="http://wiki.collectiewijzer.nl">Maak gebruik van de wiki </a></li>
                <li><a href="http://www.linkedin.com/groups?mostPopular=&amp;gid=2672013">Praat mee op LinkedIn </a> </li>
            </ul>
        </div>
    </div>

    <div class="clear"></div>

</div>
<script type="text/javascript">
    $.ajax({
      url: '/${portalName}/news.dml?onlyContent=true',
      type: "GET",
        success: function(data) {
            if(data == "This page does not exist."){
                <#if !user??>
                $('#news').html("<h2>Nieuws<\/h2><p>Geen nieuws items</p>");
                <#elseif (user??) && ((user.role=="ROLE_GOD") || (user.role=="ROLE_GOD"))>
                $('#news').html("<h2>Nieuws<\/h2><p><a href=\"_news.dml\">Niews pagina aanmaken</a></p>");
                </#if>
            }else{
                $('#news').html(data);
            }
        }
});
</script>
<#include "inc_footer.ftl"/>
</#compress>

