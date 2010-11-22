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
    if($(oId).length > 0){
       $(oId).effect("pulsate", { times:4 }, 500);
    }
    return false;
}

function writeMessage(target,msg){
    $(target).html(msg);
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

function takeMeBack(){
    var futdate = new Date()		//Get the current time and date
    var expdate = futdate.getTime()  //Get the milliseconds since Jan 1, 1970
    expdate += 120000  //expires in 2 minutes (milliseconds)
    var location = document.location.href;
    $.cookie('takeMeBack', location, { expires: expdate });
}

function delvingPageCall(targetId,pageName,msgHead,msgBody,msgLink){

    if(!msgHead){msgHead="Fout";}
    if(!msgBody){msgBody="Er is een fout opgetreden";}
    $.ajax({
      url: pageName,
      async: false,
      cache: false,
      processData: false,
//      context: document.body.content,
      type: "GET",
        success: function(data) {
            if(data == "This page does not exist."){
//                $(targetId).html("<h2>"+msgHead+"<\/h2><p>"+msgBody+"</p>");
                $(targetId).html("<h2>Fout<\/h2>");
            }else{
                $(targetId).html(data);
            }
        }
    });
}



$(document).ready(function() {

    var buttons = $(document).find("input[type=submit],input[type=reset],button,a.button");
        buttons.addClass("fg-button ui-state-default ui-corner-all");
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

    //onclick for login href to take user back to last visited page before logging in
    if($("a#login")){
        $("a#login").click(function(){
            takeMeBack();
            //alert('foo');
            $("div#overlayContainer").dialog({
                modal: true,
                width: 350
            });
            $("div#overlayContainer").load("login.html?contentOnly=true");
            
            return false;
        })
    }
});
