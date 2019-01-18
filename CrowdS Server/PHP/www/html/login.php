<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();

/*
$id = "villeg@kth.se";
$password = "12321";
$firebase = "bo0IcGLfvpX8LCRwhecEThckt0qkk0qRfJZLrtmYobMSOFRjUKBhWZi5MgTTnGLtAVPd4M9nbZFnTWT1HnoHxULcSdd6bQGNgWvwfOkhK9ijbDkTBROi2BixGLLnN7lp";

$lng = "17.950315";
$lat = "59.405388";
$os = "android";
$device_version = "0.004";
*/
// sent from mobile app
$id = $dbc->escapeString($_POST["email"]);
$password = $dbc->escapeString($_POST["password"]);
$firebase = $dbc->escapeString($_POST["firebase"]);

$admin = $dbc->getUserFields($id, array("admin"))["admin"];

if(MAINTENANCE && $admin == 0){
    
    $finish_at = strtotime(MAINTENANCE_TIME);
    $time_left = ceil(($finish_at - strtotime("now"))/60);
    
    $reply = array('status' => "ERROR", 
                   'reason' => "maintenance",
                   'time' => "$time_left");
    echo json_encode($reply);
    return;
}


// coordinates are sent from the mobile app
$lat = $dbc->escapeString($_POST["lat"]);
$lng = $dbc->escapeString($_POST["lng"]);
$os = $dbc->escapeString($_POST["os"]);
$device_version = $dbc->escapeString($_POST["version"]);    


if($os == "ios"){
    $server_version = IOS_VERSION;
}
else if($os == "android"){
    $server_version = ANDROID_VERSION;
}


// check if the user is registered
$registered = $dbc->isRegistered($id, $password);

if(!$registered){
	$reply = array('status' => "ERROR", 'reason' => "not a registered user"); 
}

else{
    // only let the device login in it has the correct version
    if($server_version == $device_version){
        // create heartbeat in the future
        $future = date('H:i', strtotime("+".HB_TIME));
        
        $atjob = exec("echo 'php /var/www/html/heartbeat.php ".$id."' | at ".$future." 2>&1");
        $at = explode(" ", $atjob);
        $atid = $at[1];
        
        $heartbeat = $dbc->getUserFields($id, array("heartbeat"))["heartbeat"];
        list($old_id, $timestamp) = explode(":", $heartbeat);
        
        $timestamp = strtotime("now");
        
        exec("atrm ".$old_id." 2>&1");
        $hb = $atid.':'.$timestamp;
        
        // update login time and coordinates
        $dbc->updateUser($id, array("login_time", "latitude", "longitude", "firebase", "heartbeat", "active"), array("NOW()", "'$lat'", "'$lng'", "'$firebase'", "'$hb'", "1"));   
    
        // retrieve username
        $username = $dbc->getUserFields($id, array("username"))["username"];
        
        if($os == "ios"){

            // create reply message	
            $reply = array('status' => "OK",
                'username' =>$username);	
        }
        else if($os == "android"){

            // create reply message	
            $reply = array('status' => "OK",
                'username' =>$username);	
        }
        else{
            $reply = array('status' => "ERROR", 
                           'reason' => "UNKOWN OS");
        }

    }
    else{
        $reply = array('status' => "ERROR", 
                       'reason' => "outdated");
    }
}

echo json_encode($reply);

?>
