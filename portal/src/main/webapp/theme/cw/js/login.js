var futdate = new Date()		//Get the current time and date
var expdate = futdate.getTime()  //Get the milliseconds since Jan 1, 1970
expdate += 3600*1000  //expires in 1 hour(milliseconds)

function showForgotPassword(){
    $('#request-password').show(2000);
    //$("#login-div").css("display","none");
    $.cookie('request-pas', '1', { expires: expdate });
}

$().ready(function() {

    if($.cookie('request-pas')){
        $('#request-password').css("display","block");
       // $("#login-div").css("display","none");
    }

//    $("#loginForm").validate({
//            rules: {j_username: "required",j_password: "required"},
//            messages: {j_username: "",j_password: ""}
//     });

    $("form#forgotemailForm").validate({
        debug: true,
            rules: {
                forgot_email: {
                  required: true,
                  email: true
                }
            }
     });

//x
});