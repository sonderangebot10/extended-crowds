<?php
include("config.php");
include("firebase.php");
include("database.php");
include("notification.php");
include("push.php");
include("system_utils.php");
include(constant('TA'));

define('TTL_HIT', 180); 

// radius of search area (km)
$radius = 0.025;
$closest = 0;

$dbc = new DatabaseController();
$dbc->connect();

// OS
$server_os = getOSName();

$file_path = "task_creation/imagery.php";
include($file_path);

// get all data from POST
foreach ($_POST as $key => $value){
    $fields[$key] = $dbc->escapeString($value);
}

$tasks = $dbc->getHumanTaskAll($fields['id']);
$task = $tasks[0];

// get all online users
$active = $dbc->getActiveUsers();

// remove the task giver from active pool
for($i = 0; $i < count($active); $i++){
    if($active[$i]['email'] == $task['task_giver']){
        unset($active[$i]);
        $active = array_values($active);
    }

    if(strpos($task['participants'], $active[$i]['email']) !== false){
        unset($active[$i]);
        $active = array_values($active);
    }
}

// if there are any active users, find those in close proximity to the task area
if(count($active) > 0){
    $in_prox_users = getUserInProximityTo($active, $task['latitude'], $task['longitude']);
    
    if(count($in_prox_users) > 0){
				// Use the selected Task Allocation to find the set of providers
        $ta = getClass(constant('TA'),$in_prox_users);
        $users = $ta->getUsers();
        $cost = $ta->getCost();
        $fields['cost'] = $cost;
    }
    else{
        $users = array();
    }
		
    // the number of devices the task will be distributed to
    $distributed = count($users);
}

else{
    $distributed = 0;
}

// no devices in proximity found
if($distributed == 0){
        $reply = array('status' => "ERROR", 'reason' => "No devices found");
}
else {
    $fields['distributed'] = $distributed;
    $fields['email'] = $task['task_giver'];
    $fields['description'] = "-";
    $fields['lat'] = $task['latitude'];
    $fields['lng'] = $task['longitude'];
    $fields['question'] = $task['question'];
    $fields['hit_type'] = "image";
    
    // create the task using the specific task creation file for each task type
    // can be found in the task_creation folder
    $task_creator = getClass($file_path, $fields);
    $id = $task_creator->insertTask($users);
    $task_creator->sendData($users);
    
    $future = date('H:i', strtotime("+ ".TTL_HIT." minutes"));
    if ($server_os === "Windows") {
        $taskid = uniqid($id);
        exec("schtasks.exe /Create /st ".$future." /tn ".$taskid." /sc ONCE /tr \"php".ROOT_PATH."html\\finish_task.php ".$id." ".$fields['file']." ".$fields['type']." ".$fields['hit_type']."\" 2>&1");
    } else if ($server_os === "Linux") {
        exec("echo 'php ".ROOT_PATH."html/finish_task.php ".$id." ".$fields['file']." ".$fields['type']." ".$fields['hit_type']."' | at ".$future." 2>&1");
    } else {
        error_log("Cannot execute heartbeat due to unsupported OS.");
    }
    $task_creator->sendNotification($users);
    
    // everything went fine!
    $reply = array('status' => "OK");

    $dbc->deleteHumanTaskAll($fields['id']);
}

// send reply
echo json_encode($reply);


// help function to find users close to the target area
function getUserInProximityTo($users, $latTo, $lngTo){ 
$prox = array();
$in_prox = array();
$rad = M_PI / 180;
$max_radius = 1;
global $radius;
global $closest;

//Calculate distance from latitude and longitude
//Distance in km
for ($i = 0; $i < count($users); $i++) {
    
    $lngFrom = $users[$i]['longitude'];
    $latFrom = $users[$i]['latitude'];
    $theta = $lngFrom - $lngTo;
    $dist = sin($latFrom * $rad) * sin($latTo * $rad) +  cos($latFrom * $rad) * cos($latTo * $rad) * cos($theta * $rad);
    $res = acos($dist) / $rad * 60 *  1.852;
    
    $prox[''.$res] = $users[$i];
    
}

ksort($prox);
$closest = min(array_keys($prox)); 

while($radius <= $max_radius){
    $in_prox = array();
    
    foreach($prox as $key => $val){
        if($key <= $radius){
            $in_prox[] = $val;
        }
        else{
            break;
        }
    }
    
    $radius = $radius * 2;
    
    if(count($in_prox) >= MAX_DEVICES){
        break;
    }
}

// remove the last multiplication
$radius = $radius / 2;

return $in_prox;
}  

?>