function setLang(lang) {
       var langform = document.getElementById("frm-lang");
       var langval = langform.lang;
       langval.value = lang;
       langform.submit();
}

function toggleObject(oId) {
    var oObj = document.getElementById(oId);
    var cObj = (oObj.style.display == "none") ? "block" : "none";
    oObj.style.display = cObj;
}

function highLight(oId){
    // makes use of jQuery and jQueryUI javascript files
    if($("#"+oId).length > 0){
       $("#"+oId).effect("pulsate", { times:4 }, 500);
    }
    return false;
}

function ContactMe(prefix,suffix){
    var m =  Array(109,97,105,108,116,111,58);
    var s = '';
    for (var i = 0; i < m.length; i++){
        s += String.fromCharCode(m[i]);
    }
    window.location.replace(s + prefix + String.fromCharCode(8*8) + suffix);
    return false;
}

function isEmpty( inputStr ) {
    if ( null === inputStr || "" == inputStr ) {
        return true;
    }
    return false;
}

function checkFormSimpleSearch(oId){
    var o = document.getElementById(oId);
    if (isEmpty(o.value)){
        document.getElementById(oId).style.border="1px dotted firebrick";
        return false;
    }
    return true;
}


$(document).ready(function() {

    // instantiate the advanced search dialog overlay
    $("#search_advanced").dialog({
        autoOpen: false,
        resizable: false,  
        modal: true,
        bgiframe: true,
        closeOnEscape: true,
        draggable: true,
        width: 355
    });
    // click event on advanced search href
    $('#href-advanced').click(function() {
            $('#search_advanced').dialog('open');
            return false;
     });

    // style all the submit and button elements.
    $(document).find("input[type=submit],input[type=reset],button").addClass("fg-button ui-state-default ui-corner-all");
    // ui button hover states
	$(function(){
		//all hover and click logic for buttons
		$(".fg-button:not(.ui-state-disabled)")
		.hover(
			function(){
				$(this).addClass("ui-state-hover");
			},
			function(){
				$(this).removeClass("ui-state-hover");
			}
		)
		.mousedown(function(){
				$(this).parents('.fg-buttonset-single:first').find(".fg-button.ui-state-active").removeClass("ui-state-active");
				if( $(this).is('.ui-state-active.fg-button-toggleable, .fg-buttonset-multi .ui-state-active') ){ $(this).removeClass("ui-state-active"); }
				else { $(this).addClass("ui-state-active"); }
		})
		.mouseup(function(){
			if(! $(this).is('.fg-button-toggleable, .fg-buttonset-single .fg-button,  .fg-buttonset-multi .fg-button') ){
				$(this).removeClass("ui-state-active");
			}
		});
	});
});
