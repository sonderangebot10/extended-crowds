<?php

class Hit implements TaskOngoingInterface {
    
    private $dbc;
    private $email;
    
    public function __construct($email){
        $this->email = $email;
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
    public function getOngoingTasks(){
        
        $user = $this->dbc->getUserFields($this->email, array("hits_created"));
       
        $tasks = array();
        $hit_ids = explode(';', $user['hits_created']);
        
        for ($i = 0;$i < count($hit_ids); $i++){

            $t = $this->dbc->getHumanTaskAll($hit_ids[$i]);

            // only ongoing tasks
            if(!empty($t) && empty($t[0]['completed_time'])){
                $task = array();
                $task['question'] = $t[0]['question'];
                $task['created'] = $t[0]['create_time'];
                $task['type'] = $t[0]['task_type'];
                $tasks[] = $task;
            }
        }
        return $tasks;
    }
}

?>