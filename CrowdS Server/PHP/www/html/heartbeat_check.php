<?php
include("config.php");
include("database.php");

$id = $argv[1];

$dbc = new DatabaseController();
$dbc->connect();

$hb = $dbc->getUserFields($id, array("heartbeat"))["heartbeat"];

list($atid, $hbtime) = explode(":", $hb);
$time = strtotime("-".HB_CHECK_TIME);

if($hbtime >= $time){
    // create heartbeat in the future
    $future = date('H:i', strtotime("+".HB_TIME));

    exec("echo 'php /var/www/html/heartbeat.php ".$id."' | at ".$future." 2>&1");
}
else{
    $dbc->updateUser($id, array("active"), array("0"));
}

?>