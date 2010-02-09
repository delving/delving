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

$(document).ready(function() {
        var openTab = "0";
        if ($.cookie('ui-tabs-3')){
            openTab = $.cookie('ui-tabs-3');
        }
       $("#savedItems").tabs({selected: openTab});
/*       $("#savedItems").tabs({selected: $.cookie('ui-tabs-3')});*/
       $("#savedItems").tabs({ cookie: { expires: 30 } });
    });

    function removeRequest(className, id){
        $.ajax({
           type: "POST",
           url: "remove.ajax",
           data: "className="+className+"&id="+id,
           success: function(msg){
                window.location.reload();
           },
           error: function(msg) {
                //alert("An error occured. The item could not be removed");
               $("#removeRequestMessage-"+id).show();
           }
         });
    };

    function addEditorItemRequest(className, id){
        $.ajax({
           type: "POST",
           url: "save-editor-item.ajax",
           data: "className="+className+"&id="+id,
           success: function(msg){
                window.location.reload();
           },
           error: function(msg) {
               //alert("An error occured. The item could not be added");
               $("#removeRequestMessage-"+id).show();
           }
         });
    };

    function carouselRequest(className, id){
    	var status = document.getElementById("carousel_" + id).checked;
        $.ajax({
           type: "POST",
           url: "save-to-carousel.ajax",
           data: "className="+className+"&id="+id+"&status="+status,
           success: function(msg){
                window.location.reload();
           },
           error: function(msg) {
                alert("An error occured. The item could not be aded to carousel");
           }
         });
    };

    function pactaRequest(className, id){
    	var status = document.getElementById("pacta_" + id).checked;
        $.ajax({
           type: "POST",
           url: "save-to-pacta.ajax",
           data: "className="+className+"&id="+id+"&status="+status,
           success: function(msg){
                window.location.reload();
           },
           error: function(msg) {
                alert("An error occured. The item could not be aded to pacta");
           }
         });
    };

    function showDefault(obj,iType){
        switch(iType)
        {
        case "TEXT":
          obj.src="images/item-page.gif";
          break;
        case "IMAGE":
          obj.src="images/item-image.gif";
          break;
        case "VIDEO":
          obj.src="images/item-video.gif";
          break;
        case "SOUND":
          obj.src="images/item-sound.gif";
          break;
        default:
          obj.src="images/item-page.gif";
        }
     }