<link rel="stylesheet" type="text/css" href="mobile/zflow/zflow.css"/>
<script type="text/javascript" src="mobile/zflow/zflow.js"></script>
<script type="text/javascript">
		function initflow()
		{
    		window.onorientationchange(null);
     		
     		var images= Array(
    			<#list carouselItems as carouselItem>
				 <#if carouselItem_index <=12> <#-- we only want to see a maximum of 12 items, otherwise page-load will be too slow -->
                  <#assign doc = carouselItem.doc/>                  
					<#if useCache="true">
                      ["${cacheUrl}uri=${doc.thumbnail?url('utf-8')}&size=BRIEF_DOC&type=${doc.type}", 
                    <#else>
                      ["${doc.thumbnail}", 
					</#if>
                    "full-doc.html?query=&start=1&uri=${doc.id}",
                    <#assign title = ""/>
                    <#if doc.title??>
                      <#assign title = doc.title />
                    </#if>
                    <#if (title?length <= 1)>
                      <#assign title = "..." />
                    </#if>
                    "${title?html}"]
                    <#if carouselItem_has_next>,</#if>
                   </#if>                                
             	</#list>
                );
			zflow(images, "#tray");
		}
		window.onorientationchange = function (event)
		{
    		if (window.orientation == 0)
    		{
				document.getElementById("container").className="centering portrait";
    		}
    		else
    		{
				document.getElementById("container").className="centering landscape";		
    		}
		}
	</script>