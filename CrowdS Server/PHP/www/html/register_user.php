<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();

// email is sent from the mobile app
$id = $dbc->escapeString($_POST["email"]);


// check if user exists already
if($dbc->alreadyRegistered($id)){
	$reply = array('status' => "ERROR", 'reason' => "user already exist"); 
}
else{
	// retrieve the remaining user info
	$username = $dbc->escapeString($_POST["username"]);
	$password = $dbc->escapeString($_POST["password"]);
	$os = $dbc->escapeString($_POST["device_os"]);
	$model = $dbc->escapeString($_POST["device_model"]);
    $firebase = $dbc->escapeString($_POST["firebase"]);
    $bid = $dbc->escapeString($_POST["bid"]);
	
	// sensors is a string with 10010101
	// 1 if the device has the sensor
	// 0 otherwise	
	$sensors = $dbc->escapeString($_POST["device_sensors"]);
	$sensor = str_split($sensors);

	
	// read file with all types of sensors
    $filename = "sensor_types.txt";
	$sensor_types = file($filename, FILE_IGNORE_NEW_LINES);
    
    $userFields = array("email", "username", "password", "os", "model", "firebase", "bid");
    
    $userData = array($id, $username, $password, $os, $model, $firebase, $bid);
    
    $sensorFields = array_merge(array("email"),$sensor_types);
    
    $hasSensorData = array_merge(array($id), $sensor);
    
    $dbc->registerUser($userFields, $userData, $sensorFields, $hasSensorData);

	// everything went fine!
	$reply = array('status' => "OK");		
}

// send reply
echo json_encode($reply);
?>
