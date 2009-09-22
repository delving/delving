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

// FULL DOC
$(document).ready(function() {
    $('div.toggler-c').toggleElements(
    { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
});

function showDefault(obj, iType) {
    switch (iType)
            {
        case "TEXT":
            obj.src = "images/item-page-large.gif";

            break;
        case "IMAGE":
            obj.src = "images/item-image-large.gif";
            break;
        case "VIDEO":
            obj.src = "images/item-video-large.gif";
            break;
        case "SOUND":
            obj.src = "images/item-sound-large.gif";
            break;
        default:
            obj.src = "images/item-page-large.gif";
    }
}
;

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
            url: "save-social-tag.ajax",
            data: "europeanaUri=" + fullDocId + "&europeanaObject=" + thumbnailId + "&title=" + objTitle + "&tag=" + encodeURIComponent(tagText) + "&docType=" + objType,
            success: function(msg) {
                sr.innerHTML = " tag saved";
                document.getElementById("tag").value = "";
                var ss = document.getElementById("savedTagsCount");
                var currentCount = parseInt(ss.innerHTML);
                ss.innerHTML = currentCount + 1;
            },
            error: function(msg) {
                sr.innerHTML = "<span class='fg-red'>" + strTagAdditionFailed + "</span>";
            }
        });
    }
}
;
function saveItem(postTitle, postAuthor, objUri, thumbnail, type) {
    var sr = document.getElementById("msg-save-item");
    sr.style.display = 'block';
    $.ajax({
        type: "POST",
        url: "save-item.ajax",
        data: "title=" + postTitle + "&author=" + postAuthor + "&europeanaUri=" + objUri + "&europeanaObject=" + thumbnail + "&docType=" + type,
        success: function(msg) {
            sr.innerHTML = msgItemSaveSuccess;
            var ss = document.getElementById("savedItemsCount");
            var currentCount = parseInt(ss.innerHTML);
            ss.innerHTML = currentCount + 1;
        },
        error: function(msg) {
            sr.innerHTML = "<span class='fg-red'>" + msgItemSaveFail + "</span>";
        }
    });
}
;


// todo: is this still used
//var saveEditorPick234 = function(o, oId) {
//    curButton = oId.id;
//    var postData = "query=${query?html}&amp;europeanaUri=" + o;
//    var request = YAHOO.util.Connect.asyncRequest('POST', sUrlEP, callbackEP, postData);
//};

// todo: update func parameters
function saveEditorPick(o, oId) {
    var curButton = oId;
    $.ajax({
        type: "POST",
        url: "save-editor-pick.ajax",
        data: "query=${query?html}&amp;europeanaUri=" + o,
        success: function(msg) {
            document.getElementById(curButton).style.backgroundColor = '#339900';
            document.getElementById(curButton).style.color = '#FFFFFF';
        },
        error: function(msg) {
            document.getElementById(curButton).style.backgroundColor = '#990000';
            document.getElementById(curButton).style.color = '#FFFFFF';
        }
    });
}
;

