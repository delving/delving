// BRIEF DOC

/*$(document).ready(function() {
    $('div.toggler-c').toggleElements(
    { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
});*/

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
           var currentCount = parseInt(ss.innerHTML);
           ss.innerHTML = currentCount + 1;
       },
       error: function(msg) {
            sr.innerHTML = msgSearchSaveFail;
       }
     });
};

function showDefaultSmall(obj, iType) {
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
};

function refineSearch(query,qf){
   $("input#query-get").val(query);
    var strqf = $("input#qf-get").val(qf.replace("&qf=",""));
    //strqf = strqf.replace("&amp;","&");

   $("#form-refine-search").submit();
}

