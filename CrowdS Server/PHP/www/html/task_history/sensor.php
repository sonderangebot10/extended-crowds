<?php

class Sensor implements TaskHistoryInterface {
    
    private $dbc;
    private $email;
    
    public function __construct($email){
        $this->email = $email;
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
		// returns an array with all completed sensors tasks for user
    public function getCompletedTasks(){
        
        $user = $this->dbc->getUserFields($this->email, array("sensing_created"));
        $tasks = array();
        $sensing_ids = explode(';', $user['sensing_created']);
        
        for ($i = 0;$i < count($sensing_ids); $i++){

            $t = $this->dbc->getSensorTaskAll($sensing_ids[$i]);

            // only completed tasks
            if(!empty($t[0]['completed_time'])){

                $task = array();
                $task['id'] = $sensing_ids[$i];
                $task['duration'] = $t[0]['duration'];
                $task['sensor'] = $t[0]['sensor'];
                if(!is_null($t[0]['result'])){
                    $task['answer'] = $t[0]['result'];
                }
                else{
                    $task['answer'] = "no answer";
                }
                $task['created'] = $t[0]['create_time'];
                $task['completed'] = $t[0]['completed_time'];

                $task['task_id'] = $t[0]['id'];
                $task['participants'] = $t[0]['participants'];
                
                $task['type'] = "sensing";
                $tasks[] = $task;
            }
        }
        return $tasks;
    }
}

?>