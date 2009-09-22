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

// BRIEF DOC

$(document).ready(function() {
    $('div.toggler-c').toggleElements(
    { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
});

function saveQuery(queryToSave, queryString){
    var sr = document.getElementById("msg-save-search");
    sr.style.display = 'block';
    $.ajax({
       type: "POST",
       url: "save-search.ajax",
       data: "query="+queryToSave+"&queryString="+queryString,
       success: function(msg){
           sr.innerHTML = msgSearchSaveSuccess;
           var ss = document.getElementById("savedSearchesCount");
           var currentCount = parseInt(ss.innerHTML);
           ss.innerHTML = currentCount + 1;
       },
       error: function(msg) {
            sr.innerHTML = msgSearchSaveFail;
       }
     });
};

function showDefault(obj, iType) {
    switch (iType)
            {
        case "TEXT":
            obj.src = "images/item-page.gif";
            break;
        case "IMAGE":
            obj.src = "images/item-image.gif";
            break;
        case "VIDEO":
            obj.src = "images/item-video.gif";
            break;
        case "SOUND":
            obj.src = "images/item-sound.gif";
            break;
        default:
            obj.src = "images/item-page.gif";
    }
};
