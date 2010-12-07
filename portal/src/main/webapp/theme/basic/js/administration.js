$(document).ready(function(){
     $.ajax({
        url: portalName+"/users.ajax",
        type: "GET",
         dataType: "xml",
        success: function(xml) {
            var openTable = "<table id='user-table' class='tablesorter zebra'>";
            var closeTable = "</tbody></table>"
            var userDataRow = "<thead><tr><th>Emailadres</th><th>Current role</th><th>First name</th><th>Last name</th>";
            userDataRow += "<th>User name</th><th>Registration date</th><th>Last login date</th></thead><tbody>";
            var userRoleSelect = '<select name="newRole">';
            userRoleSelect += '<option>Choose a role</option>';

            $(xml).find('user').each(function(){
                var _email = $(this).attr('email');
                var _role = $(this).attr('role');
                var _firstName = $(this).attr('firstName');
                var _lastName = $(this).attr('lastName');
                var _userName = $(this).attr('userName');
                var _regDate = $(this).attr('registrationDate');
                var _lastLoginDate = $(this).attr('lastLoginDate');
                userDataRow += '<tr><td class="searchParam"><a href="#" class="searchParam"><span class="ui-icon ui-icon-person"></span>'+_email+'</a></td><td>'+_role+'</td><td>'+_firstName+'</td><td>'+_lastName+'</td>';
                userDataRow += '<td>'+_userName+'<td>'+_regDate+'</td><td>'+_lastLoginDate+'</td></tr>';
            });
            $('div#all-users-list').html("<h4>Existing user list</h4>"+openTable+userDataRow+closeTable);
            $("#user-table").tablesorter();
            styleUIButtons();
            $('td.searchParam, a.searchParam').click(function(){
                $('input#searchPattern').val($(this).text());
                $('form#search-form"').submit();
                return false;
            })
        },
        error: function(xml) {
            alert(xml);
            showMessage("fail","Something went wrong");
        }
    });
    $('button#rem-user').click(function(){
        var toRemove = $('input#userEmail').val();
        var confirmation = confirm("Gebruiker: "+toRemove +" verwijderen ?");
        if(confirmation){
            $.ajax({
              type: 'POST',
              url: portalName+'/user-remove.ajax',
              data: "email="+toRemove,
              success: function(){
                  window.location.href=window.location.href;
              }
            });
            return false;
        } else {
            return false;
        }
    });

    $("form.set-form").submit(function(){
        if($("select.newRole").val()=="NONE"){
            showMessage("error","You must choose a user role");
            return false;
        }
    })

})

