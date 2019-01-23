<?php
include('session.php');
include('database.php');

$dbc = new DatabaseController();
$dbc->connect();

// Select all users active the last hour
$users = $dbc->getActiveUsers();

/*
// rows is not allowed to be empty
if(isset($users)){
	// do a nice print out of the array
	echo "<pre>";
	print_r($users);
	echo "</pre>";
}
else
	echo "no active users :(";*/
?>

<html>
<title>CS Project</title>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Lato">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Montserrat">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    
<style>
body,h1,h2,h3,h4,h5,h6 {font-family: "Lato", sans-serif}
.w3-bar,h1,button {font-family: "Montserrat", sans-serif}
.fa-anchor,.fa-coffee {font-size:200px}
/* Always set the map height explicitly to define the size of the div
* element that contains the map. */
#map {
height: 96%;
}
/* Optional: Makes the sample page fill the window. */
html, body {
height: 100vh;
margin: 0;
padding: 0;
overflow: hidden;
}
    
.vertical-list {
    overflow: auto;
    max-height:100vh;
    margin:0 +16px;
}

.vertical-list li {
    background-color: #eee; /* Grey background color */
    color: black; /* Black text color */
    display: block; /* Make the links appear below each other */
    padding: 12px; /* Add some padding */
    text-decoration: none; /* Remove underline from links */
    text-align: center;
}

.vertical-list li:hover {
    background-color: #ccc; 
}
</style>
<body>

<!-- Navbar -->
<div class="w3-top">
  <div class="w3-bar w3-black w3-card-2 w3-left-align w3-large">
    <a class="w3-bar-item w3-button w3-hide-medium w3-hide-large w3-right w3-padding-large w3-hover-white w3-large w3-red" href="javascript:void(0);" onclick="myFunction()" title="Toggle Navigation Menu"><i class="fa fa-bars"></i></a>
    <a href="main.php" class="w3-bar-item w3-button w3-padding-large w3-hover-white">Home</a>
    <a href="account.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Account</a>
    <a href="server_settings.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Settings</a>
    <a href="active_users.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-white">Online</a>
    <a href="admin_logout.php" class="w3-bar-item w3-button w3-hide-small w3-padding-large w3-hover-white">Logout</a>
  </div>
</div>
    
<div class="w3-panel w3-padding-32">
    <div class="w3-threequarter" style="margin:0 -16px" id="map"></div>
    <div id="users" class="w3-quarter vertical-list">
        <ul>
        </ul>
    </div>
</div>
    
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>    

<script>
    var map;
    function initMap() {
        map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 59.405, lng: 17.950},
        zoom: 8
        });
        
        var users =<?= json_encode($users); ?>;
        var markers = [];

        for (var i = 0; i < users.length; i++) {
            var latLng = new google.maps.LatLng(users[i].latitude,users[i].longitude);
            var marker = new google.maps.Marker({
                position: latLng,
                title: users[i].email,
                map: map
            });
            markers[i] = marker;
        }
        
        // Add a marker clusterer to manage the markers.
        var markerCluster = new MarkerClusterer(map, markers,
            {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m'});
    }
</script>
    
<script language="JavaScript">
    
    $(document).ready(function() {
        
        var users =<?= json_encode($users); ?>;
        // clear the existing list
        $('.vertical-list li').remove();
        for (var i = 0; i < users.length; i++) {
            $('.vertical-list ul').append('<li>'+users[i].email+'</li>');
        }
    });
</script>
<script src="https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js"></script>
    
<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCJiTsfvPJQFg8PoUyzHGMJlrKySpfeAXE&callback=initMap"
async defer></script>

    
<script>
// Used to toggle the menu on small screens when clicking on the menu button
function myFunction() {
    var x = document.getElementById("navDemo");
    if (x.className.indexOf("w3-show") == -1) {
        x.className += " w3-show";
    } else { 
        x.className = x.className.replace(" w3-show", "");
    }
}
</script>

</body>
</html>
