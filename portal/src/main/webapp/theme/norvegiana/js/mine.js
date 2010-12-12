$(document).ready(function() {
    $("#savedItems").tabs({selected: $.cookie('ui-tabs-3')});
    $("#savedItems").tabs({ cookie: { expires: 30 } });
    $("#rem-acc").click(function(){
        removeUser($(this).attr("name"));

    })
});

function removeRequest(className, id, type) {
    $.ajax({
        type: "POST",
        url: "/portal/remove.ajax",
        data: "className=" + className + "&id=" + id,
        success: function(msg) {
            window.location.reload();
        },
        error: function(msg) {
            alert("An error occured. The item could not be removed");
        }
    });
}

function removeUser(uemail){
    alert(uemail);
    $.ajax({
        type: "POST",
        url: "/portal/remove-user.ajax",
        data: "email=" + uemail,
        success: function(msg) {
            $(rowRemove).css("display","none");
            showMessage("success","User was successfully removed!")
            //window.location.reload();
        },
        error: function(xhr) {
            showMessage("fail","An error occured. The user could not be removed <br \/> error:"+xhr.status);
        }
    });
}

function removeSavedItem(itemId, rowId){
    var rowRemove="#"+rowId;
    $.ajax({
        type: "POST",
        url: "/portal/remove-saved-item.ajax",
        data: "id=" + itemId,
        success: function(msg) {
            $(rowRemove).css("display","none");
            showMessage("success","Items was successfully removed!")
            //window.location.reload();
        },
        error: function(xhr) {
            showMessage("fail","An error occured. The item could not be removed <br \/> error:"+xhr.status);
        }
    });
}

function removeSavedSearch(searchId, rowId){
    var rowRemove="#"+rowId;
    $.ajax({
        type: "POST",
        url: "/portal/remove-saved-search.ajax",
        data: "id=" + searchId,
        success: function(msg) {
            $(rowRemove).css("display","none");
            showMessage("success","Saved search was successfully removed!")
            //window.location.reload();
        },
        error: function(xhr) {
            showMessage("fail","An error occured. The saved search could not be removed <br \/> error:"+xhr.status);
        }
    });
}

function saveRequest(className, id) {
    $.ajax({
        type: "POST",
        url: portalName + "/save.ajax",
        data: "className=" + className + "&id=" + id,
        success: function(msg) {
            window.location.reload();
        },
        error: function(msg) {
            alert("An error occured. The item could not be saved");
        }
    });
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
