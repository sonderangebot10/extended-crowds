<?php
    include('session.php');
    include('database.php');

    $dbc = new DatabaseController();
    $dbc->connect();
?>

<html>
<title>CS Project</title>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Lato">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Montserrat">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    
<style>
body,h1,h2,h3,h4,h5,h6 {font-family: "Lato", sans-serif}
.w3-bar,h1,button {font-family: "Montserrat", sans-serif}
.fa-anchor,.fa-coffee {font-size:200px}
    
.selected {
    background-color: yellow;
}

.non-editable {
    background-color: white;
    color: black;
}
    
.editable {
    background-color: black;
    color: white;
}

.delete-img{
    float:right;
    visibility: hidden;
    cursor: pointer;
}
.edit-img{
    float:right;
    visibility: hidden;
    cursor: pointer;
}
    
tr:hover {
    background-color: #ddd;
    color: black;
}
    
a {
    text-decoration: none;
    display: inline-block;
    padding: 8px 16px;
}

a:hover {
    background-color: #ddd;
    color: black;
}

.previous {
    background-color: black;
    color: white;
}

.next {
    background-color: black;
    color: white;
}

.round {
    border-radius: 50%;
}
</style>
    
<head>
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>
    
<script language="JavaScript">
    var last_index = -1;
    var current_index = -1;
    var last_icon;
    var users;
    var table_size = 25;
    var min_index = 0;
    
    $(document).on('click', '#userTable tr', function(){     
        current_index = $(this).index() + min_index;     
        if(current_index != last_index){
            $(this).addClass("selected").siblings().removeClass("selected");
            $(this).find("td img").css('visibility','visible');
            showAccount(current_index);
            switchRow(last_index+1);
            last_index = current_index;
        }
    });
    
    
    function showAccount(index) {
        $('#admin').html(users[index]['admin']);
        $('#username').html(users[index]['username']);
        $('#password').html(users[index]['password']);
    }
    
    function switchRow(row){
        if(row > 0){
            $('#userTable').find('tr:eq(' +row+') td img').css('visibility','hidden');
            $('.delete-img').show();
            if(typeof last_icon != 'undefined') last_icon.attr('src','cogwheel-icon.png');
            toggleEdit(false);
        }
    }
    
    function getAllUserInfo(){
        <?php
                $data = $dbc->getUserAll("");
                echo "users = " . json_encode($data) . ";";
        ?>
    }
    
    function getUser(email){
        $.post('find_user.php', {email: $("#searchbar").val()}, function(result){
            users = JSON.parse(result);
            min_index = 0;
            last_index = -1;
            createTable(min_index);
        });
    }
    
    function toggleEdit(edit){
        $('#account').find('td').each (function( column, td) {
            $(td).prop('contenteditable', edit);
            if(edit)
                $(td).addClass("editable");
            else
                $(td).removeClass("editable");
        });
    }
    
    function editDone(){
        toggleEdit(false);
        $('.delete-img').show();
        last_icon.attr('src','cogwheel-icon.png');
    }
    
    
    function deleteDone(){
        $('#userTable').find('.selected').remove();
        users.splice(current_index, 1);
        last_index = -1;
        $('#userTable').find("tr:eq(1)").click();
    }
    
    function selectSize(button){
        if(button.id == "b25") table_size = 25;
        else if(button.id == "b50") table_size = 50;
        else table_size = 100;
        min_index = 0;
        last_index = -1;
        createTable(min_index);
    }
    
    function nextTable(){
        if(min_index + table_size < users.length){
            min_index = min_index + table_size;
            createTable(min_index);
        }
    }
    
    function previousTable(){
        if(min_index != 0){
            if(min_index - table_size < 0)
                min_index = 0;
            else
                min_index = min_index - table_size;

            createTable(min_index);
        }
    }
    
    function createTable(start){
        $('#userTable').find('tbody').empty();
        for (i = start; i < start + table_size && i < users.length; i++) { 
            $('#userTable').find('tbody').append(
                "<tr>" +
                    "<td>"+users[i]['email']+"</td>" +
                    "<td class='delete-img'><img src='delete-icon.png' alt='' height=25 width=25/></td>" +
                    "<td class='edit-img'><img src='cogwheel-icon.png' alt='' height=25 width=25/></td>" +
                "</tr>" );
        }
        $('#userTable').find("tr:eq(1)").click();
    }
    
    $(document).ready(function() {
        getAllUserInfo();
        createTable(0);
        $('#container').append( "<td><a onclick='previousTable()' class='previous'>&laquo; Previous</a></td></tr>");
        $('#container').append( "<td><a onclick='nextTable()' class='next'>Next &raquo;</a></td></tr>");
        $('#userTable').find("tr:eq(1)").click();
        
        $('#userTable').on("click", ".delete-img", function () {
            var r = confirm("Delete user: " + $users[current_index]['email'] + "?");
            if (r == true) {
                $.post('delete_user.php', {email: $users[current_index]['email']}, deleteDone());
            }
        });
        
        $('#userTable').on("click", "img", function () {
            if($(this).attr('src') == 'delete-icon.png'){
               var r = confirm("Delete user: " + users[current_index]['email'] + "?");
                if (r == true) {
                    $.post('delete_user.php', {email: users[current_index]['email']}, deleteDone());
                } 
            }
            else if($(this).attr('src') == 'cogwheel-icon.png'){
                $(this).attr('src','checkmark-icon.png');
                $('.delete-img').hide();
                row = current_index + 1;
                last_icon = $(this);
                toggleEdit(true);
            }
            else{
                users[current_index]['admin'] = $('#admin').html();
                users[current_index]['username'] = $('#username').html();
                users[current_index]['password'] = $('#password').html();
                $.post('update_user.php', {email: users[current_index]['email'],
                                           admin: $('#admin').html(),
                                           username: $('#username').html(),
                                           password: $('#password').html()},
                       editDone());
            }
        });
    });
</script>
</head>
    
<body>

<!-- Navbar -->
<div class="w3-top">
  <div class="w3-bar w3-black w3-card-2 w3-left-align w3-large">
    <a class="w3-bar-item w3-button w3-hide-medium w3-hide-large w3-right w3-padding-large w3-hover-white w3-large w3-red" href="javascript:void(0);" onclick="myFunction()" title="Toggle Navigation Menu"><i class="fa fa-bars"></i></a>
    <a href="main.php" class="w3-bar-item w3-button w3-padding-large w3-hover-white">Home</a>
    <a href="account.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-white">Account</a>
    <a href="server_settings.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Settings</a>
    <a href="active_users.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Online</a>
    <a href="admin_logout.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Logout</a>
  </div>
</div>
    
<div class="w3-panel w3-padding-64">
    <div class="w3-row-padding " style="margin:0 -16px">
      <div class="w3-third" id="container">
        <table class="w3-table w3-white" id="userTable">
            <thead>
                <tr>
                    <input id="searchbar" type="email" style="width:73%" placeholder="Enter email for user"/>
                    <button id="b100" onclick="selectSize(this)" style="float:right">100</button>
                    <button id="b50" onclick="selectSize(this)" style="float:right">50</button>
                    <button id="b25" onclick="selectSize(this)" style="float:right">25</button>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
      </div>
    <div class="w3-twothird">
        <table class="w3-table w3-white">
            <thead>
                <tr>
                    <td>Username</td>
                    <td>Admin</td>
                    <td>Password</td>
                </tr>
            </thead>
            <tbody>
                <tr id="account">
                    <td id="username"></td>
                    <td id="admin"></td>
                    <td id="password"></td>
                </tr>
            </tbody>
        </table>
      </div>
    </div>
</div>
    
<script>
// Used to toggle the menu on small screens when clicking on the menu button
function myFunction() {
    var x = document.getElementById("navDemo");
    if (x.className.indexOf("w3-show") == -1) {
        x.className += " w3-show";
    } else { 
        x.className = x.className.replace(" w3-show", "");
    }
}
    
$('#searchbar').bind("enterKey",function(e){
   getUser(document.getElementById('searchbar').value);
});
$('#searchbar').keyup(function(e){
    if(e.keyCode == 13)
    {
        $(this).trigger("enterKey");
    }
});
</script>

</body>
</html>