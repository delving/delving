      <select onchange="setLang(this.options[selectedIndex].value)" name="dd_lang" id="dd_lang">
          <option value="Choose language" selected="selected"><@spring.message 'ChooseLanguage_t' /></option>
          <option value="ca">Catal&#224; (ca)</option>
          <option value="bg">
              &#x0411;&#x044a;&#x043b;&#x0433;&#x0430;&#x0440;&#x0441;&#x043a;&#x0438; (bul)
          </option>
          <option value="cs">
              &#268;e&#353;tina (cze/cse)
              <#--<@spring.message 'czechlanguage_t' />-->
          </option>
          <option value="da">
              Dansk (dan)
              <#--<@spring.message 'danish_t' />-->
          </option>
          <option value="de">
              Deutsch (deu)
              <#--<@spring.message 'german_t' />-->
          </option>
          <option value="nl">
              Nederlands (dut)
              <#--<@spring.message 'dutch_t' />-->
          </option>
          <option value="el">
              &#917;&#955;&#955;&#951;&#957;&#953;&#954;&#940; (ell/gre)
              <#--<@spring.message 'greek_t' />-->
          </option>
          <option value="en">
          English (eng)
          <#--<@spring.message 'english_t' />-->
          </option>
          <option value="es">
              Espa&#241;ol (esp)
              <#--<@spring.message 'spanish_t' />-->
          </option>
          <option value="et">
          Eesti (est)
          <#--<@spring.message 'estonian_t' />-->
          </option>
          <option value="fi">
          Suomi (fin)
          <#--<@spring.message 'finnish_t' />-->
          </option>
          <option value="fr">
          Fran&#231;ais (fre)
          <#--<@spring.message 'french_t' />-->
          </option>
          <option value="ga">
          Irish (gle)
          </option>
          <option value="hu">
          Magyar (hun)
          <#--<@spring.message 'hungarian_t' />-->
          </option>
          <option value="is">
          &#205;slenska (ice)
          <#--<@spring.message 'icelandic_t' />-->
          </option>
          <option value="it">
          Italiano (ita)
          <#--<@spring.message 'italian_t' />-->
          </option>
          <option value="lv">
          Latvie&#353;u (lav)
          <#--<@spring.message 'latvian_t' />-->
          </option>
          <option value="lt">
          Lietuvi&#371; (lit)
          <#--<@spring.message 'lithuanian_t' />-->
          </option>
          <option value="mt">
          Malti (mlt)
          <#--<@spring.message 'maltese_t' />-->
          </option>
          <option value="no">
          Norsk (nor)
          <#--<@spring.message 'norwegian_t' />-->
          </option>
          <option value="pl">
          Polski (pol)
          <#--<@spring.message 'polish_t' />-->
          </option>
          <option value="pt">
          Portugu&#234;s (por)
          <#--<@spring.message 'portuguese_t' />-->
          </option>
          <option value="ro">
          Rom&#226;n&#259; (rom)
          <#--<@spring.message 'romanian_t' />-->
          </option>
          <option value="sk">
          Slovensk&#253; (slo)
          <#--<@spring.message 'slovak_t' />-->
          </option>
          <option value="sl">
          Sloven&#353;&#269;ina (slv)
          <#--<@spring.message 'slovenian_t' />-->
          </option>
          <option value="sv">
              Svenska (sve/swe)
              <#--<@spring.message 'swedish_t' />-->
          </option>
      </select>
<form method="post" id="frm-lang" name="frm-lang" style="display: none;" action="../../">
    <input  type="hidden" name="lang"/>
</form>
