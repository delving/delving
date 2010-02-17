<!-- footer start -->
<div id="footer">

           <!-- AddThis Button BEGIN -->
        <#assign  showthislang = locale>
        <#if  locale = "mt" || locale = "et">
           <#assign  showthislang = "en">
        </#if>
        <p><!-- addthis_button -->
           <a class="noeffect" href="http://www.addthis.com/bookmark.php?v=250&amp;username=xa-4b4f08de468caf36" target="_blank">
           <img src="http://s7.addthis.com/static/btn/lg-share-${showthislang}.gif" alt="Bookmark and Share" style="border:0"/>
           </a>
           </p>
           <script type="text/javascript" src="http://s7.addthis.com/js/250/addthis_widget.js#username=xa-4b4f08de468caf36"></script>
            <script type="text/javascript">
                var addthis_config = {
                     ui_language: "${showthislang}"
                }
              </script>
           <!-- AddThis Button END -->
           <!-- TODO: google analytics -->
	<a href="aboutus.html">About us</a> | <a href="contact.html">Contact</a> | <a href="termsofservice.html">Terms and Conditions</a>
</div>

</body>
</html>

<!-- footer end -->