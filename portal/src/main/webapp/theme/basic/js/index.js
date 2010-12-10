$(document).ready(function() {
    // check if coming from login or logout, if so redirect to last visited page before login or logout
    if($.cookie('takeMeBack')){
        var gotoPage = $.cookie('takeMeBack');
        // kill the cookie
        $.cookie('takeMeBack', null, { path: portalName });
        // goto last visited page
        document.location.href = gotoPage;
    }else{
        delvingPageCall("#block-1", portalName+"/home/block1.dml?embedded=true"," "," "," ");
        delvingPageCall("#block-2", portalName+"/home/block2.dml?embedded=true"," "," "," ");
        delvingPageCall("#block-3", portalName+"/home/block3.dml?embedded=true"," "," "," ");
    }
    var langTab = "1";
    if(locale=="no"){langTab=0}
    $("#aboutTabs").tabs({selected: langTab});    
});


