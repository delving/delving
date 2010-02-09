/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or? as soon they
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
       url: "save.ajax",
       data: "className=SavedSearch&query="+queryToSave+"&queryString="+queryString,
       success: function(msg){
           sr.innerHTML = msgSearchSaveSuccess;
           var ss = document.getElementById("savedSearchesCount");
           var currentCount = parseInt(ss.innerHTML,10);
           ss.innerHTML = currentCount + 1;
       },
       error: function(msg) {
            sr.innerHTML = msgSearchSaveFail;
       }
     });
}

function showDefault(obj, iType, size) {
    switch (iType){
        case "TEXT":
            if(size=="brief"){
                obj.src = "images/item-page.gif";
            }
            else {
                obj.src = "images/item-page-large.gif";
            }
            break;
        case "IMAGE":
            if(size=="brief"){
                obj.src = "images/item-image.gif";
            }
            else {
                obj.src = "images/item-image-large.gif";
            }
            break;
        case "VIDEO":
            if(size=="brief"){
                obj.src = "images/item-video.gif";
            }
            else {
                obj.src = "images/item-video-large.gif";
            }
            break;
        case "SOUND":
            if(size=="brief"){
                obj.src = "images/item-sound.gif";
            }
            else {
                obj.src = "images/item-sound-large.gif";
            }
            break;
        default:
             if(size=="brief"){
                obj.src = "images/item-page.gif";
            }
            else {
                obj.src = "images/item-page-large.gif";
            }
    }
}

function sendEmail(objId) {
    $("#form-sendtoafriend").validate({
        rules: {friendEmail: "required"},
        messages: {friendEmail:{required:msgRequired,email:msgEmailValid}}
    });
    if ($("#form-sendtoafriend").valid()) {
        var sr = document.getElementById("msg-send-email");
        sr.style.display = 'block';
        var email = document.getElementById("friendEmail").value;
        $.ajax({
            type: "POST",
            url: "email-to-friend.ajax",
            data: encodeURI("uri=" + objId + "&email=" + email),
            success: function(msg) {
                sr.innerHTML = msgEmailSendSuccess;
                document.getElementById("friendEmail").value = "";
            },
            error: function(msg) {
                sr.innerHTML = "<span class='fg-red'>" + msgEmailSendFail + "<span>";
            }
        });
    }
}
function addTag(tagText, fullDocId, thumbnailId, objTitle, objType) {
    $("#form-addtag").validate({
        rules: {tag: "required"}
    });
    if ($("#form-addtag").valid()) {
        var sr = document.getElementById("msg-save-tag");
        sr.style.display = 'block';
        $.ajax({
            type: "POST",
            url: "save.ajax",
            data: "className=SocialTag&europeanaUri=" + fullDocId + "&europeanaObject=" + thumbnailId + "&title=" + objTitle + "&tag=" + encodeURIComponent(tagText) + "&docType=" + objType,
            success: function(msg) {
                sr.innerHTML = " tag saved";
                document.getElementById("tag").value = "";
                var ss = document.getElementById("savedTagsCount");
                var currentCount = parseInt(ss.innerHTML,10);
                ss.innerHTML = currentCount + 1;
            },
            error: function(msg) {
                sr.innerHTML = "<span class='fg-red'>" + strTagAdditionFailed + "</span>";
            }
        });
    }
}

function saveItem(postTitle, postAuthor, objUri, thumbnail, type) {
    var sr = document.getElementById("msg-save-item");
    sr.style.display = 'block';
    $.ajax({
        type: "POST",
        url: "save.ajax",
        data: "className=SavedItem&title=" + postTitle + "&author=" + postAuthor + "&europeanaUri=" + objUri + "&europeanaObject=" + thumbnail + "&docType=" + type,
        success: function(msg) {
            sr.innerHTML = msgItemSaveSuccess;
            var ss = document.getElementById("savedItemsCount");
            var currentCount = parseInt(ss.innerHTML,10);
            ss.innerHTML = currentCount + 1;
        },
        error: function(msg) {
            sr.innerHTML = "<span class='fg-red'>" + msgItemSaveFail + "</span>";
        }
    });
}
