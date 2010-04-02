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

    function saveRequest(className, id){
        $.ajax({
           type: "POST",
           url: "save.ajax",
           data: "className="+className+"&id="+id,
           success: function(msg){
                window.location.reload();
           },
           error: function(msg) {
               alert("An error occured. The item could not be saved.");
           }
         });
    }

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