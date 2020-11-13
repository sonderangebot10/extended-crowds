<?php
include_once("config.php");
include_once("database.php");
include_once ("system_utils.php");

$dbc = new DatabaseController();
$dbc->connect();
$server_os = getOSName();

// id is sent from the mobile app
$id = $dbc->escapeString($_POST["email"]);

$heartbeat = $dbc->getUserFields($id, array("heartbeat"))["heartbeat"];

// remove scheduled heartbeats
list($old_id,$timestamp) = explode(":", $heartbeat);
$deletetaskcommand = "";
if ($server_os === "Windows") {
    $deletetaskcommand = "schtasks.exe /Delete /tn ".$old_id." /f 2>&1";
} else if ($server_os === "Linux") {
    $deletetaskcommand = "atrm ".$old_id." 2>&1";
} else {
    error_log("Unsupported OS.");
}
exec($deletetaskcommand);

// update logout time
$dbc->updateUser($id, array("logout_time", "active"), array("NOW()", "0"));

// encode and send reply
$reply = array('status' => "OK");	
echo json_encode($reply);
?>
