<?php

class Sensor implements UpdateTaskInterface{
    
    private $email;
    private $data;
    private $string;
    private $id;
    private $index;
    private $retrieved;
    private $distributed;
    private $task_giver;
    private $new_participants;
    private $new_data;
    private $type;
    private $members;
    private $dbc;
    
    public function __construct($fields){
        
        $this->email = $fields['email'];
        $this->data = $fields['data'];
        $this->string = $fields['id'];
        $this->type = $fields['type'];
        
        list($this->id,$this->index) = explode(':', $this->string);
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
        
        // retrieve current results
        $user = $this->dbc->getSensorTaskFields($this->id, array("sensor_data", "distributed", "task_giver", "participants", "members"));
        
        $sensor_data = $user["sensor_data"];
        $this->distributed = $user["distributed"];
        $this->task_giver = $user["task_giver"];
        $this->members = $user["members"];
        
        if(!is_null($this->data)){
            $this->new_data = rtrim($this->index . ':' . $this->data  . ';' . $sensor_data, ';');
            $this->new_participants = rtrim($this->index . ':' . $this->email . ';' . $user["participants"], ';');
        }
        else{
            $this->new_data = $sensor_data;
            $this->new_participants = $user["participants"];
        }
        
        $this->retrieved = empty($sensor_data) ? 0 : count(explode(';', $sensor_data));
        
    }
    
    public function updateTask(){
       $this->dbc->updateSensorTask($this->id, array("sensor_data", "participants"), array("'$this->new_data'", "'$this->new_participants'"));   
    }
    
    public function getType(){
        return "sensor";
    }
    
    public function isCompleted(){
        return ($this->retrieved + 1) == $this->distributed;
    }
    
    public function finishTask(){
        $this->dbc->updateSensorTask($this->id, array("completed_time"), array("NOW()"));
        
        $task_data['id'] = $this->id;
        $task_data['data'] = $this->new_data;
        $task_data['type'] = $this->type;
        $task_data['participants'] = $this->new_participants;
        $task_data['members'] = $this->members;
        $task_data['task_type'] = "sensor";
        $task_data['task_giver'] = $this->task_giver;
        return $task_data;
    }
    
    public function sendNotification(){
        $user = $this->dbc->getUserFields($this->task_giver, array("firebase"));
        $token = $user["firebase"];

        $firebase = new Firebase();
        $notification = new Notification();
        
        $title = "Task finished!";
        $body = "Check the app now!!";

        $notification->setTitle($title);
        $notification->setBody($body);

        $json = $notification->getNotification();
        $firebase->sendNotification($token, $json);
    }
}
?>