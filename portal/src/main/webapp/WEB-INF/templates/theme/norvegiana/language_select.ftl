      <select onchange="setLang(this.options[selectedIndex].value)" name="dd_lang" id="dd_lang">
          <option value="Choose language" selected="selected"><@spring.message '_action.chooselanguage' /></option>

          <option value="en">
          English (eng)
          <#--<@spring.message '_locale.en' />-->
          </option>
          <option value="nb">
          Bokm&aring;l (nob)
          <#--<@spring.message '_locale.no' />-->
          </option>
          <option value="no">
          Norsk (nor)
          <#--<@spring.message '_locale.no' />-->
          </option>
      </select>
<form method="post" id="frm-lang" name="frm-lang" style="display: none;" action="/${portalName}/">
    <input  type="hidden" name="lang"/>
</form>
