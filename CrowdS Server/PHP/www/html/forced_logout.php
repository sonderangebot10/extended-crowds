<?php
include("config.php");
include("firebase.php");
include("database.php");
include("push.php");

$dbc = new DatabaseController();
$dbc->connect();

$active = $dbc->getActiveUsers();
$payload = array();
$payload['type'] = "forced_logout";

$firebase = new Firebase();
$push = new Push();

$title = "";
$message = "";

$push->setTitle($title);
$push->setMessage($message);
$push->setIsBackground(FALSE);

for($i = 0; $i < count($active); $i++) {
    $push->setPayload($payload);
    $json = $push->getPush();
    $firebase->send($active[$i]['firebase'], $json);
    $_POST["email"] = $active[$i]['email'];
    include("logout.php");
}

?>