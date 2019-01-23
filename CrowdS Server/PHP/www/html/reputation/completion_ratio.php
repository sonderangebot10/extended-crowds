<?php

// the number of tasks needed before calculating reputation
define('REPUTATION_MIN', 20);


class CompletionRatio implements ReputationInterface{
    
    private $participants;
    private $members;
    private $reputation;
    private $dbc;
    
    public function __construct($task_data){
			
				// parse task data
        $this->participants = explode(';', $task_data['participants']);
        $this->members = explode(';', $task_data['members']);
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
    public function calculateReputation(){
        
        // save user emails for later use
        $contributors = array();
        foreach($this->participants as $p){
            if($p){
              list($index, $email) = explode(':', $p);
                $contributors[$index] = $email;  
            }
        }

        // users selected for the task that actually contributed in solving it
        $contributing_members = array_intersect($this->members, $contributors);


        // calculate new reputation
        foreach($this->members as $email){
            
            // query database for reputation
            $user = $this->dbc->getUserFields($email, array('reputation'));
            list($old_rep,$str) = explode(':', $user['reputation']);

            // check if current user contributed
            if(in_array($email, $contributing_members)){
                $str = rtrim('1;'.$str,';');
            }
            else{
                $str = rtrim('0;'.$str,';');
            }

            
            $data = explode(';', $str);
            if(count($data) > REPUTATION_MIN){
                array_pop($data);
                $str = substr($str, 0, strrpos($str, ';'));
            }
            if(count($data) == REPUTATION_MIN){
                $new_rep = array_sum($data) / count($data);
            }
            else{
                $new_rep = $old_rep;
            }

            $rep_string = $new_rep . ':' . $str;
            $this->reputation[$email] = $rep_string;
        }

    }
		
		// update reputation for all participants
    public function updateReputation(){
        foreach($this->reputation as $key => $val){
            $this->dbc->updateUser($key, array("`reputation`"), array("'$val'"));
        }
    }
}
?>