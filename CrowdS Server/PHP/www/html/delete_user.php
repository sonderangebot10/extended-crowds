<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();

// email is sent from the mobile app
$id = $dbc->escapeString($_POST["email"]);

// send query to delete user
if ($dbc->deleteUser($id)) {
    $reply = array('status' => "OK");
} 
else {
    $reply = array('status' => "ERROR", 'reason' => "could not delete user");
}

// send reply
echo json_encode($reply);
?>
