/* ________________BRIEF DOC_______________________*/

function saveQuery(className, queryToSave, queryString){
    var sr = document.getElementById("msg-save-search");
    sr.style.display = 'block';
    $.ajax({
       type: "POST",
       url: "save.ajax",
       data: "className="+className+"&query="+queryToSave+"&queryString="+queryString,
       success: function(msg){
           sr.innerHTML = msgSearchSaveSuccess;
           var ss = document.getElementById("savedSearchesCount");
           var currentCount = parseInt(ss.innerHTML, 10);
           ss.innerHTML = currentCount + 1;
       },
       error: function(msg) {
            sr.innerHTML = msgSearchSaveFail;
       }
     });
}

function showDefaultSmall(obj, iType, src) {
        if(!(src.indexOf('noImageFound'))){
            obj.src = src;
        } else {
            if(obj && iType){
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
            }
         }

}


function imgError(){
    log.error("image not found");
}
/*

function refineSearch(query,qf){
   $("input#query-get").val(query);
    var strqf = $("input#qf-get").val(qf.replace("&qf=",""));
    strqf = strqf.replace("&amp;","&");
    $("#form-refine-search").submit();
}*/

/* ________________FULL DOC_______________________*/
function showDefaultLarge(obj,iType,src){
    if(!(src.indexOf('noImageFound'))){
        obj.src = src;
    } else {
        if(obj && iType){
            switch(iType)
            {
            case "TEXT":
              obj.src="images/item-page-large.gif";
              break;
            case "IMAGE":
              obj.src="images/item-image-large.gif";
              break;
            case "VIDEO":
              obj.src="images/item-video-large.gif";
              break;
            case "SOUND":
              obj.src="images/item-sound-large.gif";
              break;
            default:
              obj.src="images/item-page-large.gif";
            }
        }
    }
}

function sendEmail(objId){
    $("#form-sendtoafriend").validate({
        rules: {friendEmail: "required"},
        messages: {friendEmail:{required:msgRequired,email:msgEmailValid}}
    });
    if ($("#form-sendtoafriend").valid()){
        var sr = document.getElementById("msg-send-email");
        sr.style.display = 'block';
        var email = document.getElementById("friendEmail").value;
        $.ajax({
           type: "POST",
           url: "email-to-friend.ajax",
           data: encodeURI("uri="+objId+"&email=" + email),
           success: function(msg){
                sr.innerHTML = msgEmailSendSuccess;
                document.getElementById("friendEmail").value = "";
           },
           error: function(msg) {
                //sr.innerHTML = "<span class='fg-red'>"+msgEmailSendFail+"<span>";
           }
         });
    }
    return false;
}
function addTag(className,tagText,fullDocId,thumbnailId,objTitle,objType){
     $("#form-addtag").validate({
        rules: {tag: "required"}
    });
    if ($("#form-addtag").valid()){
        var sr = document.getElementById("msg-save-tag");
        sr.style.display = 'block';
        $.ajax({
           type: "POST",
           url: "save.ajax",
           data: "className="+className+"&europeanaUri="+fullDocId+"&europeanaObject="+thumbnailId+"&title="+objTitle+"&tag=" + encodeURIComponent(tagText) +"&docType="+objType,
           success: function(msg){
                sr.innerHTML = " tag saved";
                document.getElementById("tag").value = "";
                var ss = document.getElementById("savedTagsCount");
                var currentCount = parseInt(ss.innerHTML, 10);
                ss.innerHTML = currentCount + 1;
           },
           error: function(msg) {
                sr.innerHTML = "<span class='fg-red'>"+strTagAdditionFailed+"</span>";
           }
        });
    }
    return false;
}
function saveItem(className,postTitle,postAuthor,objUri,thumbnail,type){
    var sr = document.getElementById("msg-save-item");
    sr.style.display = 'block';
    $.ajax({
       type: "POST",
       url: "save.ajax",
       data: "className="+className+"&title="+postTitle+"&author="+postAuthor+"&europeanaUri="+objUri+"&europeanaObject="+thumbnail+"&docType="+type,
       success: function(msg){
           sr.innerHTML = msgItemSaveSuccess;
           var ss = document.getElementById("savedItemsCount");
           var currentCount = parseInt(ss.innerHTML, 10);
           ss.innerHTML = currentCount + 1;
       },
       error: function(msg) {
            sr.innerHTML = "<span class='fg-red'>"+msgItemSaveFail+"</span>";
       }
     });
    return false;
}
/*
** Resizes displayed images in the brief and full doc displays
 */
function checkSize(obj,type,w){
    if(type=="brief"){
        if (w > 150) {
            w = 150;
            obj.width=w;
        }        
    }
    else {
        if (w > 215) {
            w = 215;
            obj.width=w;
        }
    }
}

$(document).ready(function() {

    $(".dialog").dialog({
        autoOpen: false,
        modal: true,
        resizable: false,
        width: 500
    });

    $('#opener').click(function() {
        $(".dialog").dialog('open');
    });

//   var imgs = document.getElementsByTagName('img'), i = 0, img;
//   while(i < imgs.length) {
//      imgs[i].onerror = function() {
//         log.error('imgerrorlog=true&src='+this.src);
//         // you can add more params, such as time=1234567 etc.
//      }
//      i++;
//   }
});