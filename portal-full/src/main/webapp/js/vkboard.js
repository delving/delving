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

function VKeyboard(container_id, callback_ref, create_numpad, font_name,
                   font_size, font_color, dead_color, bg_color, key_color,
                   sel_item_color, border_color, inactive_border_color,
                   inactive_key_color, lang_sel_brd_color, show_click,
                   click_font_color, click_bg_color, click_border_color,
                   do_embed, langYoann)
{
  return this._construct(container_id, callback_ref, create_numpad,
                         font_name, font_size, font_color, dead_color,
                         bg_color, key_color, sel_item_color, border_color,
                         inactive_border_color, inactive_key_color,
                         lang_sel_brd_color, show_click, click_font_color,
                         click_bg_color, click_border_color, do_embed, langYoann);
}

VKeyboard.prototype = {

  kbArray: [],

  _get_event_source: function(event)
  {
    var e = event || window.event;
    return e.srcElement || e.target;
  },

  _setup_event: function(elem, eventType, handler)
  {
    return (elem.attachEvent ? elem.attachEvent("on" + eventType, handler) : ((elem.addEventListener) ? elem.addEventListener(eventType, handler, false) : null));
  },

  _detach_event: function(elem, eventType, handler)
  {
    return (elem.detachEvent ? elem.detachEvent("on" + eventType, handler) : ((elem.removeEventListener) ? elem.removeEventListener(eventType, handler, false) : null));
  },

  _start_flash: function(in_el)
  {
    function getColor(str, posOne, posTwo)
    {
      if(/rgb\((\d+),\s(\d+),\s(\d+)\)/.exec(str)) // try to detect Mozilla-style rgb value.
      {
        switch(posOne)
        {
          case 1: return parseInt(RegExp.$1, 10);
          case 2: return parseInt(RegExp.$2, 10);
          case 3: return parseInt(RegExp.$3, 10);
          default: return 0;
        }
      }
      else // standard (#xxxxxx or #xxx) way
        return str.length == 4 ? parseInt(str.substr(posOne, 1) + str.substr(posOne, 1), 16) : parseInt(str.substr(posTwo, 2), 16);
    }

    function getR(color_string)
    { return getColor(color_string, 1, 1); }

    function getG(color_string)
    { return getColor(color_string, 2, 3); }

    function getB(color_string)
    { return getColor(color_string, 3, 5); }

    var el = in_el.time ? in_el : (in_el.company && in_el.company.time ? in_el.company : null);
    if(el)
    {
      el.time = 0;
      clearInterval(el.timer);
    }

    var vkb = this;
    var ftc = vkb.fontcolor, bgc = vkb.keycolor, brc = vkb.bordercolor;

    // Special fixes for simple/dead/modifier keys:

    if(in_el.dead)
      ftc = vkb.deadcolor;

    if(((in_el.innerHTML == "Shift") && vkb.Shift) || ((in_el.innerHTML == "Caps") && vkb.Caps) || ((in_el.innerHTML == "AltGr") && vkb.AltGr))
      bgc = vkb.lic;

    // Extract base color values:
    var fr = getR(ftc), fg = getG(ftc), fb = getB(ftc);
    var kr = getR(bgc), kg = getG(bgc), kb = getB(bgc);
    var br = getR(brc), bg = getG(brc), bb = getB(brc);

    // Extract flash color values:
    var f_r = getR(vkb.cfc), f_g = getG(vkb.cfc), f_b = getB(vkb.cfc);
    var k_r = getR(vkb.cbg), k_g = getG(vkb.cbg), k_b = getB(vkb.cbg);
    var b_r = getR(vkb.cbr), b_g = getG(vkb.cbr), b_b = getB(vkb.cbr);

    var _shift_colors = function()
    {
      function dec2hex(dec)
      {
        var hexChars = "0123456789ABCDEF";
        var a = dec % 16;
        var b = (dec - a) / 16;

        return hexChars.charAt(b) + hexChars.charAt(a) + "";
      }

      in_el.time = !in_el.time ? 10 : (in_el.time - 1);

      function calc_color(start, end)
      { return (end - (in_el.time / 10) * (end - start)); }

      var t_f_r = calc_color(f_r, fr), t_f_g = calc_color(f_g, fg), t_f_b = calc_color(f_b, fb);
      var t_k_r = calc_color(k_r, kr), t_k_g = calc_color(k_g, kg), t_k_b = calc_color(k_b, kb);
      var t_b_r = calc_color(b_r, br), t_b_g = calc_color(b_g, bg), t_b_b = calc_color(b_b, bb);

      function setStyles(style)
      {
        style.color = "#" + dec2hex(t_f_r) + dec2hex(t_f_g) + dec2hex(t_f_b);
        style.borderColor = "#" + dec2hex(t_b_r) + dec2hex(t_b_g) + dec2hex(t_b_b);
        style.backgroundColor = "#" + dec2hex(t_k_r) + dec2hex(t_k_g) + dec2hex(t_k_b);
      }

      var first = (in_el == vkb.mod[4]) ? false : true, is = in_el.style, cs = in_el.company ? in_el.company.style : null;

      if(cs && first)
        setStyles(cs);

      setStyles(is);

      if(cs)
      {
        if(!first)
        {
          setStyles(cs);
          is.borderBottomColor = "#" + dec2hex(t_k_r) + dec2hex(t_k_g) + dec2hex(t_k_b);
        }
        else
          cs.borderBottomColor = "#" + dec2hex(t_k_r) + dec2hex(t_k_g) + dec2hex(t_k_b);
      }

      if(!in_el.time)
      {
        clearInterval(in_el.timer);
        return;
      }
    };

    _shift_colors();

    in_el.timer = window.setInterval(_shift_colors, 50);
  },

  _setup_style: function(obj, top, left, width, height, position, text_align, line_height, font_size, font_weight, padding_left, padding_right)
  {
    var os = obj.style;

    if(top)    os.top = top;
    if(left)   os.left = left;
    if(width)  os.width = width;
    if(height) os.height = height;

    if(position) os.position = position;

    if(text_align)  os.textAlign  = text_align;
    if(line_height) os.lineHeight = line_height;
    if(font_size)   os.fontSize   = font_size;

    os.fontWeight = font_weight || "bold";

    if(padding_left)  os.paddingLeft  = padding_left;
    if(padding_right) os.paddingRight = padding_right;
  },

  _setup_key: function(parent, id, top, left, width, height, text_align, line_height, font_size, font_weight, padding_left, padding_right)
  {
    var _id = this.Cntr.id + id;
    var exists = document.getElementById(_id);

    var key = exists ? exists.parentNode : document.createElement("DIV");
    this._setup_style(key, top, left, width, height, "absolute");

    var key_sub = exists || document.createElement("DIV");
    key.appendChild(key_sub); parent.appendChild(key);

    this._setup_style(key_sub, "", "", "", line_height, "relative", text_align, line_height, font_size, font_weight, padding_left, padding_right);
    key_sub.id = _id;

    return key_sub;
  },

  _findX: function(obj)
  { return (obj && obj.parentNode) ? parseFloat(obj.parentNode.offsetLeft) : 0; },

  _findY: function(obj)
  { return (obj && obj.parentNode) ? parseFloat(obj.parentNode.offsetTop) : 0; },

  _findW: function(obj)
  { return (obj && obj.parentNode) ? parseFloat(obj.parentNode.offsetWidth) : 0; },

  _findH: function(obj)
  { return (obj && obj.parentNode) ? parseFloat(obj.parentNode.offsetHeight) : 0; },

  _construct: function(container_id, callback_ref, create_numpad, font_name, font_size, font_color, dead_color,
                       bg_color, key_color, sel_item_color, border_color, inactive_border_color, inactive_key_color,
                       lang_sel_brd_color, show_click, click_font_color, click_bg_color, click_border_color, do_embed, langYoann)
  {
    var exists  = (this.Cntr != undefined), ct = exists ? this.Cntr : document.getElementById(container_id);
    var changed = (font_size && (font_size != this.fontsize));

    this._Callback = ((typeof(callback_ref) == "function") && ((callback_ref.length == 1) || (callback_ref.length == 2))) ? callback_ref : (this._Callback || null);

    var ff = font_name || this.fontname || "";
    var fs = font_size || this.fontsize || "14px";

    var fc = font_color   || this.fontcolor   || "#000";
    var dc = dead_color   || this.deadcolor   || "#F00";
    var bg = bg_color     || this.bgcolor     || "#FFF";
    var kc = key_color    || this.keycolor    || "#FFF";
    var bc = border_color || this.bordercolor || "#777";

    this.lic = sel_item_color        || this.lic || "#DDD";
    this.ibc = inactive_border_color || this.ibc || "#CCC";
    this.ikc = inactive_key_color    || this.ikc || "#FFF";
    this.lsc = lang_sel_brd_color    || this.lsc || "#F77";

    this.cfc = click_font_color   || this.cfc || "#CC3300";
    this.cbg = click_bg_color     || this.cbg || "#FF9966";
    this.cbr = click_border_color || this.cbr || "#CC3300";

    this.sc = (show_click == undefined) ? ((this.sc == undefined) ? false : this.sc) : show_click;

    this.fontname = ff, this.fontsize = fs, this.fontcolor = fc;
    this.bgcolor = bg,  this.keycolor = kc, this.deadcolor = dc, this.bordercolor = bc;

    if(!exists)
    {
      this.Cntr = ct;
      this.Caps = this.Shift = this.AltGr = false;

      this.DeadAction = []; this.DeadAction[0] = this.DeadAction[1] = null;
      this.keys = [], this.mod = [], this.pad = [];

      VKeyboard.prototype.kbArray[container_id] = this;
    }

    var kb = exists ? ct.childNodes[0] : document.createElement("DIV");

    if(!exists)
    {
      ct.appendChild(kb);
      ct.style.display = "block";
      ct.style.zIndex  = 999;

      if(do_embed)
        ct.style.position = "relative";
      else
      {
        ct.style.position = "absolute";

        // Many thanks to Peter-Paul Koch (www.quirksmode.org) for the find-pos-X/find-pos-Y code.
        var initX = 0, ct_ = ct;
        if(ct_.offsetParent)
        {
          while(ct_.offsetParent)
          {
            initX += ct_.offsetLeft;
            ct_ = ct_.offsetParent;
          }
        }
        else if(ct_.x)
          initX += ct_.x;

        var initY = 0; ct_ = ct;
        if(ct_.offsetParent)
        {
          while(ct_.offsetParent)
          {
            initY += ct_.offsetTop;
            ct_ = ct_.offsetParent;
          }
        }
        else if(ct_.y)
          initY += ct_.y;

        ct.style.top = initY + "px", ct.style.left = initX +"px";
      }

      kb.style.position = "relative";
      kb.style.top      = "0px", kb.style.left = "0px";
    }

    kb.style.border = "1px solid " + bc;

    var kb_main = exists ? kb.childNodes[0] : document.createElement("DIV"), ks = kb_main.style;
    if(!exists)
    {
      kb.appendChild(kb_main);

      ks.position = "relative";
      ks.width    = "1px";
      ks.cursor   = "default";
    }

    // Disable content selection:
    this._setup_event(kb_main, "selectstart", function(event) { return false; });
    this._setup_event(kb_main, "mousedown",   function(event) { if(event.preventDefault) event.preventDefault(); return false; });

    ks.fontFamily = ff, ks.backgroundColor = bg;

    if(!exists || changed)
    {
      var mag = parseFloat(fs) / 14.0, cell = Math.floor(25.0 * mag), dcell = 2 * cell;
      var cp = String(cell) + "px", lh = String(cell - 2.0) + "px";

      var prevX = 0, prevY = 1, prevW = 0, prevH = 0;

      // Convenience strings:
      var c = "center", n = "normal", r = "right", l = "left", e = "&nbsp;", pad = String(4 * mag) + "px";

      // Number row:

      var key;
      for(var i = 0; i < 13; i++)
      {
        this.keys[i] = key = this._setup_key(kb_main, "___key" + String(i), "1px", (prevX + prevW + 1) + "px", cp, cp, c, lh, fs);

        prevX = this._findX(key), prevW = this._findW(key);
      }

      prevY = this._findY(key);
      prevH = this._findH(key); // universal key height

      var kb_kbp = this._setup_key(kb_main, "___kbp", "1px", (prevX + prevW + 1) + "px", (2.96 * cell) + "px", cp, r, lh, fs, n, "", pad);
      kb_kbp.innerHTML = "BackSpace";
      this.mod[0] = kb_kbp;

      // Top row:

      var kb_tab = this._setup_key(kb_main, "___tab", (prevY + prevH + 1) + "px", "1px", (1.48 * cell + 1) + "px", cp, l, lh, fs, n, pad);
      kb_tab.innerHTML = "Tab";
      this.mod[1] = kb_tab;

      prevX = this._findX(kb_tab), prevW = this._findW(kb_tab), prevY = this._findY(kb_tab);

      for(; i < 26; i++)
      {
        this.keys[i] = key = this._setup_key(kb_main, "___key" + String(i), prevY + "px", (prevX + prevW + 1) + "px", cp, cp, c, lh, fs);

        prevX = this._findX(key), prevW = this._findW(key);
      }

      this.kbpH = this._findX(kb_kbp) + this._findW(kb_kbp);

      // Home row:

      var kb_caps = this._setup_key(kb_main, "___caps", (prevY + prevH + 1) + "px", "1px", dcell + "px", cp, l, lh, fs, n, pad);
      kb_caps.innerHTML = "Caps";
      this.mod[2] = kb_caps;

      prevX = this._findX(kb_caps), prevW = this._findW(kb_caps), prevY = this._findY(kb_caps);

      for(; i < 38; i++)
      {
        this.keys[i] = key = this._setup_key(kb_main, "___key" + String(i), prevY + "px", (prevX + prevW + 1) + "px", cp, cp, c, lh, fs);

        prevX = this._findX(key), prevW = this._findW(key);
      }

      prevY = this._findY(key);
      var s = prevX + prevW + 1;

      var kb_enter = this._setup_key(kb_main, "___enter_l", prevY + "px", s + "px", (this.kbpH - s) + "px", cp, r, lh, fs, n, "", pad);
      kb_enter.innerHTML = "Enter";
      this.mod[3] = kb_enter;

      s = this._findX(this.keys[25]) + this._findW(this.keys[25]) + 1;

      var kb_enter_top = this._setup_key(kb_main, "___enter_top", this._findY(kb_tab) + "px", s + "px", (this.kbpH - s) + "px", cp, c, cp, fs);
      kb_enter_top.innerHTML = e;
      kb_enter_top.subst = "Enter";
      this.mod[4] = kb_enter_top;

      kb_enter_top.company = kb_enter;
      kb_enter.company = kb_enter_top;

      // Bottom row:

      var kb_shift = this._setup_key(kb_main, "___shift", (prevY + prevH + 1) + "px", "1px", (2.52 * cell) + "px", cp, l, lh, fs, n, pad);
      kb_shift.innerHTML = "Shift";
      this.mod[5] = kb_shift;

      prevX = this._findX(kb_shift), prevW = this._findW(kb_shift), prevY = this._findY(kb_shift);

      for(; i < 48; i++)
      {
        this.keys[i] = key = this._setup_key(kb_main, "___key" + String(i), prevY + "px", (prevX + prevW + 1) + "px", cp, cp, c, lh, fs);

        prevX = this._findX(key), prevW = this._findW(key);
      }

      prevY = this._findY(key);

      var kb_shift_r = this._setup_key(kb_main, "___shift_r", prevY + "px", (prevX + prevW + 1) + "px", (this._findX(kb_kbp) + this._findW(kb_kbp) - prevX - prevW - 1) + "px", cp, r, lh, fs, n, "", pad);
      kb_shift_r.innerHTML = "Shift";
      this.mod[6] = kb_shift_r;

      // LanguageEnum selector:

      var vcell = String(1.32 * cell) + "px";

      var kb_lang = this._setup_key(kb_main, "___lang", (prevY + prevH + 1) + "px", "1px", String(3 * 1.32 * cell) + "px", cp, l, lh, fs, n, pad);
      this.mod[7] = kb_lang;

      prevY = this._findY(kb_lang);

      ks.height = (prevY + prevH + 1) + "px";

      prevY += "px";

      var kb_space = this._setup_key(kb_main, "___space", prevY, (this._findX(kb_lang) + this._findW(kb_lang) + 1) + "px", (6.28 * cell) + "px", cp, c, lh, fs);
      this.mod[8] = kb_space;

      var kb_alt_gr = this._setup_key(kb_main, "___alt_gr", prevY, (this._findX(kb_space) + this._findW(kb_space) + 1) + "px", vcell, cp, c, lh, parseFloat(fs) * 0.786, n);
      kb_alt_gr.innerHTML = "AltGr";
      this.mod[9] = kb_alt_gr;

      var kb_res_3 = this._setup_key(kb_main, "___res_3", prevY, (this._findX(kb_alt_gr) + this._findW(kb_alt_gr) + 1) + "px", vcell, cp, c, lh, fs);
      kb_res_3.innerHTML = e;
      this.mod[10] = kb_res_3;

      var kb_res_4 = this._setup_key(kb_main, "___res_4", prevY, (this._findX(kb_res_3) + this._findW(kb_res_3) + 1) + "px", vcell, cp, c, lh, fs);
      kb_res_4.innerHTML = e;
      this.mod[11] = kb_res_4;

      var w = this.kbpH + 1;

      // Numpad:

      if((create_numpad == undefined) ? true : create_numpad)
      {
        var w2 = this._create_numpad(container_id, kb_main);
        if(w2 > w) w = w2;
      }

      kb.style.width = ks.width = w + "px";
    }

    //this._refresh_layout(this.layout_ = this.avail_langs[0][0]); //Here is the place where it sets the defaults language ! Yoann
		var whichlg;
		switch(langYoann){
			case 'en': whichlg='Us'; break;
			case 'fr': whichlg='Fr'; break;
			case 'da': whichlg='Da'; break;
			case 'bg': whichlg='Bg'; break;
			case 'cs': whichlg='Cz'; break;
			case 'de': whichlg='De'; break;
			case 'el': whichlg='El'; break;
			case 'es': whichlg='Es'; break;
			case 'et': whichlg='Et'; break;
			case 'fi': whichlg='Fi'; break;
			case 'ga': whichlg='Us'; break;
			case 'nothing': whichlg='He'; break;
			case 'hr': whichlg='Hr'; break;
			case 'hu': whichlg='Hu'; break;
			case 'is': whichlg='Is'; break;
			case 'it': whichlg='It'; break;
			case 'lt': whichlg='Lt'; break;
			case 'lv': whichlg='Lv'; break;
			case 'mt': whichlg='Us'; break;
			case 'nl': whichlg='Us'; break;
			case 'no': whichlg='No'; break;
			case 'pl': whichlg='Pl'; break;
			case 'pt': whichlg='Es'; break;
			case 'ro': whichlg='Ro'; break;
			case 'ru': whichlg='Ru'; break;
			case 'nothing2': whichlg='Sh'; break;
			case 'sl': whichlg='Sl'; break;
			case 'sk': whichlg='Sk'; break;
			case 'sr': whichlg='Sr'; break;
			case 'sv': whichlg='Sv'; break;
			default: whichlg='Ac'; break;
		}
		//this._refresh_layout(this.layout_ = this.avail_langs[whichlg][0]);
		this._refresh_layout(this.layout_ = whichlg);

    return this;
  },

  _create_numpad: function(container_id, parent)
  {
    var c = "center", n = "normal", l = "left";
    var fs = this.fontsize, bc = this.bordercolor;

    var mag = parseFloat(fs) / 14.0, cell = Math.floor(25.0 * mag);
    var dcell = 2 * cell, dp = (dcell + 1) + "px", dp2 = (dcell - 1) + "px";
    var cp = String(cell) + "px", lh = String(Math.floor(cell - 2.0)) + "px";

    var edge = (this.kbpH + cell + 1) + "px";

    var kb_pad_eur = this._setup_key(parent, "___pad_eur", "1px", edge, cp, cp, c, lh, fs);
    kb_pad_eur.innerHTML = "&#x20AC;";
    this.pad[0] = kb_pad_eur;

    var edge_1 = (this._findX(kb_pad_eur) + this._findW(kb_pad_eur) + 1) + "px";

    var kb_pad_slash = this._setup_key(parent, "___pad_slash", "1px", edge_1, cp, cp, c, lh, fs);
    kb_pad_slash.innerHTML = "/";
    this.pad[1] = kb_pad_slash;

    var edge_2 = (this._findX(kb_pad_slash) + this._findW(kb_pad_slash) + 1) + "px";

    var kb_pad_star = this._setup_key(parent, "___pad_star", "1px", edge_2, cp, cp, c, lh, fs);
    kb_pad_star.innerHTML = "*";
    this.pad[2] = kb_pad_star;

    var edge_3 = (this._findX(kb_pad_star) + this._findW(kb_pad_star) + 1) + "px";

    var kb_pad_minus = this._setup_key(parent, "___pad_minus", "1px", edge_3, cp, cp, c, lh, fs);
    kb_pad_minus.innerHTML = "-";
    this.pad[3] = kb_pad_minus;

    this.kbpM = this._findX(kb_pad_minus) + this._findW(kb_pad_minus) + 1;

    var prevH = this._findH(kb_pad_eur), edge_Y = (this._findY(kb_pad_eur) + prevH + 1) + "px";

    var kb_pad_7 = this._setup_key(parent, "___pad_7", edge_Y, edge, cp, cp, c, lh, fs);
    kb_pad_7.innerHTML = "7";
    this.pad[4] = kb_pad_7;

    var kb_pad_8 = this._setup_key(parent, "___pad_8", edge_Y, edge_1, cp, cp, c, lh, fs);
    kb_pad_8.innerHTML = "8";
    this.pad[5] = kb_pad_8;

    var kb_pad_9 = this._setup_key(parent, "___pad_9", edge_Y, edge_2, cp, cp, c, lh, fs);
    kb_pad_9.innerHTML = "9";
    this.pad[6] = kb_pad_9;

    var kb_pad_plus = this._setup_key(parent, "___pad_plus", edge_Y, edge_3, cp, dp, c, dp2, fs);
    kb_pad_plus.innerHTML = "+";
    this.pad[7] = kb_pad_plus;

    edge_Y = (this._findY(kb_pad_7) + prevH + 1) + "px";

    var kb_pad_4 = this._setup_key(parent, "___pad_4", edge_Y, edge, cp, cp, c, lh, fs);
    kb_pad_4.innerHTML = "4";
    this.pad[8] = kb_pad_4;

    var kb_pad_5 = this._setup_key(parent, "___pad_5", edge_Y, edge_1, cp, cp, c, lh, fs);
    kb_pad_5.innerHTML = "5";
    this.pad[9] = kb_pad_5;

    var kb_pad_6 = this._setup_key(parent, "___pad_6", edge_Y, edge_2, cp, cp, c, lh, fs);
    kb_pad_6.innerHTML = "6";
    this.pad[10] = kb_pad_6;

    edge_Y = (this._findY(kb_pad_4) + prevH + 1) + "px";

    var kb_pad_1 = this._setup_key(parent, "___pad_1", edge_Y, edge, cp, cp, c, lh, fs);
    kb_pad_1.innerHTML = "1";
    this.pad[11] = kb_pad_1;

    var kb_pad_2 = this._setup_key(parent, "___pad_2", edge_Y, edge_1, cp, cp, c, lh, fs);
    kb_pad_2.innerHTML = "2";
    this.pad[12] = kb_pad_2;

    var kb_pad_3 = this._setup_key(parent, "___pad_3", edge_Y, edge_2, cp, cp, c, lh, fs);
    kb_pad_3.innerHTML = "3";
    this.pad[13] = kb_pad_3;

    var kb_pad_enter = this._setup_key(parent, "___pad_enter", edge_Y, edge_3, cp, dp, c, dp2, parseFloat(fs) * 0.643, n);
    kb_pad_enter.innerHTML = "Enter";
    this.pad[14] = kb_pad_enter;

    edge_Y = (this._findY(kb_pad_1) + prevH + 1) + "px";

    var kb_pad_0 = this._setup_key(parent, "___pad_0", edge_Y, edge, dp, cp, l, lh, fs, "", 7 * mag + "px");
    kb_pad_0.innerHTML = "0";
    this.pad[15] = kb_pad_0;

    var kb_pad_period = this._setup_key(parent, "___pad_period", edge_Y, edge_2, cp, cp, c, lh, fs);
    kb_pad_period.innerHTML = ".";
    this.pad[16] = kb_pad_period;

    return this.kbpM;
  },

  _set_key_state: function(key, on, textcolor, bordercolor, bgcolor)
  {
    if(key)
    {
      var ks = key.style;
      if(ks)
      {
        if(textcolor) ks.color = textcolor;
        if(bordercolor) ks.border = "1px solid " + bordercolor;
        if(bgcolor) ks.backgroundColor = bgcolor;
      }

      this._detach_event(key, 'mousedown', this._generic_callback_proc);

      if(on)
        this._setup_event(key, 'mousedown', this._generic_callback_proc);
    }
  },

  findLangNameByLangId: function(lang_id)
  {
    for(var l = 0; l < this.avail_langs.length; l++)
    {
      if(this.avail_langs[l][0] == lang_id)
      {
        return this.avail_langs[l][1];
      }
    }

    return "";
  },

  _refresh_layout: function(layout)
  {
    if(!layout) layout = this.layout_;

    var fc = this.fontcolor, kc = this.keycolor, ikc = this.ikc;
    var ibc = this.ibc, bc = this.bordercolor, lic = this.lic;

    var arr_type = this.AltGr ? (this.Shift ? "alt_gr_shift" : "alt_gr") : (this.Shift ? "shift" : (this.Caps ? "caps" : "normal"));

    var nkeys = this.keys.length;
    var proto = VKeyboard.prototype;

    var norm_arr  = proto[layout + "_normal"];
    var caps_arr  = proto[layout + "_caps"];
    var shift_arr = proto[layout + "_shift"];
    var alt_arr   = proto[layout + "_alt_gr"];

    var alt_shift_arr = proto[layout + "_alt_gr_shift"];

    var dead_arr = proto[this.DeadAction[1]] || null;

    var bcaps  = (caps_arr  && (caps_arr.length  == nkeys));
    var bshift = (shift_arr && (shift_arr.length == nkeys));
    var balt   = (alt_arr   && (alt_arr.length   == nkeys));
    var baltsh = (balt      && alt_shift_arr && (alt_shift_arr.length == nkeys));

    var caps = this.mod[2], shift = this.mod[5], shift_r = this.mod[6], alt_gr = this.mod[9];

    if(bshift)
    {
      this._set_key_state(shift, true, fc, bc, this.Shift ? lic : kc);
      this._set_key_state(shift_r, true, fc, bc, this.Shift ? lic : kc);
    }
    else
    {
      this._set_key_state(shift, false, ibc, ibc, ikc);
      this._set_key_state(shift_r, false, ibc, ibc, ikc);

      if(arr_type == "shift")
      {
        arr_type = "normal";
        this.Shift = false;
      }
    }

    if(balt)
    {
      this._set_key_state(alt_gr, true, fc, bc, this.AltGr ? lic : kc);

      if(this.AltGr)
      {
        if(baltsh)
        {
          this._set_key_state(shift, true, fc, bc);
          this._set_key_state(shift_r, true, fc, bc);
        }
        else
        {
          this._set_key_state(shift, false, ibc, ibc, ikc);
          this._set_key_state(shift_r, false, ibc, ibc, ikc);

          arr_type = "alt_gr";
          this.Shift = false;
        }
      }
    }
    else
    {
      this._set_key_state(alt_gr, false, ibc, ibc, ikc);

      if(arr_type == "alt_gr")
      {
        arr_type = "normal";
        this.AltGr = false;
      }
      else if(arr_type == "alt_gr_shift")
      {
        arr_type = "normal";
        this.AltGr = false, this.Shift = false;

        shift.style.backgroundColor = kc, shift_r.style.backgroundColor = kc;
      }
    }

    if(this.Shift && !baltsh)
      this._set_key_state(alt_gr, false, ibc, ibc, ikc);

    if(bcaps && !this.AltGr)
      this._set_key_state(caps, true, fc, bc, this.Caps ? lic : kc);
    else
    {
      this._set_key_state(caps, false, ibc, ibc, ikc);

      this.Caps = false;
      if(arr_type == "caps") arr_type = "normal";
    }

    var arr_cur = proto[layout + "_" + arr_type];

    var i = nkeys;
    while(--i >= 0)
    {
      var key = this.keys[i], key_val = arr_cur[i]; if(!key_val) key_val = "";

      if(this.Shift && this.Caps)
      {
        var key_nrm = norm_arr[i], key_cps = caps_arr[i], key_shf = shift_arr[i];

        if((key_cps == key_shf) && (key_nrm != key_cps)) key_val = key_nrm;
      }

      if(typeof(key_val) == "object")
      {
        key.innerHTML = key_val[0], key.dead = key_val[1];

        this._set_key_state(key, true, this.deadcolor, bc, (this.DeadAction[0] == key_val[0] ? lic : kc));
      }
      else
      {
        key.dead = null;

        var block = false;

        if(key_val != "")
        {
          if(dead_arr)
          {
            for(var j = 0, l = dead_arr.length; j < l; j++) { var dk = dead_arr[j]; if(dk[0] == key_val) { key_val = dk[1]; break;}};

            if(j == l) block = true;
          }

          key.innerHTML = key_val;

          if(block)
            this._set_key_state(key, false, ibc, ibc, ikc);
          else
            this._set_key_state(key, true, fc, bc, kc);
        }
        else
        {
          key.innerHTML = "&nbsp;";
          this._set_key_state(key, false, ibc, ibc, ikc);
        }
      }
    }

    i = this.mod.length;
    while(--i >= 0)
    {
      var key = this.mod[i];

      switch(i)
      {
        case 2: case 5: case 6: case 9:
          break;

        case 7:
          key.innerHTML = this.findLangNameByLangId(layout);

          this._detach_event(key, 'mousedown', this._handle_lang_menu);

          if(this.DeadAction[1])
            this._set_key_state(key, false, ibc, ibc, ikc);
          else
          {
            var many = (this.avail_langs.length > 1);

            this._set_key_state(key, false, fc, many ? this.lsc : ibc, many ? kc : ikc);
            if(many)
              this._setup_event(key, 'mousedown', this._handle_lang_menu);
          }
          break;

        case 8:
          key.innerHTML = this.DeadAction[1] ? this.DeadAction[0] : "&nbsp;";

        default:
          if((this.DeadAction[1] && (i != 8)) || ((i == 10) || (i == 11)))
            this._set_key_state(key, false, ibc, ibc, ikc);
          else
            this._set_key_state(key, true, fc, bc, kc);

          var ks = key.style;
          switch(i)
          {
            case 4: ks.borderBottomColor = kc; break;

            case 10: case 11: ks.borderColor = ibc; break;
          }
      }
    }

    i = this.pad.length;
    while(--i >= 0)
    {
      key = this.pad[i];

      if(this.DeadAction[1])
        this._set_key_state(key, false, ibc, ibc, ikc);
      else
        this._set_key_state(key, true, fc, bc, kc);
    }
  },

  _handle_lang_menu: function(event)
  {
    var pr = VKeyboard.prototype;

    var in_el = pr._get_event_source(event);
    var container_id = in_el.id.substring(0, in_el.id.indexOf("___"));
    var vkboard = pr.kbArray[container_id];

    var ct = vkboard.Cntr, menu = vkboard.menu;

    if(menu)
    { ct.removeChild(menu); vkboard.menu = null; }
    else
    {
      var fs = vkboard.fontsize, kc = vkboard.keycolor, bc = "1px solid " + vkboard.bordercolor;

      var pad = vkboard.pad.length, per_row = pad ? 5 : 4, item_wd = pad ? 108 : 103;
      var num_rows = Math.ceil(pr.avail_langs.length / per_row);

      var mag = parseFloat(fs) / 14.0, cell = Math.floor(25.0 * mag), cp = cell + "px", lh = (cell - 2) + "px", w = item_wd * mag;
      var h1 = Math.floor(cell + mag), h2 = String(w - mag) + "px", pad = String(4 * mag) + "px", wd = String(w * per_row + 1) + "px";

      var langs = pr.avail_langs.length;

      menu = document.createElement("DIV"); var ms = menu.style;
      ms.display  = "block";
      ms.position = "relative";

      ms.top = "1px", ms.left = "0px";
      ms.width = wd;
      ms.border = bc;
      ms.backgroundColor = vkboard.bgcolor;

      vkboard.menu = ct.appendChild(menu);

      var menu_main = document.createElement("DIV"); ms = menu_main.style;
      ms.fontFamily = vkboard.fontname;
      ms.position   = "relative";

      ms.color  = vkboard.fontcolor;
      ms.width  = wd;
      ms.height = String(num_rows * h1 + 1) + "px";
      ms.cursor = "default";

      menu.appendChild(menu_main);

      function setcolor(obj, c) { return function() { obj.style.backgroundColor = c; } };

      for(var j = 0; j < langs; j++)
      {
        var item = vkboard._setup_key(menu_main, "___lang_" + String(j), String(h1 * Math.floor(j / per_row) + 1) + "px", String((j % per_row) * w + 1) + "px", h2, cp, "center", lh, fs, "normal", pad);
        item.style.backgroundColor = kc;
        item.style.border = bc;
        item.innerHTML = pr.avail_langs[j][1];

        vkboard._setup_event(item, 'mousedown', vkboard._handle_lang_item);
        vkboard._setup_event(item, 'mouseover', setcolor(item, vkboard.lic));
        vkboard._setup_event(item, 'mouseout',  setcolor(item, kc));
      }
    }
  },

  _handle_lang_item: function(event)
  {
    var pr = VKeyboard.prototype;

    var in_el = pr._get_event_source(event);
    var container_id = in_el.id.substring(0, in_el.id.indexOf("___"));
    var vkboard = pr.kbArray[container_id];

    var ndx = in_el.id.indexOf("___lang_");
    var lng = in_el.id.substring(ndx + 8, in_el.id.length);
    var newl = pr.avail_langs[lng][0];

    if(vkboard.layout_ != newl)
      vkboard._refresh_layout(vkboard.layout_ = newl);

    vkboard.Cntr.removeChild(vkboard.menu);
    vkboard.menu = null;
  },

  _generic_callback_proc: function(event)
  {
    var pr = VKeyboard.prototype;

    var in_el = pr._get_event_source(event);
    var container_id = in_el.id.substring(0, in_el.id.indexOf("___"));
    var vkboard = pr.kbArray[container_id];

    var val = in_el.subst || in_el.innerHTML;
    if(!val) return;

    switch(val)
    {
      case "Caps": case "Shift": case "AltGr":

        vkboard[val] = !vkboard[val];
        vkboard._refresh_layout();

        if(vkboard.sc) vkboard._start_flash(in_el);
        return;

      case "Tab":    val = "\t"; break;
      case "&nbsp;": val = " ";  break;
      case "&quot;": val = "\""; break;
      case "&lt;":   val = "<";  break;
      case "&gt;":   val = ">";  break;
      case "&amp;":  val = "&";  break;
    }

    if(vkboard.sc) vkboard._start_flash(in_el);

    if(in_el.dead)
    {
      if(in_el.dead == vkboard.DeadAction[1])
      { val = ""; vkboard.DeadAction[0] = vkboard.DeadAction[1] = null; }
      else
      { vkboard.DeadAction[0] = val; vkboard.DeadAction[1] = in_el.dead; }

      vkboard._refresh_layout();
      return;
    }
    else
    { var r;
      if(vkboard.DeadAction[1]) { vkboard.DeadAction[0] = vkboard.DeadAction[1] = null; r = true; }

      if(vkboard.AltGr || vkboard.Shift || r)
      {
        vkboard.AltGr = false; vkboard.Shift = false;
        vkboard._refresh_layout();
      }
    }

    if(vkboard._Callback) vkboard._Callback(val, vkboard.Cntr.id);
  },

  SetParameters: function()
  {
    var l = arguments.length;
    if(!l || (l % 2 != 0)) return false;

    var p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16;

    while(--l > 0)
    {
      var value = arguments[l];

      switch(arguments[l - 1])
      {
        case "callback":
          p0 = ((typeof(value) == "function") && ((value.length == 1) || (value.length == 2))) ? value : this._Callback;
          break;

        case "font-name":  p1 = value; break;
        case "font-size":  p2 = value; break;
        case "font-color": p3 = value; break;
        case "dead-color": p4 = value; break;
        case "base-color": p5 = value; break;
        case "key-color":  p6 = value; break;

        case "selection-color": p7 = value; break;
        case "border-color":    p8 = value; break;

        case "inactive-border-color": p9  = value; break;
        case "inactive-key-color":    p10 = value; break;
        case "lang-cell-color":       p11 = value; break;

        case "show-click": p12 = value; break;

        case "click-font-color":   p13  = value; break;
        case "click-key-color":    p14 = value; break;
        case "click-border-color": p15 = value; break;
				case "langYoann": p16 = value; break; //Yoann
        default: break;
      }

      l -= 1;
    }

    this._construct(this.Cntr.id, p0, (this.pad.length != 0), p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16);

    return true;
  },

  Show: function(value)
  {
    var ct = this.Cntr.style;

    ct.display = ((value == undefined) || (value == true)) ? "block" : ((value == false) ? "none" : ct.display);
  },

  ShowNumpad: function(value)
  {
    var sh = ((value == undefined) || (value == true)) ? "block" : ((value == false) ? "none" : null);
    if(!sh) return;

    var kb = this.Cntr.childNodes[0];

    var i = this.pad.length;
    if(i)
    {
      while(--i >= 0)
        this.pad[i].parentNode.style.display = sh;

      kb.style.width = kb.childNodes[0].style.width = (sh == "none") ? (this.kbpH + 1) + "px" : this.kbpM + "px";
    }
    else
    {
      if(sh == "block")
      {
        kb.style.width = kb.childNodes[0].style.width = this._create_numpad(this.Cntr.id, kb.childNodes[0]);
        this._refresh_layout();
      }
    }
  },

  // Layout info:

//["Ac","Accents"],

  avail_langs: [["Ac", "Select Layout"],
["Ac","Accents"],
/*["Ca", "Canadian"],*/
/*["No", "Norsk"],*/
/*["Pl", "Polski"],*/
["Bg", "&#x0411;&#x044A;&#x043B;&#x0433;&#x0430;&#x0440;&#x0441;&#x043A;&#x0438;"],
/*["Cz", "&#x010C;esky"],*/
["Cz", "&#x010C;esky"],
["Da", "Dansk"],
/*["Da", "Dansk"],*/
["De", "Deutsch"],
["El", "&#x0388;&#x03BB;&#x03BB;&#x03B7;&#x03BD;&#x03B1;&#x03C2;"],
["Es", "Espa&#x00F1;ol"],
["Et", "Eesti"],
["Fi", "Suomi"],
["Fr", "Fran&#x00E7;ais"],
["He", "&#x05E2;&#x05D1;&#x05E8;&#x05D9;&#x05EA;"],
["Hr", "Hrvatski"],
["Sl", "Sloven&#353;&#269;ina"],
["Hu", "Magyar"],
["It", "Italiano"],
["Is", "&#205;slenska"],
["Lt", "Lietuvi&#x0173;"],
["Lv", "Latvie&#x0161;u"],
["Mk", "&#x041C;&#x0430;&#x043A;&#x0435;&#x0434;&#x043E;&#x043D;&#x0441;&#x043A;&#x0438;"],
["No", "Norsk"],
["Pl", "Polski"],
["Ro", "Rom&#x00E2;n&#x0103;"],
["Ru", "&#x0420;&#x0443;&#x0441;&#x0441;&#x043A;&#x0438;&#x0439;"],
["Sh", "Srpskohrvatski"],
["Sk", "Sloven&#x010D;ina"],
["Sr", "&#x0421;&#x0440;&#x043F;&#x0441;&#x043A;&#x0438;"],
["Sv", "Svenska"],
["Uk", "&#x0423;&#x043A;&#x0440;&#x0430;&#x0457;&#x043D;&#x0441;&#x044C;&#x043A;&#x0430;"],
["Us", "English (US)"]],

  // Us International:

  Us_normal: [["&#x0060;", "Grave"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#x005B;", "&#x005D;", "&#x005C;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x003B;", "&#x0027;",,
              "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x002F;"],

  Us_caps: [["&#x0060;", "Grave"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x005B;", "&#x005D;", "&#x005C;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x003B;", "&#x0027;",,
            "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x002F;"],

  Us_shift: [["&#x007E;", "Tilde"], "&#x0021;", "&#x0040;", "&#x0023;", "&#x0024;", "&#x0025;", ["&#x005E;", "Circumflex"], "&#x0026;", "&#x002A;", "&#x0028;", "&#x0029;", "&#x005F;", "&#x002B;",
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x007B;", "&#x007D;", "&#x007C;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x003A;", "&#x0022;",,
             "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003C;", "&#x003E;", "&#x003F;"],

  Us_alt_gr: [, "&#x00A1;", "&#x00B2;", "&#x00B3;", "&#x00A4;", "&#x20AC;", "&#x00BC;", "&#x00BD;", "&#x00BE;", "&#x0091;", "&#x0092;", "&#x00A5;", "&#x00D7;",
              "&#x00E4;", "&#x00E5;", "&#x00E9;", "&#x00AE;", "&#x00FE;", "&#x00FC;", "&#x00FA;", "&#x00ED;", "&#x00F3;", "&#x00F6;", "&#x00AB;", "&#x00BB;",
              "&#x00AC;", "&#x00E1;", "&#x00DF;", "&#x0111;",,,,,, "&#x00F8;", "&#x00B6;", ["&#x00B4;", "Acute"],, "&#x00E6;",, "&#x00A9;",,,
              "&#x00F1;", "&#x00B5;", "&#x00E7;",, "&#x00BF;"],

  Us_alt_gr_shift: [, "&#x00B9;",,, "&#x00A3;",,,,,,,, "&#x00F7;", "&#x00C4;", "&#x00C5;", "&#x00C9;",, "&#x00DE;", "&#x00DC;",
                    "&#x00DA;", "&#x00CD;", "&#x00D3;", "&#x00D6;",,, "&#x00A6;", "&#x00C1;", "&#x00A7;", "&#x0110;",,,,,, "&#x00D8;",
                    "&#x00B0;", ["&#x00A8;", "Umlaut"],, "&#x00C6;",, "&#x00A2;",,, "&#x00D1;",, "&#x00C7;",,""],

  // Canadian (multilingual standard):

  Ca_normal: ["&#x002F;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", ["&#x005E;", "Circumflex"], "&#x00E7;", "&#x00F9;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x003B;", "&#x00E8;", "&#x00E0;",
              "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x00E9;"],

  Ca_caps: ["&#x002F;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", ["&#x005E;", "Circumflex"], "&#x00C7;", "&#x00D9;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x003B;", "&#x00C8;", "&#x00C0;",
            "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x00C9;"],

  Ca_shift: ["&#x005C;", "&#x0021;", "&#x0040;", "&#x0023;", "&#x0024;", "&#x0025;", "&#x003F;", "&#x0026;", "&#x002A;", "&#x0028;", "&#x0029;", "&#x005F;", "&#x002B;",
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", ["&#x00A8;", "Umlaut"], "&#x00C7;", "&#x00D9;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x003A;", "&#x00C8;", "&#x00C0;",
             "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x0027;", "&#x0022;", "&#x00C9;"],

  Ca_alt_gr: ["&#x007C;",,,,,,, "&#x007B;", "&#x007D;", "&#x005B;", "&#x005D;",, "&#x00AC;",,,,,,,,,,, ["&#x0060;", "Grave"],
             ["&#x007E;", "Tilde"],,,,,,,,,,, "&#x00B0;",,, "&#x00AB;", "&#x00BB;",,,,,, "&#x003C;", "&#x003E;",""],

  // Russian:

  Ru_normal: ["&#x0451;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
              "&#x0439;", "&#x0446;", "&#x0443;", "&#x043A;", "&#x0435;", "&#x043D;", "&#x0433;", "&#x0448;", "&#x0449;", "&#x0437;", "&#x0445;", "&#x044A;", "&#x005C;",
              "&#x0444;", "&#x044B;", "&#x0432;", "&#x0430;", "&#x043F;", "&#x0440;", "&#x043E;", "&#x043B;", "&#x0434;", "&#x0436;", "&#x044D;",,
              "&#x044F;", "&#x0447;", "&#x0441;", "&#x043C;", "&#x0438;", "&#x0442;", "&#x044C;", "&#x0431;", "&#x044E;", "&#x002E;"],

  Ru_caps: ["&#x0401;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
            "&#x0419;", "&#x0426;", "&#x0423;", "&#x041A;", "&#x0415;", "&#x041D;", "&#x0413;", "&#x0428;", "&#x0429;", "&#x0417;", "&#x0425;", "&#x042A;", "&#x005C;",
            "&#x0424;", "&#x042B;", "&#x0412;", "&#x0410;", "&#x041F;", "&#x0420;", "&#x041E;", "&#x041B;", "&#x0414;", "&#x0416;", "&#x042D;",,
            "&#x042F;", "&#x0427;", "&#x0421;", "&#x041C;", "&#x0418;", "&#x0422;", "&#x042C;", "&#x0411;", "&#x042E;", "&#x002E;"],

  Ru_shift: ["&#x0401;", "&#x0021;", "&#x0022;", "&#x2116;", "&#x003B;", "&#x0025;", "&#x003A;", "&#x003F;", "&#x002A;", "&#x0028;", "&#x0029;", "&#x005F;", "&#x002B;",
             "&#x0419;", "&#x0426;", "&#x0423;", "&#x041A;", "&#x0415;", "&#x041D;", "&#x0413;", "&#x0428;", "&#x0429;", "&#x0417;", "&#x0425;", "&#x042A;", "&#x002F;",
             "&#x0424;", "&#x042B;", "&#x0412;", "&#x0410;", "&#x041F;", "&#x0420;", "&#x041E;", "&#x041B;", "&#x0414;", "&#x0416;", "&#x042D;",,
             "&#x042F;", "&#x0427;", "&#x0421;", "&#x041C;", "&#x0418;", "&#x0422;", "&#x042C;", "&#x0411;", "&#x042E;", "&#x002C;"],

  // German:

  De_normal: [["&#x005E;", "Circumflex"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x00DF;", ["&#x00B4;", "Acute"],
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x007A;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#x00FC;", "&#x002B;", "&#x003C;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x00F6;", "&#x00E4;", "&#x0023;",
              "&#x0079;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  De_caps: [["&#x005E;", "Circumflex"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x00DF;", ["&#x00B4;", "Acute"],
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x005A;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x00DC;", "&#x002B;", "&#x003C;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x00D6;", "&#x00C4;", "&#x0023;",
            "&#x0059;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  De_shift: ["&#x00BA;", "&#x0021;", "&#x0022;", "&#x00A7;", "&#x0024;", "&#x0025;", "&#x0026;", "&#x002F;", "&#x0028;", "&#x0029;", "&#x003D;", "&#x003F;", ["&#x0060;", "Grave"],
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x005A;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x00DC;", "&#x002A;", "&#x003E;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x00D6;", "&#x00C4;", "&#x0027;",
             "&#x0059;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003B;", "&#x003A;", "&#x005F;"],

  De_alt_gr: [,, "&#x00B2;", "&#x00B3;",,,, "&#x007B;", "&#x005B;", "&#x005D;", "&#x007D;", "&#x005C;",, "&#x0040;",, "&#x20AC;",,,,,,
              ,,, ["&#x007E;", "Tilde"], "&#x007C;",,,,,,,,,,,,,,,,,,, "&#x00B5;",,,""],

  // French:

  Fr_normal: ["&#x00B2;", "&#x0026;", "&#x00E9;", "&#x0022;", "&#x0027;", "&#x0028;", "&#x007C;", "&#x00E8;", "&#x005F;", "&#x00E7;", "&#x00E0;", "&#x0029;", "&#x003D;",
              "&#x0061;", "&#x007A;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", ["&#x005E;", "Circumflex"], "&#x0024;", "&#x003C;",
              "&#x0071;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x006D;", "&#x00F9;", "&#x002A;",
              "&#x0077;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x002C;", "&#x003B;", "&#x003A;", "&#x0021;"],

  Fr_caps: ["&#x00B2;", "&#x0026;", "&#x00C9;", "&#x0022;", "&#x0027;", "&#x0028;", "&#x007C;", "&#x00C8;", "&#x005F;", "&#x00C7;", "&#x00C0;", "&#x0029;", "&#x003D;",
            "&#x0041;", "&#x005A;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", ["&#x005E;", "Circumflex"], "&#x0024;", "&#x003C;",
            "&#x0051;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x004D;", "&#x00D9;", "&#x002A;",
            "&#x0057;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x002C;", "&#x003B;", "&#x003A;", "&#x0021;"],

  Fr_shift: [, "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x00BA;", "&#x002B;",
             "&#x0041;", "&#x005A;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", ["&#x00A8;", "Umlaut"], "&#x00A3;", "&#x003E;",
             "&#x0051;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x004D;", "&#x0025;", "&#x00B5;",
             "&#x0057;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x003F;", "&#x005F;", "&#x002F;", "&#x00A7;"],

  Fr_alt_gr: [,,, "&#x0023;", "&#x007B;", "&#x005B;", "&#x007C;",, "&#x005C;", "&#x005E;", "&#x0040;", "&#x005D;", "&#x007D;",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,""],

  // Spanish:

  Es_normal: ["&#x00BA;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x0092;", "&#x00A1;",
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", ["&#x0060;", "Grave"], "&#x002B;", "&#x003C;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x00F1;", ["&#x00B4;", "Acute"], "&#x00E7;",
              "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  Es_caps: ["&#x00BA;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x0092;", "&#x00A1;",
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", ["&#x0060;", "Grave"], "&#x002B;", "&#x003C;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x00D1;", ["&#x00B4;", "Acute"], "&#x00C7;",
            "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  Es_shift: ["&#x00AA;", "&#x0021;", "&#x0022;", "&#x0027;", "&#x0024;", "&#x0025;", "&#x0026;", "&#x002F;", "&#x0028;", "&#x0029;", "&#x003D;", "&#x003F;", "&#x00BF;",
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", ["&#x005E;", "Circumflex"], "&#x002A;", "&#x003E;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x00D1;", ["&#x00A8;", "Umlaut"], "&#x00C7;",
             "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003B;", "&#x003A;", "&#x005F;"],

  Es_alt_gr: ["&#x005C;", "&#x007C;", "&#x0040;", "&#x0023;",,, "&#x00AC;",,,,,,,,,,,,,,,,, "&#x005B;","&#x005D;",,,,,,,,,,,, "&#x007B;", "&#x007D;",,,,,,,,,,""],

  // Italian:

  It_normal: ["&#x005C;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x0092;", "&#x00EC;",
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#x00E8;", "&#x002B;", "&#x003C;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x00F2;", "&#x00E0;", "&#x00F9;",
              "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  It_caps: ["&#x005C;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x0092;", "&#x00EC;",
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x00C8;", "&#x002B;", "&#x003C;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x00D2;", "&#x00C0;", "&#x00D9;",
            "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  It_shift: ["&#x007C;", "&#x0021;", "&#x0022;", "&#x00A3;", "&#x0024;", "&#x0025;", "&#x0026;", "&#x002F;", "&#x0028;", "&#x0029;", "&#x003D;", "&#x003F;", "&#x005E;",
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x00E9;", "&#x002A;", "&#x003C;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x00E7;", "&#x00B0;", "&#x00A7;",
             "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003B;", "&#x003A;", "&#x005F;"],

  It_alt_gr: [,,,,,,,,,,,,,,,,,,,,,,"&#x005B;","&#x005D;",,,,,,,,,,,"&#x0040;","&#x0023;",,,,,,,,,,,,""],

  It_alt_gr_shift: [,,,,,,,,,,,,,,,,,,,,,,"&#x007B;","&#x007D;",,,,,,,,,,,,,,,,,,,,,,,,""],


	// Icelandic:

	Is_normal: [["&#176;","RingAbove"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#246;", "&#x002D;",
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#240;", "&#x0027;","&#60;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#230;", ["&#x0027;","Acute"],"&#43;",
              "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#254;"],

	Is_shift: [["&#168;", "Umlaut"], "&#33;", "&#34;", "&#35;", "&#36;", "&#37;", "&#38;", "&#47;", "&#40;", "&#41;", "&#61;", "&#214;", "&#95;",
              "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#208;", "&#63;","&#62;",
              "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#198;", "&#39;","&#42;",
              "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#59;", "&#58;", "&#222;"],

	Is_caps: [["&#176;","RingAbove"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#214;", "&#x002D;",
              "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#208;", "&#x0027;","&#60;",
              "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#198;", ["&#x0027;","Acute"],"&#43;",
              "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#222;"],

	Is_alt_gr: ["&#176;",,,,, "&#x20AC;",, "&#123;", "&#91;", "&#93;", "&#125;", "&#92;",,
              "&#64;",, "&#x20AC;",,,,,,,,,,"&#126;",
              ,,,,,,,,,, ["&#94;","Circumflex"], ["&#96;", "Grave"],
              "&#124;",,,,,,, "&#956;",,""],


  // Czech:

  Cz_normal: ["&#x003B;", "&#x002B;", "&#x011B;", "&#x0161;", "&#x010D;", "&#x0159;", "&#x017E;", "&#x00FD;", "&#x00E1;", "&#x00ED;", "&#x00E9;", "&#x003D;", ["&#x00B4;", "Acute"],
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x007A;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#x00FA;", "&#x0029;", "&#x0026;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x016F;", "&#x00A7;", ["&#x00A8;", "Umlaut"],
              "&#x0079;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  Cz_caps: ["&#x003B;", "&#x002B;", "&#x011A;", "&#x0160;", "&#x010C;", "&#x0158;", "&#x017D;", "&#x00DD;", "&#x00C1;", "&#x00CD;", "&#x00C9;", "&#x003D;", "&#x02CA;",
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x005A;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x00DA;", "&#x0029;", "&#x0026;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x016E;", "&#x00A7;", ["&#x00A8;", "Umlaut"],
            "&#x0059;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  Cz_shift: [["&#x00BA;", "RingAbove"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x0025;", ["&#x02C7;", "Caron"],
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x005A;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x002F;", "&#x0028;", "&#x002A;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x0022;", "&#x0021;", "&#x0027;",
             "&#x0059;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003F;", "&#x003A;", "&#x005F;"],

  Cz_alt_gr: [,["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],
              ["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],["&#x00B4;", "Acute"],["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],
              "&#x005C;","&#x007C;","&#x20AC;",,,,,,,,"&#x00F7;", "&#x00D7;", "&#x003C;",,"&#x0111;","&#x00D0;","&#x005B;","&#x005D;",,,"&#x0142;","&#x0141;","&#x0024;", "&#x00DF;", "&#x00A4;", "&#x003E;",
              "&#x0023;",,"&#x0040;","&#x007B;","&#x007D;",,,,""],

  // Greek:

  El_normal: ["&#x00BD;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
              "&#x003B;", "&#x03C2;", "&#x03B5;", "&#x03C1;", "&#x03C4;", "&#x03C5;", "&#x03B8;", "&#x03B9;", "&#x03BF;", "&#x03C0;", "&#x005B;", "&#x005D;", "&#x00A7;",
              "&#x03B1;", "&#x03C3;", "&#x03B4;", "&#x03C6;", "&#x03B3;", "&#x03B7;", "&#x03BE;", "&#x03BA;", "&#x03BB;", ["&#x00B4;", "Acute"], "&#x0092;", "&#x005C;",
              "&#x03B6;", "&#x03C7;", "&#x03C8;", "&#x03C9;", "&#x03B2;", "&#x03BD;", "&#x03BC;", "&#x002C;", "&#x002E;", "&#x002F;"],

  El_caps: ["&#x00BD;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
            "&#x003B;", "&#x03C2;", "&#x0395;", "&#x03A1;", "&#x03A4;", "&#x03A5;", "&#x0398;", "&#x0399;", "&#x039F;", "&#x03A0;", "&#x005B;", "&#x005D;", "&#x00A7;",
            "&#x0391;", "&#x03A3;", "&#x0394;", "&#x03A6;", "&#x0393;", "&#x0397;", "&#x039E;", "&#x039A;", "&#x039B;", ["&#x00B4;", "Acute"], "&#x0092;", "&#x005C;",
            "&#x0396;", "&#x03A7;", "&#x03A8;", "&#x03A9;", "&#x0392;", "&#x039D;", "&#x039C;", "&#x002C;", "&#x002E;", "&#x002F;"],

  El_shift: ["&#x00B1;", "&#x0021;", "&#x0040;", "&#x0023;", "&#x0024;", "&#x0025;", "&#x005E;", "&#x0026;", "&#x002A;", "&#x0028;", "&#x0029;", "&#x005F;", "&#x002B;",
             "&#x003A;", "&#x00A6;", "&#x0395;", "&#x03A1;", "&#x03A4;", "&#x03A5;", "&#x0398;", "&#x0399;", "&#x039F;", "&#x03A0;", "&#x007B;", "&#x007D;", "&#x00A9;",
             "&#x0391;", "&#x03A3;", "&#x0394;", "&#x03A6;", "&#x0393;", "&#x0397;", "&#x039E;", "&#x039A;", "&#x039B;", ["&#x00A8;", "Umlaut"], "&#x0091;", "&#x007C;",
             "&#x0396;", "&#x03A7;", "&#x03A8;", "&#x03A9;", "&#x0392;", "&#x039D;", "&#x039C;", "&#x003C;", "&#x003E;", "&#x003F;"],

  El_alt_gr: [,,"&#x00B2;", "&#x00B3;", "&#x00A3;", "&#x00A7;", "&#x00B6;",, "&#x00A4;", "&#x00A6;", "&#x00B0;", "&#x00B1;", "&#x00BD;",,,,,,
              ,,,,,"&#x00AB;", "&#x00BB;",,,,,,,,,,,["&#x0385;", "DialytikaTonos"],, "&#x00AC;",,,,,,,,,,""],

  // Hebrew:

  He_normal: ["&#x003B;", "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
              "&#x002F;", "&#x0027;", "&#x05E7;", "&#x05E8;", "&#x05D0;", "&#x05D8;", "&#x05D5;", "&#x05DF;", "&#x05DD;", "&#x05E4;", "&#x005B;", "&#x005D;", "&#x005C;",
              "&#x05E9;", "&#x05D3;", "&#x05D2;", "&#x05DB;", "&#x05E2;", "&#x05D9;", "&#x05D7;", "&#x05DC;", "&#x05DA;", "&#x05E3;", "&#x002C;",,
              "&#x05D6;", "&#x05E1;", "&#x05D1;", "&#x05D4;", "&#x05E0;", "&#x05DE;", "&#x05E6;", "&#x05EA;", "&#x05E5;", "&#x002E;"],

  He_shift: ["&#x007E;", "&#x0021;", "&#x0040;", "&#x0023;", "&#x0024;", "&#x0025;", "&#x005E;", "&#x0026;", "&#x002A;", "&#x0028;", "&#x0029;", "&#x005F;", "&#x002B;",
             "&#x002F;", "&#x0027;", "&#x05E7;", "&#x05E8;", "&#x05D0;", "&#x05D8;", "&#x05D5;", "&#x05DF;", "&#x05DD;", "&#x05E4;", "&#x007B;", "&#x007D;", "&#x007C;",
             "&#x05E9;", "&#x05D3;", "&#x05D2;", "&#x05DB;", "&#x05E2;", "&#x05D9;", "&#x05D7;", "&#x05DC;", "&#x05DA;", "&#x003A;", "&#x0022;",,
             "&#x05D6;", "&#x05E1;", "&#x05D1;", "&#x05D4;", "&#x05E0;", "&#x05DE;", "&#x05E6;", "&#x003C;", "&#x003E;", "&#x003F;"],

  He_alt_gr: [,,,,"&#x20AA;",,,,,,,"&#x05BE;",,,,,,,,"&#x05F0;",,,,,,,,,,,,"&#x05F2;","&#x05F1;",,,,,,,,,,,,,,,""],

	// Croatian and Slovenian (The SAME !!):
	Hr_normal: [["&#x00B8;", "Cedilla"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x007A;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x0161;","&#x0111;","&#x003C;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x010D;","&#x0107;","&#x017E;",
              "&#x0079;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Hr_caps: [["&#x00B8;", "Cedilla"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0160;","&#x00D0;","&#x003C;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x010C;","&#x0106;","&#x017D;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Hr_shift: [["&#x00A8;", "Umlaut"] ,"&#x0021;","&#x0022;","&#x0023;","&#x0024;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;","&#x002A;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0160;","&#x00D0;","&#x003E;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x010C;","&#x0106;","&#x017D;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

  Hr_alt_gr: [,["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],
              ["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],["&#x00B4;", "Acute"],["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],
              "&#x005C;","&#x007C;","&#x20AC;",,,,,,,,"&#x00F7;","&#x00D7;",,,,,"&#x005B;","&#x005D;",,,"&#x0142;","&#x0141;",,"&#x00DF;","&#x00A4;",,,,"&#x0040;","&#x007B;","&#x007D;","&#x00A7;",,,""],

	Sl_normal: ["&#184;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#124;","&#43;",
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x007A;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#353;","&#240;","&#60;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#108;","&#269;","&#263;","&#382;",
              "&#x0079;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

	Sl_caps: ["&#184;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#124;","&#43;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#352;","&#208;","&#62;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#268;","&#262;","&#381;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#59;","&#x003A;","&#x005F;"],

	Sl_shift: [["&#168;","Umlaut"],"&#x0021;","&#x0022;","&#35;","&#36;","&#x0025;","&#38;","&#x002F;","&#x0028;","&#x0029;","&#61;","&#63;","&#42;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#352;","&#208;","&#62;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#268;","&#262;","&#381;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#59;","&#x003A;","&#x005F;"],

	Sl_alt_gr: [,["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],["&#x00B4;", "Acute"],["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],"&#x005C;","&#x007C;",,,,,"",,,,"&#x00F7;","&#x00D7;","",
,"","","&#x005B;","&#x005D;",,,"&#x0142;","&#x0141;","","&#x00DF;","&#x00A4;",
"","","","&#x0040;","&#x007B;","&#x007D;","&#167;",,,""],


// Accents

 Ac_normal: [["&#x00A8;","Umlaut"], ["&#x0060;", "Grave"], ["&#x00B4;","Acute"], ["&#x5e;","Circumflex"], ["&#x7e;","Tilde"], ["&#xaf;","Macron"], ["&#x2d8;","Breve"],  ["&#x2c7;","Caron"], ["&#34;","Humlaut"], ["&#x2da;","RingAbove"], ["&#xb8;","Cedilla"], ["&#x2db;","Ogonek"], "&#x003D;",
                          "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#x005B;", "&#x005D;", "&#x00DF;",
                          "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x003B;", "&#x0153;", "&#x00F8;",
                          "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x0111;", "&#x00E6;"],

Ac_shift: [["&#x00A8;","Umlaut"], ["&#x0060;", "Grave"], ["&#x00B4;","Acute"], ["&#x0302;","Circumflex"], ["&#x0303;","Tilde"], ["&#x0304;","Macron"], ["&#x0306;","Breve"],  ["&#x030C;","Caron"], ["&#x030B;","Humlaut"], ["&#x030A;","RingAbove"], ["&#x0327;","Cedilla"], ["&#x0328;","Ogonek"], "&#x002B;",
                         "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x007B;", "&#x007D;", "&#x007C;",
                         "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x003A;", "&#x0152;", "&#x00D8;",
                         "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003C;", "&#x0110;", "&#x00C6;"],

// extra layouts


  Bg_normal: [["&#x0060;", "Grave"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002D;","&#x002E;",
              "&#x002C;","&#x0443;","&#x0435;","&#x0438;","&#x0448;","&#x0449;","&#x043A;","&#x0441;","&#x0434;","&#x0437;","&#x0446;","&#x003B;","&#x0028;",
              "&#x044C;","&#x044F;","&#x0430;","&#x043E;","&#x0436;","&#x0433;","&#x0442;","&#x043D;","&#x0432;","&#x043C;","&#x0447;",,
              "&#x044E;","&#x0439;","&#x044A;","&#x044D;","&#x0444;","&#x0445;","&#x043F;","&#x0440;","&#x043B;","&#x0431;"],

    Bg_caps: [["&#x0060;", "Grave"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002D;","&#x002E;",
              "&#x002C;","&#x0423;","&#x0415;","&#x0418;","&#x0428;","&#x0429;","&#x041A;","&#x0421;","&#x0414;","&#x0417;","&#x0426;","&#x003B;","&#x0028;",
              "&#x042C;","&#x042F;","&#x0410;","&#x041E;","&#x0416;","&#x0413;","&#x0422;","&#x041D;","&#x0412;","&#x041C;","&#x0427;",,
              "&#x042E;","&#x0419;","&#x042A;","&#x042D;","&#x0424;","&#x0425;","&#x041F;","&#x0420;","&#x041B;","&#x0411;"],

   Bg_shift: [["&#x007E;", "Tilde"],"&#x0021;","&#x003F;","&#x002B;","&#x0022;","&#x0025;","&#x003D;","&#x003A;","&#x002F;","&#x005F;","&#x2116;","&#x0049;","&#x0056;",
              "&#x044B;","&#x0423;","&#x0415;","&#x0418;","&#x0428;","&#x0429;","&#x041A;","&#x0421;","&#x0414;","&#x0417;","&#x0426;","&#x00A7;","&#x0029;",
              "&#x042C;","&#x042F;","&#x0410;","&#x041E;","&#x0416;","&#x0413;","&#x0422;","&#x041D;","&#x0412;","&#x041C;","&#x0427;",,
              "&#x042E;","&#x0419;","&#x042A;","&#x042D;","&#x0424;","&#x0425;","&#x041F;","&#x0420;","&#x041B;","&#x0411;"],

  Bg_alt_gr: [,,,,,,,,,,,,,,,"&#x20AC;",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,""],

// Czech (alternative, taken from http://www.bohemica.com/czechonline/czechkeyboard/index.htm):
/*
  Cz_normal: ["&#x003B;", "&#x002B;", "&#x011B;", "&#x0161;", "&#x010D;", "&#x0159;", "&#x017E;", "&#x00FD;", "&#x00E1;", "&#x00ED;", "&#x00E9;", "&#x003D;", ["&#x00B4;", "Acute"],
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#x00FA;", "&#x0029;", "&#x005C;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x016F;", "&#x00A7;", ["&#x00A8;", "Umlaut"],
              "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  Cz_caps: ["&#x003B;", "&#x002B;", "&#x011A;", "&#x0160;", "&#x010C;", "&#x0158;", "&#x017D;", "&#x00DD;", "&#x00C1;", "&#x00CD;", "&#x00C9;", "&#x003D;", "&#x02CA;",
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x00DA;", "&#x0029;", "&#x005C;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x016E;", "&#x00A7;", ["&#x00A8;", "Umlaut"],
            "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x002D;"],

  Cz_shift: [["&#x00BA;", "RingAbove"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x0025;", ["&#x02C7;", "Caron"],
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x002F;", "&#x0028;", "&#x007C;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x0022;", "&#x0021;", "&#x0027;",
             "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003F;", "&#x003A;", "&#x005F;"],

  Cz_alt_gr: [["&#x0060;", "Grave"], "&#x0021;", "&#x0040;", "&#x0023;", "&#x0024;", "&#x0025;", "&#x005E;", "&#x0026;", "&#x002A;", "&#x0028;", "&#x0029;", "&#x002D;", "&#x003D;",
              ,, "&#x20AC;",,,,,,,, "&#x005B;", "&#x005D;", "&#x00DF;",,,,,,,,,, "&#x003B;", "&#x00A4;", "&#x005C;",
              ,,,,,,, "&#x003C;", "&#x003E;", "&#x002F;"],

  Cz_alt_gr_shift: [["&#x007E;", "Tilde"],,,,,,,,,,,"&#x005F;", "&#x002B;",,, "&#x20AC;",,,,,,,, "&#x007B;", "&#x007D;",
                    ["&#x00A8;", "Umlaut"],,,,,,,,,,"&#x003A;", ["&#x005E;", "Circumflex"], "&#x007C;",,,,,,,, "&#x00D7;", "&#x00F7;", "&#x003F;"],

*/

Da_normal: ["&#x00BD;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;",["&#x00B4;", "Acute"],
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00E5;",["&#x00A8;", "Umlaut"],"&#x003C;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00E6;","&#x00F8;","&#x0027;",
              "&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Da_caps: ["&#x00BD;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;",["&#x00B4;", "Acute"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x00A8;", "Umlaut"],"&#x003C;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00C6;","&#x00D8;","&#x0027;",
              "&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Da_shift: ["&#x00A7;","&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;",["&#x0060;", "Grave"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x005E;", "Circumflex"],"&#x003E;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00C6;","&#x00D8;","&#x002A;",
              "&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

  Da_alt_gr: [,,"&#x0040;","&#x00A3;","&#x0024;",,,"&#x007B;","&#x005B;","&#x005D;","&#x007D;",,"&#x007C;",,,"&#x20AC;",,,,,,,,,["&#x007E;", "Tilde"],"&#x005C;",,,,,,,,,,,,,,,,,,,,,,""],
/*
Da_normal: [,"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x005C;",
               "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00E5;","&#x00FC;",
               "&#x003C;","&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00E6;",
               "&#x00F8;","&#x0027;","&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Da_caps: [,"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x005C;",
               "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;","&#x00DC;",
               "&#x003C;","&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00C6;",
               "&#x00D8;","&#x0027;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Da_shift: [,"&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;",["&#x0060;", "Grave"],
               "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x005E;", "Circumflex"],
               "&#x003E;","&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00C6;",
               "&#x00D8;","&#x002A;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],
*/
Et_shift: [["&#x007E;", "Tilde"],"&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;",["&#x0060;", "Grave"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00DC;","&#x00D5;","&#x003E;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D6;","&#x00C4;",
              "&#x002A;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

    Et_caps: [["&#x02C7;", "Caron"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;",["&#x00B4;", "Acute"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00DC;","&#x00D5;","&#x003C;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D6;","&#x00C4;",
              "&#x0027;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

  Et_normal: [["&#x02C7;", "Caron"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;",["&#x00B4;", "Acute"],
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00FC;","&#x00F5;","&#x003C;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00F6;","&#x00E4;",
              "&#x0027;","&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

  Et_alt_gr: [,,"&#x0040;","&#x00A3;","&#x0024;",,,"&#x007B;","&#x005B;","&#x005D;","&#x007D;","&#x005C;",,,,"&#x20AC;",,,,,,,,,"&#x00A7;","&#x007C;",,,,,,,,,,,["&#x005E;", "Circumflex"],"&#x00BD;",,,,,,,,,,""],

Fi_normal: ["&#x00A7;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;",["&#x00B4;", "Acute"],
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00E5;",["&#x00A8;", "Umlaut"],"&#x003C;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00F6;","&#x00E4;",
              "&#x0027;","&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Fi_caps: ["&#x00A7;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;",["&#x00B4;", "Acute"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x00A8;", "Umlaut"],"&#x003C;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D6;","&#x00C4;",
              "&#x0027;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Fi_shift: ["&#x00BD;","&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;",["&#x0060;", "Grave"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x005E;", "Circumflex"],"&#x003E;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D6;","&#x00C4;",
              "&#x002A;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

  Fi_alt_gr: [,,"&#x0040;","&#x00A3;","&#x0024;",,,"&#x007B;","&#x005B;","&#x005D;","&#x007D;","&#x005C;",,,,"&#x20AC;",,,,,,,,,["&#x007E;", "Tilde"],"&#x007C;",,,,,,,,,,,,,,,,,,,,,,""],

  Hu_normal: ["&#x0030;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x00F6;","&#x00FC;","&#x00F3;",
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x007A;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x0151;","&#x00FA;","&#x00ED;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00E9;","&#x00E1;","&#x0171;",
              "&#x0079;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Hu_caps: ["&#x0030;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x00D6;","&#x00DC;","&#x00D3;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0150;","&#x00DA;","&#x00CD;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00C9;","&#x00C1;","&#x0170;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Hu_shift: ["&#x00A7;","&#x0027;","&#x0022;","&#x002B;","&#x0021;","&#x0025;","&#x002F;","&#x003D;","&#x0028;","&#x0029;","&#x00D6;","&#x00DC;","&#x00D3;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0150;","&#x00DA;","&#x00CD;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00C9;","&#x00C1;","&#x0170;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003F;","&#x003A;","&#x005F;"],

Hu_alt_gr: [,["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],["&#x00B4;", "Acute"],["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],"&#x005C;","&#x007C;",,,,,"&#x20AC;",,,,"&#x00F7;","&#x00D7;","&#x003C;",
,"&#x0111;","&#x00D0;","&#x005B;","&#x005D;",,,"&#x0142;","&#x0141;","&#x0024;","&#x00DF;","&#x00A4;",
"&#x003E;","&#x0023;","&#x0026;","&#x0040;","&#x007B;","&#x007D;",,"&#x003B;",,"&#x002A;"],

Lv_shift: ["&#x003F;","&#x0021;","&#x00AB;","&#x00BB;","&#x00A7;","&#x0025;","&#x002F;","&#x0026;","&#x00D7;","&#x0028;","&#x0029;","&#x005F;","&#x0046;",
              "&#x016A;","&#x0047;","&#x004A;","&#x0052;","&#x004D;","&#x0056;","&#x004E;","&#x005A;","&#x0112;","&#x010C;","&#x017D;","&#x0048;","&#x0122;",
              "&#x0160;","&#x0055;","&#x0053;","&#x0049;","&#x004C;","&#x0044;","&#x0041;","&#x0054;","&#x0045;","&#x0043;","&#x00B0;","&#x0136;",
              "&#x0145;","&#x0042;","&#x012A;","&#x004B;","&#x0050;","&#x004F;","&#x0100;","&#x003B;","&#x003A;","&#x013B;"],

    Lv_caps: ["&#x00AD;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002D;","&#x0046;",
              "&#x016A;","&#x0047;","&#x004A;","&#x0052;","&#x004D;","&#x0056;","&#x004E;","&#x005A;","&#x0112;","&#x010C;","&#x017D;","&#x0048;","&#x0122;",
              "&#x0160;","&#x0055;","&#x0053;","&#x0049;","&#x004C;","&#x0044;","&#x0041;","&#x0054;","&#x0045;","&#x0043;","&#x0027;","&#x0136;",
              "&#x0145;","&#x0042;","&#x012A;","&#x004B;","&#x0050;","&#x004F;","&#x0100;","&#x002C;","&#x002E;","&#x013B;"],

  Lv_normal: ["&#x00AD;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002D;","&#x0066;",
              "&#x016B;","&#x0067;","&#x006A;","&#x0072;","&#x006D;","&#x0076;","&#x006E;","&#x007A;","&#x0113;","&#x010D;","&#x017E;","&#x0068;","&#x0123;",
              "&#x0161;","&#x0075;","&#x0073;","&#x0069;","&#x006C;","&#x0064;","&#x0061;","&#x0074;","&#x0065;","&#x0063;","&#x0027;","&#x0137;",
              "&#x0146;","&#x0062;","&#x012B;","&#x006B;","&#x0070;","&#x006F;","&#x0101;","&#x002C;","&#x002E;","&#x013C;"],
//Lithuanian
Lt_shift: [["&#x007E;", "Tilde"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#43;","&#88;",
              "&#x0104;","&#x017D;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x012E;","&#87;","&#62;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0160;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x0172;","&#x0116;",
              "&#81;","&#x005A;","&#x016A;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x010C;","&#x0046;","&#x0118;"],

    Lt_caps: [["&#x0060;", "Grave"],"&#x0021;","&#45;","&#x002F;","&#x003B;","&#x003A;","&#x002C;","&#x002E;","&#61;","&#x0028;","&#x0029;","&#63;","&#120;",
              "&#x0104;","&#x017D;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x012E;","&#87;","&#62;",
              "&#x0041;","&#x0053;","&#x0044;","&#X0160;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x0172;","&#x0116;",
              "&#81;","&#x005A;","&#x016A;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x010C;","&#x0046;","&#x0118;"],

  Lt_normal: [["&#x0060;", "Grave"],"&#x0021;","&#45;","&#x002F;","&#x003B;","&#x003A;","&#x002C;","&#x002E;","&#61;","&#x0028;","&#x0029;","&#63;","&#120;",
              "&#x0105;","&#x017E;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x012F;","&#119;","&#60;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0161;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x0173;","&#x0117;",
              "&#113;","&#x007A;","&#x016B;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x010D;","&#x0066;","&#x0119;"],

  Lt_alt_gr: [["&#x00B4;","Acute"],"&#64;","&#95;","&#35;","&#36;","&#167;",["&#x005E;", "Circumflex"],"&#38;","&#42;","&#91;","&#93;","&#39;","&#37;",
              "","","&#x20AC;","",,,,,,,"&#123;","&#125;",,,,,,,,,,,,"&#34;","&#124;",,,,,,,,"&bdquo;","&#147;","&#92;"],

Mk_shift: ["&#x007C;","&#x0021;","&#x0022;","&#x0023;","&#x0024;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;","&#x002A;",
              "&#x0409;","&#x040A;","&#x0415;","&#x0420;","&#x0422;","&#x0417;","&#x0423;","&#x0418;","&#x041E;","&#x041F;","&#x0428;","&#x0403;","&#x003E;",
              "&#x0410;","&#x0421;","&#x0414;","&#x0424;","&#x0413;","&#x0425;","&#x0408;","&#x041A;","&#x041B;","&#x0427;","&#x040C;","&#x0416;",
              "&#x0405;","&#x040F;","&#x0426;","&#x0412;","&#x0411;","&#x041D;","&#x041C;","&#x003B;","&#x003A;","&#x005F;"],

    Mk_caps: ["&#x005C;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0409;","&#x040A;","&#x0415;","&#x0420;","&#x0422;","&#x0417;","&#x0423;","&#x0418;","&#x041E;","&#x041F;","&#x0428;","&#x0403;","&#x003C;",
              "&#x0410;","&#x0421;","&#x0414;","&#x0424;","&#x0413;","&#x0425;","&#x0408;","&#x041A;","&#x041B;","&#x0427;","&#x040C;","&#x0416;",
              "&#x0405;","&#x040F;","&#x0426;","&#x0412;","&#x0411;","&#x041D;","&#x041C;","&#x002C;","&#x002E;","&#x002D;"],

  Mk_normal: ["&#x005C;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0459;","&#x045A;","&#x0435;","&#x0440;","&#x0442;","&#x0437;","&#x0443;","&#x0438;","&#x043E;","&#x043F;","&#x0448;","&#x0453;","&#x003C;",
              "&#x0430;","&#x0441;","&#x0434;","&#x0444;","&#x0433;","&#x0445;","&#x0458;","&#x043A;","&#x043B;","&#x0447;","&#x045C;","&#x0436;",
              "&#x0455;","&#x045F;","&#x0446;","&#x0432;","&#x0431;","&#x043D;","&#x043C;","&#x002C;","&#x002E;","&#x002D;"],

  Mk_alt_gr: [,"&#x007E;",,"&#x005E;",,,,"&#x0060;",,,,,,"&#x005C;","&#x007C;",,,,,,,,,"&#x0403;","&#x0452;",,,,,"&#x005B;","&#x005D;",,,,,"&#x040C;","&#x045B;",,,,,"&#x0040;","&#x007B;","&#x007D;","&#x00A7;",,,""],

No_normal: ["&#x007C;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x005C;",
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00E5;",["&#x00A8;", "Umlaut"],"&#x003C;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00F8;","&#x00E6;","&#x0027;",
              "&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],


    No_caps: ["&#x007C;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x005C;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x00A8;", "Umlaut"],"&#x003C;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D8;","&#x00C6;","&#x0027;",
              "&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   No_shift: ["&#x00A7;","&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;",["&#x0060;", "Grave"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x005E;", "Circumflex"],"&#x003E;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D8;","&#x00C6;","&#x002A;",
              "&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

  No_alt_gr: [,,"&#x0040;","&#x00A3;","&#x0024;",,,"&#x007B;","&#x005B;","&#x005D;","&#x007D;",,["&#x00B4;", "Acute"],,,"&#x20AC;",,,,,,,,,["&#x007E;", "Tilde"],,,,,,,,,,,,,,,,,,,,,,,""],
/*
  No_normal: [,"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x005C;",
               "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00E5;","&#x00FC;",
               "&#x003C;","&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00F8;",
               "&#x00E6;","&#x0027;","&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],


    No_caps: [,"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x005C;",
               "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;","&#x00DC;",
               "&#x003C;","&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D8;",
               "&#x00C6;","&#x0027;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   No_shift: [,"&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;",["&#x0060;", "Grave"],
               "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x005E;", "Circumflex"],
               "&#x003E;","&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D8;",
               "&#x00C6;","&#x002A;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"]  Pl_normal: [["&#x02DB;", "Ogonek"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x0027;",
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x007A;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x017C;","&#x015B;","&#x003C;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x0142;","&#x0105;","&#x00F3;",
              "&#x0079;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],
*/

    Pl_caps: [["&#x02DB;", "Ogonek"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x0027;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x017B;","&#x015A;","&#x003C;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x0141;","&#x0104;","&#x00D3;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Pl_shift: [["&#x02D9;", "DotAbove"],"&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;","&#x002A;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0144;","&#x0107;","&#x003E;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x0141;","&#x0119;","&#x017A;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

  Pl_alt_gr: [,["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],["&#x00B4;", "Acute"],["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],
              "&#x005C;","&#x007C;",,,,,"&#x20AC;",,,,"&#x00F7;","&#x00D7;",,,"&#x0111;","&#x00D0;","&#x005B;","&#x005D;",,,,,"&#x0024;","&#x00DF;",,,,,"&#x0040;","&#x007B;","&#x007D;","&#x00A7;",,,""],

Pl_normal: [["&#x0060;", "Grave"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
              "&#x0071;", "&#x0077;", "&#x0065;", "&#x0072;", "&#x0074;", "&#x0079;", "&#x0075;", "&#x0069;", "&#x006F;", "&#x0070;", "&#x005B;", "&#x005D;", "&#x005C;",
              "&#x0061;", "&#x0073;", "&#x0064;", "&#x0066;", "&#x0067;", "&#x0068;", "&#x006A;", "&#x006B;", "&#x006C;", "&#x003B;", "&#x0027;",,
              "&#x007A;", "&#x0078;", "&#x0063;", "&#x0076;", "&#x0062;", "&#x006E;", "&#x006D;", "&#x002C;", "&#x002E;", "&#x002F;"],

/*
  Pl_caps: [["&#x0060;", "Grave"], "&#x0031;", "&#x0032;", "&#x0033;", "&#x0034;", "&#x0035;", "&#x0036;", "&#x0037;", "&#x0038;", "&#x0039;", "&#x0030;", "&#x002D;", "&#x003D;",
            "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x005B;", "&#x005D;", "&#x005C;",
            "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x003B;", "&#x0027;",,
            "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x002C;", "&#x002E;", "&#x002F;"],

  Pl_shift: ["&#x02DC;", "&#x0021;", "&#x0040;", "&#x0023;", "&#x0024;", "&#x0025;", ["&#x005E;", "Circumflex"], "&#x0026;", "&#x002A;", "&#x0028;", "&#x0029;", "&#x005F;", "&#x002B;",
             "&#x0051;", "&#x0057;", "&#x0045;", "&#x0052;", "&#x0054;", "&#x0059;", "&#x0055;", "&#x0049;", "&#x004F;", "&#x0050;", "&#x007B;", "&#x007D;", "&#x007C;",
             "&#x0041;", "&#x0053;", "&#x0044;", "&#x0046;", "&#x0047;", "&#x0048;", "&#x004A;", "&#x004B;", "&#x004C;", "&#x003A;", "&#x0022;",,
             "&#x005A;", "&#x0058;", "&#x0043;", "&#x0056;", "&#x0042;", "&#x004E;", "&#x004D;", "&#x003C;", "&#x003E;", "&#x003F;"],

  Pl_alt_gr: [,,,,,,,,,,,,,,,"&#x0119;",,,,"&#x20AC;",,"&#x00F3;",,,,,"&#x0105;","&#x015B;",,,,,,,"&#x0142;",,,,"&#x017C;","&#x017A;","&#x0107;",,,"&#x0144;",,,,""],

  Pl_alt_gr_shift: [,,,,,,,,,,,,,,,"&#x0118;",,,,"&#x20AC;",,"&#x00D3;",,,,,"&#x0104;","&#x015A;",,,,,,,"&#x0141;",,,,"&#x017B;","&#x0179;","&#x0106;",,,"&#x0143;",,,,""],
*/

Ro_normal: ["&#x00E2;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x0027;",
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x0103;","&#x00EE;","&#x00D7;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x015F;","&#x0163;","&#x005C;",
              "&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Ro_caps: ["&#x00C2;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x0027;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0102;","&#x00CE;","&#x00D7;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x015E;","&#x0162;","&#x005C;",
              "&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Ro_shift: ["&#x00C2;","&#x0021;","&#x0022;","&#x0023;","&#x0024;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;","&#x002A;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0102;","&#x00CE;","&#x00F7;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x015E;","&#x0162;","&#x007C;",
              "&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

  Ro_alt_gr: ["&#x00AD;",["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],
              ["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],["&#x00B4;", "Acute"],["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],
              ,,"&#x20AC;",,,,,,,,"&#x007B;","&#x007D;","&#x00A7;",,,,,,,,,,"&#x005B;","&#x005D;","&#x00DF;",,,,,,,,"&#x003C;","&#x003E;","&#x0040;"],

  Sr_shift: ["&#x007C;","&#x0021;","&#x0022;","&#x0023;","&#x0024;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;","&#x002A;",
              "&#x0409;","&#x040A;","&#x0415;","&#x0420;","&#x0422;","&#x0417;","&#x0423;","&#x0418;","&#x041E;","&#x041F;","&#x0428;","&#x0402;","&#x003E;",
              "&#x0410;","&#x0421;","&#x0414;","&#x0424;","&#x0413;","&#x0425;","&#x0408;","&#x041A;","&#x041B;","&#x0427;","&#x040B;","&#x0416;",
              "&#x0405;","&#x040F;","&#x0426;","&#x0412;","&#x0411;","&#x041D;","&#x041C;","&#x003B;","&#x003A;","&#x005F;"],

    Sr_caps: ["&#x005C;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0409;","&#x040A;","&#x0415;","&#x0420;","&#x0422;","&#x0417;","&#x0423;","&#x0418;","&#x041E;","&#x041F;","&#x0428;","&#x0402;","&#x003C;",
              "&#x0410;","&#x0421;","&#x0414;","&#x0424;","&#x0413;","&#x0425;","&#x0408;","&#x041A;","&#x041B;","&#x0427;","&#x040B;","&#x0416;",
              "&#x0405;","&#x040F;","&#x0426;","&#x0412;","&#x0411;","&#x041D;","&#x041C;","&#x002C;","&#x002E;","&#x002D;"],

  Sr_normal: ["&#x005C;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0459;","&#x045A;","&#x0435;","&#x0440;","&#x0442;","&#x0437;","&#x0443;","&#x0438;","&#x043E;","&#x043F;","&#x0448;","&#x0452;","&#x003C;",
              "&#x0430;","&#x0441;","&#x0434;","&#x0444;","&#x0433;","&#x0445;","&#x0458;","&#x043A;","&#x043B;","&#x0447;","&#x045B;","&#x0436;",
              "&#x0455;","&#x045F;","&#x0446;","&#x0432;","&#x0431;","&#x043D;","&#x043C;","&#x002C;","&#x002E;","&#x002D;"],

  Sr_alt_gr: [,"&#x007E;",,"&#x005E;",,,,"&#x0060;",,,,,,"&#x005C;","&#x007C;",,,,,,,,,"&#x0403;","&#x0453;",,,,,"&#x005B;","&#x005D;",,,,,"&#x040C;","&#x045C;",,,, ,"&#x0040;","&#x007B;","&#x007D;","&#x00A7;",,,""],

  Sh_normal: [["&#x00B8;", "Cedilla"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x007A;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x0161;","&#x0111;","&#x003C;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x010D;","&#x0107;","&#x017E;",
              "&#x0079;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Sh_caps: [["&#x00B8;", "Cedilla"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0027;","&#x002B;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0160;","&#x00D0;","&#x003C;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x010C;","&#x0106;","&#x017D;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Sh_shift: [["&#x00A8;", "Umlaut"] ,"&#x0021;","&#x0022;","&#x0023;","&#x0024;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;","&#x002A;",
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x0160;","&#x00D0;","&#x003E;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x010C;","&#x0106;","&#x017D;",
              "&#x0059;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

  Sh_alt_gr: [,["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],
              ["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],["&#x00B4;", "Acute"],["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],
              "&#x005C;","&#x007C;","&#x20AC;",,,,,,,,"&#x00F7;","&#x00D7;",,,,,"&#x005B;","&#x005D;",,,"&#x0142;","&#x0141;",,"&#x00DF;","&#x00A4;",,,,"&#x0040;","&#x007B;","&#x007D;","&#x00A7;",,,""],

   Sk_shift: [["&#x00B0;", "RingAbove"],"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x0025;",["&#x02C7;", "Caron"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x002F;","&#x0028;","&#x002A;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x0022;","&#x0021;","&#x0029;","&#x0059;",
              "&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003F;","&#x003A;","&#x005F;"],

    Sk_caps: ["&#x003B;","&#x002B;","&#x013D;","&#x0160;","&#x010C;","&#x0164;","&#x017D;","&#x00DD;","&#x00C1;","&#x00CD;","&#x00C9;","&#x003D;",["&#x00B4;", "Acute"],
              "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x005A;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00DA;","&#x00C4;","&#x0026;",
              "&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D4;","&#x00A7;","&#x0147;","&#x0059;",
              "&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

  Sk_normal: ["&#x003B;","&#x002B;","&#x013E;","&#x0161;","&#x010D;","&#x0165;","&#x017E;","&#x00FD;","&#x00E1;","&#x00ED;","&#x00E9;","&#x003D;",["&#x00B4;", "Acute"],
              "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x007A;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00FA;","&#x00E4;","&#x0026;",
              "&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00F4;","&#x00A7;","&#x0148;","&#x0079;",
              "&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

  Sk_alt_gr: [,["&#x007E;", "Tilde"],["&#x02C7;", "Caron"],["&#x005E;", "Circumflex"],["&#x02D8;", "Breve"],["&#x00B0;", "RingAbove"],["&#x02DB;", "Ogonek"],["&#x0060;", "Grave"],["&#x02D9;", "DotAbove"],"&#x0027;",["&#x02DD;", "DoubleAcute"],["&#x00A8;", "Umlaut"],["&#x00B8;", "Cedilla"],
              "&#x005C;","&#x007C;","&#x20AC;",,,,,,,,"&#x00F7;","&#x00D7;","&#x003C;",,"&#x0111;","&#x00D0;","&#x005B;","&#x005D;",,,"&#x0142;","&#x0141;","&#x0024;","&#x00DF;","&#x00A4;","&#x003E;","&#x0023;",,"&#x0040;","&#x007B;","&#x007D;",,,,""],

Sv_normal: [,"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x00E9;",
               "&#x0071;","&#x0077;","&#x0065;","&#x0072;","&#x0074;","&#x0079;","&#x0075;","&#x0069;","&#x006F;","&#x0070;","&#x00E5;","&#x00FC;",
               "&#x003C;","&#x0061;","&#x0073;","&#x0064;","&#x0066;","&#x0067;","&#x0068;","&#x006A;","&#x006B;","&#x006C;","&#x00F6;",
               "&#x00E4;","&#x0027;","&#x007A;","&#x0078;","&#x0063;","&#x0076;","&#x0062;","&#x006E;","&#x006D;","&#x002C;","&#x002E;","&#x002D;"],

    Sv_caps: [,"&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x002B;","&#x00C9;",
               "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;","&#x00DC;",
               "&#x003C;","&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D6;",
               "&#x00C4;","&#x0027;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x002C;","&#x002E;","&#x002D;"],

   Sv_shift: [,"&#x0021;","&#x0022;","&#x0023;","&#x00A4;","&#x0025;","&#x0026;","&#x002F;","&#x0028;","&#x0029;","&#x003D;","&#x003F;","&#x00C9;",
               "&#x0051;","&#x0057;","&#x0045;","&#x0052;","&#x0054;","&#x0059;","&#x0055;","&#x0049;","&#x004F;","&#x0050;","&#x00C5;",["&#x005E;", "Circumflex"],
               "&#x003E;","&#x0041;","&#x0053;","&#x0044;","&#x0046;","&#x0047;","&#x0048;","&#x004A;","&#x004B;","&#x004C;","&#x00D6;",
               "&#x00C4;","&#x002A;","&#x005A;","&#x0058;","&#x0043;","&#x0056;","&#x0042;","&#x004E;","&#x004D;","&#x003B;","&#x003A;","&#x005F;"],

Uk_normal: ["&#x0060;","&#x2116;","&#x002D;","&#x002F;","&#x0022;","&#x003A;","&#x002C;","&#x002E;","&#x005F;","&#x003F;","&#x0025;","&#x0021;","&#x003B;",
              "&#x0439;","&#x0446;","&#x0443;","&#x043A;","&#x0435;","&#x043D;","&#x0433;","&#x0448;","&#x0449;","&#x0437;","&#x0445;","&#x0491;","&#x0029;",
              "&#x0444;","&#x0438;","&#x0432;","&#x0430;","&#x043F;","&#x0440;","&#x043E;","&#x043B;","&#x0434;","&#x0436;","&#x0454;",,
              "&#x044F;","&#x0447;","&#x0441;","&#x043C;","&#x0456;","&#x0442;","&#x044C;","&#x0431;","&#x044E;","&#x0457;"],

    Uk_caps: ["&#x0060;","&#x2116;","&#x002D;","&#x002F;","&#x0022;","&#x003A;","&#x002C;","&#x002E;","&#x005F;","&#x003F;","&#x0025;","&#x0021;","&#x003B;",
              "&#x0419;","&#x0426;","&#x0423;","&#x041A;","&#x0415;","&#x041D;","&#x0413;","&#x0428;","&#x0429;","&#x0417;","&#x0425;","&#x0490;","&#x0029;",
              "&#x0424;","&#x0418;","&#x0412;","&#x0410;","&#x041F;","&#x0420;","&#x041E;","&#x041B;","&#x0414;","&#x0416;","&#x0404;",,
              "&#x042F;","&#x0427;","&#x0421;","&#x041C;","&#x0406;","&#x0422;","&#x042C;","&#x0411;","&#x042E;","&#x0407;"],

   Uk_shift: ["&#x002B;","&#x0031;","&#x0032;","&#x0033;","&#x0034;","&#x0035;","&#x0036;","&#x0037;","&#x0038;","&#x0039;","&#x0030;","&#x003D;","&#x005C;",
              "&#x0419;","&#x0426;","&#x0423;","&#x041A;","&#x0415;","&#x041D;","&#x0413;","&#x0428;","&#x0429;","&#x0417;","&#x0425;","&#x0490;","&#x0028;",
              "&#x0424;","&#x0418;","&#x0412;","&#x0410;","&#x041F;","&#x0420;","&#x041E;","&#x041B;","&#x0414;","&#x0416;","&#x0404;",,
              "&#x042F;","&#x0427;","&#x0421;","&#x041C;","&#x0406;","&#x0422;","&#x042C;","&#x0411;","&#x042E;","&#x0407;"],



  // Diacritic arrays:

  Acute: [["&#x0061;", "&#x00E1;"], ["&#x0065;", "&#x00E9;"], ["&#x0069;", "&#x00ED;"], ["&#x006F;", "&#x00F3;"],
          ["&#x0075;", "&#x00FA;"], ["&#x0079;", "&#x00FD;"], ["&#x0041;", "&#x00C1;"], ["&#x0045;", "&#x00C9;"],
          ["&#x0049;", "&#x00CD;"], ["&#x004F;", "&#x00D3;"], ["&#x0055;", "&#x00DA;"], ["&#x0059;", "&#x00DD;"],
          ["&#x0063;", "&#x0107;"], ["&#x0043;", "&#x0106;"], ["&#x006C;", "&#x013A;"], ["&#x004C;", "&#x0139;"],
          ["&#x006D;", "&#x1E3F;"], ["&#x004D;", "&#x1E3E;"], ["&#x006E;", "&#x0144;"], ["&#x004E;", "&#x0143;"],
          ["&#x0072;", "&#x0155;"], ["&#x0052;", "&#x0154;"], ["&#x0073;", "&#x015B;"], ["&#x0053;", "&#x015A;"],
          ["&#x007A;", "&#x017A;"], ["&#x005A;", "&#x0179;"], ["&#x0391;", "&#x0386;"], ["&#x0395;", "&#x0388;"],
          ["&#x0397;", "&#x0389;"], ["&#x0399;", "&#x038A;"], ["&#x039F;", "&#x038C;"], ["&#x03A5;", "&#x038E;"],
          ["&#x03A9;", "&#x038F;"], ["&#x03B1;", "&#x03AC;"], ["&#x03B5;", "&#x03AD;"], ["&#x03B7;", "&#x03AE;"],
          ["&#x03B9;", "&#x03AF;"], ["&#x03BF;", "&#x03CC;"], ["&#x03C5;", "&#x03CD;"], ["&#x03C9;", "&#x03CE;"],
          ["&#x0057;", "&#x1E82;"], ["&#x0077;", "&#x1E83;"]],

  Breve: [["&#x0061;", "&#x0103;"], ["&#x0065;", "&#x0115;"], ["&#x0069;", "&#x012D;"], ["&#x006F;", "&#x014F;"],
          ["&#x0075;", "&#x016D;"], ["&#x0041;", "&#x0102;"], ["&#x0045;", "&#x0114;"], ["&#x0049;", "&#x012C;"],
          ["&#x004F;", "&#x014E;"], ["&#x0055;", "&#x016C;"], ["&#x0079;", "y&#x306;"], ["&#x0059;", "Y&#x306;"],
          ["&#x0067;", "&#x011F;"], ["&#x0047;", "&#x011E;"]],

  Caron: [["&#x0063;", "&#x010D;"], ["&#x0043;", "&#x010C;"], ["&#x0064;", "&#x010F;"], ["&#x0044;", "&#x010E;"],
          ["&#x0065;", "&#x011B;"], ["&#x0045;", "&#x011A;"], ["&#x006E;", "&#x0148;"], ["&#x004E;", "&#x0147;"],
          ["&#x0072;", "&#x0159;"], ["&#x0052;", "&#x0158;"], ["&#x0073;", "&#x0161;"], ["&#x0053;", "&#x0160;"],
          ["&#x0074;", "&#x0165;"], ["&#x0054;", "&#x0164;"], ["&#x007A;", "&#x017E;"], ["&#x005A;", "&#x017D;"],
          ["&#x006C;", "&#x013E;"], ["&#x004C;", "&#x013D;"]],

  Cedilla: [["&#x0063;", "&#x00E7;"], ["&#x0043;", "&#x00C7;"], ["&#x0067;", "&#x0123;"], ["&#x0047;", "&#x0122;"],
            ["&#x006B;", "&#x0137;"], ["&#x004B;", "&#x0136;"], ["&#x006C;", "&#x013C;"], ["&#x004C;", "&#x013B;"],
            ["&#x006E;", "&#x0146;"], ["&#x004E;", "&#x0145;"], ["&#x0072;", "&#x0157;"], ["&#x0052;", "&#x0156;"],
            ["&#x0073;", "&#x015F;"], ["&#x0053;", "&#x015E;"], ["&#x0074;", "&#x0163;"], ["&#x0054;", "&#x0162;"]],

  Circumflex: [["&#x0061;", "&#x00E2;"], ["&#x0041;", "&#x00C2;"], ["&#x0065;", "&#x00EA;"], ["&#x0045;", "&#x00CA;"],
               ["&#x0069;", "&#x00EE;"], ["&#x0049;", "&#x00CE;"], ["&#x006F;", "&#x00F4;"], ["&#x004F;", "&#x00D4;"],
               ["&#x0063;", "&#x0109;"], ["&#x0043;", "&#x0108;"], ["&#x0067;", "&#x011D;"], ["&#x0047;", "&#x011C;"],
               ["&#x0068;", "&#x0125;"], ["&#x0048;", "&#x0124;"], ["&#x006A;", "&#x0135;"], ["&#x004A;", "&#x0134;"],
               ["&#x0073;", "&#x015D;"], ["&#x0053;", "&#x015C;"], ["&#x0075;", "&#x00FB;"], ["&#x0055;", "&#x00DB;"],
               ["&#x0077;", "&#x0175;"], ["&#x0057;", "&#x0174;"], ["&#x0079;", "&#x0177;"], ["&#x0059;", "&#x0176;"]],

  DialytikaTonos: [["&#x03B9;", "&#x0390;"], ["&#x03C6;", "&#x03B0;"]], // combined acute + ulmaut

  DotAbove: [["&#x0063;", "&#x010B;"], ["&#x0043;", "&#x010A;"], ["&#x0067;", "&#x0121;"], ["&#x0047;", "&#x0120;"],
             ["&#x007A;", "&#x017C;"], ["&#x005A;", "&#x017B;"], ["&#x0065;", "&#x0117;"], ["&#x0045;", "&#x0116;"],
             ["&#x006E;", "&#x1E45;"], ["&#x004E;", "&#x1E44;"], ["&#x006D;", "m&#x307;"], ["&#x004D;", "M&#x307;"],
             ["&#x0062;", "b&#x307;"], ["&#x0042;", "B&#x307;"]],

  DoubleAcute: [["&#x006F;", "&#x0151;"], ["&#x004F;", "&#x0150;"], ["&#x0075;", "&#x0171;"], ["&#x0055;", "&#x0170;"]],

  Grave: [["&#x0061;", "&#x00E0;"], ["&#x0065;", "&#x00E8;"], ["&#x0069;", "&#x00EC;"], ["&#x006F;", "&#x00F2;"], ["&#x0075;", "&#x00F9;"],
          ["&#x0041;", "&#x00C0;"], ["&#x0045;", "&#x00C8;"], ["&#x0049;", "&#x00CC;"], ["&#x004F;", "&#x00D2;"], ["&#x0055;", "&#x00D9;"],
          ["&#x0057;", "&#x1E80;"], ["&#x0077;", "&#x1E81;"], ["&#x0059;", "&#x1EF2;"], ["&#x0079;", "&#x1EF3;"],
          ["&#x006D;", "m&#x300;"], ["&#x004D;", "M&#x300;"], ["&#x006E;", "n&#x300;"], ["&#x004E;", "N&#x300;"]],

Humlaut: [["&#x006F;","&#x0151;"],["&#x0075;","&#x0171;"],["&#x004F;","&#x0150;"],["&#x0055;","&#x0170;"]],


  Macron: [["&#x0061;", "&#x0101;"], ["&#x0041;", "&#x0100;"], ["&#x0065;", "&#x0113;"], ["&#x0045;", "&#x0112;"], ["&#x0069;", "&#x012B;"],
           ["&#x0049;", "&#x012A;"], ["&#x006F;", "&#x014D;"], ["&#x004F;", "&#x014C;"], ["&#x0075;", "&#x016B;"], ["&#x0055;", "&#x016A;"],
           ["&#x0079;", "y&#x304;"], ["&#x0059;", "Y&#x304;"], ["&#x006D;", "m&#x304;"], ["&#x004D;", "M&#x304;"], ["&#x006E;", "n&#x304;"],
           ["&#x004E;", "N&#x304;"]],

  Ogonek: [["&#x0069;", "&#x012F;"], ["&#x006F;", "&#x01EB;"], ["&#x0075;", "&#x0173;"],
           ["&#x0049;", "&#x012E;"], ["&#x004F;", "&#x01EA;"], ["&#x0055;", "&#x0172;"]],

  RingAbove: [["&#x0061;", "&#x00E5;"], ["&#x0041;", "&#x00C5;"], ["&#x0075;", "&#x016F;"], ["&#x0055;", "&#x016E;"]],

  Tilde: [["&#x0061;", "&#x00E3;"], ["&#x006F;", "&#x00F5;"], ["&#x006E;", "&#x00F1;"], ["&#x0041;", "&#x00C3;"], ["&#x004F;", "&#x00D5;"],
          ["&#x0069;", "&#x0129;"], ["&#x0049;", "&#x0128;"], ["&#x0075;", "&#x0169;"], ["&#x0055;", "&#x0168;"], ["&#x004E;", "&#x00D1;"],
          ["&#x0065;", "&#x1EBD;"], ["&#x0045;", "&#x1EBC;"], ["&#x0079;", "&#x1EF9;"], ["&#x0059;", "&#x1EF8;"], ["&#x0067;", "g&#x303;"],
          ["&#x0047;", "G&#x303;"]],

  Umlaut: [["&#x0061;", "&#x00E4;"], ["&#x0065;", "&#x00EB;"], ["&#x0069;", "&#x00EF;"], ["&#x006F;", "&#x00F6;"], ["&#x0075;", "&#x00FC;"],
           ["&#x0079;", "&#x00FF;"], ["&#x0041;", "&#x00C4;"], ["&#x0045;", "&#x00CB;"], ["&#x0049;", "&#x00CF;"], ["&#x004F;", "&#x00D6;"],
           ["&#x0055;", "&#x00DC;"], ["&#x0059;", "&#x0178;"], ["&#x0399;", "&#x03AA;"], ["&#x03A5;", "&#x03AB;"], ["&#x03B9;", "&#x03CA;"],
           ["&#x03C5;", "&#x03CB;"]]
};

//-----------------------

/*
*  Class implements cross-browser work with text selection
*
*  @author Ilya Lebedev
*  @license LGPL
*/

/*
*  @class DocumentSelection
*/
DocumentSelection = new function () {
  var self = this;
  /*
  *  Stores hash of keys, applied to elements
  *
  *  @type Object
  *  @scope private
  */
  var keys = {
    'selectionStart' : '__DSselectionStart',
    'selectionEnd' : '__DSselectionEnd'
  }
  /*
  *  Properties to save current scrolling position in Mozilla
  *
  *
  */
  var scrollTop, scrollLeft;
  //---------------------------------------------------------------------------
  //  SETTERS
  //---------------------------------------------------------------------------
  /**
   *  getSelectionRange wrapper/emulator
   *  adapted version
   *
   *  @see http://www.bazon.net/mishoo/articles.epl?art_id=1292
   *  @param {HTMLElement}
   *  @param {Number} start position
   *  @param {Number} end position
   *  @param {Boolean} related indicates calculation of range relatively to current start point
   *  @return void
   *  @scope public
   */
  this.setRange = function(el, start, end, related) {
    /*
    *  set range on relative coordinates
    */
    if (related) {
      var st = self.getStart(el);
      end = st+end;
      start = st+start;
    }
    if ('function' == typeof el.setSelectionRange) {
      /*
      *  for Mozilla
      */
      try {el.setSelectionRange(start, end)} catch (e) {}
    } else {
      /*
      *  for IE
      */
      var range;
      /*
      *  just try to create a range....
      */
      try {
        range = el.createTextRange();
      } catch(e) {
        try {
          range = document.body.createTextRange();
          range.moveToElementText(el);
        } catch(e) {
          range = false;
        }
      }
      // if cannot create range
      if (!range) return false;
      range.collapse(true);

      range.moveStart("character", start);
      range.moveEnd("character", end - start);
      range.select();
    }
    self.setCursorPosition(el,start,end);
  }
  /**
   *  Set sursor position for supplied child
   *
   *  @param {HTMLElement} element to set cursor position on
   *  @param {Number} start selection start
   *  @param {Number} end selection end
   *  @scope public
   */
  this.setCursorPosition = function (el,start,end) {
    el[keys['selectionStart']] = parseInt(start);
    el[keys['selectionEnd']] = parseInt(end);
//    if (scrollTop) el.scrollTop  = scrollTop;
//    if (scrollLeft) el.scrollLeft = scrollLeft;
//    scrollTop = null;
//    scrollLeft= null;
  }
  //---------------------------------------------------------------------------
  //  GETTERS
  //---------------------------------------------------------------------------
  /**
   *  Return contents of the current selection
   *
   *  @param {HTMLElement} el element to look position on
   *  @return {String}
   *  @scope public
   */
  this.getSelection = function(el) {
    var s = self.getCursorPosition(el),
        e = self.getEnd(el);
    /*
    *  w/o this check content might be duplicated on delete
    */
    if (e<s) e = s;
    /*
    *  check for IE, because Opera does use \r\n sequence, but calculate positions correctly
    */
    var tmp = document.selection&&!window.opera?el.value.replace(/\r/g,""):el.value;
    return tmp.substring(s,e);
  }
  /**
   *  getSelectionStart wrapper/emulator
   *  adapted version
   *
   *  @see http://www.bazon.net/mishoo/articles.epl?art_id=1292
  *  @param {HTMLElement} el element to calculate end position for
  *  @param {HTMLElement} force force calculation
   *  @return {Number} start position
   *  @scope public
   */
  this.getStart = function (el, force) {
    var start;
    /*
    *  for IE
    */
    try {
      start = Math.abs(document.selection.createRange().moveStart("character", -100000000)); // start
      if (start>0 || force) {
        try {
          var endReal = Math.abs(el.createTextRange().moveEnd("character", -100000000));
          /*
          *  calculate node offset
          */
          var r = document.body.createTextRange();
          r.moveToElementText(el);
          var sTest = Math.abs(r.moveStart("character", -100000000));
          var eTest = Math.abs(r.moveEnd("character", -100000000));
          /*
          *  test for the TEXTAREA's dumb behavior
          */
          if (el.tagName.toLowerCase() != 'input' && eTest - endReal == sTest) {
            start -= sTest;
          }
        } catch(err) {}
      }
    } catch (e) {}
    /*
    *  for Mozilla/Opera/Safari
    */
    if (isNaN(start)) try { start = el.selectionStart } catch (e) { start = -1 }
    return start<1?(start==0&&force?0:(parseInt(el[keys['selectionStart']])?parseInt(el[keys['selectionStart']]):0)):start;
  }
  /*
  *  getSelectionStart wrapper/emulator
  *  adapted version
  *
  *  @see http://www.bazon.net/mishoo/articles.epl?art_id=1292
  *  @param {HTMLElement} el element to calculate end position for
  *  @param {HTMLElement} force force calculation
  *  @return {Number} start position
  *  @scope public
  */
  this.getEnd = function (el,force) {
    var end;
    /*
    *  for IE
    */
    try {
      end = Math.abs(document.selection.createRange().moveEnd("character", -100000000)); // end
      if (end>0 || force) {
        try {
          var endReal = Math.abs(el.createTextRange().moveEnd("character", -100000000));
          /*
          *  calculate node offset
          */
          var r = document.body.createTextRange();
          r.moveToElementText(el);
          var sTest = Math.abs(r.moveStart("character", -100000000));
          var eTest = Math.abs(r.moveEnd("character", -100000000));
          /*
          *  test for the TEXTAREA's dumb behavior
          */
          if (el.tagName.toLowerCase() != 'input' && eTest - endReal == sTest) {
            end -= sTest;
          }
        } catch(err) {}
      }
    } catch (e) {}
    /*
    *  for Mozilla/Opera/Safari
    */
    if (isNaN(end)) try { end = el.selectionEnd } catch (e) { end = -1 }
    return end<1?(end==0&&force?0:(parseInt(el[keys['selectionEnd']])?parseInt(el[keys['selectionEnd']]):0)):end;
  }
  /*
  *  Return cursor position for supplied field
  *
  *  @param {HTMLElement} element to get cursor position from
  *  @return {Number} position
  *  @scope public
  */
  this.getCursorPosition = function (el) {
//    scrollTop  = el.scrollTop;
//    scrollLeft = el.scrollLeft;

    return self.getStart(el);
  }
  //---------------------------------------------------------------------------
  //  MICS FUNCTIONS
  //---------------------------------------------------------------------------
  /*
  *  Used to save cursor position on click.
  *
  *  @param {MouseEvent} click event
  *  @scope protected
  */
  this.saveCursorPosition = function (e) {
    var el = e.srcElement || e.target;
    if(!el || !el.tagName) return false;

    var t = el.tagName.toLowerCase();
    if(t == 'textarea' || (t == 'input' && el.type == 'text'))
    {
        if(el[keys.selectionStart] == undefined) el[keys.selectionStart] = -1;
        if(el[keys.selectionEnd] == undefined) el[keys.selectionEnd] = -1;

        self.setCursorPosition(el,self.getStart(el,true),self.getEnd(el,true));
    }
  }
  /*
  *  Insert text at cursor position
  *
  *  @param {HTMLElement} text field to insert text
  *  @param {String} text to insert
  *  @scope public
  */
  this.insertAtCursor = function (fld, val) {
    var r = self.getCursorPosition(fld);
    /*
    *  check for IE, because Opera does use \r\n sequence, but calculate positions correctly
    */
    var tmp = document.selection&&!window.opera?fld.value.replace(/\r/g,""):fld.value;
    fld.value = tmp.substring(0, r)+val+tmp.substring(r,tmp.length);
    self.setRange(fld,r+val.length,r+val.length);
  }
  /*
  *  Deletes char at cursor position
  *
  *  @param {HTMLElement} text field to delete text
  *  @param {Boolean} delete text before (backspace) or after (del) cursor
  *  @scope public
  */
  this.deleteAtCursor = function (fld, after) {
    if (!after) after = false;
    var r = self.getCursorPosition(fld),
        e = self.getEnd(fld);
    /*
    *  w/o this check content might be duplicated on delete
    */
    if (e<r) e = r;
    if (r==e) {
      r=after?r:r-1<0?0:r-1;
      e=after?e+1:e;
    }
    /*
    *  check for IE, because Opera does use \r\n sequence, but calculate positions correctly
    */
    var tmp = document.selection&&!window.opera?fld.value.replace(/\r/g,""):fld.value;
    fld.value = tmp.substring(0, r)+tmp.substring(e,tmp.length);
    self.setRange(fld, r, r);
  }
  /**
   *  Removes the selection, if available
   *
   *  @param {HTMLElement} fld field to delete text from
   *  @scope public
   */
  this.deleteSelection = function (fld) {
    var r = self.getCursorPosition(fld),
        e = self.getEnd(fld);
    if (r==e) return;
    /*
    *  check for IE, because Opera does use \r\n sequence, but calculate positions correctly
    */
    var tmp = document.selection&&!window.opera?fld.value.replace(/\r/g,""):fld.value;
    fld.value = tmp.substring(0, r)+tmp.substring(e,tmp.length);
    self.setRange(fld, r, r);
  }
}

/*
*  Add cursor position saving
*/
if (document.attachEvent) {
  document.attachEvent('onmouseup', DocumentSelection.saveCursorPosition);
  document.attachEvent('onkeyup', DocumentSelection.saveCursorPosition);
} else if (document.addEventListener) {
  document.addEventListener('mouseup', DocumentSelection.saveCursorPosition,false);
  document.addEventListener('keyup', DocumentSelection.saveCursorPosition,false);
}


/********************************************************************************/

   // 'source' is the field which is currently focused:
   var source = null, opened = false, vkb = null;

   function search_for_input_field(num)
   {
      var tg = document.getElementsByTagName("INPUT");

      if(tg && tg[num])
        return tg[num];

      return null;
   }

   // This function retrieves the source element
   // for the given event object:
   function get_event_source(e)
   {
     var event = e ? e : window.event;
     return event.srcElement ? event.srcElement : event.target;
   }

   // This function binds 'handler' function to the
   // 'eventType' event of the 'elem' element:
   function setup_event(elem, eventType, handler)
   {
     return (elem.attachEvent) ? elem.attachEvent("on" + eventType, handler) : ((elem.addEventListener) ? elem.addEventListener(eventType, handler, false) : false);
   }

   // By focusing the INPUT field we set the 'source'
   // to the newly focused field:
   function focus_keyboard(e)
   {
     source = get_event_source(e);
   }

   // This function slightly differs from one with the same name
   // in '4-test-fly' sample. Now it accepts not the id, but the
   // number (index in the INPUT elements array) of the INPUT field.
   function register_field(num)
   {
     var tg = document.getElementsByTagName("INPUT");

     if(tg && tg[num])
       setup_event(tg[num], "focus", focus_keyboard);
   }

   // This function enumerates and "registers" all INPUT fields
   // on the page:
   function register_input_fields()
   {
     var tg = document.getElementsByTagName("INPUT");

     if(tg)
     {
       for(var i = 0; i < tg.length; i++){
       	if(tg[i].getAttribute("type") != 'submit'){
         	register_field(i);
        }
       }
     }
   }

   function keyb_change(newLg)
   {
     if(!vkb)
     {
       // Note: all parameters, starting with 3rd, in the following
       // expression are equal to the default parameters for the
       // VKeyboard object. The only exception is 15th parameter
       // (flash switch), which is false by default.

       vkb = new VKeyboard("keyboard",    // container's id
                           keyb_callback, // reference to the callback function
                           false,          // create the numpad or not? (this and the following params are optional)
                           "",            // font name ("" == system default)
                           "13px",        // font size in px
                           "#000",        // font color
                           "red",        // font color for the dead keys
                           "#A63700",        // keyboard base background color
                           "#FFF",        // keys' background color
                           "#DDD",        // background color of switched/selected item
                           "#777",        // border color
                           "#CCC",        // border/font color of "inactive" key (key with no value/disabled)
                           "#FFF",        // background color of "inactive" key (key with no value/disabled)
                           "#F77",        // border color of the language selector's cell
                           true,          // show key flash on click? (false by default)
                           "#CC3300",     // font color during flash
                           "#FFCC00",     // key background color during flash
                           "#CC3300",     // key border color during flash
                           false,		// embed VKeyboard into the page?
                           newLg);     //The default language to use - Yoann




       // The very 1st (index == 0) field is "focused" by default:
       source = search_for_input_field(1);

       // Any INPUTs? Register them all!
       if(source) register_input_fields();

       opened = true;
     }
     else
     {
       opened = !opened;
       vkb.Show(opened);
     }
   }

   // Advanced callback function:
   function keyb_callback(ch)
   {
     var text = source, val = text.value;

     switch(ch)
     {
       case "BackSpace":
         if(val.length)
           DocumentSelection.deleteSelection(text);
         else
           DocumentSelection.deleteAtCursor(text);

         break;

       default:
         DocumentSelection.insertAtCursor(text, (ch == "Enter" ? (window.opera ? '\r\n' : '\n') : ch));
     }
   }

