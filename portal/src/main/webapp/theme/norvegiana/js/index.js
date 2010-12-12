$(document).ready(function() {
    // check if coming from login or logout, if so redirect to last visited page before login or logout
    if($.cookie('takeMeBack')){
        var gotoPage = $.cookie('takeMeBack');
        // kill the cookie
        $.cookie('takeMeBack', null, { path: portalName });
        // goto last visited page
        document.location.href = gotoPage;
    }else{
        delvingPageCall("section#home", portalName+"/home.dml?embedded=true"," "," "," ");
    }
});


