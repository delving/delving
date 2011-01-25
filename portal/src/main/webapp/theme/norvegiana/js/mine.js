$(document).ready(function() {
    if ($.cookie('mine-tabs')){
        $("#savedItems").tabs({selected: $.cookie('mine-tabs')});
    } else {
        $("#savedItems").tabs({selected: 0});
    }
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

function removeUser(email){
        $.ajax({
          type: 'POST',
          url: portalName+'/remove-user.ajax',
          data: "email="+email,
          success: function(data){
              //showMessage("success","User: "+toRemove+ " has been successfully removed");
              document.location.href=portalName+"/logout.html";
          },
          error: function(xhr, ajaxOptions, thrownError){
            showMessage("error","An error has occured: "+xhr.status);
          }
        });
}

function removeSavedItem(index, rowId){
    var rowRemove="#"+rowId;
    $.ajax({
        type: "POST",
        url: "/portal/remove-item.ajax",
        data: "index=" + index,
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
        url: "/portal/remove-search.ajax",
        data: "index=" + searchId,
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
