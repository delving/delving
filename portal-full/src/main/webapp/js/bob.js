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

/**
 *	ImageFlow 0.8
 *
 *	This code is based on Michael L. Perrys Cover flow in Javascript.
 *	For he wrote that "You can take this code and use it as your own" [1]
 *	this is my attempt to improve some things. Feel free to use it! If
 *	you have any questions on it leave me a message in my shoutbox [2].
 *
 *	The reflection is generated server-sided by a slightly hacked
 *	version of Richard Daveys easyreflections [3] written in PHP.
 *
 *	The mouse wheel support is an implementation of Adomas Paltanavicius
 *	JavaScript mouse wheel code [4].
 *
 *	Thanks to Stephan Droste ImageFlow is now compatible with Safari 1.x.
 *
 *
 *	[1] http://www.adventuresinsoftware.com/blog/?p=104#comment-1981
 *	[2] http://shoutbox.finnrudolph.de/
 *	[3] http://reflection.corephp.co.uk/v2.php
 *	[4] http://adomas.org/javascript-mouse-wheel/
 */

var ImageFlow = function() {

/* Configuration variables */
var conf_reflection_p = 0;         // Sets the height of the reflection in % of the source image
var conf_focus = 5;                  // Sets the numbers of images on each side of the focussed one
var conf_slider_width = 14;          // Sets the px width of the slider div
var conf_images_cursor = 'pointer';  // Sets the cursor type for all images default is 'default'
var conf_slider_cursor = 'default';  // Sets the slider cursor type: try "e-resize" default is 'default'

/* Id names used in the HTML */
var conf_imageflow = 'imageflow';    // Default is 'imageflow'
var conf_loading = 'loading';        // Default is 'loading'
var conf_images = 'images';          // Default is 'images'
var conf_captions = 'captions';      // Default is 'captions'
var conf_scrollbar = 'scrollbar';    // Default is 'scrollbar'
var conf_slider = 'slider';          // Default is 'slider'

/* Define global variables */
var caption_id = 0;
var new_caption_id = 0;
var current = 0;
var target = 0;
var mem_target = 0;
var timer = 0;
var array_images = [];
var new_slider_pos = 0;
var dragging = false;
var dragobject = null;
var dragx = 0;
var posx = 0;
var new_posx = 0;
var xstep = 150;

var imageflow_div, img_div, scrollbar_div, slider_div, caption_div;
var images_width, images_top, images_left, max_conf_focus, size, scrollbar_width, max_height;


function step()
{
	if (target < current-1 || target > current+1) {
		moveTo(current + (target-current)/3);
		window.setTimeout(step, 50);
		timer = 1;
    } else {
		timer = 0;
	}
}

function glideTo(x, new_caption_id)
{
	/* Animate gliding to new x position */
	target = x;
	mem_target = x;
	if (timer === 0) {
		window.setTimeout(step, 50);
		timer = 1;
	}

	/* Display new caption */
	caption_id = new_caption_id;
	caption = img_div.childNodes.item(array_images[caption_id]).getAttribute('alt');
	if (caption === '') {
	    caption = '&nbsp;';
	}
	caption_div.innerHTML = caption;

	/* Set scrollbar slider to new position */
	if (dragging === false) {
		new_slider_pos = (scrollbar_width * (-(x*100/((max-1)*xstep))) / 100) - new_posx;
		slider_div.style.marginLeft = (new_slider_pos - conf_slider_width) + 'px';
	}
}

function moveTo(x)
{
	current = x;
	var zIndex = max;

	/* Main loop */
	for (var index = 0; index < max; index++)
    {
        var image = img_div.childNodes.item(array_images[index]);
        var current_image = index * -xstep;

        /* Don't display images that are not conf_focussed */
        if (!((current_image + max_conf_focus) < mem_target || (current_image - max_conf_focus) > mem_target)) {
            var z = Math.sqrt(10000 + x * x) + 100;
            var xs = x / z * size + size;

            /* Still hide images until they are processed, but set display style to block */
            image.style.display = 'block';

            /* Process new image height and image width */
            var new_img_h = (image.h / image.w * image.pc) / z * size;
            var new_img_w;
            if (new_img_h > max_height) {
                new_img_h = max_height;
                new_img_w = image.w * new_img_h / image.h;
            } else {
                new_img_w = image.pc / z * size;
            }
            var new_img_top = (images_width * 0.34 - new_img_h) + images_top + ((new_img_h / (conf_reflection_p + 1)) * conf_reflection_p);

            if(new_img_h && new_img_w) {
                /* Set new image properties */
                image.style.left = xs - (image.pc / 2) / z * size + images_left + 'px';
                image.style.height = new_img_h + 'px';
                image.style.width = new_img_w + 'px';
                image.style.top = new_img_top + 'px';
                image.style.visibility = 'visible';
            }
            /* Set image layer through zIndex */
            if (x < 0) {
                zIndex++;
            } else {
                zIndex--;
            }

            /* Change zIndex and onclick function of the focussed image */
            if (image.i === caption_id) {
                zIndex++;
                image.onclick = function() {
                    document.location = this.url;
                    return false;
                };
            } else {
                image.onclick = function() {
                    glideTo(this.x_pos, this.i);
                };
            }
            image.style.zIndex = zIndex;
        } else {
            image.style.visibility = 'hidden';
            image.style.display = 'none';
        }
        x += xstep;
    }
}

/* Main function */
function refresh(onload)
{

	/* Cache document objects in global variables */
	img_div = document.getElementById(conf_images);

	scrollbar_div = document.getElementById(conf_scrollbar);
	slider_div = document.getElementById(conf_slider);
	caption_div = document.getElementById(conf_captions);

	/* Cache global variables, that only change on refresh */
	images_width = img_div.offsetWidth;
	images_top = imageflow_div.offsetTop;
	images_left = imageflow_div.offsetLeft;
	max_conf_focus = conf_focus * xstep;

    size = images_width * 0.5;
	scrollbar_width = images_width * 0.6;
	conf_slider_width = conf_slider_width * 0.5;
	max_height = images_width * 0.51;

	/* Change imageflow div properties */
	imageflow_div.style.height = max_height + 'px';

	/* Change images div properties */
	img_div.style.height = images_width * 0.338 + 'px';

	/* Change captions div properties */
	caption_div.style.width = images_width + 'px';
	caption_div.style.marginTop = images_width * 0.03 + 'px';

	/* Change scrollbar div properties */
	scrollbar_div.style.marginTop = images_width * 0.02 + 'px';
	scrollbar_div.style.marginLeft = images_width * 0.2 + 'px';
	scrollbar_div.style.width = scrollbar_width + 'px';
    /* Set slider attributes */
	slider_div.onmousedown = function () { dragstart(this); };
	slider_div.style.cursor = conf_slider_cursor;

	/* Cache EVERYTHING! */
	max = img_div.childNodes.length;

	var i = 0;
	for (var index = 0; index < max; index++) {
		var image = img_div.childNodes.item(index);
		if (image.nodeType === 1) {
			array_images[i] = index;

			/* Set image onclick by adding i and x_pos as attributes! */
			image.onclick = function() { glideTo(this.x_pos, this.i); };
			image.x_pos = (-i * xstep);
			image.i = i;

			/* Add width and height as attributes ONLY once onload */
			if (onload === true) {
				image.w = image.width;
				image.h = image.height;
			}

			/* Check source image format. Get image height minus reflection height! */
			if ((image.w + 1) > (image.h / (conf_reflection_p + 1))) {
				/* Landscape format */
				image.pc = 118;
			} else {
				/* Portrait and square format */
				image.pc = 100;
			}

			/* Set ondblclick event */
			image.url = image.getAttribute('longdesc');
			image.ondblclick = function() { document.location = this.url; };

			/* Set image cursor type */
			image.style.cursor = conf_images_cursor;

			i++;
		}
	}
	max = array_images.length;

	/* Display images in current order */
	moveTo(current);
	glideTo(current, caption_id);
}

/* Show/hide element functions */
function show(id)
{
	var element = document.getElementById(id);
	element.style.visibility = 'visible';
}
function hide(id)
{
	var element = document.getElementById(id);
	element.style.visibility = 'hidden';
	element.style.display = 'none';
}

/* Handle the wheel angle change (delta) of the mouse wheel */
function handle(delta)
{
	var change = false;
	if (delta > 0) {
		if (caption_id >= 1)	{
				target = target + xstep;
				new_caption_id = caption_id - 1;
				change = true;
		}
	} else {
		if (caption_id < (max-1)) {
				target = target - xstep;
				new_caption_id = caption_id + 1;
				change = true;
		}
	}

	/* Glide to next (mouse wheel down) / previous (mouse wheel up) image */
	if (change === true) {
		glideTo(target, new_caption_id);
	}
}

/* Event handler for mouse wheel event */
function wheel(event)
{
	var delta = 0;
	if (!event) {
	    event = window.event;
	}

	if (event.wheelDelta) {
		delta = event.wheelDelta / 120;
	} else if (event.detail) {
		delta = -event.detail / 3;
	}

	if (delta) {
	    handle(delta);
	}

	if (event.preventDefault) {
	    event.preventDefault();
	}
	event.returnValue = false;
}

/* Initialize mouse wheel event listener */
function initMouseWheel()
{
	if (window.addEventListener) {
	    imageflow_div.addEventListener('DOMMouseScroll', wheel, false);
	}
	imageflow_div.onmousewheel = wheel;
}

/* This function is called to drag an object (= slider div) */
function dragstart(element)
{
	dragobject = element;
	dragx = posx - dragobject.offsetLeft + new_slider_pos;
}

/* This function is called to stop dragging an object */
function dragstop()
{
	dragobject = null;
	dragging = false;
}

/* This function is called on mouse movement and moves an object (= slider div) on user action */
function drag(e)
{
	posx = document.all ? window.event.clientX : e.pageX;
	if (dragobject) {
		dragging = true;
		new_posx = (posx - dragx) + conf_slider_width;

		/* Make sure, that the slider is moved in proper relation to previous movements by the glideTo function */
		if (new_posx < ( - new_slider_pos)) {
		    new_posx = - new_slider_pos;
		}
		if (new_posx > (scrollbar_width - new_slider_pos)) {
		    new_posx = scrollbar_width - new_slider_pos;
		}

		var slider_pos = (new_posx + new_slider_pos);
		var step_width = slider_pos / ((scrollbar_width) / (max-1));
		var image_number = Math.round(step_width);
		var new_target = (image_number) * -xstep;
		var new_caption_id = image_number;

		dragobject.style.left = new_posx + "px";
		glideTo(new_target, new_caption_id);
	}
}

/* Initialize mouse event listener */
function initMouseDrag()
{
	document.onmousemove = drag;
	document.onmouseup = dragstop;
}

function getKeyCode(event)
{
	event = event || window.event;
	return event.keyCode;
}

/* Refresh ImageFlow on window resize */
window.onresize = function()
{
    if (imageflow_div) {
        refresh();
    }
};

document.onkeydown = function(event)
{
	var charCode  = getKeyCode(event);
	switch (charCode) {
		/* Right arrow key */
		case 39:
			handle(-1);
			break;

		/* Left arrow key */
		case 37:
			handle(1);
			break;
	}
};

return {
    /* Hide loading bar, show content and initialize mouse event listening after loading */
    initialize : function() {
        // set internal global variable
        imageflow_div = document.getElementById(conf_imageflow);

        if (imageflow_div) {
            var images = imageflow_div.getElementsByTagName("img");
            var ready = true;
            for (var i = 0; i < images.length; i++) {
                if (! images[i].complete) {
                    ready = false;
                    break;
                }
            }
            if (! ready) {
                setTimeout(ImageFlow.initialize, 500);
            } else {
                hide(conf_loading);
                refresh(true);
                show(conf_images);
                show(conf_scrollbar);
                initMouseWheel();
                initMouseDrag();
            }
        }
    }

    };
}();