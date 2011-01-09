/**
 * Created by IntelliJ IDEA.
 * User: zemyatin
 * Date: Jan 2, 2011
 * Time: 3:37:11 PM
 * To change this template use File | Settings | File Templates.
 */

$(document).ready(function() {
  
    $('#provider-list :checkbox').each(function(){
        if(this.checked) {
            $("#provider-list").show();
        }
    });

    $('#county-list :checkbox').each(function(){
        if(this.checked) {
            $("#county-list").show();
        }
    });

    $("#sel-counties").change(function() {
        if ($("#sel-counties :selected").val() == "false") {
            $("#county-list").show("slow");
        }
        if ($("#sel-counties :selected").val() == "true") {
            $("#county-list").hide("slow");
        }
    });
    $("#sel-dataproviders").change(function() {
        if ($("#sel-dataproviders :selected").val() == "false") {
            $("#provider-list").show("slow");
        }
        if ($("#sel-dataproviders :selected").val() == "true") {
            $("#provider-list").hide("slow");
        }
    });
    if($("#sel-collections")){
        $("#sel-collections").change(function() {
        if ($("#sel-collections :selected").val() == "false") {
            $("#collections-list").show("slow");
        }
        if ($("#sel-collections :selected").val() == "true") {
            $("#collections-list").hide("slow");
        }
        });
     }

});
