<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();

$email = $dbc->escapeString($_POST['email']);
$active = $dbc->getActiveUsers();

// remove the requester from active pool
for($i = 0; $i < count($active); $i++){
    if($active[$i]['email'] == $email){
        unset($active[$i]);
        $active = array_values($active);
        break;
    }
}

$coords = array();

for($i = 0; $i<count($active); $i++){
    $coords[$i] = 
        array('lat' => $active[$i]['latitude'],
              'lng' => $active[$i]['longitude']);
}

$reply = array('status' => "OK", 'coords' => $coords);

echo json_encode($reply);

?>