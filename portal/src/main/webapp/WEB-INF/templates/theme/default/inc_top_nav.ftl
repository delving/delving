      <select onchange="setLang(this.options[selectedIndex].value)" name="dd_lang" id="dd_lang">
          <option value="Choose language" selected="selected"><@spring.message '_menu.i18n.chooselanguage' /></option>
          <option value="ca">Catal&#224; (ca)</option>
          <option value="bg">
              &#x0411;&#x044a;&#x043b;&#x0433;&#x0430;&#x0440;&#x0441;&#x043a;&#x0438; (bul)
          </option>
          <option value="cs">
              &#268;e&#353;tina (cze/cse)
              <#--<@spring.message '_locale.cs' />-->
          </option>
          <option value="da">
              Dansk (dan)
              <#--<@spring.message '_locale.danish' />-->
          </option>
          <option value="de">
              Deutsch (deu)
              <#--<@spring.message '_locale.de' />-->
          </option>
          <option value="nl">
              Nederlands (dut)
              <#--<@spring.message '_locale.nl' />-->
          </option>
          <option value="el">
              &#917;&#955;&#955;&#951;&#957;&#953;&#954;&#940; (ell/gre)
              <#--<@spring.message '_locale.el' />-->
          </option>
          <option value="en">
          English (eng)
          <#--<@spring.message '_locale.en' />-->
          </option>
          <option value="es">
              Espa&#241;ol (esp)
              <#--<@spring.message '_locale.es' />-->
          </option>
          <option value="et">
          Eesti (est)
          <#--<@spring.message '_locale.EEn' />-->
          </option>
          <option value="fi">
          Suomi (fin)
          <#--<@spring.message '_locale.fi' />-->
          </option>
          <option value="fr">
          Fran&#231;ais (fre)
          <#--<@spring.message '_locale.fr' />-->
          </option>
          <option value="ga">
          Irish (gle)
          </option>
          <option value="hu">
          Magyar (hun)
          <#--<@spring.message '_locale.hu' />-->
          </option>
          <option value="is">
          &#205;slenska (ice)
          <#--<@spring.message '_locale.ISic' />-->
          </option>
          <option value="it">
          Italiano (ita)
          <#--<@spring.message '_locale.it' />-->
          </option>
          <option value="lv">
          Latvie&#353;u (lav)
          <#--<@spring.message '_locale.LVn' />-->
          </option>
          <option value="lt">
          Lietuvi&#371; (lit)
          <#--<@spring.message '_locale.LTn' />-->
          </option>
          <option value="mt">
          Malti (mlt)
          <#--<@spring.message '_locale.mt' />-->
          </option>
          <option value="no">
          Norsk (nor)
          <#--<@spring.message '_locale.norwegian' />-->
          </option>
          <option value="pl">
          Polski (pol)
          <#--<@spring.message '_locale.pl' />-->
          </option>
          <option value="pt">
          Portugu&#234;s (por)
          <#--<@spring.message '_locale.pt' />-->
          </option>
          <option value="ro">
          Rom&#226;n&#259; (rom)
          <#--<@spring.message '_locale.ROn' />-->
          </option>
          <option value="sk">
          Slovensk&#253; (slo)
          <#--<@spring.message '_locale.sk' />-->
          </option>
          <option value="sl">
          Sloven&#353;&#269;ina (slv)
          <#--<@spring.message '_locale.sl' />-->
          </option>
          <option value="sv">
              Svenska (sve/swe)
              <#--<@spring.message '_locale.sv' />-->
          </option>
      </select>


<form method="post" id="frm-lang" name="frm-lang" style="display: none;" action="../../">
    <input  type="hidden" name="lang"/>
</form>
