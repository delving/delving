/* ________________BRIEF DOC_______________________*/

function saveQuery(className, queryToSave, queryString){
    var sr = $("#msg-save-search");
    sr.css("display","block");
    $.ajax({
       type: "POST",
       url: "/portal/save.ajax",
       data: "className="+className+"&query="+queryToSave+"&queryString="+queryString,
       success: function(msg){
           
           sr.html(msgSearchSaveSuccess);
           //$("#msg-save-search").delay(2000).hide();
           var ss = $("#savedSearchesCount");
           var currentCount = parseInt(ss.html(), 10);
           ss.html(currentCount + 1);
       },
       error: function(msg) {
            sr.html(msgSearchSaveFail);
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
function saveItem(className,postTitle,postAuthor,objUri,thumbnail,type){
    var sr = document.getElementById("msg-save-item");
    sr.style.display = 'block';
    $.ajax({
       type: "POST",
       url: "/portal/save.ajax",
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
        if (w > 200) {
            $("#"+obj).css("width","200px");
        }
    }
    else {
        if (w > 255) {
            $("#"+obj).css("width","300px");
        }
    }
}

$(document).ready(function(){
//    if($("a.overlay").length > 0){
//        $("a.overlay").fancybox({
//        titleShow : true,
//        titlePosition: 'inside'
//        })
//    }

    if($(".facets_container").length > 0){
        //Hide (Collapse) the toggle containers on load
        $(".facets_container").hide();

        //Switch the "Open" and "Close" state per click then slide up/down (depending on open/close state)
        $("h4.trigger").click(function(){
            $(this).toggleClass("active").next().slideToggle("slow");
            return false; //Prevent the browser jump to the link anchor
        });

        //Check to see if there are any active facets that need to be toggled to open
        var toggles = $(document).find("h4.trigger");
        $.each(toggles, function(){
            if($(this).hasClass("active")){
                $(this).toggleClass("active").next().css("display","block");                
            }
        })
    }


});




