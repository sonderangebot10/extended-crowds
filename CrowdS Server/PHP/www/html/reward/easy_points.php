<?php

class EasyPoints implements RewardInterface{
    
    private $dbc;
    
    public function __construct(){
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
     }
    
    public function addPay($id, $pay){
        
				// if pay is 0 only 1 provider was available, pay with its bid
        if($pay == 0){
            $pay = $this->dbc->getUserFields($id, array("bid"))["bid"];
        }
        
        $this->dbc->updateUser($id, array("pay"), array("pay + ".$pay));
    }
    
    public function incrementSensorPoints($id){
        $this->dbc->updateUser($id, array("sensor_points"), array("sensor_points + 1"));
    }
    
    public function incrementHitPoints($id){
        $this->dbc->updateUser($id, array("hit_points"), array("hit_points + 1"));
    }
    
    public function getPoints($id){
        $user = $this->dbc->getUserFields($id, array('sensor_points', 'hit_points'));
        return $user['sensor_points'].';'.$user['hit_points'];
    }
    
    public function getPay($id){
        $user = $this->dbc->getUserFields($id, array('pay'));
        return $user['pay'];
    }
}
?>