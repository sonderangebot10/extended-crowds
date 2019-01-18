<?php

class Sensor implements TaskOngoingInterface {
    
    private $dbc;
    private $email;
    
    public function __construct($email){
        $this->email = $email;
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
    public function getOngoingTasks(){
        
        $user = $this->dbc->getUserFields($this->email, array("sensing_created"));
        $tasks = array();
        $sensing_ids = explode(';', $user['sensing_created']);
        
        for ($i = 0;$i < count($sensing_ids); $i++){

            $t = $this->dbc->getSensorTaskAll($sensing_ids[$i]);
            

            // only ongoing tasks
            if(!empty($t) && empty($t[0]['completed_time'])){
                $task = array();
                $task['sensor'] = $t[0]['sensor'];
                $task['created'] = $t[0]['create_time'];
                $task['type'] = "sensing";
                $tasks[] = $task;
            }
        }
        return $tasks;
    }
}

?>