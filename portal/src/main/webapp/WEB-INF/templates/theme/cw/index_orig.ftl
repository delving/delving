<#compress>
<#assign thisPage = "index.html">
<#assign pageId = "in">
<#include "inc_header.ftl">



<div id="main">

    <div id="breadcrumbs" class="grid_12">
        breadcrumbs
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
        <h3>Nieuws</h3>
        <ul>
            <li><a href="">Hkj;io aoiafia faifaif af</a></li>
            <li><a href="">Hkj;io aoiafia faifaif af</a></li>
            <li><a href="">Hkj;io aoiafia faifaif af</a></li>
            <li><a href="">Hkj;io aoiafia faifaif af</a></li>
        </ul>
    </div>

    <div class="clear"></div>

    <div class="grid_4">
        <div class="box open">
            <h4 class="head">Title</h4>
            <p>
                asdfasfasf dsaf asdf sdf dsfasdf asdf
                asdf asdf asdfasdf asdf asdfadsf
                asffaffdafdfsasfasf adsfdsafdasf asdf dsf
                afafdfasdfsdfasdfadsfadsf
            </p>
        </div>
    </div>
    <div class="grid_4">
        <div class="box open">
            <h4 class="head">Title</h4>
            <p>
                asdfasfasf dsaf asdf sdf dsfasdf asdf
                asdf asdf asdfasdf asdf asdfadsf
                asffaffdafdfsasfasf adsfdsafdasf asdf dsf
                afafdfasdfsdfasdfadsfadsf
            </p>
        </div>
    </div>
    <div class="grid_4">
        <div class="box closed">
            <h4 class="head">Collectiewijzer</h4>

            <p class="intro">Netwerk voor het delen van kennis, praktijkervaring en nieuws over conservering, restauratie, beheer en behoud van cultureel erfgoed collecties. </p>
            <ul>
                <li><a href="http://www.collectiewijzer.nl">Lees het Collectiewijzer Blog</a></li>
                <li><a href="http://wiki.collectiewijzer.nl">Maak gebruik van de wiki </a></li>
                <li><a href="http://www.linkedin.com/groups?mostPopular=&amp;gid=2672013">Praat mee op LinkedIn </a> </li>
            </ul>
        </div>
    </div>






</div>

<#include "inc_footer.ftl"/>
</#compress>

