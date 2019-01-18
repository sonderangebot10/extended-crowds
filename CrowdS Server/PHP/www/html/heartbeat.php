<?php
include("config.php");
include("database.php");
include("firebase.php");
include("push.php");

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

exec("echo 'php /var/www/html/heartbeat_check.php ".$id."' | at ".$future." 2>&1");

?>