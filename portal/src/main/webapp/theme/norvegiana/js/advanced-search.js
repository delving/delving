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

    $('#sel-counties :selected').each(function(){
        if($(this).val()=="false") {
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

//    if($("#sel-collections")){
//        $("#sel-collections").change(function() {
//        if ($("#sel-collections :selected").val() == "false") {
//            $("#collections-list").show("slow");
//        }
//        if ($("#sel-collections :selected").val() == "true") {
//            $("#collections-list").hide("slow");
//        }
//        });
//     }

    $("#county-list").change(function(){
        var county = $("#county-list :selected").val();
//        var multipleValues = $(this).val() || [];
//            $(this.options).each(function(){
//                if(this.selected){
//                    multipleValues.join("&")
//                }
//            });
//            alert(multipleValues);
         $.getJSON(portalName+"/getFacets.html", "qf=COUNTY:"+county, function(data) {
            var options = '';
             for (var i = 0; i < data.municipalities.length; i++) {
               options +=
                       '<option value="' + data.municipalities[i].name + '">' +
                               data.municipalities[i].name + ' (' +
                               data.municipalities[i].count + ')' +
                               '</option>';
             }
             $("tr#municipalities-row").show();
             $("select#municipality-list").html(options);
        });

    });

});


