/* ________________BRIEF DOC_______________________*/

function saveQuery(queryToSave, queryString){
    var sr = $("#msg-save-search");
    sr.css("display","block");
    $.ajax({
       type: "POST",
       url: "/portal/save-search.ajax",
       data: "query="+queryToSave+"&queryString="+queryString,
       success: function(msg){
           
           showMessage("success",msgSearchSaveSuccess);
           //$("#msg-save-search").delay(2000).hide();
           var ss = $("#savedSearchesCount");
           var currentCount = parseInt(ss.html(), 10);
           ss.html(currentCount + 1);
       },
       error: function(msg) {
           showMessage("fail",msgSearchSaveFail);
       }
     });
    return false;
}

function showDefaultSmall(obj, iType) {
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
/*

function refineSearch(query,qf){
   $("input#query-get").val(query);
    var strqf = $("input#qf-get").val(qf.replace("&qf=",""));
    strqf = strqf.replace("&amp;","&");
    $("#form-refine-search").submit();
}*/

/* ________________FULL DOC_______________________*/
function showDefaultLarge(obj,iType){
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

function addTag(className,tagText,fullDocId,thumbnailId,objTitle,objType){
     $("#form-addtag").validate({
        rules: {tag: "required"}
    });
    if ($("#form-addtag").valid()){
        var sr = document.getElementById("msg-save-tag");
        sr.style.display = 'block';
        $.ajax({
           type: "POST",
           url: "/portal/save.ajax",
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
function saveItem(postTitle,postAuthor,delvingId,europeanaId,thumbnail,type){
    var sr = document.getElementById("msg-save-item");
    sr.style.display = 'block';
    $.ajax({
       type: "POST",
       url: "/portal/save-item.ajax",
       data: "title="+postTitle+"&author="+postAuthor+"&delvingId="+delvingId+"&europeanaId="+europeanaId+"&thumbnail="+thumbnail+"&docType="+type,
       success: function(msg){
           showMessage("success",msgItemSaveSuccess);
           var ss = document.getElementById("savedItemsCount");
           var currentCount = parseInt(ss.innerHTML, 10);
           ss.innerHTML = currentCount + 1;
       },
       error: function(msg) {
           showMessage("failure",msgItemSaveFail);
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
            $("#"+obj).css("width","150px");
        }
    }
    else {
        if (w > 230) {
            $("#"+obj).css("width","230px");
        }
    }
}

$(document).ready(function(){
    if($("a.overlay").length > 0){
        $("a.overlay").fancybox({
        titleShow : true,
        titlePosition: 'inside'
        })
    }
    if($(".facets_container input[type=checkbox]").length > 0){
        $(".facets_container input[type=checkbox]").click(function(){
            window.location.href = $(this).val();
        })
    }

//    if($("div.facets_container").length > 0){
//        //Hide (Collapse) the toggle containers on load
//        $("div.facets_container").show();
//
//        //Switch the "Open" and "Close" state per click then slide up/down (depending on open/close state)
//        $("h4.trigger").click(function(){
//            $(this).toggleClass("closed").next().slideToggle("slow");
//            return false; //Prevent the browser jump to the link anchor
//        });
//
//        //Check to see if there are any active facets that need to be toggled to open
//        var toggles = $(document).find("h4.trigger");
//        $.each(toggles, function(){
//            if($(this).hasClass("closed")){
//                $(this).next().css("display","block");
//            }
//        })
//    }


});







