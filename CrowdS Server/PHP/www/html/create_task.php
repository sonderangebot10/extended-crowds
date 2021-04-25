<?php
include("config.php");
include("firebase.php");
include("database.php");
include("notification.php");
include("push.php");
include("system_utils.php");
include(constant('TA'));

// TTL for sensor and hit tasks
define('TTL_SENSOR', 10);
define('TTL_HIT', 180); 

// radius of search area (km)

// RADIUS KM
$radius = 5000;
$closest = 0;

$dbc = new DatabaseController();
$dbc->connect();

// OS
$server_os = getOSName();

$file_path = "task_creation/" . $_POST['file'];
include($file_path);

// get all data from POST
foreach ($_POST as $key => $value){
    $fields[$key] = $dbc->escapeString($value);
}

// get all online users
$active = $dbc->getActiveUsers();

// remove the task giver from active pool
for($i = 0; $i < count($active); $i++){
    if($active[$i]['email'] == $fields['email']){
        unset($active[$i]);
        $active = array_values($active);
        break;
    }
}

// filter away users without required sensor if its a sensor task
if($fields['type'] == "sensor"){
    $active = $dbc->filterUsersWithSensor($active, $fields['sensor']);
}

// if there are any active users, find those in close proximity to the task area
if(count($active) > 0){
    $in_prox_users = getUserInProximityTo($active, $fields['lat'], $fields['lng']);
    
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
    
    if($fields['type'] != "sensor"){
        $reply = array('status' => "ERROR", 'reason' => "No devices found within " . $radius ." km of (".round($fields['lat'],4).",".round($fields['lng'],4)."), closest user is " . round($closest,2) . " km away");
    }
    else if(count($active) > 0){
        $reply = array('status' => "ERROR", 'reason' => "No devices found within " . $radius ." km of (".round($fields['lat'],4).",".round($fields['lng'],4)."), closest user is " . round($closest,2) . " km away");
    }
    else{
        $reply = array('status' => "ERROR", 'reason' => "No devices with " . $fields['sensor'] . " found");
    }
}

else {
    $fields['distributed'] = $distributed;
    
		// create the task using the specific task creation file for each task type
		// can be found in the task_creation folder
    $task_creator = getClass($file_path, $fields);
    $id = $task_creator->insertTask($users);
    $task_creator->sendData($users);
    
    // schedule to finish the task if it was not completed within the timeframe
    if($fields['type'] == "sensor"){
        $time = $fields['duration'] + TTL_SENSOR;
        $future = date('H:i', strtotime("+".$time." minutes"));
        if ($server_os === "Windows") {
            $taskid = uniqid($id);
            exec("schtasks.exe /Create /st ".$future." /tn ".$taskid." /sc ONCE /tr \"php".ROOT_PATH."html\\finish_task.php ".$id." ".$fields['file']." ".$fields['type']." ".$fields['sensor']."\" 2>&1");
        } else if ($server_os === "Linux") {
            exec("echo 'php ".ROOT_PATH."html/finish_task.php ".$id." ".$fields['file']." ".$fields['type']." ".$fields['sensor']."' | at ".$future." 2>&1");
        } else {
            error_log("Cannot execute heartbeat due to unsupported OS.");
        }
    }
    else {
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
    }
    
    // everything went fine!
    $reply = array('status' => "OK");
}

echo json_encode($reply);


// help function to find users close to the target area
function getUserInProximityTo($users, $latTo, $lngTo){
    
    $prox = array();
    $in_prox = array();
    $rad = M_PI / 180;
    $max_radius = 5000;
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