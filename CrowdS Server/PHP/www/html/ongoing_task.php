<?php
include("config.php");
include("database.php");
include("system_utils.php");

$dbc = new DatabaseController();
$dbc->connect();

// sent from mobile app
$email = $dbc->escapeString($_POST["email"]);

$dir = new DirectoryIterator(ROOT_PATH . "html/task_ongoing");

$tasks = array();

foreach ($dir as $fileinfo) {
    if (!$fileinfo->isDot()) {
        $file = $fileinfo->getPathname();
        include($file);
        $th = getClass($file, $email);
        $ct = $th->getOngoingTasks();
        $tasks = array_merge($tasks, $ct);
    }
}

if(!empty($tasks)){
    // Sort the array with completed time
    foreach ($tasks as $key => $val) {
        $time[$key] = $val['created'];
    }
    array_multisort($time, SORT_DESC, $tasks);
}


$reply = array('status' => "OK", 'tasks' => $tasks);
echo json_encode($reply);



?>