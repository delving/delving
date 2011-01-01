/**
 * Created by IntelliJ IDEA.
 * User: zemyatin
 * Date: Jan 1, 2011
 * Time: 12:52:30 PM
 * To change this template use File | Settings | File Templates.
 */
function createImage(){
    var name = $("#imgName").attr("value");
    var ext = $("#imgExt :selected").text();
    var pName = $("#pName").attr("value");
    var makeURL = pName+name+ext+".img";
    window.location.href=makeURL+"?edit=true";
}

if ($("a.delete").length){
    $("a.delete").click(function(){
        var target = $(this).attr("name");
        var targetURL = $(this).attr("href");
        var confirmation = confirm(deleteConfirm)
        if(confirmation){
            $.ajax({
                url: targetURL+"?edit=false&delete=true",
                type: "GET",
                success: function(data) {
                    $("table.user-images tr#" + target).css("display", "none");
                    showMessage("success","Image deleted") //deleteConform set in ftl
                },
                error: function(data) {
                    showMessage("error",deleteFail) //deleteFail set in ftl
                }
            });
        }
        return false;
    });
}
