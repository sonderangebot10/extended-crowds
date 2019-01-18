<?php
include_once("config.php");
include_once("database.php");

$dbc = new DatabaseController();
$dbc->connect();

//$id = "jowalle@kth.se";

// id is sent from the mobile app
$id = $dbc->escapeString($_POST["email"]);

$heartbeat = $dbc->getUserFields($id, array("heartbeat"))["heartbeat"];

list($atid,$timestamp) = explode(":", $heartbeat);
exec("atrm ".$atid." 2>&1");

// update logout time
$dbc->updateUser($id, array("logout_time", "active"), array("NOW()", "0"));

// encode and send reply
$reply = array('status' => "OK");	
echo json_encode($reply);
?>
