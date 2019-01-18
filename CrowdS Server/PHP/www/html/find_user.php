<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();

// email from admin site
$email = $dbc->escapeString($_POST["email"]);

$data = $dbc->findUser($email);

echo json_encode($data);

?>