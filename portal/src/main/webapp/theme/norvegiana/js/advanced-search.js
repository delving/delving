/**
 * Created by IntelliJ IDEA.
 * User: zemyatin
 * Date: Jan 2, 2011
 * Time: 3:37:11 PM
 * To change this template use File | Settings | File Templates.
 */

$(document).ready(function() {

    $('#sel-counties :selected').each(function(){
        if($(this).val()=="false") {
            $("#county-list").show();
            $("tr#municipalities-row").show();
            if ($("#sel-municipalities :selected").val() == "false") {
                loadMunicipalities();
                $("#municipality-list").show("slow");
            }
        }
    });

    $("#sel-counties").change(function() {
        if ($("#sel-counties :selected").val() == "false") {
            $("#county-list").show("slow");
            $("tr#municipalities-row").show();
        }
        if ($("#sel-counties :selected").val() == "true") {
            $("#county-list").hide("slow");
            $("tr#municipalities-row").hide();
        }
    });

    $("#sel-municipalities").change(function() {
        if ($("#sel-municipalities :selected").val() == "false") {
            loadMunicipalities();
            $("#municipality-list").show("slow");
        }
        if ($("#sel-municipalities :selected").val() == "true") {
            $("#municipality-list").hide("slow");
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

    $("#county-list").change(function(){
        loadMunicipalities();
    });

    // extra form reset functionality
    $("input[type=reset]").click(function(){
        $("#provider-list").hide();
        $("#county-list").hide();
        $("tr#municipalities-row").hide();
        $("#municipality-list").hide();
    })

});

function loadMunicipalities(){
        var county = $("#county-list :selected").val();
//        var multipleValues = $(this).val() || [];
//            $(this.options).each(function(){
//                if(this.selected){
//                    multipleValues.join("&")
//                }
//            });
         $.getJSON(portalName+"/getFacets.html", "qf=COUNTY:"+county, function(data) {
            var options = '';
             for (var i = 0; i < data.municipalities.length; i++) {
               options +='<option value="' + data.municipalities[i].value + '">'
                       +data.municipalities[i].name + ' ('
                       +data.municipalities[i].count + ')'
                       +'</option>';
             }
             $("tr#municipalities-row").show();
             $("select#municipality-list").html(options);
         })
};



