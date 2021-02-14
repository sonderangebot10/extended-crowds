<?php
include("config.php");
include("firebase.php");
include("database.php");
include("notification.php");
include("system_utils.php");
include(constant('REP'));
include(constant('REW'));

$dbc = new DatabaseController();
$dbc->connect();

// include the file that handles the update
$file_path = "task_update/" . $_POST['file'];
include($file_path);

// get all data from POST
foreach ($_POST as $key => $value){
    $fields[$key] = $dbc->escapeString($value);
}

// include quality control file
$QC = 'QC_' . strtoupper($_POST['type']);
include(constant($QC));

// update the task
$task_updator = getClass($file_path, $fields);
$task_updator->minusPoint();

// send reply
$reply = array('status' => "OK");
echo json_encode($reply);

?>