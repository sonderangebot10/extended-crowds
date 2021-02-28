<?php

// interface for database
interface DatabaseInterface {
    public function connect();
    public function disconnect();
    public function getConnection();
    public function escapeString($str);
    public function getLastId();
    public function isRegistered($id, $password);
    public function alreadyRegistered($id);
    public function isRegisteredAdmin($id, $password);
    public function registerUser($userFields, $userData, $sensorFields, $hasSensorData);
    public function updateUser($id, $fields, $data);
    public function getUserAll($id);
    public function getUserFields($id, $fields);
    public function getActiveUsers();
    public function deleteUser($id);
    public function findUser($email);
    public function filterUsersWithSensor($users, $sensor);
    public function insertSensorTask($fields, $data);
    public function getSensorTaskAll($id);
    public function getSensorTaskFields($id, $fields);
    public function updateSensorTask($id, $fields, $data);
    public function insertHumanTask($fields, $data);
    public function getHumanTaskAll($id);
    public function deleteHumanTaskAll($id);
    public function getHumanTaskFields($id, $fields);
    public function updateHumanTask($id, $fields, $data);
    public function getQualityAll($id);
    public function updateQuality($id, $fields, $data);
    public function query($query);
}

interface CreateTaskInterface {
    public function insertTask($group);
    public function sendData($group);
    public function sendNotification($group);
}

interface UpdateTaskInterface {
    public function updateTask();
    public function getType();
    public function isCompleted();
    public function finishTask();
    public function sendNotification();
}

interface TaskOngoingInterface{
    public function getOngoingTasks();
}

interface TaskHistoryInterface {
    public function getCompletedTasks();
}

interface TaskAllocationInterface {
    public function getUsers();
}

interface QualityControlInterface {
    public function anyoneParticipated();
    public function calculateQuality();
    public function updateQuality();
    public function setTaskResult();
}

interface ReputationInterface {
    public function calculateReputation();
    public function updateReputation();
}

interface RewardInterface{
    public function incrementSensorPoints($id);
    public function incrementHitPoints($id);
    public function getPoints($id);
}

?>