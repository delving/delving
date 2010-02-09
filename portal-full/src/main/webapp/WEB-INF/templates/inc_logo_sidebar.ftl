<a href="index.html">
    <img src="images/europeana-logo-small.jpg" alt="Click here to return to the Europeana homepage" title="logo Europeana think culture"/>
    <#assign image_langs = ["cs","da","de","el","en","es","et","fi","fr","ga","hu","is","it","lt","lv","mt","nl","pl","pt","sk","sl","sv"]>
    <#if image_langs?seq_index_of(interfaceLanguage) != -1>
        <img src="images/think_culture_${interfaceLanguage}_small.gif" alt=""/>
    <#else>
        <img src="images/think_culture_en_small.gif" alt=""/>
    </#if>
 </a>