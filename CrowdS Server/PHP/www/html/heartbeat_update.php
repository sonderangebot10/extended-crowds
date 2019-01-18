<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();

$id = $dbc->escapeString($_POST["email"]);
//$lat = $dbc->escapeString($_POST["lat"]);
//$lng = $dbc->escapeString($_POST["lng"]);

$heartbeat = $dbc->getUserFields($id, array("heartbeat"))["heartbeat"];

list($atid, $timestamp) = explode(":", $heartbeat);

$now = strtotime("now");
$new_hb = $atid.':'.$now;

$dbc->updateUser($id, array("heartbeat"), array("'$new_hb'"));
    
?>