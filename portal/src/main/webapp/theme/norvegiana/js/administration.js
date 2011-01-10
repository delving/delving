function removeUser(email){
        $.ajax({
          type: 'POST',
          url: portalName+'/remove-user.ajax',
          data: "email="+email,
          success: function(data){
              showMessage("success","User: "+email+ " has been successfully removed");
              $("#tbl-users-found").hide();
              showUserList();
          },
          error: function(xhr, ajaxOptions, thrownError){
            showMessage("error","An error has occured: "+xhr.status);
          }
        });
}

function showUserList(){
         $.ajax({
        url: "/portal/list-users.ajax",
        type: "GET",
        dataType: "XML",
        success: function(xml) {
            var openTable = "<table id='user-table' class='tablesorter zebra'>";
            var closeTable = "</tbody></table>"
            var userDataRow = "<thead><tr><th>Emailadres</th><th>Current role</th>";
            userDataRow += "<th>User name</th><th>Registration date</th><th>Last login date</th></thead><tbody>";
            var userRoleSelect = '<select name="newRole">';
            userRoleSelect += '<option>Choose a role</option>';

            $(xml).find('user').each(function(){
                var _email = $(this).attr('email');
                var _role = $(this).attr('role');
                var _userName = $(this).attr('userName');
                var _regDate = $(this).attr('registrationDate');
                var _lastLoginDate = $(this).attr('lastLoginDate');
                userDataRow += '<tr><td class="searchParam"><a href="#" class="searchParam"><span class="ui-icon ui-icon-person"></span>'+_email+'</a></td><td>'+_role+'</td>';
                userDataRow += '<td>'+_userName+'<td>'+_regDate+'</td><td>'+_lastLoginDate+'</td></tr>';
            });
            $('div#all-users-list').html("<h4>Existing user list</h4>"+openTable+userDataRow+closeTable);
            $("#user-table").tablesorter();
            styleUIButtons();
            $('td.searchParam, a.searchParam').click(function(){
                $('input#searchPattern').val($(this).text());
                $('form#search-form').submit();
                return false;
            })
        },
        error: function(xhr) {
            showMessage("fail","Something went wrong<br/>error: "+xhr.status );
        }
    });
}
$(document).ready(function(){
    showUserList();
    $("form.set-form").submit(function(){
        //get the target row
        var targetRow = "#row_"+$(this).next('input.formIndex').val();
        if($(targetRow+" select.newRole").val()=="NONE"){
            showMessage("error","You must choose a user role");
            return false;
        }
    })

})

