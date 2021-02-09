<?php
include("config.php");
include("database.php");
include("system_utils.php");

$dbc = new DatabaseController();
$dbc->connect();

// sent from mobile app
$email = $dbc->escapeString($_POST["email"]);

$dir = new DirectoryIterator(ROOT_PATH . "html/task_history");

$tasks = array();

foreach ($dir as $fileinfo) {
    if (!$fileinfo->isDot()) {
        $file = $fileinfo->getPathname();
        include($file);
        $th = getClass($file, $email);
        $ct = $th->getCompletedTasks();
        $tasks = array_merge($tasks, $ct);
    }
}

if(!empty($tasks)){
    // Sort the array with completed time
    foreach ($tasks as $key => $val) {
        $time[$key] = $val['completed'];
    }
    array_multisort($time, SORT_DESC, $tasks);
}

// if there is prefix img= in tasks[data] then send the string from file
$task = $tasks[0];
if(substr( $task['answer'], 0, 4 ) === "img=")
{
    $filename = $task['answer'];
    $fp = fopen($filename, "r");

    $content = fread($fp, filesize($filename));
    fclose($fp);

    $task['answer'] = "img=".$content;
}

$tasks = [$task];
$reply = array('status' => "OK", 'tasks' => $tasks);
echo json_encode($reply);

?>