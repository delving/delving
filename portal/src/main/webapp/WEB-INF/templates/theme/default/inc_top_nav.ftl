      <select onchange="setLang(this.options[selectedIndex].value)" name="dd_lang" id="dd_lang">
          <option value="Choose language" selected="selected"><@spring.message 'ChooseLanguage_t' /></option>
          <option value="ca">Catal&#224; (ca)</option>
          <option value="bg">
              &#x0411;&#x044a;&#x043b;&#x0433;&#x0430;&#x0440;&#x0441;&#x043a;&#x0438; (bul)
          </option>
          <option value="cs">
              &#268;e&#353;tina (cze/cse)
              <#--<@spring.message 'locale.czechlanguage' />-->
          </option>
          <option value="da">
              Dansk (dan)
              <#--<@spring.message 'locale.danish' />-->
          </option>
          <option value="de">
              Deutsch (deu)
              <#--<@spring.message 'locale.german' />-->
          </option>
          <option value="nl">
              Nederlands (dut)
              <#--<@spring.message 'locale.dutch' />-->
          </option>
          <option value="el">
              &#917;&#955;&#955;&#951;&#957;&#953;&#954;&#940; (ell/gre)
              <#--<@spring.message 'locale.greek' />-->
          </option>
          <option value="en">
          English (eng)
          <#--<@spring.message 'locale.english' />-->
          </option>
          <option value="es">
              Espa&#241;ol (esp)
              <#--<@spring.message 'locale.spanish' />-->
          </option>
          <option value="et">
          Eesti (est)
          <#--<@spring.message 'locale.estonian' />-->
          </option>
          <option value="fi">
          Suomi (fin)
          <#--<@spring.message 'locale.finnish' />-->
          </option>
          <option value="fr">
          Fran&#231;ais (fre)
          <#--<@spring.message 'locale.french' />-->
          </option>
          <option value="ga">
          Irish (gle)
          </option>
          <option value="hu">
          Magyar (hun)
          <#--<@spring.message 'locale.hungarian' />-->
          </option>
          <option value="is">
          &#205;slenska (ice)
          <#--<@spring.message 'locale.icelandic' />-->
          </option>
          <option value="it">
          Italiano (ita)
          <#--<@spring.message '_language_select.italian' />-->
          </option>
          <option value="lv">
          Latvie&#353;u (lav)
          <#--<@spring.message 'locale.latvian' />-->
          </option>
          <option value="lt">
          Lietuvi&#371; (lit)
          <#--<@spring.message 'locale.lithuanian' />-->
          </option>
          <option value="mt">
          Malti (mlt)
          <#--<@spring.message 'locale.maltese' />-->
          </option>
          <option value="no">
          Norsk (nor)
          <#--<@spring.message 'locale.norwegian' />-->
          </option>
          <option value="pl">
          Polski (pol)
          <#--<@spring.message 'locale.polish' />-->
          </option>
          <option value="pt">
          Portugu&#234;s (por)
          <#--<@spring.message 'locale.portuguese' />-->
          </option>
          <option value="ro">
          Rom&#226;n&#259; (rom)
          <#--<@spring.message 'locale.romanian' />-->
          </option>
          <option value="sk">
          Slovensk&#253; (slo)
          <#--<@spring.message 'locale.slovak' />-->
          </option>
          <option value="sl">
          Sloven&#353;&#269;ina (slv)
          <#--<@spring.message 'locale.slovenian' />-->
          </option>
          <option value="sv">
              Svenska (sve/swe)
              <#--<@spring.message 'locale.swedish' />-->
          </option>
      </select>


<form method="post" id="frm-lang" name="frm-lang" style="display: none;" action="../../">
    <input  type="hidden" name="lang"/>
</form>
