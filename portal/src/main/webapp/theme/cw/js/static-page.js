$(document).ready(function() {
    tinyMCE.init({
        mode : "textareas",
        theme : "advanced",
        plugins: "advimage,template,fullscreen",
        fullscreen_new_window : true,
        fullscreen_settings : {
            theme_advanced_path_location : "top"
        },

        body_class : "${portalColor}",
        relative_urls : false,
        theme_advanced_toolbar_location : "top",
        theme_advanced_toolbar_align : "left",
        theme_advanced_buttons1 : "|,template,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect,|,bullist,numlist,|,undo,redo,|,link,unlink,anchor,|,image,|,forecolor,backcolor,|,removeformat,code,|,fullscreen",
        theme_advanced_buttons2 : "",
        theme_advanced_statusbar_location : "bottom",
        content_css : baseThemePath+"/css/reset-text-grid.css,"+baseThemePath+"/css/type.css,"+baseThemePath+"/css/screen.css,"+baseThemePath+"/css/colors.css",
        external_image_list_url : "/${portalName}/_.img?javascript=true",
        template_external_list_url : baseThemePath+"/js/tiny-templates.js"
    })

});
function toggleEditor(id) {
    if (!tinyMCE.get(id)) {
        tinyMCE.execCommand('mceAddControl', false, id);
    } else {
        tinyMCE.execCommand('mceRemoveControl', false, id);
    }
}