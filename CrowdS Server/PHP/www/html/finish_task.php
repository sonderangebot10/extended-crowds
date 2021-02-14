<?php
include("config.php");
include("firebase.php");
include("database.php");
include("notification.php");
include("push.php");
include("system_utils.php");
include(constant('REP'));
include(constant('REW'));

// file called by at command
// get id, file and type from args

$id = $argv[1];
$file = $argv[2];
$type = $argv[3];
$task_type = $argv[4];

$QC = 'QC_' . strtoupper($task_type);

$dbc = new DatabaseController();
$dbc->connect();

include(constant($QC));

if($type == "hit"){
    $task = $dbc->getHumanTaskFields($id, array("completed_time", "cost"));
}
else if($type == "sensor"){
    $task = $dbc->getSensorTaskFields($id, array("completed_time", "cost"));
}

if(empty($task['completed_time'])){
    $file_path = "task_update/" . $file;
    include($file_path);

    $fields['email'] = '';
    $fields['data'] = null;
    $fields['type'] = $task_type;
    $fields['id'] = $id . ':' . '0';
    
    $task_updator = getClass($file_path, $fields);
    $task_data = $task_updator->finishTask();
    $task_updator->sendNotification();
    
    $qc = getClass(constant($QC), $task_data);
    
    $result = null;
    $completed = false;
    
    if(!is_null($task_data['participants'])){
        $qc->calculateQuality();
        $qc->updateQuality();
        $result = $qc->setTaskResult();
        $completed = true;
        
        $rew = getClass(constant('REW'));
        if($task_data['task_type'] == 'sensor'){
            $rew->incrementSensorPoints($task_data['task_giver']);
        }
        else{
            $rew->incrementHitPoints($task_data['task_giver']);
        }
        
        $participants = explode(";", $task_data['participants']);
        foreach($participants as $str){
            list($index, $tid) = explode(":", $str);
            $rew->addPay($tid, $task["cost"]);
        }
        
    }
    
    $rep = getClass(constant('REP'), $task_data);
    $rep->calculateReputation();
    $rep->updateReputation();
    
    $members = array();
    foreach(explode(';',$task_data['members']) as $email){
        $user = $dbc->getUserFields($email, array('firebase'));
        $members[]['firebase']= $user['firebase'];
    }
    
    sendData($members, $id);
}

function sendData($group, $id){
    $payload = array();
    $payload['id'] = $id;
    $payload['type'] = 'expired';        

    $firebase = new Firebase();
    $push = new Push();

    $title = "";
    $message = "";

    $push->setTitle($title);
    $push->setMessage($message);
    $push->setIsBackground(FALSE);

    for ($i = 0; $i < count($group); $i++) {
        $push->setPayload($payload);
        $json = $push->getPush();
        $firebase->send($group[$i]['firebase'], $json);
    }
}
?>