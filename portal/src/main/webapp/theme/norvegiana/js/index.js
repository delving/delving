function mycarousel_initCallback(carousel)
{
    // Disable autoscrolling if the user clicks the prev or next button.
    carousel.buttonNext.bind('click', function() {
        carousel.startAuto(0);
    });

    carousel.buttonPrev.bind('click', function() {
        carousel.startAuto(0);
    });

    // Pause autoscrolling if the user moves with the cursor over the clip.
    carousel.clip.hover(function() {
        carousel.stopAuto();
    }, function() {
        carousel.startAuto();
    });
}

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
        // slidshow
        $('.slideshow').cycle({
            fx: 'fade' // choose your transition type, ex: fade, scrollUp, shuffle, etc...
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


