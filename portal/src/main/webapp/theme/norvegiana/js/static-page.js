function toggleEditor(id) {
    if (!tinyMCE.get(id)) {
        tinyMCE.execCommand('mceAddControl', false, id);

    } else {
        tinyMCE.execCommand('mceRemoveControl', false, id);
    }
}

function editSource(id){
    tinyMCE.execCommand('mceRemoveControl', false, id);

    editAreaLoader.init({
        id : id		// textarea id
        ,syntax: "html"			// syntax to be uses for highgliting
        ,start_highlight: true		// to display with highlight mode on start-up
        ,replace_tab_by_spaces: false
        ,allow_toggle: false
        ,toolbar: "search, go_to_line, |, undo, redo, |, select_font,|, change_smooth_selection, highlight, reset_highlight, word_wrap, |, help"
    });

}

function editWysiwyg(){
    tinyMCE.init({
        mode : "textareas"
        ,theme : "advanced"
        ,plugins: "advimage,template,autoresize"
        ,fullscreen_new_window : true,
        fullscreen_settings : {
            theme_advanced_path_location : "top"
        }
        ,body_class : "container_12"
        ,relative_urls : false
        ,cleanup : false
        ,cleanup_on_startup : false
        ,apply_source_formatting : true
        ,indent_mode : "simple"
        ,preformatted : false
        ,object_resizing : false
        ,theme_advanced_toolbar_location : "top"
        ,theme_advanced_toolbar_align : "left"
        ,theme_advanced_buttons1 : "|,template,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect,|,bullist,numlist,|,undo,redo,|,link,unlink,anchor,|,image,|,forecolor,backcolor,|,removeformat,source"
        ,theme_advanced_buttons2 : ""
        ,theme_advanced_statusbar_location : "bottom"
        ,content_css : baseThemePath+"/css/reset.css,"+baseThemePath+"/css/text.css,"+baseThemePath+"/css/960.css,"+baseThemePath+"/css/screen.css"
        ,extended_valid_elements : "dl|dt|dd"
        ,external_image_list_url : portalName+"/_.img?javascript=true"
        ,template_external_list_url : baseThemePath+"/js/tiny-templates.js"
        ,external_link_list_url : portalName+"/_.dml?javascript=true"
    });
}
$(document).ready(function() {

    editSource("editor");

    $("button#edit-source").click(function(){
        editSource("editor");
    });
    $("button#edit-wysiwyg").click(function(){
        eAL.toggle("editor","off");
        editWysiwyg()
//        tinyMCE.execCommand('mceAddControl', false, "editor");
    });

});
