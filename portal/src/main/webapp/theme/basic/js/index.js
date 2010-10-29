$(document).ready(function() {
    // check if coming from login or logout, if so redirect to last visited page before login or logout
    if($.cookie('takeMeBack')){
        var gotoPage = $.cookie('takeMeBack');
        // kill the cookie
        $.cookie('takeMeBack', null);
        // goto last visited page
        document.location.href = gotoPage;
    }

// load DMS content
    delvingPageCall("#news", portalName+"/news.dml?embedded=true"," "," "," ");
    
});


