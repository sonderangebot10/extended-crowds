<?php
include("config.php");
include("database.php");
include("system_utils.php");
include(constant('REW'));

$dbc = new DatabaseController();
$dbc->connect();

foreach ($_POST as $key => $value){
    $fields[$key] = $dbc->escapeString($value);
}

// get the reward management file and collect reward data
$rew = getClass(constant('REW'));
$points = $rew->getPoints($fields['email']);

// send reply
$reply = array('status' => "OK", 'points' => $points);
echo json_encode($reply);

?>