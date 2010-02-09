/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

function setLang(lang) {
       var langform = document.getElementById("frm-lang");
       var langval = langform.lang;
       langval.value = lang;
       langform.submit();
}

function toggleObject(oId) {
    var oObj = document.getElementById(oId);
    var cObj = (oObj.style.display == "none") ? "block" : "none";
    oObj.style.display = cObj;
}

function highLight(oId){
    document.getElementById(oId).style.backgroundColor='#ffffcc';
}

function ContactMe(prefix,suffix){
    var m =  Array(109,97,105,108,116,111,58);
    var s = '';
    for (var i = 0; i < m.length; i++){
        s += String.fromCharCode(m[i]);
    }
    window.location.replace(s + prefix + String.fromCharCode(8*8) + suffix);
    return false;
}

function isEmpty( inputStr ) {
    if ( null === inputStr || "" == inputStr ) {
        return true;
    }
    return false;
}

function checkFormSimpleSearch(oId){
    var o = document.getElementById(oId);
    if (isEmpty(o.value)){
        document.getElementById(oId).style.border="1px dotted firebrick";
        return false;
    }
    return true;
}

function makeUiErrorInfo(){

    if ( $(".ui-error").length > 0 ) {

        var errorDivOpen = '<div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em; margin:5px 0;"><p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>';
        var errorDivClose = '</p></div>';
        var errorContent = '';
        var errorToAppend = '';

        $(".ui-error").each(function(i){
            errorContent = $(this).html();
            errorToAppend = errorDivOpen+errorContent+errorDivClose;
            $(this).empty().append(errorToAppend);
        });
    }

    if ( $(".ui-info").length > 0 ) {

        var hlDivOpen = '<div class="ui-state-highlight ui-corner-all" style="padding: 0pt 0.7em; margin:5px 0;"><p><span class="ui-icon ui-icon-info" style="float: left; margin-right: 0.3em;"></span>';
        var hlDivClose = '</p></div>';
        var hlContent = '';
        var hlToAppend = '';

        $(".ui-info").each(function(i){
            hlContent = $(this).html();
            hlToAppend = hlDivOpen+hlContent+hlDivClose;
            $(this).empty().append(hlToAppend);
        });
    }
}

function init() {
    if ($.browser.opera) {
        $("#mainNav > li ").css("min-width","0px");
    }
}

$(document).ready(function() {
   init();
   makeUiErrorInfo();
});

