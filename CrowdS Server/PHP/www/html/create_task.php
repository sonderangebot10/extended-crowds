<?php
include("config.php");
include("firebase.php");
include("database.php");
include("notification.php");
include("push.php");
include("system_utils.php");
include(constant('TA'));

define('TTL_SENSOR', 10);
define('TTL_HIT', 180); //180

$radius = 0.025;
$closest = 0;

$dbc = new DatabaseController();
$dbc->connect();

/*
$_POST['email'] = "jowalle@kth.se";
$_POST['description'] = "pre-made test";
$_POST['lng'] = "17.950634";
$_POST['lat'] = "59.404690";


$_POST['question'] = "RIP";
$_POST['hit_type'] = "numeric";
$_POST['type'] = "hit";
$_POST['file'] = "numeric.php";

/*
$_POST['sensor'] = "light";
$_POST['duration'] = "1";
$_POST['readings'] = "1";
$_POST['file'] = "sensor.php";
$_POST['type'] = "sensor";
*/

$file_path = "task_creation/" . $_POST['file'];
include($file_path);

foreach ($_POST as $key => $value){
    $fields[$key] = $dbc->escapeString($value);
}

$active = $dbc->getActiveUsers();

// remove the task giver from active pool
for($i = 0; $i < count($active); $i++){
    if($active[$i]['email'] == $fields['email']){
        unset($active[$i]);
        $active = array_values($active);
        break;
    }
}

if($fields['type'] == "sensor"){
    $active = $dbc->filterUsersWithSensor($active, $fields['sensor']);
}

if(count($active) > 0){
    $in_prox_users = getUserInProximityTo($active, $fields['lat'], $fields['lng']);
    
    if(count($in_prox_users) > 0){
        $ta = getClass(constant('TA'),$in_prox_users);
        $users = $ta->getUsers();
        $cost = $ta->getCost();
        $fields['cost'] = $cost;
    }
    else{
        $users = array();
    }

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
    
    $task_creator = getClass($file_path, $fields);
    $id = $task_creator->insertTask($users);
    $task_creator->sendData($users);
    
    if($fields['type'] == "sensor"){
        $time = $fields['duration'] + TTL_SENSOR;
        $future = date('H:i', strtotime("+".$time." minutes"));
        
        exec("echo 'php /var/www/html/finish_task.php ".$id." ".$fields['file']." ".$fields['type']." ".$fields['sensor']."' | at ".$future." 2>&1");
    }
    else{
        $future = date('H:i', strtotime("+ ".TTL_HIT." minutes"));
        
        exec("echo 'php /var/www/html/finish_task.php ".$id." ".$fields['file']." ".$fields['type']." ".$fields['hit_type']."' | at ".$future." 2>&1");
        
        $task_creator->sendNotification($users);
    }
    
    if(LOG){
        if($fields['type'] == "sensor"){
            $type = $fields['sensor'];
        }
        else{
            $type = $fields['hit_type'];
        }
        
        $now = date('Y-m-d H:i:s');
        if($fields['type'] == "sensor"){
            $time = $fields['duration'] + TTL_SENSOR;
            $ttl = date('Y-m-d H:i:00', strtotime("+".$time." minutes"));
            $log = array('id'=>$id,
                      'type'=>$type,
                      'radius'=>$radius,
                      'cost'=>$cost,
                      'ttl'=>$ttl,
                      'duration'=>$fields['duration'],
                      'readings'=>$fields['readings'],
                      'created'=> array('by'=>$fields['email'],
                                        'possible members'=>array_column($in_prox_users,'email'),
                                        'members'=>array_column($users,'email'),
                                        'time'=>$now));
            $fp = fopen(ROOT_PATH.'html/log/sensor_'.$id. '.json', 'w');
        }
        else{
            $ttl = date('Y-m-d H:i:00', strtotime("+ ".TTL_HIT." minutes"));
            $log = array('id'=>$id,
                      'type'=>$type,
                      'radius'=>$radius,
                      'cost'=>$cost,
                      'ttl'=>$ttl,
                      'question'=>$fields['question'],
                      'created'=> array('by'=>$fields['email'],
                                        'possible members'=>array_column($in_prox_users,'email'),
                                        'members'=>array_column($users,'email'),
                                        'time'=>$now));
            $fp = fopen(ROOT_PATH.'html/log/hit_'.$id. '.json', 'w');
        }
        
        
        fwrite($fp, json_encode($log,JSON_PRETTY_PRINT));
        fclose($fp);
    }
    
    // everything went fine!
    $reply = array('status' => "OK");
}

echo json_encode($reply);


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
    
    //echo '<pre>'; print_r($prox); echo '</pre>';
    
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