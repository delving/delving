$(document).ready(function(){
     $.ajax({
        url: portalName+"/users.ajax",
        type: "GET",
         dataType: "xml",
        success: function(xml) {
            var openTable = "<table id='user-table' class='tablesorter'>";
            var closeTable = "</tbody></table>"
            var userDataRow = "<thead><tr><th>Emailadres</th><th>Huidige Rol</th><th>Voornaam</th><th>Achternaam</th>";
            userDataRow += "<th>Gebruikersnaam</th><th>Registratiedatum</th><th>Laatste inlogdatum</th></thead><tbody>";
            var userRoleSelect = '<select name="newRole">';
            userRoleSelect += '<option>Kies een rol</option>';

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
            $('div#all-users-list').html("<h2>Gebruikers lijst</h2>"+openTable+userDataRow+closeTable);
            $("#user-table").tablesorter({
                widgets: ['zebra']});
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
              success: function(data){   
                  window.location.href=window.location.href;
              }
            });
            return false;
        } else {
            return false;
        }
    })
})