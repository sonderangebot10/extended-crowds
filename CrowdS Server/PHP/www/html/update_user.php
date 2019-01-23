<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();

// data sent from the mobile app
$id = $dbc->escapeString($_POST["email"]);
$password = $dbc->escapeString($_POST["password"]);
$username = $dbc->escapeString($_POST["username"]);

// if called by a webpage, the br tag might still be present
$username = str_replace('<br>', "", $username);

// admin premissions can only be set/updated by the server
// if called from the device, the header will not contain the admin key
if(isset($_POST['admin'])){
    $admin = $dbc->escapeString($_POST["admin"]);
}
else
    $admin = 0;

// make query to update database
if ($dbc->updateUser($id, array("admin","password", "username"), array("'$admin'","'$password'","'$username'"))){
    $reply = array('status' => "OK");
} 
else {
    $reply = array('status' => "ERROR", 'reason' => "could not update user");
}

// send reply
echo json_encode($reply);
?>
