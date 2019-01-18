<?php

define('THRESHOLD_DECREASE', 0.1);
define('MAX_TRIES', 5);

class Random implements TaskAllocationInterface{
    
    private $users;
    private $dbc;
    
    public function __construct($users){
        $this->users = $users;
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
    public function getUsers(){
        $qc_threshold = 0.5;
        $rep_threshold = 0.5;

        $n = count($this->users);

        if($n <= MAX_DEVICES){
            return $this->users;
        }

        $tmp = array();
        for($i=0; $i<MAX_DEVICES; $i++){

            $found = false;

            while(!$found){

                for($try=0; $try < MAX_TRIES; $try++){

                    $index = rand(0,$n-1);
                    $email = $this->users[$index]['email'];

                    $r = $this->dbc->getUserFields($email, array('reputation'));
                    list($rep,$rdata) = explode(':',$r['reputation']);

                    $q = $this->dbc->getQualityAll($email);
                    if(!empty($fields['sensor'])){
                        list($quality,$qdata) = explode(':',$q[$fields['sensor']]);   
                    }
                    else{
                        list($quality,$qdata) = explode(':',$q[$fields['hit_type']]);
                    }

                    if($rep >= $rep_threshold && $quality >= $qc_threshold){
                        $tmp[] = $this->users[$index];
                        unset($users[$index]);
                        $found = true;
                        break;
                    }
                }
                if(!$found){
                    $rep_threshold = $rep_threshold - THRESHOLD_DECREASE;
                    $qc_threshold = $qc_threshold - THRESHOLD_DECREASE;
                }
            }
        }

        return $tmp;

    }
}
?>