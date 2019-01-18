<?php
include_once("config.php");
include(constant('DB'));

class DatabaseController {
    
    private $database;
    
    public function __construct(){
        $this->database = getDBClass();
    }
    
    public function connect(){
        $this->database->connect();
    }
    
    public function disconnect(){
        $this->database->disconnect();
    }
    
    public function getConnection(){
        $this->database->getConnection();
    }
    
    public function escapeString($str){
        return $this->database->escapeString($str);
    }
    
    public function getLastId(){
        return $this->database->getLastId();
    }
    
    public function alreadyRegistered($id){
        return $this->database->alreadyRegistered($id);
    }
    
    public function isRegistered($id, $password){
        return $this->database->isRegistered($id, $password);
    }
    
    public function isRegisteredAdmin($id, $password){
        return $this->database->isRegisteredAdmin($id, $password);
    }
    
    public function registerUser($userFields, $userData, $sensorFields, $hasSensorData){
        return $this->database->registerUser($userFields, $userData, $sensorFields, $hasSensorData);
    }
    
    
    public function updateUser($id, $fields, $data){
        return $this->database->updateUser($id, $fields, $data);
    }
    
    public function getUserAll($id){
        return $this->database->getUserAll($id);
    }
    
    public function getUserFields($id, $fields){
        return $this->database->getUserFields($id, $fields);
    }
    
    public function getActiveUsers(){
        return $this->database->getActiveUsers();
    }
    
    public function deleteUser($id){
        return $this->database->deleteUser($id);
    }
    
    public function findUser($email){
        return $this->database->findUser($email);
    }
    
    public function filterUsersWithSensor($users, $sensor){
        return $this->database->filterUsersWithSensor($users, $sensor);
    }
    
    public function insertSensorTask($fields, $data){
        return $this->database->insertSensorTask($fields, $data);
    }
    
    public function getSensorTaskAll($id){
        return $this->database->getSensorTaskAll($id);
    }
    
    public function getSensorTaskFields($id, $fields){
        return $this->database->getSensorTaskFields($id, $fields);
    }
    
    public function updateSensorTask($id, $fields, $data){
        return $this->database->updateSensorTask($id, $fields, $data);
    }
    
    public function insertHumanTask($fields, $data){
        return $this->database->insertHumanTask($fields, $data);
    }
    
    public function getHumanTaskAll($id){
        return $this->database->getHumanTaskAll($id);
    }
    
    public function getHumanTaskFields($id, $fields){
        return $this->database->getHumanTaskFields($id, $fields);
    }
    
    public function updateHumanTask($id, $fields, $data){
        return $this->database->updateHumanTask($id, $fields, $data);
    }
    
    public function getQualityAll($id){
        return $this->database->getQualityAll($id);
    }
    
    public function updateQuality($id, $fields, $data){
        return $this->database->updateQuality($id, $fields, $data);
    }
    
    public function query($query){
        return $this->database->query($query);
    }
    
}

function getDBClass(){
    
    $fp = fopen(constant('DB'), 'r');
    $class = $buffer = '';
    $i = 0;
    while (!$class) {
        if (feof($fp)) break;

        $buffer .= fread($fp, 512);
        $tokens = token_get_all($buffer);

        if (strpos($buffer, '{') === false) continue;

        for (;$i<count($tokens);$i++) {
            if ($tokens[$i][0] === T_CLASS) {
                for ($j=$i+1;$j<count($tokens);$j++) {
                    if ($tokens[$j] === '{') {
                        $class = $tokens[$i+2][1];
                    }
                }
            }
        }
    }
    
    return new $class();
}
?>