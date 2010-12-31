$(document).ready(function() {
    // check if coming from login or logout, if so redirect to last visited page before login or logout
    if(!$.cookie('takeMeBack')){
        // STAY ON PAGE
        // fill in text under the search input
        delvingPageCall("section#info", portalName+"/home.dml?embedded=true"," "," "," ");
        // fill in text on page
        delvingPageCall("p#intro-text", portalName+"/home/intro-text.dml?embedded=true"," "," "," ");
        // random background images for the header
        var images = ['bg-houses.jpg', 'bg-cows.jpg','bg-statues-close.jpg','bg-lion.jpg'];
        $('div#header.home').css({'background-image': 'url('+baseThemePath+'/images/' + images[Math.floor(Math.random() * images.length)] + ')'});
        // carousel
        $('ul#mycarousel').jcarousel({
            vertical: true,
            scroll: 2
        });

    }else{
        // REDIRECT
        var gotoPage = $.cookie('takeMeBack');
        // kill the cookie
        $.cookie('takeMeBack', null, { path: portalName });
        // goto last visited page
        document.location.href = gotoPage;
    }

});


