<?php

class MultipleChoice implements UpdateTaskInterface{
    
    private $email;
    private $data;
    private $string;
    private $id;
    private $index;
    private $retrieved;
    private $distributed;
    private $task_giver;
    private $new_data;
    private $new_participants;
    private $choices;
    private $members;
    private $dbc;
    
    public function __construct($fields){
        // parse fields
        $this->email = $fields['email'];
        $this->data = $fields['data'];
        $this->string = $fields['id'];
        
        list($this->id,$this->index) = explode(':', $this->string);
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
        
        // retrieve current results
        $user = $this->dbc->getHumanTaskFields($this->id, array("results", "distributed", "task_giver", "participants", "answer_choices", "members"));
        
        $results = $user["results"];
        $this->distributed = $user["distributed"];
        $this->task_giver = $user["task_giver"];
        $this->choices = $user["answer_choices"];
        $this->members = $user["members"];
        
        if($this->data == "0" || $this->data){
            $this->new_data = rtrim($this->index . ':' . $this->data  . ';' . $results, ';');
            $this->new_participants = rtrim($this->index . ':' . $this->email . ';' . $user["participants"], ';');
        }
        else{
            $this->new_data = $results;
            $this->new_participants = $user["participants"];
        }
        
        
        $this->retrieved = empty($results) ? 0 : count(explode(';', $results));
        
    }
    
    public function updateTask(){
        $this->dbc->updateHumanTask($this->id, array("results", "participants"), array("'$this->new_data'", "'$this->new_participants'"));
    }
    
    public function getType(){
        return "hit";
    }
    
    public function isCompleted(){
        return true;
        // return ($this->retrieved + 1) == $this->distributed;
    }
    
    public function finishTask(){
        $this->dbc->updateHumanTask($this->id, array("completed_time"), array("NOW()"));
        
        $task_data['id'] = $this->id;
        $task_data['data'] = $this->new_data;
        $task_data['participants'] = $this->new_participants;
        $task_data['members'] = $this->members;
        $task_data['choices'] = $this->choices;
        $task_data['task_giver'] = $this->task_giver;
        $task_data['task_type'] = "hit";
        $task_data['type'] = "multiple";
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