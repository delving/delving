/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

var toggleElements_animating = false;

(function($) {

jQuery.fn.toggleElements = function(settings) {

	// Settings
	settings = jQuery.extend({
		fxAnimation: "slide",   // slide, show, fade
		fxSpeed: "normal",   // slow, normal, fast or number of milliseconds
		className: "toggler",
		removeTitle: true,
		showTitle: false,
		onClick: null,
		onHide: null,
		onShow: null
	}, settings);

	var onClick = settings.onClick, onHide = settings.onHide, onShow = settings.onShow;

	if ((settings.fxAnimation!='slide')&&(settings.fxAnimation!='show')&&(settings.fxAnimation!='fade'))
		settings.fxAnimation='slide';

	// First hide all elements without class 'opened'
	this.each(function(){
		if (jQuery(this).attr('class').indexOf("opened")==-1){
			jQuery(this).hide();
		}
	});

	// Add Toggle-Links before elements
	this.each(function(){

		wtitle='';
		wlinktext=jQuery(this).attr('title');

		if (settings.showTitle==true) wtitle=wlinktext;
		if (settings.removeTitle==true) jQuery(this).attr('title','');

		if (jQuery(this).attr('class').indexOf("opened")!=-1){
			jQuery(this).before('<a class="'+settings.className+' '+settings.className+'-opened" href="#" title="'+ wtitle +'">' + wlinktext + '</a>');
			jQuery(this).addClass(settings.className+'-c-opened');
		} else {
			jQuery(this).before('<a class="'+settings.className+' '+settings.className+'-closed" href="#" title="'+ wtitle +'">' + wlinktext + '</a>');
			jQuery(this).addClass(settings.className+'-c-closed');
		}

		// Click-Function for Toggle-Link
		jQuery(this).prev('a.'+settings.className).click(function() {

			if (toggleElements_animating) return false;

			thelink = this;
			jQuery(thelink)[0].blur();

			if (thelink.animating||toggleElements_animating) return false;
			toggleElements_animating = true;
			thelink.animating = true;

			// Callback onClick
			if ( typeof onClick == 'function' && onClick(thelink) === false) {
				toggleElements_animating = false;
				thelink.animating = false;
				return false;
			}

			// Hide Element
			if (jQuery(this).next().css('display')=='block') {
				jQuery(this).next().each(function(){
					if (settings.fxAnimation == 'slide') jQuery(this).slideUp(settings.fxSpeed,function(){
						jQuery.toggleElementsHidden(this,settings.className,onHide,thelink);
					});
					if (settings.fxAnimation == 'show') jQuery(this).hide(settings.fxSpeed,function(){
						jQuery.toggleElementsHidden(this,settings.className,onHide,thelink);
					});
					if (settings.fxAnimation == 'fade') jQuery(this).fadeOut(settings.fxSpeed,function(){
						jQuery.toggleElementsHidden(this,settings.className,onHide,thelink);
					});
				});
			// Show Element
			} else {
				jQuery(this).next().each(function(){
					if (settings.fxAnimation == 'slide') jQuery(this).slideDown(settings.fxSpeed,function(){
						jQuery.toggleElementsShown(this,settings.className,onShow,thelink);
					});
					if (settings.fxAnimation == 'show')  jQuery(this).show(settings.fxSpeed,function(){
						jQuery.toggleElementsShown(this,settings.className,onShow,thelink);
					});
					if (settings.fxAnimation == 'fade')  jQuery(this).fadeIn(settings.fxSpeed,function(){
						jQuery.toggleElementsShown(this,settings.className,onShow,thelink);
					});
				});
			}
			return false;

		});

	});

};

// Remove/Add classes to Toggler-Link
jQuery.toggleElementsHidden = function(el,cname,onHide,thelink) {
	jQuery(el).prev('a.'+cname).removeClass(cname+'-opened').addClass(cname+'-closed').blur();
	if ( typeof onHide == 'function') onHide(this); // Callback onHide
	jQuery(el).removeClass(cname+'-c-opened').addClass(cname+'-c-closed');
	toggleElements_animating = false;
	thelink.animating = false;
};
jQuery.toggleElementsShown = function(el,cname,onShow,thelink) {
	jQuery(el).prev('a.'+cname).removeClass(cname+'-closed').addClass(cname+'-opened').blur();
	if ( typeof onShow == 'function') onShow(this); // Callback onShow
	jQuery(el).removeClass(cname+'-c-closed').addClass(cname+'-c-opened');
	toggleElements_animating = false;
	thelink.animating = false;
};

})(jQuery);