<?php
include(dirname(__DIR__) . '/' ."interfaces.php");
    
class MySQL implements DatabaseInterface {
    private $server;
    private $username;
    private $password;
    private $database;
    private $conn;
    private $db_errors = 
        array('connect' => " Failed to connect to MySQL, ",
              'disconnect' => " Failed to disconnect from MySQL ",
              'query' => ", Failed to query MySQL ");

    function __construct() {
        $this->server = $_ENV["DB_URL"];
        $this->username = $_ENV["DB_USERNAME"];
        $this->password = $_ENV["DB_PASSWORD"];
        $this->database = $_ENV["DB_NAME"];
    }
    
    public function connect(){

        $this->conn = mysqli_connect($this->server,$this->username,$this->password,$this->database);
        if(mysqli_connect_errno()) echo $this->db_errors['connect'] . mysqli_connect_error();
    }
    
    public function disconnect(){
        mysqli_close($this->conn);
        if(mysqli_errno($this->conn)) echo $this->db_errors['disconnect'] . mysqli_errno($this->conn);
    }
    
    public function getConnection(){
        return $this->conn;
    }
    
    public function escapeString($str){
        return mysqli_real_escape_string($this->conn, $str); 
    }
    
    public function getLastId(){
        return $this->conn->insert_id;
    }
    
    public function alreadyRegistered($id){
        // check database for user
        $result = $this->query("SELECT count(*) FROM cs.user WHERE email = '$id'");
        
        return mysqli_fetch_row($result)[0];
    }
    
    public function isRegistered($id, $password){
        // check database for user
        $result = $this->query("SELECT count(*) FROM cs.user WHERE email = '$id' AND password = '$password'");

        return mysqli_fetch_row($result)[0];
    }
    
    public function isRegisteredAdmin($id, $password){
        // check database for user
        $result = $this->query("SELECT count(*) FROM cs.user WHERE email = '$id' AND password = '$password' AND admin = '1'");
        
        // if a match was found the table row must be one row
        if(!mysqli_fetch_row($result)[0]) return 0;
        else return 1;
    }
    
    public function registerUser($userFields, $userData, $sensorFields, $hasSensorData){
        
        $query1 = "INSERT INTO cs.user (";
        for ($i = 0; $i < count($userFields); $i++) {
            $query1 = $query1 . $userFields[$i] . ", ";
        }
        $query1 = rtrim($query1, ", ") . ") VALUES (";
        for ($i = 0; $i < count($userData); $i++) {
            $query1 = $query1 . "'$userData[$i]'" . ", ";
        }
        $query1 = rtrim($query1, ", ") . ")";
        
        $query2 = "INSERT INTO cs.sensor (";
        for ($i = 0; $i < count($sensorFields); $i++) {
            $query2 = $query2 . $sensorFields[$i] . ", ";
        }
        $query2 = rtrim($query2, ", ") . ") VALUES (";
        for ($i = 0; $i < count($hasSensorData); $i++) {
            $query2 = $query2 . "'$hasSensorData[$i]'" . ", ";
        }
        $query2 = rtrim($query2, ", ") . ")";
        
        $query3 = "INSERT INTO cs.quality (email) VALUES ('$userData[0]')";
        
        // set autocommit to off
        mysqli_autocommit($this->conn,FALSE);

        // insert some values
        $this->query($query1);
        $this->query($query2);
        $this->query($query3);

        // commit transaction
        mysqli_commit($this->conn);
    }
    
    public function updateUser($id, $fields, $data){
        
        $query = "UPDATE cs.user SET ";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ' = ' . "$data[$i]" . ', ';
        }
        $query = rtrim($query, ", ") . " WHERE email = '$id'";
        
        return $this->query($query);
    }
    
    public function getUserAll($id){
        
        if($id == ""){
            $query = "SELECT * FROM cs.user";
            $results = $this->query($query);
            $data = array();
            while($row = mysqli_fetch_array($results)){
                $data[] = $row;
            }
            return $data;
        }
        else{
            $query = "SELECT * FROM cs.user WHERE email = '$id'";
            $result = $this->query($query);
            return mysqli_fetch_assoc($result);
        }
    }
    
    public function getUserFields($id, $fields){
        $query = "SELECT ";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ', ';
        }
        $query = rtrim($query, ", ") . " FROM cs.user WHERE email = '$id'";
        
        $result = $this->query($query);
        return mysqli_fetch_assoc($result);
    }
    
    public function getActiveUsers(){
        $query = "SELECT email, longitude, latitude, bid, login_time, firebase FROM cs.user WHERE active = 1";
        
        $result = $this->query($query);
        $users = array();
        
        for ($x = 1; $x <= $this->conn->affected_rows; $x++) {
            $users[] = $result->fetch_assoc();
        }
        
        return $users;
    }
    
    public function deleteUser($id){
        $query = "DELETE FROM cs.user WHERE email = '$id'";
        return $this->query($query);
    }
    
    public function findUser($email){
        $query = "SELECT * FROM cs.user WHERE email LIKE '%{$email}%'";
        $results = $this->query($query);
        $data = array();
        while($row = mysqli_fetch_array($results)){
            $data[] = $row;
        }
        echo json_encode($data);
    }
    
    public function filterUsersWithSensor($users, $sensor){
        
        $has_sensor = array();
        
        // find devices with the correct sensor
        for ($i = 0; $i < count($users); $i++) {
            $id = $users[$i]['email'];
            $query = "SELECT email FROM cs.sensor WHERE email = '$id' AND $sensor = '1'";
            $result = $this->query($query);
            // what if this result is null?
            if(mysqli_fetch_row($result)[0]){
                $has_sensor[] = $users[$i];
            }
        }
        
        return $has_sensor;
    }
    
    public function insertSensorTask($fields, $data){
        $query = "INSERT INTO cs.sensor_task (";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ", ";
        }
        $query = rtrim($query, ", ") . ") VALUES (";
        for ($i = 0; $i < count($data); $i++) {
            $query = $query . "'$data[$i]'" . ", ";
        }
        $query = rtrim($query, ", ") . ")";
        
        return $this->query($query);
    }
    
    public function getSensorTaskAll($id){
        $query = "SELECT * FROM cs.sensor_task WHERE id = '$id'";
        $results = $this->query($query);
        $data = array();
        while($row = mysqli_fetch_array($results)){
            $data[] = $row;
        }
        return $data;
    }
    
    public function getSensorTaskFields($id, $fields){
        
        $query = "SELECT ";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ', ';
        }
        $query = rtrim($query, ", ") . " FROM cs.sensor_task WHERE id = '$id'";
        
        $result = $this->query($query);
        return mysqli_fetch_assoc($result);
    
    }
    
    public function updateSensorTask($id, $fields, $data){
        $query = "UPDATE cs.sensor_task SET ";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ' = ' . $data[$i] . ', ';
        }
        $query = rtrim($query, ", ") . " WHERE id = '$id'";
        
        return $this->query($query);
    }
    
    public function insertHumanTask($fields, $data){
        $query = "INSERT INTO cs.human_task (";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ", ";
        }
        $query = rtrim($query, ", ") . ") VALUES (";
        for ($i = 0; $i < count($data); $i++) {
            $query = $query . "'$data[$i]'" . ", ";
        }
        $query = rtrim($query, ", ") . ")";
        
        return $this->query($query);
    }
    
    public function getHumanTaskAll($id){
        $query = "SELECT * FROM cs.human_task WHERE id = '$id'";
        $results = $this->query($query);
        $data = array();
        while($row = mysqli_fetch_array($results)){
            $data[] = $row;
        }
        return $data;
    }
    
    public function getHumanTaskFields($id, $fields){
        $query = "SELECT ";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ', ';
        }
        $query = rtrim($query, ", ") . " FROM cs.human_task WHERE id = '$id'";
        
        $result = $this->query($query);
        return mysqli_fetch_assoc($result);
    }
    
    public function updateHumanTask($id, $fields, $data){
        $query = "UPDATE cs.human_task SET ";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ' = ' . $data[$i] . ', ';
        }
        $query = rtrim($query, ", ") . " WHERE id = '$id'";
        
        return $this->query($query);
    }
    
    public function getQualityAll($id){
        $query = "SELECT * FROM cs.quality WHERE email = '$id'";
        $results = $this->query($query);
        $data = array();
        while($row = mysqli_fetch_array($results)){
            $data[] = $row;
        }
        return $data[0];
    }
    
    public function updateQuality($id, $fields, $data){
        $query = "UPDATE cs.quality SET ";
        for ($i = 0; $i < count($fields); $i++) {
            $query = $query . $fields[$i] . ' = ' . $data[$i] . ', ';
        }
        $query = rtrim($query, ", ") . " WHERE email = '$id'";

        return $this->query($query);
    }
    
    public function query($query){
        $res = mysqli_query($this->conn, $query) or die($this->db_errors['query'] . mysqli_error($this->conn));
        
        return $res;
    }
}

?>