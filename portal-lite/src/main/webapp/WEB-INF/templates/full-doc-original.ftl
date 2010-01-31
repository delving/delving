<#import "spring.ftl" as spring />
<#assign model = result/>
<#assign result = result/>
<#assign uri = uri>
<#assign view = "table"/>
<#assign thisPage = "full-doc.html"/>
<#if startPage??><#assign startPage = startPage/></#if>
<#if RequestParameters.view??> <#assign view = "${RequestParameters.view}"/></#if>
<#if format??><#assign format = format/></#if>
<#if queryStringForPaging??><#assign queryStringForPaging = queryStringForPaging />
    <#assign defaultQueryParams = "full-doc.html?"+queryStringForPaging+"&start="+docIdWindow.offset+"&uri="+result.fullDoc.id+"&view="+view />
<#else>
    <#assign defaultQueryParams = "full-doc.html?uri="+result.fullDoc.id />
</#if>
<#if result.fullDoc.dcTitle[0]?length &gt; 110>
    <#assign postTitle = result.fullDoc.dcTitle[0]?substring(0, 110)?url('utf-8') + "..."/>
<#else>
    <#assign postTitle = result.fullDoc.dcTitle[0]?url('utf-8')/>
</#if>
<#if result.fullDoc.dcCreator[0]?matches(" ")>
    <#assign postAuthor = "none"/>
<#else>
    <#assign postAuthor = result.fullDoc.dcCreator[0]/>
</#if>
<#if hasPrevious??><#assign hasPrevious = hasPrevious></#if>
<#if RequestParameters.query??><#assign query = "${RequestParameters.query?url('utf-8')}"/></#if>
<#include "inc_search_form.ftl"/>
<#if docIdWindow??><#assign docIdWindow = docIdWindow/></#if>
<#include "inc_header.ftl">

<script type="text/javascript">
    $(document).ready(function() {
        $('div.toggler-c').toggleElements(
        { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
        $('ul.toggler-c').toggleElements();
    });
    function toggleObject(oId) {
        var oObj = document.getElementById(oId);
        var cObj = (oObj.style.display == "none") ? "block" : "none";
        oObj.style.display = cObj;
    }
</script>

<script type="text/javascript">

    var handleSuccess = function(o) {
        var sr = document.getElementById("msg-save-item");
        sr.style.display = 'block';
        sr.innerHTML = "<@spring.message 'ItemSaved_t' />";
    };

    var handleFailure = function(o) {
        var sr = document.getElementById("msg-save-item");
        sr.style.display = 'block';
        sr.innerHTML = "<span class='fg-red'><@spring.message 'ItemSaveFailed_t' /></span>";
    };

    var callback =
    {
        success:handleSuccess,
        failure: handleFailure
    };

    var sUrl = "save-item.ajax";
    var title = "${postTitle}";
    var postData = "title=" + title +  "&author=${postAuthor}&europeanaUri=${result.fullDoc.id}&europeanaObject=${result.fullDoc.thumbnails[0]}";

    function saveItem() {
        var request = YAHOO.util.Connect.asyncRequest('POST', sUrl, callback, postData);
    }
    ;


    var handleSuccess2 = function(o) {
        var response = o.responseXML;
        var status = response.getElementsByTagName('success')[0].childNodes[0].nodeValue;
        var sr = document.getElementById("msg-save-tag");
        if (status == "true") {
            sr.style.display = 'block';
            sr.innerHTML = "<@spring.message 'TagAdded_t' />";
            document.getElementById('tag').value = "";
            //setTimeout("window.location.reload();","2000");

        }
        else {
            //alert(o.responseText);
            handleFailure2(o);
        }
    };
    var handleFailure2 = function(o) {
        var sr = document.getElementById("msg-save-tag");
        sr.style.display = 'block';
        sr.innerHTML = "<span class='fg-red'><@spring.message 'TagAdditionFailed_t' /></span>";
    };
    var callback2 =
    {
        success:handleSuccess2,
        failure: handleFailure2
    };

    function addTag(tagText) {
        var tagUrl = "save-social-tag.ajax";
        var title = "${postTitle}";
        var tagData = "europeanaUri=${result.fullDoc.id}&europeanaObject=${result.fullDoc.thumbnails[0]}&title="+title+"&tag=" + encodeURIComponent(tagText);
        var request = YAHOO.util.Connect.asyncRequest('POST', tagUrl, callback2, tagData);
    }
    ;

    var sendEmailAction = "email-to-friend.ajax";


    var handleSuccessEmail = function(o) {
        //alert("mail sent");
        var response = o.responseXML;
        var status = response.getElementsByTagName('success')[0].childNodes[0].nodeValue;
        if (status == "true") {
            var mr = document.getElementById("msg-send-email");
            mr.style.display = "block";
            mr.innerHTML = "<@spring.message 'EmailSent_t' />";
            document.getElementById("friendEmail").value = "";
            setTimeout("window.location.reload();", "1500");
        }
        else {
            handleFailureEmail();
        }
    };
    var handleFailureEmail = function(o) {
        var mr = document.getElementById("msg-send-email");
        mr.style.display = "block";
        mr.innerHTML = "<span class='fg-red'><@spring.message 'EmailSendFailed_t' /><span>";
    };
    var callBackEmail = {
        success:handleSuccessEmail,
        failure:handleFailureEmail

    };
    function sendEmail(email) {
        var email = document.getElementById("friendEmail").value;
        var emailData = encodeURI("uri=${result.fullDoc.id}&email=" + email);
        var request = YAHOO.util.Connect.asyncRequest("POST", sendEmailAction, callBackEmail, emailData);

    }

</script>

<#if result.fullDoc.thumbnail?size &gt; 1>
<!-- smooth gallery -->
<link rel="stylesheet" href="css/jd.gallery.css" type="text/css" media="screen" charset="utf-8"/>
<script src="javascript/mootools.namespaced.js" type="text/javascript"></script>
<script src="javascript/jd.gallery.namespaced.js" type="text/javascript"></script>

<script type="text/javascript">
    function startGallery() {
        var myGallery = new gallery(Moo.$('myGallery'), {
            timed: false
        });
    }
    window.onDomReady(startGallery);
</script>
</#if>


<style type="text/css">
    #ysearchautocomplete {
        margin-bottom: 2em;
        width: 130px;
    }

    #ysearchautocomplete form .button {
        position: absolute;
        left: 0px;
        top: 24px;
        clear: both;
        margin-bottom: 40px;
        float: left;
    }

    /* styles for prehighlighted result item */
    .yui-skin-sam .yui-ac-content li.yui-ac-prehighlight {
        background: #B3D4FF;
    }

    /* styles for highlighted result item */
    .yui-skin-sam .yui-ac-content li.yui-ac-highlight, .yui-skin-sam .yui-ac-content li.yui-ac-highlight span {
        background: #26a097;
        color: #FFF;
    }

</style>

<body class="yui-skin-sam">
<div id="doc4" class="yui-t2">

<div id="hd">
    <#include "inc_top_nav.ftl"/>
</div>
<div id="bd">
<div id="yui-main">
<div class="yui-b">
<div class="yui-g" id="search">

    <@SearchForm "search_result"/>

</div>
<div class="yui-g">

<div id="breadcrumb">
    <#if query?exists>
    <ul>
        <li class="first"><@spring.message 'YouHaveSearchFor_t' />:</li>
        <li><strong><a href="#">${query}</a></strong></li>
    </ul>
    <#else>
    <ul>
        <li>&#160;</li>
    </ul>
    </#if>
</div>

<div id="navResultTabs">
    <ul>
        <li class="selected"><a><em><@spring.message 'ItemDetails_t'/></em></a></li>
        <li><a><em>&#160;</em></a></li>
        <li><a><em>&#160;</em></a></li>
        <li><a href="${defaultQueryParams}&format=srw"><em><@spring.message 'ViewAsXML_t'/></em></a></li>
        <li>
            <#if format?? && format?matches("labels")>
                <a href="${defaultQueryParams}"><em><@spring.message 'ViewWithoutLabels_t'/></em></a>
            <#else>
                <a href="${defaultQueryParams}&format=labels"><em><@spring.message 'ViewWithLabels_t'/></em></a>
            </#if>
        </li>
        <!--<li><a><em>&#160;</em></a></li>-->
    </ul>
</div>


<div class="pagination">
    <div class="viewselect">
        <#if queryStringForPaging?exists>
        <a href="brief-doc.html?${queryStringForPaging}&start=${startPage}&view=${view}">
            <img src="images/arr-up.gif" alt="previous button" hspace="5" width="7" height="9"
                 alt="click to return to results page"/>
            <span><@spring.message 'ReturnToResults_t' /></span>
        </a>
        </#if>
    </div>
    <div class="nav" id="full">
        <#if docIdWindow?exists>
        <ul>
            <li>
                <#if hasPrevious>
                <a href="full-doc.html?${queryStringForPaging}&start=${docIdWindow.offset}&uri=${docIdWindow.ids[0]}&view=${view}">
                    <img src="images/arr-left.gif" alt="previous button" hspace="5" width="9" height="7"
                         alt="click here for previous item"/>
                </a>
                <#assign docNext = docIdWindow.offset + 2/>
                <#else>
                <#assign docNext = 2/>
                </#if>
            </li>
            <li>
                <#if hasNext>
                <a href="full-doc.html?${queryStringForPaging}&start=${docNext}&uri=${docIdWindow.ids[2]}&view=${view}">
                    <img src="images/arr-right.gif" alt="next button" hspace="5" width="9" height="7"
                         alt="click here for next item"/>
                </a>
                </#if>
            </li>
        </ul>
        <#else>
        &#160;
        </#if>
    </div>
    <div class="printpage">
        <a href="#" onclick="window.print();"><img src="images/btn-print.gif" alt="<@spring.message 'AltPrint_t' />" vspace="4"/></a>
    </div>
</div>

<div id="wrapper">
    <table id="multi" border="0" cellspacing="10" cellpadding="10" summary="results - item detail">
        <tr>
            <td>
                <#if result.fullDoc.thumbnail?size &gt; 1>
                <div id="myGallery">
                    <#list result.fullDoc.thumbnail as image>
                    <div class="imageElement">
                        <h3>${result.fullDoc.dcTitle[0]}</h3>

                        <p>${model.fullDoc.dcDescription[0]}</p>
                        <a href="#" title="" class="open"></a>
                        <img src="${cacheUrl}uri=${image}&size=FULL_DOC&type=${result.fullDoc.europeanaType}"
                             class="full"
                             alt="Image of ${result.fullDoc.dcTitle[0]}"/>
                        <img src="${cacheUrl}uri=${image}&size=BRIEF_DOC&type=${result.fullDoc.europeanaType}"
                             class="thumbnail"
                             alt="Thumbnail image of ${result.fullDoc.dcTitle[0]}"/>
                    </div>
                    </#list>
                </div>
                <#else>
                <div style="width:200px; height: 400px; overflow-x:hidden; overflow-y:hidden; scrolling: none;  text-align:center;">
                    <#assign imageRef = "#"/>
                    <#if !result.fullDoc.europeanaIsShownBy[0]?matches(" ")>
                        <#assign imageRef = result.fullDoc.europeanaIsShownBy[0]/>
                    <#elseif !result.fullDoc.europeanaIsShownAt[0]?matches(" ")>
                        <#assign imageRef = result.fullDoc.europeanaIsShownAt[0]/>
                    </#if>
                    <script>
                        function checkSize(h){
                            if (h > 200) {
                                h = 200;
                                document.getElementById("imgview").height=h;
                            }
                        }
                    </script>
                    <a href="${imageRef}" target="_blank">
                        <img src="${cacheUrl}uri=${result.fullDoc.thumbnails[0]}&size=FULL_DOC&type=${result.fullDoc.europeanaType}"
                         class="full" alt="Image title: ${result.fullDoc.dcTitle[0]}" />
                        <#--<img src="${cacheUrl}uri=${result.fullDoc.thumbnails[0]}&size=FULL_DOC&type=${result.fullDoc.europeanaType}"-->
                         <#--class="full" alt="Image title: ${result.fullDoc.dcTitle[0]}"/>-->
                        <#--<img src="${result.fullDoc.thumbnails[0]}" alt="Image title: ${result.fullDoc.dcTitle[0]}" id="imgview" onload="checkSize(this.height);"/>-->
                    </a>

                </div>
                </#if>

            </td>
            <td>
                <div id="item-detail">
                    <h2 class="${result.fullDoc.europeanaType}">${result.fullDoc.dcTitle[0]}</h2>
                    <#if format??>
                    <#assign doc = result.fullDoc />
                    <#assign showFieldNames = true />
                    <@show_value "europeana:uri" doc.id showFieldNames />
                    <@show_array_values "europeana:country" doc.europeanaCountry  showFieldNames />
                    <@show_array_values "europeana:source" doc.europeanaSource  showFieldNames />
                    <@show_array_values "europeana:provider" doc.europeanaProvider  showFieldNames />
                    <@show_value "europeana:hasObject" doc.europeanaHasObject?string  showFieldNames />
                    <@show_array_values "europeana:isShownAt" doc.europeanaIsShownAt  showFieldNames />
                    <@show_array_values "europeana:isShownBy" doc.europeanaIsShownBy  showFieldNames />
                    <#--<@show_array_values "europeana:unstored" doc.europeanaUnstored  showFieldNames />-->
                    <@show_array_values "europeana:object" doc.thumbnail  showFieldNames />
                    <@show_value "europeana:language" doc.europeanaLanguage  showFieldNames />
                    <@show_value "europeana:type" doc.europeanaType  showFieldNames />
                    <@show_array_values "europeana:userTag" doc.europeanaUserTag  showFieldNames />
                    <@show_array_values "europeana:year" doc.europeanaYear  showFieldNames />

                    <!-- here the dcterms namespaces starts -->
                    <@show_array_values "dcterms:alternative" doc.dcTermsAlternative  showFieldNames />
                    <@show_array_values "dcterms:conformsTo" doc.dcTermsConformsTo  showFieldNames />
                    <@show_array_values "dcterms:created" doc.dcTermsCreated  showFieldNames />
                    <@show_array_values "dcterms:extent" doc.dcTermsExtent  showFieldNames />
                    <@show_array_values "dcterms:hasFormat" doc.dcTermsHasFormat  showFieldNames />
                    <@show_array_values "dcterms:hasPart" doc.dcTermsHasPart  showFieldNames />
                    <@show_array_values "dcterms:hasVersion" doc.dcTermsHasVersion  showFieldNames />
                    <@show_array_values "dcterms:isFormatOf" doc.dcTermsIsFormatOf  showFieldNames />
                    <@show_array_values "dcterms:isPartOf" doc.dcTermsIsPartOf  showFieldNames />
                    <@show_array_values "dcterms:isReferencedBy" doc.dcTermsIsReferencedBy  showFieldNames />
                    <@show_array_values "dcterms:isReplacedBy" doc.dcTermsIsReplacedBy  showFieldNames />
                    <@show_array_values "dcterms:isRequiredBy" doc.dcTermsIsRequiredBy  showFieldNames />
                    <@show_array_values "dcterms:issued" doc.dcTermsIssued  showFieldNames />
                    <@show_array_values "dcterms:isVersionOf" doc.dcTermsIsVersionOf  showFieldNames />
                    <@show_array_values "dcterms:medium" doc.dcTermsMedium  showFieldNames />
                    <@show_array_values "dcterms:provenance" doc.dcTermsProvenance  showFieldNames />
                    <@show_array_values "dcterms:references" doc.dcTermsReferences  showFieldNames />
                    <@show_array_values "dcterms:replaces" doc.dcTermsReplaces  showFieldNames />
                    <@show_array_values "dcterms:requires" doc.dcTermsRequires  showFieldNames />
                    <@show_array_values "dcterms:spatial" doc.dcTermsSpatial  showFieldNames />
                    <@show_array_values "dcterms:tableOfContents" doc.dcTermsTableOfContents  showFieldNames />
                    <@show_array_values "dcterms:temporal" doc.dcTermsTemporal  showFieldNames />

                    <!-- here the dc namespaces starts -->
                    <@show_array_values "dc:contributor" doc.dcContributor  showFieldNames />
                    <@show_array_values "dc:coverage" doc.dcCoverage  showFieldNames />
                    <@show_array_values "dc:creator" doc.dcCreator  showFieldNames />
                    <@show_array_values "dc:date" doc.dcDate  showFieldNames />
                    <@show_array_values "dc:description" doc.dcDescription  showFieldNames />
                    <@show_array_values "dc:format" doc.dcFormat  showFieldNames />
                    <@show_array_values "dc:identifier" doc.dcIdentifier  showFieldNames />
                    <@show_array_values "dc:language" doc.dcLanguage  showFieldNames />
                    <@show_array_values "dc:publisher" doc.dcPublisher  showFieldNames />
                    <@show_array_values "dc:relation" doc.dcRelation  showFieldNames />
                    <@show_array_values "dc:rights" doc.dcRights  showFieldNames />
                    <@show_array_values "dc:source" doc.dcSource  showFieldNames />
                    <@show_array_values "dc:subject" doc.dcSubject  showFieldNames />
                    <@show_array_values "dc:title" doc.dcTitle  showFieldNames />
                    <@show_array_values "dc:type" doc.dcType  showFieldNames />
                    <#else>
                    <#if !model.fullDoc.europeanaYear[0]?matches(" ")><p class="first">${model.fullDoc.europeanaYear[0]}</p></#if>
                    <#if !model.fullDoc.dcCreator[0]?matches(" ")><p>${model.fullDoc.dcCreator[0]}</p></#if>
                    <#if !model.fullDoc.dcDescription[0]?matches(" ")><p>${model.fullDoc.dcDescription[0]}</p></#if>
                    <#if !model.fullDoc.europeanaProvider[0]?matches(" ")><p><@spring.message 'Provider_t' />: ${model.fullDoc.europeanaProvider[0]}</p></#if>


                    <p id="morelink">
                        <a href="#" onclick="toggleObject('moremetadata');toggleObject('lesslink');toggleObject('morelink')"><@spring.message 'More_t' /></a>
                    </p>

                    <p id="lesslink" style="display:none;">
                        <a href="#" onclick="toggleObject('lesslink');toggleObject('morelink');toggleObject('moremetadata')"><@spring.message 'Less_t' /></a>
                    </p>

                    <div id="moremetadata" style="display:none">
                        <#assign arrsubj = model.fullDoc.dcSubject>
                        <#if arrsubj?size &gt; 0>
                        <p>
                            <em><@spring.message 'Subject_t' /></em>:
                            <#list arrsubj as sub>
                            ${sub}<#if sub_has_next>, </#if>
                            </#list>
                        </p>
                        </#if>
                        <#if result.fullDoc.dcTermsTemporal??><p>${result.fullDoc.dcTermsTemporal[0]}</p></#if>
                        <#if result.fullDoc.dcTermsSpatial[0]??><p>${result.fullDoc.dcTermsSpatial[0]}</p></#if>
                    </div>
                    </#if>
                    <p class="view-orig-green"><a href="${result.fullDoc.europeanaIsShownAt[0]}" target="_blank"><@spring.message 'ViewInOriginalContext_t' /></a>
                        <@spring.message 'OpensInNewWindow_t'/>
                        <a href="${defaultQueryParams}&format=srw"><@spring.message 'ViewAsXML_t'/></a>
                        <#if format?? && format?matches("labels")>
                            <a href="${defaultQueryParams}"><@spring.message 'ViewWithoutLabels_t'/></a>
                        <#else>
                            <a href="${defaultQueryParams}&format=labels"><@spring.message 'ViewWithLabels_t'/></a>
                        </#if>
                    </p>
                </div>
            </td>
        </tr>
    </table>
</div>

</div>
</div>
</div>
<div class="yui-b">
<center>
    <a href="index.html"><img src="images/logo-sm.gif"
                              alt="Click here to return to the Europeana homepage"
                              title="logo Europeana think culture"/></a>
</center>

<div id="leftOptions">
<h3><@spring.message 'RelatedContent_t' />:</h3>

<div class="toggler-c toggler-c-opened" title="Items">
    <p>
    <table summary="related items" id="tbl-related-items" width="100%">
        <#assign max=3/><!-- max shown in list -->
        <#list result.briefDocWindow.docs as doc>
        <#if doc_index &gt; 2><#break/></#if>
        <tr>
            <td width="30" valign="top">
                <div class="related-thumb-container">
                    <#if queryStringForPaging?exists>
                        <a href="full-doc.html?${queryStringForPaging}&start=${doc.index}&uri=${doc.id}&view=${view}">
                     <#else>
                        <a href="full-doc.html?&uri=${doc.id}">
                     </#if>
                        <img src="${cacheUrl}uri=${doc.thumbnail}&size=BRIEF_DOC&type=${doc.type}&view=${view}" alt="Click here to view related item" width="25"/>
                        </a>
                </div>
            </td>

            <td id="item-titles" valign="top">
                <#assign tl = doc.title />
                <#if tl?length &gt; 40>
                <#assign tl = doc.title?substring(0, 40) + "..."/>
                </#if>
                <#if queryStringForPaging?exists>
                <a href="full-doc.html?${queryStringForPaging}&start=${doc.index}&uri=${doc.id}">${tl}</a>
                <#else>
                <a href="full-doc.html?uri=${doc.id}">${tl}</a>
                </#if>
            </td>
        </tr>

        </#list>
        <#if result.briefDocWindow.docs?size &gt; max>
        <tr>
            <td>&#160;</td>
            <td align="right" id="see-all"><a href='brief-doc.html?query=europeana_uri:"${uri}"&view=${view}'><@spring.message 'SeeAllRelatedItems_t' /></a></td>
        </tr>
        </#if>
    </table>
    <br/>
</div>
<div class="toggler-c" title="<@spring.message 'UserTags_t' />">
    <p>
        <#list model.fullDoc.europeanaUserTag as userTag>
        <a href="brief-doc.html?query=europeana_userTag:${userTag}&view=${view}">${userTag}</a><br/>
        </#list>
    </p>
</div>
<h3><@spring.message 'Actions_t' />:</h3>

<div class="toggler-c" title="<@spring.message 'AddATag_t' />">

    <#if user??>

    <div id="ysearchautocomplete">
        <form action="#" method="post" onsubmit="addTag(document.getElementById('tag').value); return false;"  id="form-addtag">
            <input type="text" name="tag" id="tag" maxlength="50" class="text"/>
            <input type="submit" class="button small" value="Add"/>
        </form>
        <br/>

        <div id="ysearchcontainer"></div>
    </div>

    <div id="msg-save-tag" style="display:block" class="hide fg-green"></div>

    <#else>
    <p><a href="login.html"><u><@spring.message 'LogIn_t' /></u></a> | <a href="login.html"><u><@spring.message 'Register_t' /></u></a></p>
    </#if>

</div>
<SCRIPT type="text/javascript">
    YAHOO.example.ACXml = new function() {
        // Instantiate an XHR DataSource and define schema as an array:
        //     ["Multi-depth.object.notation.to.find.a.single.result.item",
        //     "Query Key",
        //     "Additional Param Name 1",
        //     ...
        //     "Additional Param Name n"]      http://www.lutsr.nl/yui/yui/examples/autocomplete/assets/php/ysearch_proxy.php?query=yui&results=100
        this.oACDS = new YAHOO.widget.DS_XHR("tag-autocomplete.ajax", ["Result", "Title", "Count"]);
        this.oACDS.responseType = YAHOO.widget.DS_XHR.TYPE_XML;
        this.oACDS.queryMatchContains = true;
        //             this.oACDS.scriptQueryAppend = "results=100"; // Needed for YWS

        // Instantiate AutoComplete
        this.oAutoComp = new YAHOO.widget.AutoComplete("tag", "ysearchcontainer", this.oACDS);
        this.oAutoComp.formatResult = function(oResultItem, sQuery) {
            // This was defined by the schema array of the data source
            var sTitle = oResultItem[0];
            var sCount = oResultItem[1];
            var sMarkup = sTitle + " <span class='fg-gray'>(" + sCount + ")</span>";
            return (sMarkup);
        };

        // Stub for AutoComplete form validation
        this.validateForm = function() {
            // Validation code goes here
            return true;
        };
    };
</SCRIPT>
<div class="toggler-c" title="<@spring.message 'ShareWithAFriend_t' />">
   <#if user??>
    <form action="#" method="post" onsubmit="sendEmail(); return false;" id="form-sendtoafriend">
        <label for="friendEmail"></label>
        <input type="text" name="friendEmail" id="friendEmail" maxlength="50" class="text" value="<@spring.message 'EmailAddress_t' />"
               onfocus="this.value=''"/>
        <input type="submit" id="mailer" class="button small" value="<@spring.message 'Send_t' />"/>
    </form>
    <span id="msg-send-email" style="display:block" class="fg-green"></span>
    <#else>
    <p><a href="login.html"><u><@spring.message 'LogIn_t' /></u></a> | <a href="login.html"><u><@spring.message 'Register_t' /></u></a></p>
    </#if>
</div>

<div class="related-links">
    <p class="linetop">
        <a href="#" onclick="saveItem();"><@spring.message 'SaveToMyEuropeana_t' /></a>
        <span id="msg-save-item" class="msg-hide fg-green">item saved</span>
    </p>

</div>
</div>
</div>


</div>
<div id="ft">
    <#include "inc_footer.ftl"/>
</div>
</div>
</body>
</html>

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
