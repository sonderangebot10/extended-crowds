<?php
    include('session.php');
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

input {
    text-decoration: none;
    display: inline-block;
    padding: 8px 16px;
}

input:hover {
    background-color: #ddd;
    color: black;
}
    
.save {
background-color: black;
color: white;
}
</style>
    
<head>
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>
    
<script language="JavaScript">

    $(document).ready(function() {
        
        // select saved database file as default
        <?php
            include_once('config.php');
            echo "db = " . json_encode($dynamic_settings['DB']) . ";" .
                 "qc = " . json_encode($dynamic_settings['QC_SINGLE']) . ";";
        ?>
        //$('#container select=database_file').val(db);
        //$('#container select=quality_control_file').val(qc);
    });
    
    function saveSettings() {
        alert("disabled for now");
        //$.post('update_server_settings.php', {database_file: $("#database_file option:selected").text()});
        return false;
    }
    
    function forcedLogout(){
        var jqxhr = $.post("forced_logout.php")
          .done(function() {
            alert( "finished!" );
          })
          .fail(function() {
            alert( "error!!" );
          });
    }
               
</script>
</head>
<body>

<!-- Navbar -->
<div class="w3-top">
  <div class="w3-bar w3-black w3-card-2 w3-left-align w3-large">
    <a class="w3-bar-item w3-button w3-hide-medium w3-hide-large w3-right w3-padding-large w3-hover-white w3-large w3-red" href="javascript:void(0);" onclick="myFunction()" title="Toggle Navigation Menu"><i class="fa fa-bars"></i></a>
    <a href="main.php" class="w3-bar-item w3-button w3-padding-large w3-hover-white">Home</a>
    <a href="account.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Account</a>
    <a href="server_settings.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-white">Settings</a>
    <a href="active_users.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Online</a>
    <a href="admin_logout.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Logout</a>
  </div>
</div>
    

<div class="w3-panel w3-padding-64">
    <div class="w3-row-padding " style="margin:0 -16px">
        <div class="w3-third" id="container">
            <form onsubmit="return saveSettings()">
                <table class="w3-table w3-white">
                    <thead>
                        <tr>
                            <th>type</th>
                            <th>file</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td>Database</td>
                            <td>
                                <select id="database_file">
                                <?php 
                                    foreach(glob(dirname(__FILE__) . '/database/*') as $filename){
                                       $filename = basename($filename);
                                       echo "<option value='" . $filename . "'>".$filename."</option>";
                                   }
                                ?>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td>Task allocation</td>
                            <td>
                            <select id="task_allocation_file">
                            <?php 
                                foreach(glob(dirname(__FILE__) . '/task_allocation/*') as $filename){
                                   $filename = basename($filename);
                                   echo "<option value='" . $filename . "'>".$filename."</option>";
                               }
                            ?>
                            </select>
                            </td>
                        </tr>
                        <tr>
                            <td>Quality control</td>
                            <td>
                            <select id="quality_control_file">
                            <?php 
                                foreach(glob(dirname(__FILE__) . '/quality_control/*') as $filename){
                                   $filename = basename($filename);
                                   echo "<option value='" . $filename . "'>".$filename."</option>";
                               }
                            ?>
                            </select>
                            </td>
                        </tr>
                        <tr>
                            <td>Reputation</td>
                            <td>
                            <select id="reputation_file">
                            <?php 
                                foreach(glob(dirname(__FILE__) . '/reputation/*') as $filename){
                                   $filename = basename($filename);
                                   echo "<option value='" . $filename . "'>".$filename."</option>";
                               }
                            ?>
                            </select>
                        </td>
                        </tr>
                        <tr>
                            <td>Reward</td>
                            <td>
                            <select id="reward_file">
                            <?php 
                                foreach(glob(dirname(__FILE__) . '/reward/*') as $filename){
                                   $filename = basename($filename);
                                   echo "<option value='" . $filename . "'>".$filename."</option>";
                               }
                            ?>
                            </select>
                        </td>
                        </tr>
                    </tfoot>
                </table>
            <input type="submit" class='save' style="margin:0 15px" value="Save"/>
            </form>
        </div>
        <div class="w3-third">
            <button id="fl" onclick="forcedLogout()">forced logout</button>
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
</script>

</body>
</html>