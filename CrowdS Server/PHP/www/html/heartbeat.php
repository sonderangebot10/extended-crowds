<?php
include("config.php");
include("database.php");
include("firebase.php");
include("push.php");
include("system_utils.php");

$id = $argv[1];

$dbc = new DatabaseController();
$dbc->connect();

$fb = $dbc->getUserFields($id, array("firebase"))["firebase"];

$firebase = new Firebase();
$push = new Push();

$title = "heartbeat";
$message = "";

$push->setTitle($title);
$push->setMessage($message);
$push->setIsBackground(FALSE);

$payload['type'] = "heartbeat";
$push->setPayload($payload);
$json = $push->getPush();
$firebase->send($fb, $json);

// check the heartbeat in the future
$future = date('H:i', strtotime("+ ".HB_CHECK_TIME));

// OS
$os = getOSName();
$command = "";
if ($os === "Windows") {
    $taskid = uniqid($id);
    $command = "schtasks.exe /Create /st ".$future." /tn ".$taskid." /sc ONCE /tr \"php ".ROOT_PATH."html\heartbeat_check.php ".$id."\" 2>&1";
} else if ($os === "Linux") {
    $command = "echo 'php ".ROOT_PATH."html/heartbeat_check.php ".$id."' | at ".$future." 2>&1";
} else {
    error_log("Unsupported OS.");
}
error_log($command);
exec($command);

?>