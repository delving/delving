    <div class="grid_12" id="footer">
        <div class="inner">
            <#--<img src="images/poweredbydelving.png" alt="Powerd by Delving"/>-->
        </div>
    </div><!-- end footer -->
</div><!-- end container_12 -->
<#if trackingCode??>
    <script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '${trackingCode}']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
</#if>
</body>
</html>
