<?php
include("config.php");
include("database.php");

$dbc = new DatabaseController();
$dbc->connect();
   
$id = $dbc->escapeString($_POST["email"]);
$password = $dbc->escapeString($_POST["password"]);

// check database for admin
$registered = $dbc->isRegisteredAdmin($id, $password);


session_start();

// if a match was found the table row must be one row
if(!$registered){
    $_SESSION['errors'] = "Email or password was incorrect";
    header("location:index.php");
}
else{
    $_SESSION['login_user'] = $id;
    header("location:main.php");
}
?>