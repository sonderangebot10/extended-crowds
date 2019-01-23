<?php
define('QUALITY_MIN', 10);

class Distance implements QualityControlInterface{
    
    private $participants;
    private $task_id;
    private $task_type;
    private $data;
    private $users;
    private $quality;
    private $dbc;
    private $mean;
     
    public function __construct($task_data){
				// parse task data
        $this->participants = explode(';', $task_data['participants']);
        $this->task_id = $task_data['id'];
        $this->task_type = $task_data['type'];
        $this->data = explode(';', $task_data['data']);
        $this->users = array();
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
		// will return 1 if anyone participated 
    public function anyoneParticipated(){
        return $this->participants;
    }
    
    public function calculateQuality(){
        
        // save user emails for later use
        foreach($this->participants as $p){
            list($index, $email) = explode(':', $p);
            $this->users[$index] = $email;
        }
        
        // parse task data
        // User1Data;User2Data;...;UserNData
        $sdata = array();
        foreach($this->data as $values){
            //UserNData = index:Data
            list($index, $value) = explode(':', $values);
            
            //Data = d1_d2_..._dn
            $v = explode('_', $value);
            for($i = 0;$i < count($v); $i++){
                if(!isset($sdata[$i])){
                    $sdata[$i] = array();
                }
                $sdata[$i][$index] = $v[$i];
            } 
        }
        
        //calculate mean value(s)
        for($i = 0;$i < count($sdata); $i++){
            $this->mean[$i] = array_sum($sdata[$i])/count($sdata[$i]);
        }
        
        // calculate distance for each participant
        for($i = 0;$i < count($this->users); $i++){
            $sum = 0;
            foreach($sdata as $d){
                for($j = 0; $j < count($d); $j++){
                    $d = $this->mean[$j] - $d[$j];
                    $sum = $sum + $d ** 2;
                }
            }
            $distance[$i] = sqrt($sum);
        }

        $max_distance = max($distance);
        
        //calculate quality
        for ($i = 0;$i <= count($this->users); $i++){
            
            if(array_key_exists($i, $this->users)){
                $id = $this->users[$i];

                // query database for quality info
                $user = $this->dbc->getQualityAll($id);
                list($old_quality, $data) = explode(':',$user[$this->task_type]);

                // division with 0
                if($max_distance == 0){
                    $dist = 0;
                }
                else{
                    $dist = 1-($distance[$i]/$max_distance);
                }

                // append the latest distance to the data string
                $data = rtrim($dist . ';' . $data, ';');

                $dist = explode(';', $data);
                if(count($dist) > QUALITY_MIN){
                    array_pop($dist);
                    $data = substr($data, 0, strrpos($data, ';'));
                }
                if(count($dist) >= QUALITY_MIN){
                    $new_quality  = array_sum($dist)/count($dist);
                }
                else{
                    $new_quality = $old_quality;
                }

                $quality_string = $new_quality . ':' . $data;
                $this->quality[$id] = $quality_string;
            }
        }
    }
    public function updateQuality(){
        foreach($this->quality as $key => $val){
            $this->dbc->updateQuality($key, array("`$this->task_type`"), array("'$val'"));
        }
    }
    public function setTaskResult(){
        // Update result for task (mean value(s))
        $result = implode(";", $this->mean);

        if($this->task_type == "numeric"){
            $this->dbc->updateHumanTask($this->task_id, array("answer"), array("'$result'"));
        }
        else {
            $this->dbc->updateSensorTask($this->task_id, array("result"), array("'$result'"));
        }
        return $result;
    }
        
}

?>