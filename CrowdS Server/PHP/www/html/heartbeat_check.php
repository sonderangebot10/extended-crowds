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

    // OS
    $server_os = getOSName();
    $command = "";
    if ($server_os === "Windows") {
        $taskid = uniqid($id);
        $command = "schtasks.exe /Create /st ".$future." /tn ".$taskid." /sc ONCE /tr \"php ".ROOT_PATH."html\heartbeat.php ".$id."\" 2>&1";
    } else if ($server_os === "Linux") {
        $command = "echo 'php ".ROOT_PATH."html/heartbeat.php ".$id."' | at ".$future." 2>&1";
    } else {
        error_log("Unsupported OS.");
    }
    exec($command);
}
else{
    $dbc->updateUser($id, array("active"), array("0"));
}

?>