// FULL DOC
/*$(document).ready(function() {
    $('div.toggler-c').toggleElements(
    { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
});*/

function showDefaultLarge(rType,obj,iType){
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
};

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
                sr.innerHTML = "<span class='fg-red'>"+msgEmailSendFail+"<span>";
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
                var currentCount = parseInt(ss.innerHTML);
                ss.innerHTML = currentCount + 1;
           },
           error: function(msg) {
                sr.innerHTML = "<span class='fg-red'>"+strTagAdditionFailed+"</span>";
           }
        });
    }
    return false;
};
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
           var currentCount = parseInt(ss.innerHTML);
           ss.innerHTML = currentCount + 1;
       },
       error: function(msg) {
            sr.innerHTML = "<span class='fg-red'>"+msgItemSaveFail+"</span>";
       }
     });
    return false;
};

