<?php

class Hit implements TaskHistoryInterface {
    
    private $dbc;
    private $email;
    
    public function __construct($email){
        $this->email = $email;
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
    public function getCompletedTasks(){
        
        $user = $this->dbc->getUserFields($this->email, array("hits_created"));
       
        $tasks = array();
        $hit_ids = explode(';', $user['hits_created']);
        
        for ($i = 0;$i < count($hit_ids); $i++){

            $t = $this->dbc->getHumanTaskAll($hit_ids[$i]);

            // only completed tasks
            if(!empty($t[0]['completed_time'])){

                $task = array();
                $task['id'] = $hit_ids[$i];
                $task['question'] = $t[0]['question'];
                if(!is_null($t[0]['answer'])){
                    $task['answer'] = $t[0]['answer'];
                }
                else{
                    $task['answer'] = "no answer";
                }
                $task['created'] = $t[0]['create_time'];
                $task['completed'] = $t[0]['completed_time'];
                $task['type'] = $t[0]['task_type'];
                $tasks[] = $task;
            }
        }
        return $tasks;
    }
}

?>