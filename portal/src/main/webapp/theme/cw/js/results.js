/* ________________BRIEF DOC_______________________*/

function showDefaultSmall(obj, iType, src) {
        if(!(src.indexOf('noImageFound'))){
            obj.src = src;
        } else {
            if(obj && iType){
                switch (iType)
                {
                    case "TEXT":
                        obj.src = baseThemePath+"/images/item-page.gif";
                        break;
                    case "IMAGE":
                        obj.src = baseThemePath+"/images/item-image.gif";
                        break;
                    case "VIDEO":
                        obj.src = baseThemePath+"/images/item-video.gif";
                        break;
                    case "SOUND":
                        obj.src = baseThemePath+"/images/item-sound.gif";
                        break;
                    default:
                        obj.src = baseThemePath+"/images/item-page.gif";
                }
            }
         }

}

/* ________________FULL DOC_______________________*/
function showDefaultLarge(obj,iType,src){
    if(!(src.indexOf('noImageFound'))){
        obj.src = src;
    } else {
        if(obj && iType){
            switch(iType)
            {
            case "TEXT":
              obj.src=baseThemePath+"/images/item-page-large.gif";
              break;
            case "IMAGE":
              obj.src=baseThemePath+"/images/item-image-large.gif";
              break;
            case "VIDEO":
              obj.src=baseThemePath+"/images/item-video-large.gif";
              break;
            case "SOUND":
              obj.src=baseThemePath+"/images/item-sound-large.gif";
              break;
            default:
              obj.src=baseThemePath+"/images/item-page-large.gif";
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

function sendEmail(objId){
    $("#form-sendtoafriend").validate({
        rules: {friendEmail: "required"},
        messages: {friendEmail:{required:msgRequired,email:msgEmailValid}}
    });
    if ($("#form-sendtoafriend").valid()){
         var message = $("#msg-send-email");
        var email = document.getElementById("friendEmail").value;
        $.ajax({
           type: "POST",
           url: "email-to-friend.ajax",
           data: encodeURI("uri="+objId+"&email=" + email),
           success: function(msg){
                message.css({"display":"block","color":"green"}).html(msgSearchSaveSuccess);
                document.getElementById("friendEmail").value = "";
               message.delay("5000").fadeOut('slow');
           },
           error: function(msg) {
               message.css({"display":"block","color":"red"}).html(msgEmailSendFail);
               message.delay("5000").fadeOut('slow');
           }
         });
    }
    return false;
}


function saveQuery(className, queryToSave, queryString){
    var message = $("#msg-save-search");
    $.ajax({
       type: "POST",
       url: portalName + "/save.ajax",
       data: "className="+className+"&query="+queryToSave+"&queryString="+queryString,
       success: function(msg){
           message.css({"display":"block","color":"green"}).html(msgSearchSaveSuccess);
           var ss = document.getElementById("savedSearchesCount");
           var currentCount = parseInt(ss.innerHTML, 10);
           ss.innerHTML = currentCount + 1;
           highLight("href-saved-searches");
           message.delay("5000").fadeOut('slow');
       },
       error: function(msg) {
           message.css({"display":"block","color":"red"}).html(msgSearchSaveFail);
           message.delay("5000").fadeOut('slow');
       }
     });
}


function addTag(className,tagText,fullDocId,thumbnailId,objTitle,objType){
     $("#form-addtag").validate({
        rules: {tag: "required"}
    });
    if ($("#form-addtag").valid()){
        var message = $("span#msg-save-tag");
        $.ajax({
           type: "POST",
           url: portalName + "/save.ajax",
           data: "className="+className+"&europeanaUri="+fullDocId+"&europeanaObject="+thumbnailId+"&title="+objTitle+"&tag=" + encodeURIComponent(tagText) +"&docType="+objType,
           success: function(msg){
                message.css({"display":"block","color":"green"}).html("Tag bewaard");
                document.getElementById("tag").value = "";
                var ss = document.getElementById("savedTagsCount");
                var currentCount = parseInt(ss.innerHTML, 10);
                ss.innerHTML = currentCount + 1;
                highLight("href-saved-tags");
                message.delay("1500").fadeOut('slow');
                window.location.reload();
           },
           error: function(msg) {
               message.css({"display":"block","color":"red"}).html("Tag toevoegen mislukt");
               message.delay("5000").fadeOut('slow');
               alert(msg.text());

           }
        });
    }
    return false;
}
function saveItem(className,postTitle,postAuthor,objUri,thumbnail,type){
    //var sr = document.getElementById("msg-save-item");
    //sr.style.display = 'block';
    var message = $("span#msg-save-item");
    $.ajax({
       type: "POST",
       url: portalName + "/save.ajax",
       data: "className="+className+"&title="+postTitle+"&author="+postAuthor+"&europeanaUri="+objUri+"&europeanaObject="+thumbnail+"&docType="+type,
       success: function(msg){
           message.css({"display":"block","float":"left","color":"green"}).html(msgItemSaveSuccess);
           message.delay("5000").fadeOut('slow')
           var ss = document.getElementById("savedItemsCount");
           var currentCount = parseInt(ss.innerHTML, 10);
           ss.innerHTML = currentCount + 1;
           highLight("href-saved-items");
       },
       error: function(msg) {
           message.css({"display":"block","float":"left","color":"red"}).html(msgItemSaveFail);
           message.delay("5000").fadeOut('slow');
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
        if (w > 235) {
            w = 235;
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